package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import constants.BusinessConstant;
import io.ebean.*;
import models.groupon.GroupProduct;
import models.groupon.Groupon;
import models.order.GroupOrder;
import models.user.Member;
import play.Logger;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import security.Secured;
import utils.IdGenerator;
import utils.ValidationUtil;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static constants.BusinessConstant.*;
import static models.groupon.Groupon.STATUS_PROCESSING;
import static models.order.GroupOrder.*;
import static models.product.Product.STATUS_ON_SHELVE;

/**
 * 用户控制类
 */
public class GroupOrderController extends BaseController {

    Logger.ALogger logger = Logger.of(GroupOrderController.class);
    @Inject
    WechatPayV3Controller wechatPayController;

    /**
     * @api {GET} /v1/o/group_orders/?page= 01订单列表
     * @apiName listGroupOrders
     * @apiGroup Group-Order
     * @apiSuccess (Success 200){int} pages 页数
     * @apiSuccess (Success 200){JsonArray} list 订单列表
     */
    public CompletionStage<Result> listGroupOrders(Http.Request request, int page, long productId) {
        return CompletableFuture.supplyAsync(() -> {
            ExpressionList<GroupOrder> expressionList = GroupOrder.find.query().where()
                    .ge("status", GroupOrder.ORDER_STATUS_PAID);
            if (productId > 0) {
                expressionList.eq("productId", productId);
            }
            PagedList<GroupOrder> pagedList = expressionList.orderBy().desc("id")
                    .setFirstRow((page - 1) * PAGE_SIZE_20)
                    .setMaxRows(PAGE_SIZE_20)
                    .findPagedList();
            List<GroupOrder> list = pagedList.getList();
            //读取订单详情
            ArrayNode nodes = Json.newArray();
            list.forEach((each) -> {
                ObjectNode eachNode = Json.newObject();
                eachNode.put("avatar", each.avatar);
                eachNode.put("userName", businessUtils.hidepartialChar(each.userName));
                eachNode.put("phoneNumber", businessUtils.hidepartialChar(each.phoneNumber));
                eachNode.put("joinType", each.grouponId > 0 ? "已开团" : "已参团");
                nodes.add(eachNode);
            });
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            result.put("hasNext", pagedList.hasNext());
            result.put("totalCount", pagedList.getTotalCount());
            result.set("list", nodes);
            return ok(result);
        });
    }

    /**
     * @api {GET} /v1/o/my_group_orders/?page= 02我的团购订单列表
     * @apiName listMyGroupOrders
     * @apiGroup Group-Order
     * @apiSuccess (Success 200){int} pages 页数
     * @apiSuccess (Success 200){JsonArray} list 订单列表
     */
    public CompletionStage<Result> listMyGroupOrders(Http.Request request, int page) {
        long uid = businessUtils.getUserIdByAuthToken2(request);
        return CompletableFuture.supplyAsync(() -> {
            if (uid < 1) return unauth403();
            ExpressionList<GroupOrder> expressionList = GroupOrder.find.query().where()
                    .eq("uid", uid);

            PagedList<GroupOrder> pagedList = expressionList.orderBy().desc("id")
                    .setFirstRow((page - 1) * PAGE_SIZE_20)
                    .setMaxRows(PAGE_SIZE_20)
                    .findPagedList();
            List<GroupOrder> list = pagedList.getList();
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            result.put("hasNext", pagedList.hasNext());
            result.put("totalCount", pagedList.getTotalCount());
            result.set("list", Json.toJson(list));
            return ok(result);
        });
    }

