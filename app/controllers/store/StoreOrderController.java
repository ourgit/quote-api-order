package controllers.store;

import actor.ActorProtocol;
import akka.actor.ActorRef;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConstants;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import constants.BusinessConstant;
import controllers.*;
import io.ebean.DB;
import io.ebean.Ebean;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import models.log.BalanceLog;
import models.order.Order;
import models.order.OrderLog;
import models.postservice.OrderReturns;
import models.product.MailFee;
import models.product.ProductSku;
import models.store.*;
import models.user.*;
import org.xml.sax.SAXException;
import play.Logger;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import scala.compat.java8.FutureConverters;
import utils.*;
import utils.alipay.AlipayConfig;
import utils.wechat.WechatConfig;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import static akka.pattern.Patterns.ask;
import static constants.BusinessConstant.*;
import static models.order.Order.*;
import static models.postservice.OrderReturns.*;
import static models.product.Product.STATUS_ON_SHELVE;
import static models.user.MemberCoupon.STATUS_NOT_USE;


public class StoreOrderController extends BaseController {

    Logger.ALogger logger = Logger.of(StoreOrderController.class);

    @Inject
    BalanceUtils balanceUtils;

    @Inject
    WechatPayV3Controller wechatPayController;

    @Inject
    WechatHelper wechatHelper;

    @Inject
    WSClient wsclient;

    @Inject
    AliPayController aliPayController;

    @Inject
    @Named("storeOrderActor")
    private ActorRef storeOrderActorRef;