    /**
     * @api {POST} /v1/o/groupon_order/new/ 03团购下单
     * @apiName placeGrouponOrder
     * @apiGroup Group-Order
     * @apiParam {long} [productId] 商品ID 团购发起人需要送productId这个参数
     * @apiParam {long} [grouponId] 团购ID 参与团购需要送grouponId这个参数
     * @apiParam {long} [dealerId] dealerId
     * @apiParam {int} orderType 1单购 2团购
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Success 200){String} transactionId 流水号
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    @Security.Authenticated(Secured.class)
    public CompletionStage<Result> placeGrouponOrder(Http.Request request) {
        long uid = businessUtils.getUserIdByAuthToken2(request);
        JsonNode requestNode = request.body().asJson();
        return CompletableFuture.supplyAsync(() -> {
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            if (null == requestNode) return okCustomJson(CODE40001, "参数错误");
            long currentTime = dateUtils.getCurrentTimeBySecond();
            int orderType = requestNode.findPath("orderType").asInt();
            final long grouponId = requestNode.findPath("grouponId").asLong();
            final long dealerId = requestNode.findPath("dealerId").asLong();
            long productId = requestNode.findPath("productId").asLong();
            if (orderType < ORDER_TYPE_SINGLE_BUY || orderType > ORDER_TYPE_GROUP_BUY_JOIN) {
                return okCustomJson(CODE40001, "团购类型有误");
            }
            Groupon groupon = null;
            if (grouponId > 0) {
                orderType = ORDER_TYPE_GROUP_BUY_JOIN;
                groupon = Groupon.find.byId(grouponId);
                if (null == groupon) return okCustomJson(CODE40001, "该团购不存在");
                if (member.id == groupon.launcherUid) return okCustomJson(CODE40001, "不可参与自己发起的团购");
                if (groupon.uidList.contains("/" + member.id + "")) return okCustomJson(CODE40001, "您已参与过该团购");
                if (groupon.status != STATUS_PROCESSING) return okCustomJson(CODE40001, "该团购已结束");
                productId = groupon.productId;
            }
            if (productId < 1) return okCustomJson(CODE40001, "商品有误");

            GroupProduct product = GroupProduct.find.byId(productId);
            if (null == product) return okCustomJson(CODE40001, "该商品不存在");
            if (product.status < STATUS_ON_SHELVE) return okCustomJson(CODE40001, "该商品不存在或已下架");
            if (null != groupon && currentTime > groupon.expireTime) {
                int status = Groupon.STATUS_FAILED;
                if (groupon.orders >= product.requireUsers) status = Groupon.STATUS_SUCCEED;
                groupon.setStatus(status);
                groupon.save();
                return okCustomJson(CODE40001, "该团购已结束，你可以参与别的团购");
            }

            if (!businessUtils.setLock(String.valueOf(member.id), OPERATION_PLACE_ORDER))
                return okCustomJson(CODE40004, "正在下单中,请稍等");
            try {
                int status = ORDER_STATUS_UNPAY;
                DB.beginTransaction();
                GroupOrder groupOrder = new GroupOrder();
                groupOrder.setUid(member.id);
                groupOrder.setProductId(productId);
                groupOrder.setProductName(product.name);
                groupOrder.setProductPoster(product.poster);
                if (dealerId > 0) {
                    Member dealer = Member.find.byId(dealerId);
                    if (null != dealer) {
                        groupOrder.setDealerUid(dealer.id);
                        groupOrder.setDealerName(businessUtils.getMemberName(dealer));
                        groupOrder.setStaffId(dealer.rootDealerId);
                        if (dealer.rootDealerId > 0) {
                            Member staff = Member.find.byId(dealer.rootDealerId);
                            if (null != staff) {
                                groupOrder.setStaffName(businessUtils.getMemberName(staff));
                            }
                        }
                        if (member.dealerId < 1 || member.rootDealerId < 1) {
                            member.setDealerId(dealer.id);
                            member.setRootDealerId(dealer.rootDealerId);
                            member.save();
                        }
                    }
                }
                groupOrder.setUserName(businessUtils.getMemberName(member));
                groupOrder.setPhoneNumber(member.phoneNumber);
                groupOrder.setOrderNo(dateUtils.getCurrentTimeWithHMS() + IdGenerator.getId() + "G" + orderType);
                groupOrder.setStatus(ORDER_STATUS_UNPAY);
                groupOrder.setProductCount(1);
                long payMoney = (orderType == ORDER_TYPE_SINGLE_BUY ? product.price : product.groupPrice);
                groupOrder.setTotalMoney(payMoney);
                groupOrder.setRealPay(payMoney);
                groupOrder.setAvatar(member.avatar);
                groupOrder.setAddress("");
                groupOrder.setOutTradeNo("");
                groupOrder.setPayTxNo("");
                groupOrder.setPayTime(0);
                groupOrder.setOrderSettlementTime(0);
                groupOrder.setCommissionHandled(false);
                groupOrder.setDescription("");
                groupOrder.setUpdateTime(currentTime);
                groupOrder.setCreateTime(currentTime);
                if (null != groupon) groupOrder.setGrouponId(groupon.id);
                groupOrder.setOrderType(orderType);
                if (orderType >= ORDER_TYPE_GROUP_BUY_LAUNCH) groupOrder.setGrouponStatus(STATUS_PROCESSING);
                groupOrder.save();

                DB.commitTransaction();
                ObjectNode result = Json.newObject();
                result.put("code", 200);
                result.put("orderId", groupOrder.id);
                result.put("orderNo", groupOrder.orderNo);
                result.put("status", status);
                businessUtils.unLock(String.valueOf(member.id), OPERATION_PLACE_ORDER);
                return ok(result);
            } catch (Exception e) {
                logger.error("order actor placeGrouponOrder:" + e.getMessage());
                return okCustomJson(CODE500, "团购发生异常，请稍后再试");
            } finally {
                DB.endTransaction();
            }
        });
    }

    /**
     * @api {POST} /v1/o/pay_group_order/ 04支付订单
     * @apiName payGroupOrder
     * @apiGroup Group-Order
     * @apiParam {long} orderId 订单ID
     * @apiSuccess (Success 200){int} code 200
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    @Security.Authenticated(Secured.class)
    public CompletionStage<Result> payGroupOrder(Http.Request request) {
        JsonNode requestNode = request.body().asJson();
        return CompletableFuture.supplyAsync(() -> {
            if (null == requestNode) return okCustomJson(CODE40001, "参数错误");
            long uid = businessUtils.getUserIdByAuthToken2(request);
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            long orderId = requestNode.findPath("orderId").asLong();
            if (ValidationUtil.isEmpty(member.openId))
                return okCustomJson(CODE40001, "微信支付需要openId");
            if (orderId < 1) return okCustomJson(CODE40001, "参数错误");
            try {
                if (!businessUtils.setLock(String.valueOf(orderId), OPERATION_PAY_ORDER))
                    return okCustomJson(CODE40004, "正在支付中,请稍等");
                GroupOrder order = GroupOrder.find.byId(orderId);
                if (null == order) return okCustomJson(CODE40002, "该订单不存在");
                if (order.status != ORDER_STATUS_UNPAY) return okCustomJson(CODE40003, "该订单不是待支付状态,不能支付");
                if (order.grouponId > 0) {
                    Groupon groupon = Groupon.find.byId(order.grouponId);
                    if (null != groupon) {
                        Groupon existGroupon = Groupon.find.byId(groupon.id);
                        if (null != existGroupon && existGroupon.status != STATUS_PROCESSING) {
                            return okCustomJson(CODE40003, "该团购已结束了");
                        }
                    }
                }
                PayParam payParam = new PayParam.Builder()
                        .tradeNo(order.orderNo)
                        .subject("晴松-订单支付")
                        .productionCode("oid" + order.id)
                        .realPayMoney(order.realPay)
                        .totalAmount(order.realPay)
                        .uid(member.id)
                        .openId(member.openId)
                        .payMethod(BusinessConstant.PAYMENT_WEPAY_MINIAPP)
                        .build();
                Result result = invokeWechatPay(request, order.orderNo, payParam);
                businessUtils.unLock(String.valueOf(orderId), OPERATION_PAY_ORDER);
                return result;
            } catch (Exception e) {
                logger.error("payOrder:" + e.getMessage());
                businessUtils.unLock(String.valueOf(orderId), OPERATION_PAY_ORDER);
                return okCustomJson(CODE500, "支付发生异常，请稍后再试");
            }
        });
    }

    private Result invokeWechatPay(Http.Request request, String orderNo, PayParam payParam) {
        logger.info("invokeWechatPay:" + payParam.toString());
        payParam.tradeNo = orderNo;
        return wechatPayController.wechatPay(payParam).toCompletableFuture().join();
    }


    /**
     * @api {GET} /v1/o/group_order/:id/ 05团购订单详情
     * @apiName getGroupOrder
     * @apiGroup Group-Order
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Success 200){long} id 商品id
     * @apiSuccess {String} name 名称
     */
    public CompletionStage<Result> getGroupOrder(Http.Request request, long orderId) {
        return CompletableFuture.supplyAsync(() -> {
            if (orderId < 1) return okCustomJson(CODE40001, "参数错误");
            GroupOrder groupOrder = GroupOrder.find.byId(orderId);
            if (null == groupOrder) return okCustomJson(CODE40002, "该订单不存在");
            ObjectNode node = (ObjectNode) Json.toJson(groupOrder);
            Groupon groupon = Groupon.find.byId(groupOrder.grouponId);
            if (null != groupon) {
                node.put("avatars", groupon.avatars);
                node.put("expireTime", groupon.expireTime);
            }
            node.put(CODE, CODE200);
            return ok(node);
        });
    }

    /**
     * @api {GET} /v1/o/my_customer_orders/?page=&filter= 06我的客户团购列表
     * @apiName listMyCustomerOrders
     * @apiGroup Group-Order
     * @apiSuccess (Success 200){Array} list 列表
     * @apiSuccess (Success 200){String} userName 用户名字
     * @apiSuccess (Success 200){String} dealerName 推荐人
     * @apiSuccess (Success 200){String} avatar 头像
     * @apiSuccess (Success 200){String} source 来源
     * @apiSuccess (Success 200){String} phoneNumber 手机号码
     * @apiSuccess (Success 200){long} createTime 下单时间
     * @apiSuccess (Success 200){int} orderType 1为单购  2为团购
     * @apiSuccess (Success 200){int} grouponId 0为开团  1为参团
     * @apiSuccess (Success 200){int} grouponStatus 拼团状态
     * @apiSuccess (Success 200) {int} code 200成功修改
     */
    public CompletionStage<Result> listMyCustomerOrders(Http.Request request, int page, String filter, long productId) {
        return CompletableFuture.supplyAsync(() -> {
            long uid = businessUtils.getUserIdByAuthToken2(request);
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member || member.userType < Member.USER_TYPE_STAFF)
                return okCustomJson(CODE40001, "非营业员，不可使用");
            ExpressionList<GroupOrder> expressionList = GroupOrder.find.query().where()
                    .eq("staffId", member.id)
                    .ge("status", GroupOrder.ORDER_STATUS_PAID);
            if (productId > 0) {
                expressionList.eq("productId", productId);
            }
            if (!ValidationUtil.isEmpty(filter)) {
                expressionList.or(Expr.icontains("phoneNumber", filter),
                        Expr.icontains("userName", filter));
            }

            PagedList<GroupOrder> pagedList = expressionList.orderBy("userName")
                    .setFirstRow((page - 1) * PAGE_SIZE_10)
                    .setMaxRows(PAGE_SIZE_10)
                    .findPagedList();
            List<GroupOrder> list = pagedList.getList();
            ArrayNode nodes = Json.newArray();
            list.forEach((groupOrder) -> {
                ObjectNode node = Json.newObject();
                node.put("userName", groupOrder.userName);
                node.put("dealerName", groupOrder.dealerName);
                node.put("avatar", groupOrder.avatar);
                node.put("source", (groupOrder.dealerUid == groupOrder.staffId) ? "老客户" : "新客户");
                node.put("phoneNumber", groupOrder.phoneNumber);
                node.put("createTime", groupOrder.createTime);
                node.put("orderType", groupOrder.orderType);
                node.put("grouponId", groupOrder.grouponId);
                node.put("grouponStatus", groupOrder.grouponStatus);
                node.put("subMoney", groupOrder.totalMoney);
                nodes.add(node);
            });
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            result.set("list", nodes);
            result.put("orders", pagedList.getTotalCount());
            result.put("hasNext", pagedList.hasNext());
            ExpressionList<GroupOrder> moneyExpressionList = GroupOrder.find.query().select("totalMoney").where()
                    .eq("staffId", member.id)
                    .ge("status", GroupOrder.ORDER_STATUS_PAID);
            if (productId > 0) {
                moneyExpressionList.eq("productId", productId);
            }
            List<Long> moneyList = moneyExpressionList.findSingleAttributeList();
            long totalMoney = moneyList.parallelStream().reduce(0L, Long::sum);
            result.put("totalMoney", totalMoney);
            return ok(result);
        });
    }

    /**
     * @api {GET} /v1/o/group_order_stat/:productId/ 07统计数据
     * @apiName groupOrderStat
     * @apiGroup Group-Order
     * @apiSuccess (Success 200)  {String} productName
     * @apiSuccess (Success 200)  {long} orders 订单数量
     * @apiSuccess (Success 200)  {long} totalMoney 订单总金额
     * @apiSuccess (Success 200)  {long} buyers 购买人数
     * @apiSuccess (Success 200)  {long} directInviteOrders 老客订单数
     * @apiSuccess (Success 200)  {long} indirectInviteOrders 新客订单数
     * @apiSuccess (Success 200) {int} code 200成功修改
     */
    @Security.Authenticated(Secured.class)
    public CompletionStage<Result> groupOrderStat(Http.Request request, long productId) {
        return CompletableFuture.supplyAsync(() -> {
            long uid = businessUtils.getUserIdByAuthToken2(request);
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (member.userType < Member.USER_TYPE_STAFF) return okCustomJson(CODE40001, "权限不足");
            GroupProduct groupProduct = GroupProduct.find.byId(productId);
            if (null == groupProduct) return okCustomJson(CODE40001, "商品有误");
            List<GroupOrder> groupOrderList = GroupOrder.find.query().where()
                    .eq("productId", productId)
                    .ge("status", GroupOrder.ORDER_STATUS_PAID)
                    .findList();
            String productName = groupProduct.name;
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            result.put("productName", productName);
            result.put("orders", groupOrderList.size());
            long totalMoney = groupOrderList.parallelStream().mapToLong((order) -> order.realPay).sum();
            result.put("totalMoney", totalMoney);
            Set<Long> buyers = new HashSet<>();
            groupOrderList.stream().forEach((order) -> buyers.add(order.uid));
            result.put("buyers", buyers.size());
            long directInviteOrders = groupOrderList.parallelStream().filter((order) -> order.dealerUid == order.staffId).count();
            long indirectInviteOrders = groupOrderList.parallelStream().filter((order) -> order.dealerUid != order.staffId).count();
            result.put("directInviteOrders", directInviteOrders);
            result.put("indirectInviteOrders", indirectInviteOrders);
            return ok(result);
        });
    }

}