    /**
     * @api {POST} /v1/o/store/orders/ 01我的订单列表
     * @apiName listMyOrders
     * @apiGroup Order
     * @apiParam status 0全部，1待付款 3待发货 5待收货 7待评价 9已评价 -1退货申请,-2退货中,-3已退货,-4取消交易 -5撤销申请 -6系统取消 -7退款 -8支付超时关闭
     * @apiParam page page
     * @apiParam size size
     * @apiParam orderType orderType 订单类型
     * @apiParam deliveryType deliveryType 配送类型
     * @apiParam filter 订单关键字
     * @apiParam productName 商品名字
     * @apiParam payBeingTime 付款开始时间
     * @apiParam payEndTime 付款结束时间
     * @apiSuccess (Success 200){int} pages 页数
     * @apiSuccess (Success 200){JsonArray} list 订单列表
     * @apiSuccess (Success 200){String} orderNo 订单编号
     * @apiSuccess (Success 200){String} totalMoney 总金额
     * @apiSuccess (Success 200){String} realPay 实付
     * @apiSuccess (Success 200){String} logisticsFee 运费
     * @apiSuccess (Success 200){int} productCount 商品数量
     * @apiSuccess (Success 200){String} statusName 状态名字
     * @apiSuccess (Success 200){String} description 描述
     * @apiSuccess (Success 200){JsonArray} detailList 商品列表
     * @apiSuccess (Success 200){string} productName 商品名字
     * @apiSuccess (Success 200){string} productImgUrl 商品图片
     * @apiSuccess (Success 200){string} skuName 商品属性描述
     * @apiSuccess (Success 200){int} status 状态
     */
    public CompletionStage<Result> listMyOrders(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            JsonNode requestNode = request.body().asJson();
            if (null == requestNode) return okCustomJson(CODE40001, "参数错误");

            int page = requestNode.findPath("page").asInt();
            if (page < 1) page = 1;
            int size = requestNode.findPath("size").asInt();
            if (size < 1) size = PAGE_SIZE_10;
            int status = requestNode.findPath("status").asInt();
            int orderType = requestNode.findPath("orderType").asInt();
            int deliveryType = requestNode.findPath("deliveryType").asInt();
            long payBeingTime = requestNode.findPath("payBeingTime").asLong();
            long payEndTime = requestNode.findPath("payEndTime").asLong();
            String filter = requestNode.findPath("filter").asText();
            String productName = requestNode.findPath("productName").asText();

            ExpressionList<StoreOrder> expressionList = StoreOrder.find.query().where()
                    .eq("uid", uid)
                    .gt("status", ORDER_STATUS_DELETE);
            if (status > 0) {
                if (status == Order.ORDER_STATUS_TO_DELIEVERY) {
                    expressionList.ge("status", Order.ORDER_STATUS_TO_DELIEVERY)
                            .le("status", Order.ORDER_STATUS_DELIEVERED);
                } else expressionList.eq("status", status);
            }
            if (payBeingTime > 0) expressionList.ge("payTime", payBeingTime);
            if (payEndTime > 0) expressionList.le("payTime", payEndTime);
            if (orderType > 0) {
                expressionList.eq("orderType", orderType);
            }
            if (deliveryType > 0) {
                expressionList.eq("deliveryType", deliveryType);
            }
            if (!ValidationUtil.isEmpty(filter)) {
                expressionList.icontains("filter", filter);
            }
            if (!ValidationUtil.isEmpty(productName)) {
                List<StoreOrderDetail> orderDetailList = StoreOrderDetail.find.query().where()
                        .eq("productName", productName)
                        .findList();
                Set<Long> orderIdSet = new HashSet<>();
                if (orderDetailList.size() > 0) {
                    orderDetailList.forEach((each) -> orderIdSet.add(each.getOrderId()));
                } else orderIdSet.add(0L);
                expressionList.in("id", orderIdSet);
            }
            PagedList<StoreOrder> pagedList = expressionList.orderBy().desc("id")
                    .setFirstRow((page - 1) * size)
                    .setMaxRows(size)
                    .findPagedList();
            List<StoreOrder> list = pagedList.getList();
            //读取订单详情
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            list.parallelStream().forEach((each) -> {
                List<StoreOrderDetail> details = StoreOrderDetail.find.query().where()
                        .eq("orderId", each.id)
                        .orderBy().asc("id").findList();
                each.detailList.addAll(details);
                StoreOrderReturns orderReturn = StoreOrderReturns.find.query().where()
                        .eq("orderId", each.id)
                        .orderBy().desc("id")
                        .setMaxRows(1).findOne();
                each.orderReturns = orderReturn;
            });
            boolean hasNext = pagedList.hasNext();
            result.put("hasNext", hasNext);
            result.put("totalCount", pagedList.getTotalCount());
            result.set("list", Json.toJson(list));
            return ok(result);
        });
    }

    /**
     * @api {POST} /v1/o/store/orders/new/ 02下单
     * @apiName placeOrder
     * @apiGroup Order
     * @apiParam {String} [dealerCode] 推荐人code
     * @apiParam {long} [dealerUid] 推荐人uid 与dealerCode 二者任选一个
     * @apiParam {JsonArray} list 列表
     * @apiParam {long} shopId shopId
     * @apiParam {long} deliveryMethod 配送方式 1自提 2跑腿 3邮寄
     * @apiParam {String} [remark] 备注，每个单品的备注，预留
     * @apiParam {long} [memberCouponId] 用户优惠券ID
     * @apiParam {int} [payMethod] 支付方式，余额支付时传入
     * @apiParam {JsonArray} productList 商品列表
     * @apiParam {long} productId 商品ID
     * @apiParam {long} skuId 商品skuID,为0时表示只有一个标准的产品，没有sku
     * @apiParam {long} amount 购买数量
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Success 200){String} transactionId 流水号
     * @apiSuccess (Error 40001){int} code 40001 参数错误
     * @apiSuccess (Error 40002){int} code 40002 该收货地址不存在
     * @apiSuccess (Error 40003){int} code 40003 该商品不存在
     * @apiSuccess (Error 40004){int} code 40004 正在下单中,请稍等
     * @apiSuccess (Error 40005){int} code 40005 余额不足,请先充值
     * @apiSuccess (Error 40006){int} code 40005 该商品已售完
     * @apiSuccess (Error 40007){int} code 40007 库存只剩下*件
     * @apiSuccess (Error 40009){int} code 40009 交易密码错误
     */
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> placeOrder(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            JsonNode jsonNode = request.body().asJson();
            if (null == jsonNode) return okCustomJson(CODE40001, "参数错误");
            if (!businessUtils.setLock(String.valueOf(uid), OPERATION_STORE_PLACE_ORDER)) {
                return okCustomJson(CODE40004, "正在下单中,请稍等");
            }
            JsonNode list = jsonNode.findPath("list");
            if (null == list || !list.isArray() || list.size() < 1) return okCustomJson(CODE40001, "购买数量为空，请先选择商品");
            final String description = jsonNode.findPath("description").asText();
            List<StoreOrderDetail> detailList = new ArrayList<>();
            long totalMoney = 0;
            int productAmount = 0;
            long currentTime = dateUtils.getCurrentTimeBySecond();
            long shopId = -1;
            String shopName = "";
            for (JsonNode node : list) {
                StoreOrderDetail orderDetail = new StoreOrderDetail();
                final int amount = node.findPath("amount").asInt();
                final long productId = node.findPath("productId").asLong();
                final long skuId = node.findPath("skuId").asLong();
                final String remark = node.findPath("remark").asText();
                StoreProduct product = StoreProduct.find.byId(productId);
                if (null == product) return okCustomJson(CODE40001, "该商品不存在");
                if (amount < 1) return okCustomJson(CODE40001, "请选择商品数量");
                if (product.status < STATUS_ON_SHELVE) return okCustomJson(CODE40001, "该商品不存在或已下架");
                if (shopId == -1) {
                    shopId = product.shopId;
                    shopName = product.shopName;
                } else if (product.shopId != shopId) {
                    return okCustomJson(CODE40001, "不同商家的商品请分开下单");
                }
                if (skuId < 1) return okCustomJson(CODE40001, "SKU有误");
                ProductSku productSku = getSku(product.sku, skuId);
                if (null == productSku) return okCustomJson(CODE40001, "商品SKU有误");
                if (amount > productSku.stock) {
                    return okCustomJson(CODE40001, "下单数量超出" + product.name + "的库存数量");
                }
                orderDetail.setProductPrice(productSku.price);
                long thisItemTotalMoney = productSku.price * amount;
                totalMoney = totalMoney + thisItemTotalMoney;
                setOrderDetail(member.id, currentTime, orderDetail, amount, remark, product, productSku);
                orderDetail.setSubTotal(thisItemTotalMoney);
                detailList.add(orderDetail);
                productAmount = productAmount + amount;
            }
            OrderParam param = new OrderParam.Builder()
                    .uid(uid)
                    .deliveryType(DELIVERY_TYPE_PICK_UP)
                    .description(description)
                    .totalMoney(totalMoney)
                    .realPayMoney(totalMoney)
                    .productAmount(productAmount)
                    .shopId(shopId)
                    .shopName(shopName)
                    .mailFee(0)
                    .build();
            ActorProtocol.PLACE_STORE_ORDER orderParam = new ActorProtocol.PLACE_STORE_ORDER(param, member, detailList);
            return orderParam;
        }).thenCompose((result) -> {
            if (result instanceof ActorProtocol.PLACE_STORE_ORDER)
                return executeOrder((ActorProtocol.PLACE_STORE_ORDER) result);
            return CompletableFuture.supplyAsync(() -> (Result) result);
        });
    }

    private CompletionStage<Result> executeOrder(ActorProtocol.PLACE_STORE_ORDER orderParam) {
        return FutureConverters.toJava(ask(storeOrderActorRef, orderParam, 30000))
                .thenApply(response -> ok((ObjectNode) response));
    }

    private void setOrderDetail(long uid, long currentTime, StoreOrderDetail orderDetail,
                                int amount, String remark, StoreProduct product, ProductSku productSku) {
        orderDetail.setUid(uid);
        orderDetail.setProductPrice(productSku.price);
        orderDetail.setSkuName(productSku.name);
        orderDetail.setProductSkuId(productSku.id);
        orderDetail.setProductImgUrl(product.coverImgUrl);
        orderDetail.setNumber(amount);
        orderDetail.setProductId(product.id);
        orderDetail.setProductName(product.name);
        orderDetail.setRemark(remark);
        orderDetail.setUnit(product.unit);
        orderDetail.setUpdateTime(currentTime);
        orderDetail.setCreateTime(currentTime);
        orderDetail.setOldPrice(productSku.oldPrice);
        orderDetail.setCategoryId(product.categoryId);
    }

    public String getAttribute(String attrKey) {
        if (!ValidationUtil.isEmpty(attrKey)) return attrKey + ",";
        return " ";
    }


    /**
     * @api {POST} /v1/o/store/pay_order/  03支付订单
     * @apiName payOrder
     * @apiGroup Order
     * @apiParam {long} orderId 订单ID
     * @apiParam {int} payMethod 1银行卡支付2支付宝支付3微信支付4余额支付6微信支付H5 7支付宝H5支付8小程序支付 10储值卡
     * @apiParam {String} openId 微信公众号支付时需要传入
     * @apiParam {String} [password] 余额支付时需要密码，这个密码为登录密码
     * @apiParam {String} [vcode] 余额支付时需要短信验证码
     * @apiParam {int} [useScore] 0不使用 1使用
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Success 200){int} note 如果是支付宝直接跳到支付宝支付的网页，如果是微信支付返回一个待支付的二维码，字段名codeUrl
     * @apiSuccess (Error 40001){int} code 40001 参数错误
     * @apiSuccess (Error 40002){int} code 40002 该订单不存在
     * @apiSuccess (Error 40003){int} code 40003 该订单不是待支付状态,不能支付
     * @apiSuccess (Error 40004){int} errCodeDesc 微信支付错误提示
     * @apiSuccess (Error 40005){int} code 40005 请输入短信验证码
     * @apiSuccess (Error 40006){int} code 40006 余额不足
     * @apiSuccess (Error 40006){int} code 40007 微信openId为空
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public CompletionStage<Result> payOrder(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            JsonNode requestNode = request.body().asJson();
            if (null == requestNode) return okCustomJson(CODE40001, "参数错误");
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            long orderId = requestNode.findPath("orderId").asLong();
            String openId = requestNode.findPath("openId").asText();
            int payMethod = requestNode.findPath("payMethod").asInt();
            if (orderId < 1 || payMethod < 1)
                return okCustomJson(CODE40001, "参数错误");
            StoreOrder order = StoreOrder.find.byId(orderId);
            if (null == order) return okCustomJson(CODE40002, "该订单不存在");
            if (order.status != ORDER_STATUS_UNPAY) return okCustomJson(CODE40003, "该订单不是待支付状态,不能支付");
            Result result = null;
            try {
                if (!businessUtils.setLock(String.valueOf(orderId), OPERATION_PAY_STORE_ORDER))
                    return okCustomJson(CODE40004, "正在支付中，请稍等");
                if (payMethod == BusinessConstant.PAYMENT_WEPAY) payMethod = BusinessConstant.PAYMENT_WEPAY_MINIAPP;
                if (ValidationUtil.isEmpty(openId)) openId = member.openId;
                PayParam payParam = new PayParam.Builder()
                        .tradeNo(order.orderNo)
                        .subject("晴松-订单支付")
                        .productionCode("oid" + order.id)
                        .realPayMoney(order.realPay)
                        .totalAmount(order.realPay)
                        .uid(member.id)
                        .openId(openId)
                        .payMethod(payMethod)
                        .build();

                switch (payMethod) {
                    case BusinessConstant.PAYMENT_BANK:
                        result = okCustomJson(CODE40005, "正在申请中...");
                        break;
                    case BusinessConstant.PAYMENT_ALIPAY:
                        result = aliPayController.aliAppPay(payParam);
                        break;
                    case BusinessConstant.PAYMENT_ALIPAY_PC:
                        result = aliPayController.aliPagePay(payParam);
                        break;
                    case BusinessConstant.PAYMENT_ALIPAY_WAP:
                        result = aliPayController.aliWapPay(payParam);
                        break;
                    case BusinessConstant.PAYMENT_WEPAY:
                        //wechat app
                        result = invokeWechatPay(order, payParam, payMethod);
                        break;
                    case BusinessConstant.PAYMENT_WEPAY_MINIAPP:
                        if (ValidationUtil.isEmpty(openId)) {
                            return okCustomJson(CODE40001, "微信支付需要openId");
                        }
                        result = invokeWechatPay(order, payParam, payMethod);
                        break;
                    case BusinessConstant.PAYMENT_WEPAY_NATIVE:
                        result = invokeWechatPay(order, payParam, payMethod);
                        break;
                    case BusinessConstant.PAYMENT_WEPAY_H5:
                        if (ValidationUtil.isEmpty(openId)) {
                            return okCustomJson(CODE40001, "公众号支付需要openId");
                        }
                        result = invokeWechatPay(order, payParam, payMethod);
                        break;
                    case BusinessConstant.PAYMENT_BALANCE_PAY:
//                        if (!businessUtils.checkVcode(member.phoneNumber, vcode)) {
//                            result = okCustomJson(CODE40009, "短信验证码有误");
//                            break;
//                        }
                        result = payByBalance(payParam);
                        break;
                    default:
                        result = okCustomJson(CODE40004, "该支付方式尚未支持,请换成其他支付方式");
                        break;
                }
            } catch (Exception e) {
                logger.error("payOrder:" + e.getMessage());
            } finally {
                businessUtils.unLock(String.valueOf(orderId), OPERATION_PAY_STORE_ORDER);
            }
            return result;
        });
    }

    private Result invokeWechatPay(StoreOrder order, PayParam payParam, int payMethod) {
        logger.info("wechat payparam:" + payParam.toString());
        try {
            payParam.tradeNo = order.orderNo;
            if (payMethod == PAYMENT_WEPAY_NATIVE) {
                return wechatPayController.nativePay(payParam).toCompletableFuture().get(15, TimeUnit.SECONDS);
            } else {
                return wechatPayController.wechatPay(payParam).toCompletableFuture().get(15, TimeUnit.SECONDS);
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("payOrder:" + e.getMessage());
            return okCustomJson(CODE500, "微信支付发生异常，请稍后再试");
        }
    }

    /**
     * 使用余额支付
     *
     * @param payParam
     * @return
     */
    private Result payByBalance(PayParam payParam) {
        StoreOrder order = StoreOrder.find.query().where()
                .eq("orderNo", payParam.tradeNo)
                .orderBy().desc("id")
                .setMaxRows(1).findOne();
        if (null == order) return okCustomJson(CODE40002, "订单不存在");
        MemberBalance balance = MemberBalance.find.query().where()
                .eq("uid", payParam.uid)
                .eq("itemId", BusinessItem.CASH)
                .setMaxRows(1).findOne();
        String desc = "用户下单扣余额：" + payParam.totalAmount / 100.00 + "元";
        BalanceParam.Builder builder = new BalanceParam.Builder()
                .changeAmount(-payParam.totalAmount)
                .leftBalance(-payParam.totalAmount)
                .totalBalance(-payParam.totalAmount)
                .desc(desc)
                .orderNo(order.orderNo)
                .memberId(payParam.uid)
                .bizType(TRANSACTION_TYPE_PLACE_ORDER);
        if (null != balance && balance.leftBalance >= payParam.totalAmount) {
            BalanceParam param = builder.itemId(BusinessItem.CASH).build();
            balanceUtils.saveChangeBalance(param, true);
            order.setStatus(ORDER_STATUS_PAID);
            long currentTime = dateUtils.getCurrentTimeBySecond();
            order.setPayTime(currentTime);
            order.save();
            businessUtils.handleOrderPaid(order.orderNo, payParam.tradeNo,
                    BusinessConstant.PAYMENT_BALANCE_PAY, "");
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            return ok(result);
        }

        return okCustomJson(CODE500, "您的余额不足，请换个支付方式");
    }

    /**
     * @api {POST} /v1/o/store/product_orders/:orderId/ 04确认收货
     * @apiName confirmOrder
     * @apiGroup Order
     * @apiParam {String} location location
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Error 40001){int} code 40001 参数错误
     * @apiSuccess (Error 40002){int} code 40002 只能确认自己的订单
     * @apiSuccess (Error 40003){int} code 40003 该商品不存在
     */
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> confirmOrder(Http.Request request, long orderId) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            if (orderId < 1) return okCustomJson(CODE40001, "参数错误");
            JsonNode requestNode = request.body().asJson();
            if (null == requestNode) return okCustomJson(CODE40001, "参数错误");
            String location = requestNode.findPath("location").asText();
            StoreOrder order = StoreOrder.find.byId(orderId);
            if (null == order) return okCustomJson(CODE40003, "该订单不存在");
            if (order.uid != member.id && order.storeId != member.shopId) return okCustomJson(CODE40003, "无权操作该订单");
            if (order.status < ORDER_STATUS_PAID) return okCustomJson(CODE40001, "该订单状态不可核销");
            if (order.status >= ORDER_STATUS_TAKEN) return okCustomJson(CODE40001, "该订单已签收/核销");
            OrderLog orderLog = new OrderLog();
            orderLog.setOrderId(order.id);
            orderLog.setOldStatus(order.status);
            orderLog.setNewStatus(Order.ORDER_STATUS_TAKEN);
            orderLog.setOperatorName(businessUtils.getUserName(member));
            orderLog.setCreateTime(dateUtils.getCurrentTimeBySecond());
            String note = "";
            if (order.uid == member.id) note = "用户确认收货";
            else if (order.storeId == member.shopId) note = "营业员确认核销";
            orderLog.setNote(note);
            orderLog.save();
            order.setStatus(Order.ORDER_STATUS_TAKEN);
            long currentTime = dateUtils.getCurrentTimeBySecond();
            order.setUpdateTime(currentTime);
            order.setLocation(location);
            order.save();
            List<StoreOrderDetail> detailList = StoreOrderDetail.find.query().where()
                    .eq("orderId", order.id)
                    .findList();
            if (detailList.size() > 0) {
                detailList.parallelStream().forEach((each) -> {
                    each.setStatus(order.status);
                });
                DB.saveAll(detailList);
            }
            return okJSON200();
        });
    }

    /**
     * @api {GET} /v1/o/store/orders/:orderId/ 05订单详情
     * @apiName getOrder
     * @apiGroup Order
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Success 200){String} orderNo 订单编号
     * @apiSuccess (Success 200){String} totalMoney 总金额
     * @apiSuccess (Success 200){String} realPay 实付
     * @apiSuccess (Success 200){String} logisticsFee 运费
     * @apiSuccess (Success 200){int} productCount 商品数量
     * @apiSuccess (Success 200){String} statusName 状态名字
     * @apiSuccess (Success 200){String} description 描述
     * @apiSuccess (Success 200){JsonArray} detailList 商品列表
     * @apiSuccess (Success 200){string} productName 商品名字
     * @apiSuccess (Success 200){string} productImgUrl 商品图片
     * @apiSuccess (Success 200){string} skuName 商品属性描述
     * @apiSuccess (Success 200){int} state 售后状态 0 未发起售后 1 申请售后 -1 售后已取消 2 处理中 200 处理完毕
     * @apiSuccess (Success 200){int} status  0未付款,1已付款,2已发货,3已签收,-1退货申请,-2退货中,-3已退货,-4取消交易 -5撤销申请
     * @apiSuccess (Success 200){String} isMix 是否混搭
     * @apiSuccess (Error 40001){int} code 40001 参数错误
     * @apiSuccess (Error 40002){int} code 40002 找不到该订单
     */
    public CompletionStage<Result> getOrder(Http.Request request, long orderId) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            if (orderId < 1) return okCustomJson(CODE40001, "参数错误");
            StoreOrder order = StoreOrder.find.byId(orderId);
            if (null == order) return okCustomJson(CODE40002, "找不到该订单");

            if (member.id != order.uid) {
                if (order.storeId != member.shopId) return okCustomJson(CODE40003, "无权查看该订单详情");
            }
            List<StoreOrderDetail> list = StoreOrderDetail.find.query().where()
                    .eq("orderId", order.id)
                    .orderBy().asc("id")
                    .findList();
            order.detailList.addAll(list);
            ObjectNode result = (ObjectNode) Json.toJson(order);
            result.put(CODE, CODE200);

            List<StoreOrderReturns> orderReturnList = StoreOrderReturns.find.query().where()
                    .eq("orderId", orderId)
                    .ge("status", OrderReturns.STATUS_RETURN_TO_AUDIT)
                    .orderBy().asc("id")
                    .findList();
            result.set("orderReturnList", Json.toJson(orderReturnList));

            List<StoreOrderDelivery> deliveryList = StoreOrderDelivery.find.query().where()
                    .eq("orderId", orderId)
                    .orderBy().asc("id")
                    .findList();
            deliveryList.parallelStream().forEach((orderDelivery) -> {
                List<StoreOrderDeliveryDetail> orderDeliveryDetailList = StoreOrderDeliveryDetail.find.query().where()
                        .eq("orderId", orderId)
                        .orderBy().asc("id")
                        .findList();
                orderDelivery.orderDeliveryDetailList.addAll(orderDeliveryDetailList);
            });
            result.set("deliveryList", Json.toJson(deliveryList));
            return ok(result);
        });

    }


    /**
     * @api {POST} /v1/o/store/cancel_order/ 07取消订单
     * @apiName cancelOrder
     * @apiGroup Order
     * @apiParam {long} orderId 订单Id
     * @apiParam {String} operation 为cancel取消
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Error 40001){int} code 40001 参数错误
     * @apiSuccess (Error 40002){int} code 40002 交易密码错误
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public CompletionStage<Result> cancelOrder(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            JsonNode requestNode = request.body().asJson();
            if (null == requestNode) return okCustomJson(CODE40001, "参数错误");
            String operation = requestNode.findPath("operation").asText();
            long orderId = requestNode.findPath("orderId").asLong();
            if (ValidationUtil.isEmpty(operation) || !operation.equals("cancel") || orderId < 1)
                return okCustomJson(CODE40001, "参数错误");
            StoreOrder order = StoreOrder.find.query().where().eq("id", orderId)
                    .eq("uid", member.id).setMaxRows(1).findOne();
            if (null == order) return okCustomJson(CODE40002, "订单不存在");
            if (order.orderType == ORDER_TYPE_MEMBERSHIP) return okCustomJson(CODE40002, "购买会员的订单不可取消");
            if (order.orderType == ORDER_TYPE_NORMAL) {
                if (order.status < ORDER_STATUS_UNPAY || order.status > ORDER_STATUS_TO_DELIEVERY)
                    return okCustomJson(CODE40003, "订单处于不可取消状态，如有需要请申请售后");
            } else if (order.orderType == ORDER_TYPE_SERVICE) {
                List<MemberCardCoupon> list = MemberCardCoupon.find.query().where()
                        .eq("orderId", order.id)
                        .ne("status", MemberCardCoupon.STATUS_NOT_USE)
                        .findList();
                if (list.size() > 0) {
                    return okCustomJson(CODE40003, "该订单产生的卡包已使用或过期了，不可退款");
                }
            } else {
                if (order.status < ORDER_STATUS_UNPAY || order.status > ORDER_STATUS_ARRIVE_SELF_TAKEN_PLACE)
                    return okCustomJson(CODE40003, "订单处于不可取消状态，如有需要请申请售后");
            }
            long currentTime = dateUtils.getCurrentTimeBySecond();
            try {
                if (!businessUtils.setLock(String.valueOf(orderId), OPERATION_CANCEL_ORDER))
                    return okCustomJson(CODE40004, "正在取消订单中,请稍等");
                if (order.status == ORDER_STATUS_UNPAY) {
                    setOrderCancelled(order, currentTime, member.realName + member.id);
                    returnCoupon(order);
                    businessUtils.unLock(String.valueOf(orderId), OPERATION_CANCEL_ORDER);
                    return okJSON200();
                } else {
                    boolean result;
                    if (order.realPay > 0) {
                        result = refundMoney(order, "用户取消订单，退款", "" + order.id);
                    } else result = true;
                    if (result) {
                        setOrderCancelled(order, currentTime, member.realName + member.id);
                        //如果使用积分，需要退还
                        if (order.scoreUse > 0) returnScoreForCancelOrder(order);
                        //扣除赠送的积分
                        if (order.scoreGave > 0) subtractScoreGave(order);
                        returnCoupon(order);
                        return okJSON200();
                    } else {
                        logger.info("refund status false:");
                        return okCustomJson(CODE500, "取消订单发生异常，请稍后再试");
                    }
                }
            } catch (Exception e) {
                logger.error("cancelOrder:" + e.getMessage());
                return okCustomJson(CODE500, "取消订单发生异常，请稍后再试");
            } finally {
                businessUtils.unLock(String.valueOf(orderId), OPERATION_CANCEL_ORDER);
            }
        });
    }

    private void returnCoupon(StoreOrder order) {
        if (order.couponId > 0) {
            MemberCoupon coupon = MemberCoupon.find.byId(order.couponId);
            if (coupon.status == MemberCoupon.STATUS_USED) {
                coupon.setStatus(STATUS_NOT_USE);
                coupon.setUseTime(0);
                coupon.save();
            }
        }
    }

    private void setOrderCancelled(StoreOrder order, long currentTime, String operatorName) {
        StoreOrderLog orderLog = new StoreOrderLog();
        orderLog.setOrderId(order.id);
        orderLog.setOldStatus(order.status);
        orderLog.setNewStatus(Order.ORDER_STATUS_CANCELED);
        orderLog.setOperatorName(operatorName);
        orderLog.setCreateTime(currentTime);
        orderLog.setNote("用户取消订单");
        orderLog.save();

        order.setStatus(Order.ORDER_STATUS_CANCELED);
        order.setUpdateTime(currentTime);
        order.save();
        List<StoreOrderDetail> detailList = StoreOrderDetail.find.query().where()
                .eq("orderId", order.id)
                .findList();
        if (detailList.size() > 0) {
            detailList.parallelStream().forEach((each) -> each.setStatus(order.status));
            DB.saveAll(detailList);
        }
    }

    private void returnScoreForCancelOrder(StoreOrder order) {
        BalanceParam param = new BalanceParam.Builder()
                .itemId(BusinessItem.SCORE)
                .changeAmount(order.scoreUse)
                .leftBalance(order.scoreUse)
                .totalBalance(order.scoreUse)
                .memberId(order.uid)
                .desc("用户取消订单退回积分")
                .bizType(TRANSACTION_TYPE_CANCEL_ORDER).build();
        balanceUtils.saveChangeBalance(param, true);
    }

    public void subtractScoreGave(StoreOrder order) {
        if (order.scoreGave > 0) {
            BalanceParam param = new BalanceParam.Builder()
                    .itemId(BusinessItem.SCORE)
                    .changeAmount(-order.scoreGave)
                    .leftBalance(-order.scoreGave)
                    .totalBalance(-order.scoreGave)
                    .memberId(order.uid)
                    .desc("取消订单扣除赠送的消费积分:" + order.scoreGave)
                    .bizType(TRANSACTION_TYPE_SUBTRACT_SCORE_GAVE_FOR_CANCEL_ORDER).build();
            balanceUtils.saveChangeBalance(param, true);
        }
    }

    /**
     * 退款
     *
     * @param order
     * @param subject
     * @param productionCode
     * @return
     */
    public boolean refundMoney(StoreOrder order, String subject, String productionCode) {
        if (order.realPay <= 0) return true;
        long returnMoney = order.realPay;
        PayParam payParam = new PayParam.Builder()
                .tradeNo(order.orderNo).subject(subject)
                .productionCode(productionCode)
                .totalAmount(order.realPay)
                .returnMoney(returnMoney)
                .uid(order.uid).build();
        switch (order.payMethod) {
            case BusinessConstant.PAYMENT_BALANCE_PAY:
                return refundMoneyWithBalancePayment(order, subject);
            case BusinessConstant.PAYMENT_BEAUTY_CARD:
                return refundMoneyWithBalancePayment(order, subject);
            case BusinessConstant.PAYMENT_ALIPAY:
                return refundMoneyWithAlipay(payParam, order, subject);
            case BusinessConstant.PAYMENT_ALIPAY_WAP:
                return refundMoneyWithAlipay(payParam, order, subject);
            case BusinessConstant.PAYMENT_WEPAY:
                return refundMoneyWithWechatPay(payParam, order);
            case BusinessConstant.PAYMENT_WEPAY_MINIAPP:
                return refundMoneyWithWechatPay(payParam, order);
        }
        return false;
    }

    private boolean refundMoneyWithBalancePayment(StoreOrder order, String subject) {
        BalanceLog balanceLog = BalanceLog.find.query().where().eq("orderNo", order.orderNo)
                .orderBy().asc("id")
                .setMaxRows(1)
                .findOne();
        if (null != balanceLog) {
            long totalAmount = order.realPay;
            BalanceParam param = new BalanceParam.Builder()
                    .itemId(balanceLog.itemId)
                    .changeAmount(totalAmount)
                    .leftBalance(totalAmount)
                    .totalBalance(totalAmount)
                    .days(0)
                    .desc(subject)
                    .memberId(order.uid)
                    .bizType(TRANSACTION_TYPE_REFUND_MONEY).build();
            balanceUtils.saveChangeBalance(param, true);
            setOrderRefunded(order);
            return true;
        }
        return false;
    }


    private boolean refundMoneyWithAlipay(PayParam payParam, StoreOrder order, String subject) {
        String appId = businessUtils.getAlipayAppId();
        String privateKey = businessUtils.getAlipayAppPrivateKey();
        String alipayAliPublicKeyRSA2 = businessUtils.getAlipayAliPublicKey();
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.ALI_PAY_URL,
                appId, privateKey, AlipayConstants.FORMAT, AlipayConstants.CHARSET,
                alipayAliPublicKeyRSA2, AlipayConstants.SIGN_TYPE_RSA2); //获得初始化的AlipayClient
        AlipayTradeRefundRequest alipayRequest = new AlipayTradeRefundRequest();//创建API对应的request
        alipayRequest.setApiVersion("1.0");
        ObjectNode node = Json.newObject();
        node.put("out_trade_no", payParam.tradeNo);
        node.put("refund_amount", String.valueOf(payParam.totalAmount));
        node.put("refund_reason", subject);
        alipayRequest.setBizContent(node.toString());
        try {
            AlipayTradeRefundResponse response = alipayClient.execute(alipayRequest);
            if (response.isSuccess()) {
                String userLogonId = response.getBuyerLogonId();
                String tradeNo = response.getTradeNo();
                String refundId = userLogonId + " " + tradeNo;
                order.setRefundTxId(refundId);
                setOrderRefunded(order);
                return true;
            } else logger.error("未成团退款失败:" + response.getBody());
        } catch (AlipayApiException e) {
            logger.error("refundMoneyWithAlipay:" + e.getMessage());
        }
        return false;
    }

    private boolean refundMoneyWithWechatPay(PayParam payParam, StoreOrder order) {
        try {
            Map<String, String> param = new HashMap<>();
            param.put("appid", businessUtils.getWechatMiniAppId());
            param.put("mch_id", businessUtils.getWechatMchId());
            param.put("sign_type", "MD5");
            param.put("nonce_str", UUID.randomUUID().toString().replace("-", "").toUpperCase());
            param.put("out_trade_no", payParam.tradeNo);
            param.put("out_refund_no", "REFUND" + payParam.tradeNo);
            param.put("total_fee", payParam.totalAmount + "");//微信以分为单位
            param.put("refund_fee", payParam.returnMoney + "");//微信以分为单位
            param.put("op_user_id", businessUtils.getWechatMchId());
            param.put("refund_account", "REFUND_SOURCE_UNSETTLED_FUNDS");
            String sign = wechatHelper.signWithMd5(param);
            param.put("sign", sign);
            String xml = wechatHelper.convertMapToXML(param);
            WSResponse wsResponse = wsclient.url(WechatConfig.REFUND_URL).post(xml).toCompletableFuture().get();
            if (null != wsResponse) {
                try {
                    logger.info("微信退款返回的报文: " + new String(wsResponse.getBody().getBytes("ISO8859-1")));
                    String response = new String(wsResponse.getBody().getBytes("ISO8859-1"));
                    Map<String, String> returnMap = wechatHelper.getMapFromXML(response);
                    if (null != returnMap) {
                        String signedStr = wechatHelper.signWithMd5(returnMap).toUpperCase();
                        String returnCode = returnMap.getOrDefault("return_code", "");
                        String returnMsg = returnMap.getOrDefault("return_msg", "");
                        String returnSign = returnMap.getOrDefault("sign", "");
                        if (returnCode.equalsIgnoreCase("SUCCESS") && returnMsg.equalsIgnoreCase("OK")
                                && signedStr.equals(returnSign)) {
                            order.setRefundTxId(returnMap.getOrDefault("transaction_id", ""));
                            setOrderRefunded(order);
                            return true;
                        }
                    }
                } catch (ParserConfigurationException | IOException | SAXException e) {
                    logger.error("refundMoneyWithWechatPay:" + e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    private void setOrderRefunded(StoreOrder order) {
        if (order.totalReturnNumber >= order.productCount) {
            order.setStatus(Order.ORDER_STATUS_SYSTEM_REFUNDED);
            order.setUpdateTime(System.currentTimeMillis() / 1000);
            order.save();
        }

        if (order.orderType == ORDER_TYPE_SERVICE) {
            List<MemberCardCoupon> list = MemberCardCoupon.find.query().where().eq("orderId", order.id)
                    .findList();
            if (list.size() > 0) {
                list.parallelStream().forEach((each) -> {
                    each.setStatus(MemberCardCoupon.STATUS_CANCELED);
                });
                Ebean.saveAll(list);
            }
        }
    }

    /**
     * @api {GET} /v1/o/store/order_stat/ 08订单数统计
     * @apiName listOrderStat
     * @apiGroup Order
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Success 200) {int} unpaySize 待支付订单数
     * @apiSuccess (Success 200) {int} toMailSize 待发货订单数
     * @apiSuccess (Success 200) {int} toConfirmSize 待收货订单数
     * @apiSuccess (Success 200) {int} toCommentSize 待评论订单数
     */
    public CompletionStage<Result> listOrderStat(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            List<Object> unpaylist = StoreOrder.find.query().where().eq("uid", uid)
                    .eq("status", ORDER_STATUS_UNPAY).findIds();
            List<Object> toMailList = StoreOrder.find.query().where().eq("uid", uid)
                    .eq("status", ORDER_STATUS_TO_DELIEVERY).findIds();
            List<Object> toConfirmList = StoreOrder.find.query().where().eq("uid", uid)
                    .eq("status", ORDER_STATUS_TO_TAKE).findIds();
            List<Object> toCommentList = StoreOrder.find.query().where().eq("uid", uid)
                    .eq("status", ORDER_STATUS_TAKEN).findIds();
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            result.put("unpaySize", unpaylist.size());
            result.put("toMailSize", toMailList.size());
            result.put("toConfirmSize", toConfirmList.size());
            result.put("toCommentSize", toCommentList.size());
            return ok(result);
        });
    }


    /**
     * @api {POST} /v1/o/store/order_return/:id/  11申请售后修改
     * @apiName updateOrderReturns
     * @apiGroup Order
     * @apiParam {String} reason 理由
     * @apiParam {long} returnMoney 退款金额
     * @apiParam {String} note 备注
     * @apiParam {Array} [imgList] 图片地址的数组
     * @apiSuccess (Success 200){int} code 200
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public CompletionStage<Result> updateOrderReturns(Http.Request request, long id) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            JsonNode requestNode = request.body().asJson();
            if (null == requestNode) return okCustomJson(CODE40001, "参数错误");
            String reason = requestNode.findPath("reason").asText();
            String note = requestNode.findPath("note").asText();
            long returnMoney = requestNode.findPath("returnMoney").asLong();

            StoreOrderReturns returnsApply = StoreOrderReturns.find.byId(id);
            if (returnsApply.uid != uid) return unauth403();
            long currentTime = dateUtils.getCurrentTimeBySecond();
            returnsApply.setReason(reason);
            returnsApply.setUpdateTime(currentTime);
            if (returnsApply.state == STATE_RETURN_REFUND) {
                long maxTime = DEFAULT_MAX_TIME_FOR_RETURN_GOODS;
                String maxTimeStr = businessUtils.getConfigValue(PARAM_KEY_MAX_TIME_FOR_RETURN_GOODS);
                if (!ValidationUtil.isEmpty(maxTimeStr)) maxTime = Long.parseLong(maxTimeStr);
                returnsApply.setHandlingApplyEndTime(currentTime + maxTime);
                returnsApply.setStatus(STATUS_RETURN_TO_AUDIT);
            } else {
                long maxTime = DEFAULT_MAX_TIME_FOR_REFUND;
                String maxTimeStr = businessUtils.getConfigValue(PARAM_KEY_MAX_TIME_FOR_REFUND);
                if (!ValidationUtil.isEmpty(maxTimeStr)) maxTime = Long.parseLong(maxTimeStr);
                returnsApply.setHandlingRefundEndTime(currentTime + maxTime);
                returnsApply.setStatus(STATUS_RETURN_TO_REFUND);
            }
            returnsApply.setRemark(note);
            returnsApply.setReturnMoney(returnMoney);
            returnsApply.save();
            if (requestNode.has("imgList")) {
                JsonNode imgList = requestNode.findPath("imgList");
                if (null != imgList) {
                    ArrayNode imgNodes = (ArrayNode) requestNode.findPath("imgList");
                    List<StoreOrderReturnsImage> updateList = new ArrayList<>();
                    if (imgNodes.isArray() && imgNodes.size() > 0) {
                        int currentCount = imgNodes.size();
                        if (currentCount > 9) return okCustomJson(CODE40001, "图片最多允许9张");
                        imgNodes.forEach((each) -> {
                            if (null != each) {
                                StoreOrderReturnsImage image = new StoreOrderReturnsImage();
                                image.setReturnApplyId(returnsApply.id);
                                image.setOrderId(returnsApply.orderId);
                                image.setOrderDetailId(returnsApply.orderDetailId);
                                image.setUid(uid);
                                String imgUrl = businessUtils.escapeHtml(each.asText());
                                image.setImgUrl(imgUrl);
                                image.setCreateTime(currentTime);
                                updateList.add(image);
                            }
                        });
                    }
                    List<StoreOrderReturnsImage> imageList = StoreOrderReturnsImage.find.query().where()
                            .eq("returnApplyId", returnsApply.id)
                            .orderBy().asc("id")
                            .findList();
                    if (imageList.size() > 0) DB.deleteAll(imageList);
                    if (updateList.size() > 0) DB.saveAll(updateList);
                }
            }
            String returnsNo = returnsApply.getReturnsNo();
            ObjectNode resultNode = Json.newObject();
            resultNode.put("type", TASK_NEW_POST_SERVICE);
            resultNode.put("id", returnsApply.id);
            resultNode.put("orderId", returnsApply.orderId);
            resultNode.put("returnsNo", returnsNo);
            businessUtils.unLock(String.valueOf(uid), OPERATION_APPLY_ORDER_RETURN);
            resultNode.put(CODE, CODE200);
            return ok(resultNode);
        });
    }

    /**
     * @api {POST} /v1/o/store/calc_order/ 12计算下单金额与优惠
     * @apiName calcOrder
     * @apiGroup Order
     * @apiParam {string} [description] 备注
     * @apiParam {JsonArray} list 购买的商品列表
     * @apiParam {long} productId 商品ID
     * @apiParam {long} skuId 商品skuID,为0时表示只有一个标准的产品，没有sku
     * @apiParam {long} amount 购买数量
     * @apiParam {String} [remark] 备注，每个单品的备注，预留
     * @apiParam {long} [memberCouponId] 用户优惠券ID
     * @apiParam {int} [payMethod] 支付方式，余额支付时传入
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Success 200){String} transactionId 流水号
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public CompletionStage<Result> calcOrder(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            JsonNode requestNode = request.body().asJson();
            if (null == requestNode) return okCustomJson(CODE40001, "参数错误");
            JsonNode paramList = requestNode.findPath("list");

            if (null == paramList || !paramList.isArray()) return okCustomJson(CODE40001, "请选择商品");
            List<StoreOrderDetail> detailList = new ArrayList<>();
            long totalMoney = 0;
            long realPay = 0;
            int productAmount = 0;
            long requireScore = 0;
            for (int i = 0; i < paramList.size(); i++) {
                JsonNode node = paramList.get(i);
                StoreOrderDetail orderDetail = new StoreOrderDetail();
                final int amount = node.findPath("amount").asInt();
                final long productId = node.findPath("productId").asLong();
                final long skuId = node.findPath("skuId").asLong();
                final String remark = node.findPath("remark").asText();
                StoreProduct product = StoreProduct.find.byId(productId);
                if (null == product) return okCustomJson(CODE40001, "该商品不存在");
                if (amount < 1) return okCustomJson(CODE40001, "请选择商品数量");
                if (product.status < STATUS_ON_SHELVE) return okCustomJson(CODE40001, "该商品不存在或已下架");
                if (skuId < 1) return okCustomJson(CODE40001, "sku参数有误");
                ProductSku productSku = getSku(product.sku, skuId);
                if (null == productSku) return okCustomJson(CODE40001, "商品SKU有误");

                if (amount > productSku.stock) {
                    return okCustomJson(CODE40001, "下单数量超出" + product.name + "的库存数量");
                }
                setOrderDetailByShop(orderDetail, amount, remark, product, productSku);
                productAmount = productAmount + amount;
                requireScore = requireScore + amount * productSku.requireScore;
                orderDetail.setProductPrice(productSku.price);
                long thisItemTotalMoney = orderDetail.productPrice * orderDetail.number;
                totalMoney = totalMoney + thisItemTotalMoney;
                realPay = realPay + thisItemTotalMoney;
                detailList.add(orderDetail);
            }

            ObjectNode resultNode = Json.newObject();
            resultNode.put(CODE, CODE200);
            resultNode.set("orderDetailList", Json.toJson(detailList));

            //积分计算
            long scoreToMoney = 0;
            if (requireScore > 0) {
                MemberBalance caseBalance = MemberBalance.find.query().where().eq("uid", uid)
                        .eq("itemId", BusinessItem.SCORE).setMaxRows(1).findOne();
                if (null == caseBalance || caseBalance.leftBalance < requireScore) {
                    return okCustomJson(CODE40005, "您的积分不足，无法购买该积分商品");
                }
            }
            long mailFee = 0;
            resultNode.put("mailFee", mailFee);
            resultNode.put("totalMoney", totalMoney);
            resultNode.put("requireScore", requireScore);
            resultNode.put("scoreToMoney", scoreToMoney);
            if (realPay <= 0) realPay = 1;
            resultNode.put("realPayMoney", realPay);
            return ok(resultNode);
        });
    }

    private ProductSku getSku(String sku, long skuId) {
        if (ValidationUtil.isEmpty(sku)) return null;
        JsonNode productSku = Json.parse(sku);
        if (null == productSku) return null;
        ArrayNode arrayNode = (ArrayNode) productSku.findPath("skuList");
        for (JsonNode node : arrayNode) {
            long targetSkuId = node.findPath("id").asLong();
            if (targetSkuId == skuId) {
                ProductSku eachSku = Json.fromJson(node, ProductSku.class);
                return eachSku;
            }
        }
        return null;
    }

    private void setOrderDetailByShop(StoreOrderDetail orderDetail, int amount, String remark, StoreProduct product, ProductSku productSku) {
        long currentTime = dateUtils.getCurrentTimeBySecond();
        orderDetail.setStoreId(product.shopId);
        orderDetail.setProductPrice(productSku.price);
        orderDetail.setSkuName(productSku.name);
        orderDetail.setProductImgUrl(productSku.imgUrl);
        orderDetail.setNumber(amount);
        orderDetail.setProductId(product.id);
        orderDetail.setProductName(product.name);
        orderDetail.setProductSkuId(productSku.id);
        orderDetail.setNumber(amount);
        orderDetail.setRemark(remark);
        orderDetail.setUnit(product.unit);
        orderDetail.setUpdateTime(currentTime);
        orderDetail.setCreateTime(currentTime);
    }

    /**
     * @api {GET} /v1/o/store/order_count/ 13分类订单数量
     * @apiName listMyOrderCount
     * @apiGroup Order
     * @apiSuccess (Success 200){int} unpayOrders 未付款订单数
     * @apiSuccess (Success 200){int} paidOrders 待发货订单数
     * @apiSuccess (Success 200){int} delieveredOrders 待收货订单数
     * @apiSuccess (Success 200){int} toCommentOrders 待评价订单数
     * @apiSuccess (Success 200){int} postServiceOrders 售后订单数
     */
    public CompletionStage<Result> listMyOrderCount(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            long unpayOrders;
            long paidOrders;
            long delieveredOrders;
            long toCommentOrders;
            long postServiceOrders;
            List<Object> unPayList = StoreOrder.find.query().where().eq("uid", uid)
                    .eq("status", ORDER_STATUS_UNPAY).findIds();
            unpayOrders = unPayList.size();

            List<Object> paidList = StoreOrder.find.query().where().eq("uid", uid)
                    .eq("status", ORDER_STATUS_TO_DELIEVERY).findIds();
            paidOrders = paidList.size();

            List<Object> delieveredList = StoreOrder.find.query().where().eq("uid", uid)
                    .eq("status", ORDER_STATUS_TO_TAKE).findIds();
            delieveredOrders = delieveredList.size();

            List<Object> toCommentList = StoreOrder.find.query().where().eq("uid", uid)
                    .eq("status", ORDER_STATUS_TAKEN)
                    .findIds();
            toCommentOrders = toCommentList.size();

            List<Object> postServiceList = StoreOrderReturns.find.query().where()
                    .eq("uid", uid)
                    .ge("status", StoreOrderReturns.STATUS_RETURN_TO_AUDIT)
                    .findIds();
            postServiceOrders = postServiceList.size();

            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            result.put("unpayOrders", unpayOrders);
            result.put("paidOrders", paidOrders);
            result.put("delieveredOrders", delieveredOrders);
            result.put("toCommentOrders", toCommentOrders);
            result.put("postServiceOrders", postServiceOrders);
            return ok(result);
        });
    }

    /**
     * @api {GET} /v1/o/store/store_orders/?page=&status= 14店铺订单列表
     * @apiName listStoreOrders
     * @apiGroup Order
     * @apiSuccess (Success 200){int} pages 页数
     * @apiSuccess (Success 200){JsonArray} list 订单列表
     * @apiSuccess (Success 200){String} orderNo 订单编号
     * @apiSuccess (Success 200){String} totalMoney 总金额
     * @apiSuccess (Success 200){String} realPay 实付
     * @apiSuccess (Success 200){String} logisticsFee 运费
     * @apiSuccess (Success 200){int} productCount 商品数量
     * @apiSuccess (Success 200){String} statusName 状态名字
     * @apiSuccess (Success 200){String} description 描述
     * @apiSuccess (Success 200){JsonArray} detailList 商品列表
     * @apiSuccess (Success 200){string} productName 商品名字
     * @apiSuccess (Success 200){string} productImgUrl 商品图片
     * @apiSuccess (Success 200){string} skuName 商品属性描述
     * @apiSuccess (Success 200){int} status 状态
     */
    public CompletionStage<Result> listStoreOrders(Http.Request request, int page, int status) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            if (member.shopId < 1) {
                result.set("list", Json.toJson(new ArrayList()));
                return ok(result);
            }
            ExpressionList<StoreOrder> expressionList = StoreOrder.find.query().where()
                    .eq("storeId", member.shopId);
            if (status > 0) {
                if (status == Order.ORDER_STATUS_TO_DELIEVERY) {
                    expressionList.ge("status", Order.ORDER_STATUS_TO_DELIEVERY)
                            .le("status", Order.ORDER_STATUS_DELIEVERED);
                } else if (status == Order.ORDER_STATUS_ARRIVE_SELF_TAKEN_PLACE) {
                    expressionList.ge("status", Order.ORDER_STATUS_TO_TAKE)
                            .le("status", Order.ORDER_STATUS_ARRIVE_SELF_TAKEN_PLACE);
                } else expressionList.eq("status", status);
            }
            PagedList<StoreOrder> pagedList = expressionList.orderBy().desc("id")
                    .setFirstRow((page - 1) * PAGE_SIZE_20)
                    .setMaxRows(PAGE_SIZE_20)
                    .findPagedList();
            List<StoreOrder> list = pagedList.getList();
            //读取订单详情
            list.parallelStream().forEach((each) -> {
                List<StoreOrderDetail> details = StoreOrderDetail.find.query().where()
                        .eq("orderId", each.id)
                        .orderBy().asc("id").findList();
                each.detailList.addAll(details);
                StoreOrderReturns orderReturn = StoreOrderReturns.find.query().where()
                        .eq("orderId", each.id)
                        .orderBy().desc("id")
                        .setMaxRows(1).findOne();
                each.orderReturns = orderReturn;
            });
            int pages = pagedList.getTotalPageCount();
            result.put("pages", pages);
            boolean hasNext = pagedList.hasNext();
            result.put("hasNext", hasNext);
            result.set("list", Json.toJson(list));
            return ok(result);
        });
    }

    /**
     * @api {POST} /v1/o/store/change_order_address/ 15修改订单地址
     * @apiName updateOrderContactAddress
     * @apiGroup Order
     * @apiParam {long} orderId orderId
     * @apiParam {long} addressId addressId
     * @apiSuccess (Success 200){int} code 200
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public CompletionStage<Result> updateOrderContactAddress(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((member) -> {
            if (null == member) return unauth403();
            JsonNode requestNode = request.body().asJson();
            if (null == requestNode) return okCustomJson(CODE40001, "参数错误");
            final long addressId = requestNode.findPath("addressId").asLong();
            final long orderId = requestNode.findPath("orderId").asLong();

            StoreOrder order = StoreOrder.find.byId(orderId);
            if (null == order) return okCustomJson(CODE40003, "该订单不存在");
            if (order.status != Order.ORDER_STATUS_TO_DELIEVERY) return okCustomJson(CODE40001, "该订单不是处于待发货状态，不可修改地址");
            ContactDetail contactDetail = ContactDetail.find.byId(addressId);
            if (null == contactDetail) return okCustomJson(CODE40001, "收货地址有误，请重新选择");
            if (contactDetail.uid != order.uid) return okCustomJson(CODE40001, "收货地址有误，请重新选择");
            String newAddress = Json.stringify(Json.toJson(contactDetail));
            order.setNote(order.note + "用户修改收货地址，修改前：" + order.address + "    修改后：" + newAddress);
            order.setAddress(newAddress);
            order.setUpdateTime(dateUtils.getCurrentTimeBySecond());
            order.save();
            return okJSON200();
        });
    }

    /**
     * @api {POST} /v1/o/store/order_delievered/ 16设置订单已发货
     * @apiName setOrderDelivered
     * @apiGroup Order
     * @apiParam {long} orderId 单号
     * @apiParam {String} [expressNo] 物流单号
     * @apiParam {String} [expressCompany] 物流公司
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Error 40001){int} code 40001 参数错误
     * @apiSuccess (Error 40002){int} code 40002 找不到该订单
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public CompletionStage<Result> setOrderDelivered(Http.Request request) {
        JsonNode jsonNode = request.body().asJson();
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            if (null == jsonNode) return okCustomJson(CODE40001, "参数错误");
            String expressNo = jsonNode.findPath("expressNo").asText();
            String expressCompany = jsonNode.findPath("expressCompany").asText();
            long orderId = jsonNode.findPath("orderId").asLong();
            if (orderId < 1) return okCustomJson(CODE40001, "参数错误");
            StoreOrder order = StoreOrder.find.byId(orderId);
            if (null == order) return okCustomJson(CODE40002, "找不到该订单");

            if (member.shopId != order.storeId)
                return okCustomJson(CODE40001, "非特定人员，无权操作");
            if (order.status >= Order.ORDER_STATUS_DELIEVERED) return okCustomJson(CODE40002, "已经发货了");
            boolean needUpdateOrderStatus = needUpdateOrderStatus(jsonNode, orderId);
            order.setExpressNo(expressNo);
            order.setExpressCompany(expressCompany);
            if (needUpdateOrderStatus) {
                OrderLog orderLog = new OrderLog();
                orderLog.setOrderId(orderId);
                orderLog.setOldStatus(order.status);
                orderLog.setNewStatus(Order.ORDER_STATUS_DELIEVERED);
                order.setStatus(Order.ORDER_STATUS_DELIEVERED);
                orderLog.setNote("设置订单已送货");
                long currentTime = dateUtils.getCurrentTimeBySecond();
                orderLog.setOperatorName(businessUtils.getMemberName(member));
                orderLog.setCreateTime(currentTime);
                orderLog.save();
                order.setDeliveryTime(currentTime);
                List<StoreOrderDetail> detailList = StoreOrderDetail.find.query().where().eq("orderId", order.id)
                        .findList();
                if (detailList.size() > 0) {
                    detailList.parallelStream().forEach((each) -> {
                        each.setStatus(order.status);
                    });
                    DB.saveAll(detailList);
                }
            }
            order.save();
            return okJSON200();
        });
    }


    private boolean needUpdateOrderStatus(JsonNode jsonNode, long orderId) {
        boolean needUpdateOrderStatus = false;
        if (jsonNode.has("detailList")) {
            ArrayNode orderDetailList = (ArrayNode) jsonNode.findPath("detailList");
            int count = StoreOrderDetail.find.query().where().eq("orderId", orderId)
                    .ge("status", Order.ORDER_STATUS_DELIEVERED)
                    .findCount();
            List<StoreOrderDetail> list = new ArrayList<>();
            orderDetailList.forEach((each) -> {
                StoreOrderDetail orderDetail = StoreOrderDetail.find.byId(each.asLong());
                if (null != orderDetail) {
                    orderDetail.setStatus(Order.ORDER_STATUS_DELIEVERED);
                    list.add(orderDetail);
                }
            });
            if (list.size() > 0) DB.saveAll(list);
            int deliverCount = StoreOrderDetail.find.query().where().eq("orderId", orderId)
                    .ge("status", Order.ORDER_STATUS_DELIEVERED)
                    .findCount();
            if (count == deliverCount) needUpdateOrderStatus = true;
        } else needUpdateOrderStatus = true;
        return needUpdateOrderStatus;
    }


    /**
     * @api {POST} /v1/o/store/delete_order/ 17删除订单
     * @apiName deleteOrder
     * @apiGroup Order
     * @apiParam {long} orderId 订单Id
     * @apiParam {String} operation 为delete删除
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Error 40001){int} code 40001 参数错误
     * @apiSuccess (Error 40002){int} code 40002 交易密码错误
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public CompletionStage<Result> deleteOrder(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            JsonNode requestNode = request.body().asJson();
            if (null == requestNode) return okCustomJson(CODE40001, "参数错误");
            String operation = requestNode.findPath("operation").asText();
            long orderId = requestNode.findPath("orderId").asLong();
            if (ValidationUtil.isEmpty(operation) || !operation.equals("delete") || orderId < 1)
                return okCustomJson(CODE40001, "参数错误");
            StoreOrder order = StoreOrder.find.query().where().eq("id", orderId)
                    .eq("uid", uid)
                    .setMaxRows(1)
                    .findOne();
            if (null == order) return okCustomJson(CODE40002, "订单不存在");
            if (order.status >= Order.ORDER_STATUS_PAID && order.status <= ORDER_STATUS_ARRIVE_SELF_TAKEN_PLACE)
                return okCustomJson(CODE40002, "该订单未完成，不可删除");
            if (order.postServiceStatus >= POST_SERVICE_STATUS_APPLYING && order.postServiceStatus < POST_SERVICE_STATUS_REFUND) {
                return okCustomJson(CODE40002, "该订单处于售后处理中，不可删除");
            }
            order.setStatus(ORDER_STATUS_DELETE);
            order.setUpdateTime(dateUtils.getCurrentTimeBySecond());
            order.save();
            return okJSON200();
        });
    }
}
