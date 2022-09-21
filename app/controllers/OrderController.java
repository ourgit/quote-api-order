package controllers;

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
import io.ebean.Ebean;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import io.ebean.SqlUpdate;
import models.dealer.DealerAward;
import models.enroll.EnrollConfig;
import models.groupon.Groupon;
import models.groupon.GrouponOrder;
import models.log.BalanceLog;
import models.order.*;
import models.postservice.OrderReturns;
import models.postservice.OrderReturnsImage;
import models.product.*;
import models.promotion.Bargain;
import models.promotion.CouponConfig;
import models.shop.Shop;
import models.stat.StatMemberSalesOverview;
import models.stat.StatPlatformDaySalesOverview;
import models.stat.StatPlatformMonthSalesOverview;
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
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import static akka.pattern.Patterns.ask;
import static constants.BusinessConstant.*;
import static models.order.Order.*;
import static models.postservice.OrderReturns.*;
import static models.product.Product.STATUS_ON_SHELVE;
import static models.user.MemberCoupon.STATUS_NOT_USE;

/**
 * 用户控制类
 */
public class OrderController extends BaseController {

    Logger.ALogger logger = Logger.of(OrderController.class);

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
    @Named("orderActor")
    private ActorRef orderActorRef;

    @Inject
    @Named("exchangeActor")
    private ActorRef exchangeActorRef;

    /**
     * @api {POST} /v1/o/orders/ 01我的订单列表
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

            ExpressionList<Order> expressionList = Order.find.query().where()
                    .eq("uid", uid)
                    .gt("status", ORDER_STATUS_DELETE)
                    .eq("isSubOrder", true);
            if (status > 0) {
                if (status == Order.ORDER_STATUS_TO_DELIEVERY) {
                    expressionList.ge("status", Order.ORDER_STATUS_TO_DELIEVERY)
                            .le("status", Order.ORDER_STATUS_DELIEVERED);
                } else expressionList.eq("status", status);
            }
            if (payBeingTime > 0) expressionList.ge("payTime", payBeingTime);
            if (payEndTime > 0) expressionList.le("payTime", payEndTime);
            if (orderType > 0) {
                if (orderType == ORDER_TYPE_NORMAL) {
                    expressionList.ne("orderType", ORDER_TYPE_ENROLL);
                } else expressionList.eq("orderType", orderType);
            }
            if (deliveryType > 0) {
                expressionList.eq("deliveryType", deliveryType);
            }
            if (!ValidationUtil.isEmpty(filter)) {
                expressionList.icontains("filter", filter);
            }
            if (!ValidationUtil.isEmpty(productName)) {
                List<OrderDetail> orderDetailList = OrderDetail.find.query().where()
                        .eq("productName", productName)
                        .findList();
                Set<Long> orderIdSet = new HashSet<>();
                if (orderDetailList.size() > 0) {
                    orderDetailList.forEach((each) -> orderIdSet.add(each.getOrderId()));
                } else orderIdSet.add(0L);
                expressionList.in("id", orderIdSet);
            }
            PagedList<Order> pagedList = expressionList.orderBy().desc("id")
                    .setFirstRow((page - 1) * size)
                    .setMaxRows(size)
                    .findPagedList();
            List<Order> list = pagedList.getList();
            //读取订单详情
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            list.parallelStream().forEach((each) -> {
                List<OrderDetail> details = OrderDetail.find.query().where()
                        .eq("orderId", each.id)
                        .orderBy().asc("id").findList();
                each.detailList.addAll(details);
                OrderReturns orderReturn = OrderReturns.find.query().where()
                        .eq("orderId", each.id)
                        .orderBy().desc("id")
                        .setMaxRows(1).findOne();
                each.orderReturns = orderReturn;
                setOrderShop(each);
                setOrderServiceCount(each);
                setOrderMemberCouponProduct(each);
                if (each.enrollId > 0) {
                    EnrollConfig enrollConfig = EnrollConfig.find.byId(each.enrollId);
                    if (null != enrollConfig) {
                        Product product = Product.find.byId(enrollConfig.productId);
                        if (null != product) {
                            List<ProductParam> paramList = ProductParam.find.query().where()
                                    .eq("productId", enrollConfig.productId)
                                    .order().asc("sort")
                                    .order().asc("id")
                                    .findList();
                            product.paramList.addAll(paramList);
                            each.enrollProduct = product;
                        }
                    }
                }
            });

            boolean hasNext = pagedList.hasNext();
            result.put("hasNext", hasNext);
            result.put("totalCount", pagedList.getTotalCount());
            result.set("list", Json.toJson(list));
            return ok(result);
        });
    }

    private void setOrderServiceCount(Order each) {
        int leftServiceCount = 0;
        int usedServiceCount = 0;
        if (each.orderType == ORDER_TYPE_SERVICE) {
            leftServiceCount = MemberCardCoupon.find.query().where()
                    .eq("orderId", each.id)
                    .eq("status", MemberCardCoupon.STATUS_NOT_USE)
                    .findCount();
            usedServiceCount = MemberCardCoupon.find.query().where()
                    .eq("orderId", each.id)
                    .ge("status", MemberCardCoupon.STATUS_USED)
                    .findCount();
        }
        each.leftServiceCount = leftServiceCount;
        each.usedServiceCount = usedServiceCount;
    }

    private void setOrderShop(Order each) {
        if (each.shopId > 0) {
            Shop shop = Shop.find.byId(each.shopId);
            if (null != shop) {
                shop.setFilter("");
            }
            each.shop = shop;
        }
    }

    private void setOrderMemberCouponProduct(Order order) {
        if (order.memberCardCouponId > 0) {
            MemberCardCoupon memberCardCoupon = MemberCardCoupon.find.byId(order.memberCardCouponId);
            if (null != memberCardCoupon) {
                if (memberCardCoupon.productId > 0) {
                    Product product = Product.find.byId(memberCardCoupon.productId);
                    if (null != product) {
                        product.setDetails("");
                        product.setVirtualAmount(0);
                        order.memberCardCouponProduct = product;
                    }
                }
            }
        }
    }


    /**
     * @api {POST} /v1/o/orders/new/ 02下单
     * @apiName placeOrder
     * @apiGroup Order
     * @apiParam {long} memberCouponId 平台优惠券D
     * @apiParam {long} addressId 收货地址ID
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
            try {
                if (uid < 1) return unauth403();
                Member member = Member.find.byId(uid);
                if (null == member) return unauth403();
                JsonNode requestNode = request.body().asJson();
                if (!businessUtils.setLock(String.valueOf(uid), OPERATION_PLACE_ORDER))
                    return okCustomJson(CODE40004, "正在下单中,请稍等");
                long currentTime = dateUtils.getCurrentTimeBySecond();
                if (null == requestNode) return okCustomJson(CODE40001, "参数错误");

                JsonNode list = requestNode.findPath("list");
                if (null == list || !list.isArray() || list.size() < 1) return okCustomJson(CODE40001, "购买数量为空，请先选择商品");
                List<OrderDetail> detailList = new ArrayList<>();
                long addressId = requestNode.findPath("addressId").asLong();
                if (addressId < 1) return okCustomJson(CODE40001, "请选择收货地址");
                long totalMoney = 0;
                long requireScore = 0;
                double totalWeight = 0;
                int productAmount = 0;
                StringBuilder orderShops = new StringBuilder();
                int mailAmount = 0;
                Map<Long, Integer> productAmountMap = new HashMap<>();
                Map<Long, Product> productMap = new HashMap<>();
                for (JsonNode eachShopNode : list) {
                    final String remark = eachShopNode.findPath("remark").asText();
                    int deliveryMethod = MailFee.METHOD_MAIL;
                    JsonNode productList = eachShopNode.findPath("productList");
                    for (JsonNode node : productList) {
                        OrderDetail orderDetail = new OrderDetail();
                        final int amount = node.findPath("amount").asInt();
                        final long productId = node.findPath("productId").asLong();
                        final long skuId = node.findPath("skuId").asLong();
                        orderDetail.deliveryMethod = deliveryMethod;
                        mailAmount = mailAmount + amount;
                        Product product = Product.find.byId(productId);
                        if (null == product) return okCustomJson(CODE40001, "该商品不存在");
                        if (amount < 1) return okCustomJson(CODE40001, "请选择商品数量");
                        if (product.status < STATUS_ON_SHELVE) return okCustomJson(CODE40001, "该商品不存在或已下架");

                        if (skuId < 1) return okCustomJson(CODE40001, "SKU有误");
                        ProductSku productSku = ProductSku.find.byId(skuId);
                        if (null == productSku) return okCustomJson(CODE40001, "该SKU不存在");
                        if (productSku.productId != productId) return okCustomJson(CODE40001, "商品ID有误");
                        if (productSku.requireInvites > 0) {
                            List<Long> invites = Member.find.query().where().eq("dealerId", uid).findIds();
                            if (invites.size() < productSku.requireInvites)
                                return okCustomJson(CODE40001, "该特惠商品需要邀请" + productSku.requireInvites + "人才可购买");
                        }

                        if (productSku.limitAmount > 0) {
                            if (amount > productSku.limitAmount)
                                return okCustomJson(CODE40001, "'" + product.name + "'" + "已达到限购数量,无法购买更多");
                            boolean uptoMaxAmount = checkUptoMaxAmount(productSku, member.id, amount);
                            if (uptoMaxAmount)
                                return okCustomJson(CODE40001, "'" + product.name + "'" + "已达到限购数量,无法购买更多");
                        }
                        if (amount > productSku.stock) {
                            return okCustomJson(CODE40001, "下单数量超出" + product.name + "的库存数量");
                        }
                        Integer mapAmount = productAmountMap.get(productId);
                        if (null == mapAmount) productAmountMap.put(productId, amount);
                        else productAmountMap.put(productId, mapAmount + amount);
                        productMap.put(productId, product);

                        long thisItemTotalMoney = businessUtils.getTotalMoney(productSku, amount, orderDetail);
                        totalMoney = totalMoney + thisItemTotalMoney;
                        setOrderDetail(member.id, currentTime, orderDetail, amount, remark, product, productSku);

                        orderDetail.setSubTotal(thisItemTotalMoney);
                        orderShops.append(product.shopId).append("/");
                        detailList.add(orderDetail);
                        requireScore = requireScore + amount * productSku.requireScore;
                        productAmount = productAmount + amount;
                        totalWeight = totalWeight + productSku.weight * amount;
                    }
                }

                for (Map.Entry<Long, Product> entry : productMap.entrySet()) {
                    Product product = entry.getValue();
                    Integer buyAmount = productAmountMap.get(entry.getKey());
                    product.buyAmount = buyAmount;
                    if (product.minOrderAmount > 0) {
                        if (buyAmount < product.minOrderAmount)
                            return okCustomJson(CODE40001, product.name + "最低购买数量为" + product.minOrderAmount);
                    }
                }
                ActorProtocol.PLACE_ORDER orderParam = new ActorProtocol.PLACE_ORDER(requestNode, member, productMap);
                return orderParam;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("place order:" + e.getMessage());
            } finally {
                businessUtils.unLock(String.valueOf(uid), OPERATION_PLACE_ORDER);
            }
            return okCustomJson(CODE500, "下单发生异常，请稍后再试");
        }).thenCompose((result) -> {
            if (result instanceof ActorProtocol.PLACE_ORDER)
                return executeOrder((ActorProtocol.PLACE_ORDER) result);
            return CompletableFuture.supplyAsync(() -> (Result) result);
        });
    }

    private CompletionStage<Result> executeOrder(ActorProtocol.PLACE_ORDER orderParam) {
        return FutureConverters.toJava(ask(orderActorRef, orderParam, 30000))
                .thenApply(response -> ok((ObjectNode) response));
    }

    private void setOrderDetail(long uid, long currentTime, OrderDetail orderDetail, int amount, String remark, Product product, ProductSku productSku) {
        orderDetail.setUid(uid);
        orderDetail.setProductPrice(productSku.price);
        orderDetail.setSkuName(productSku.name);
        orderDetail.setProductImgUrl(productSku.imgUrl);
        orderDetail.setBrandId(product.brandId);
        orderDetail.setNumber(amount);
        orderDetail.setProductId(product.id);
        orderDetail.setProductName(product.name);
        orderDetail.setProductSkuId(productSku.id);
        orderDetail.setRemark(remark);
        orderDetail.setUnit(product.unit);
        orderDetail.setUpdateTime(currentTime);
        orderDetail.setCreateTime(currentTime);
        orderDetail.setShopId(product.shopId);
        orderDetail.setSupplierId(product.supplierUid);
        orderDetail.setSource(productSku.source);
        orderDetail.setOldPrice(productSku.oldPrice);
        orderDetail.setBidPrice(productSku.bidPrice);
        orderDetail.setCategoryId(product.categoryId);
        orderDetail.setShopCategoryId(product.shopCategoryId);
        orderDetail.setTakeTime(currentTime + productSku.deliverHours * 3600);
    }

    private boolean checkUptoMaxAmount(ProductSku productSku, long uid, long amount) {
        List<OrderDetail> list = OrderDetail.find.query().where()
                .eq("productSkuId", productSku.id)
                .ge("status", Order.ORDER_STATUS_UNPAY)
                .le("returnStatus", OrderDetail.STATUS_APPLY_RETURN)
                .eq("uid", uid)
                .ge("createTime", productSku.beginTime)
                .lt("createTime", productSku.endTime)
                .findList();
        long totalAmount = list.parallelStream().mapToLong((each) -> each.number).sum();
        if (totalAmount + amount > productSku.limitAmount) return true;
        return false;
    }


    public String getAttribute(String attrKey) {
        if (!ValidationUtil.isEmpty(attrKey)) return attrKey + ",";
        return " ";
    }

    /**
     * 使用积分
     *
     * @param requestNode
     * @param uid
     * @param totalMoney
     * @return
     */
    private ObjectNode checkUseScore(JsonNode requestNode, long uid, long totalMoney) {
        long useScore = requestNode.findPath("useScore").asLong();//全部使用
        ObjectNode result = Json.newObject();
        result.put(CODE, CODE200);
        long realPayMoney = totalMoney;
        MemberBalance memberBalance = MemberBalance.find.query().where()
                .eq("uid", uid)
                .eq("itemId", BusinessItem.SCORE)
                .setMaxRows(1).findOne();
        if (null == memberBalance || memberBalance.leftBalance < 1) {
            result.put("scoreToMoney", 0);
            useScore = 0;
        } else {
            //totalMoney以分为单位， 1个积分等于2分钱=0.02元
            double scoreToMoneyScale = businessUtils.getScoreMoneyScale();
            long scoreToMoney = (long) (memberBalance.leftBalance * scoreToMoneyScale);
            realPayMoney = totalMoney - scoreToMoney;
            useScore = (long) memberBalance.leftBalance;
            if (realPayMoney < 0) {
                realPayMoney = 0;
                long scoreNeed = (long) (totalMoney / scoreToMoneyScale);
                if (scoreNeed < 1) scoreNeed = 1;
                useScore = scoreNeed;//需要扣除的积分
                scoreToMoney = totalMoney;
            }
            result.put("scoreToMoney", scoreToMoney);
        }
        result.put("useScore", useScore);
        result.put("realPayMoney", realPayMoney);
        return result;
    }


    /**
     * @api {POST} /v1/o/pay_order/  03支付订单
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
            String code = requestNode.findPath("code").asText();
            int payMethod = requestNode.findPath("payMethod").asInt();
            if (orderId < 1 || payMethod < 1)
                return okCustomJson(CODE40001, "参数错误");
            String vcode = requestNode.findPath("vcode").asText();
            Order order = Order.find.byId(orderId);
            if (null == order) return okCustomJson(CODE40002, "该订单不存在");
            if (order.status != ORDER_STATUS_UNPAY) return okCustomJson(CODE40003, "该订单不是待支付状态,不能支付");
            Result result = null;
            try {
                if (!businessUtils.setLock(String.valueOf(orderId), OPERATION_PAY_ORDER))
                    return okCustomJson(CODE40004, "正在支付中，请稍等");
                openId = member.openId;
                PayParam payParam = new PayParam.Builder()
                        .tradeNo(order.orderNo)
                        .subject("晴松-订单支付")
                        .productionCode("oid" + order.id)
                        .realPayMoney(order.realPay)
                        .totalAmount(order.realPay)
                        .uid(member.id)
                        .openId(openId)
                        .payMethod(payMethod)
//                        .useScore(useScore)
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
                businessUtils.unLock(String.valueOf(orderId), OPERATION_PAY_ORDER);
            }
            return result;
        });
    }

    private Result invokeWechatPay(Order order, PayParam payParam, int payMethod) {
        logger.info("wechat payparam:" + payParam.toString());
        try {
            payParam.tradeNo = order.orderNo;
            if (payMethod == PAYMENT_WEPAY_NATIVE) {
                return wechatPayController.nativePay(payParam).toCompletableFuture().get(10, TimeUnit.SECONDS);
            } else {
                return wechatPayController.wechatPay(payParam).toCompletableFuture().get(10, TimeUnit.SECONDS);
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
        Order order = Order.find.query().where().eq("orderNo", payParam.tradeNo)
                .orderBy().desc("id").setMaxRows(1).findOne();
        if (null == order) return okCustomJson(CODE40002, "订单不存在");
        boolean enough = false;
        MemberBalance balance = MemberBalance.find.query().where()
                .eq("uid", payParam.uid)
                .eq("itemId", BusinessItem.AWARD)
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
            enough = true;
            BalanceParam param = builder.itemId(BusinessItem.AWARD).build();
            balanceUtils.saveChangeBalance(param, true);
            businessUtils.handleOrderPaid(order.orderNo, payParam.tradeNo, BusinessConstant.PAYMENT_BALANCE_PAY, "");
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            return ok(result);
        }
        if (!enough) {
            balance = MemberBalance.find.query().where()
                    .eq("uid", payParam.uid)
                    .eq("itemId", BusinessItem.CASH)
                    .setMaxRows(1).findOne();
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
        }
        return okCustomJson(CODE500, "您的余额不足，请换个支付方式");
    }

    /**
     * @api {POST} /v1/o/product_orders/:orderId/ 04确认收货
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
            Order order = Order.find.byId(orderId);
            if (null == order) return okCustomJson(CODE40003, "该订单不存在");
            if (order.uid != member.id && order.shopId != member.shopId) return okCustomJson(CODE40003, "无权操作该订单");
            if (order.status >= ORDER_STATUS_TAKEN) return okCustomJson(CODE40001, "该订单已签收/核销");
            OrderLog orderLog = new OrderLog();
            orderLog.setOrderId(order.id);
            orderLog.setOldStatus(order.status);
            orderLog.setNewStatus(Order.ORDER_STATUS_TAKEN);
            orderLog.setOperatorName(businessUtils.getUserName(member));
            orderLog.setCreateTime(dateUtils.getCurrentTimeBySecond());
            String note = "";
            if (order.uid == member.id) note = "用户确认收货";
            else if (order.shopId == member.shopId) note = "营业员确认核销";
            orderLog.setNote(note);
            orderLog.save();
            order.setStatus(Order.ORDER_STATUS_TAKEN);
            long currentTime = dateUtils.getCurrentTimeBySecond();
            order.setUpdateTime(currentTime);
            order.setLocation(location);
            order.save();
            if (order.orderType != ORDER_TYPE_MEMBERSHIP && order.realPay > 500) {
                boolean isVipMember = false;
                if (isVipMember) {
                    saveFreeMembershipCardLog(order);
                    BalanceParam param = new BalanceParam.Builder()
                            .changeAmount(1)
                            .itemId(BusinessItem.MEMBER_CARD_FOR_GIVE)
                            .leftBalance(1)
                            .totalBalance(1)
                            .memberId(order.uid)
                            .desc("赠送七天免费会员分享卡")
                            .bizType(TRANSACTION_TYPE_GIVE_FREE_MEMBER_SHIP).build();
                    balanceUtils.saveChangeBalance(param, true);
                } else {
                    MembershipLog membershipLog = MembershipLog.find.query().where()
                            .eq("orderId", order.id)
                            .eq("status", MembershipLog.STATUS_ORDER)
                            .orderBy().asc("id")
                            .setMaxRows(1)
                            .findOne();
                    if (null != membershipLog) {
                        membershipLog.setStatus(MembershipLog.STATUS_ORDER_GIVEN);
                        membershipLog.save();
                    }
                }
            }
            businessUtils.updateOrderDetailStatus(order);
            businessUtils.updateSupplierOrderStat(order);
            return okJSON200();
        });
    }


    private void saveFreeMembershipCardLog(Order order) {
        if (order.orderType != Order.ORDER_TYPE_MEMBERSHIP) {
            MembershipLog log = new MembershipLog();
            log.setOrderId(order.id);
            log.setCreateTime(dateUtils.getCurrentTimeBySecond());
            log.setUid(0);
            log.setUserName("");
            log.setMembershipTitle("七天免费会员卡");
            log.setLevel(Member.LEVEL_1);
            log.setMoney(0);
            log.setSenderUid(order.uid);
            log.setUuid("");
            log.setStatus(MembershipLog.STATUS_NOT_TAKE);
            log.setFirstDealerId(0);
            log.setFirstDealerName("");
            log.setFirstDealerAward(0);
            log.setSecondDealerId(0);
            log.setSecondDealerName("");
            log.setSecondDealerAward(0);
            log.save();
        }
    }

    /**
     * @api {GET} /v1/o/orders/:orderId/ 05订单详情
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
            Order order = Order.find.byId(orderId);
            if (null == order) return okCustomJson(CODE40002, "找不到该订单");

            if (member.id != order.uid) {
                if (order.shopId != member.shopId) return okCustomJson(CODE40003, "无权查看该订单详情");
            }
            List<OrderDetail> list = OrderDetail.find.query().where()
                    .eq("orderId", order.id)
                    .orderBy().asc("id")
                    .findList();
            order.detailList.addAll(list);
            setOrderShop(order);
            setOrderServiceCount(order);
            ObjectNode result = (ObjectNode) Json.toJson(order);
            result.put(CODE, CODE200);

            List<OrderReturns> orderReturnList = OrderReturns.find.query().where()
                    .eq("orderId", orderId)
                    .ge("status", OrderReturns.STATUS_RETURN_TO_AUDIT)
                    .orderBy().asc("id")
                    .findList();
            result.set("orderReturnList", Json.toJson(orderReturnList));

            List<OrderDelivery> deliveryList = OrderDelivery.find.query().where()
                    .eq("orderId", orderId)
                    .orderBy().asc("id")
                    .findList();
            deliveryList.parallelStream().forEach((orderDelivery) -> {
                List<OrderDeliveryDetail> orderDeliveryDetailList = OrderDeliveryDetail.find.query().where()
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
     * @api {GET} /v1/o/order_pay_result/:orderId/ 06查询订单状态
     * @apiName getPayResult
     * @apiGroup Order
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Success 200) {int} status 支付状态
     * @apiSuccess (Error 40001){int} code 40001 参数错误
     * @apiSuccess (Error 40002){int} code 40002 找不到该订单
     */
    public CompletionStage<Result> getPayResult(Http.Request request, long orderId) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) unauth403();
            if (orderId < 1) return okCustomJson(CODE40001, "参数错误");
            Order order = Order.find.query().where().eq("id", orderId)
                    .eq("uid", uid)
                    .orderBy().desc("id").setMaxRows(1).findOne();
            if (null == order) return okCustomJson(CODE40002, "找不到该订单");
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            result.put("status", order.status);
            return ok(result);
        });
    }

    /**
     * @api {POST} /v1/o/cancel_order/ 07取消订单
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
            Order order = Order.find.query().where().eq("id", orderId)
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
                        updateMemberSalesOverview(order);
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

    private void returnCoupon(Order order) {
        if (order.couponId > 0) {
            MemberCoupon coupon = MemberCoupon.find.byId(order.couponId);
            if (coupon.status == MemberCoupon.STATUS_USED) {
                coupon.setStatus(STATUS_NOT_USE);
                coupon.setUseTime(0);
                coupon.save();
            }
        }
    }

    private void setOrderCancelled(Order order, long currentTime, String operatorName) {
        OrderLog orderLog = new OrderLog();
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

        businessUtils.updateOrderDetailStatus(order);
        if (order.activityType != ORDER_ACTIVITY_TYPE_FLASH_SALE)
            businessUtils.updateMerchantStock(order, BusinessConstant.TYPE_CANCEL_ORDER);
        //change freeze balance
//        changeFreezeBalance(order);
    }

    private void updateMemberSalesOverview(Order order) {
        //用户总消费额
        StatMemberSalesOverview memberSalesOverview = StatMemberSalesOverview.find.query().where()
                .eq("uid", order.uid).setMaxRows(1).findOne();
        if (null != memberSalesOverview) {
            memberSalesOverview.setOrderAmount(memberSalesOverview.orderAmount - order.realPay);
            memberSalesOverview.setOrders(memberSalesOverview.orders - 1);
            memberSalesOverview.save();
        }
    }

    private void changeFreezeBalance(Order order) {
        BalanceLog balanceLog = BalanceLog.find.query().where()
                .eq("orderNo", order.orderNo)
                .orderBy().asc("id")
                .setMaxRows(1)
                .findOne();
        if (null != balanceLog && balanceLog.freezeStatus == BalanceLog.FREEZE_STATUS_TO_HANDLE) {
            balanceLog.setFreezeStatus(BalanceLog.FREEZE_STATUS_CANCELED);
            balanceLog.save();
            long changeAmount = Math.abs(balanceLog.changeAmount);
            String sql = "UPDATE " +
                    "    `v1_member_balance` AS `dest`, " +
                    "    ( SELECT * FROM `v1_member_balance` " +
                    "        WHERE id = :id  limit 1" +
                    "    ) AS `src`" +
                    "   SET" +
                    " `dest`.`freeze_balance` =  `src`.freeze_balance - :changeAmount ," +
                    " `dest`.`total_balance` =  `src`.total_balance - :changeAmount" +
                    "   WHERE" +
                    "   dest.id = :id " +
                    ";";
            SqlUpdate sqlUpdate = Ebean.createSqlUpdate(sql);
            sqlUpdate.setParameter("id", balanceLog.uid);
            sqlUpdate.setParameter("changeAmount", changeAmount);
            sqlUpdate.executeNow();
        }
    }

    private void returnScoreForCancelOrder(Order order) {
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

    public void subtractScoreGave(Order order) {
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
    public boolean refundMoney(Order order, String subject, String productionCode) {
        if (order.realPay <= 0) return true;
        long returnMoney = order.realPay - order.membershipFee;
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

    private boolean refundMoneyWithBalancePayment(Order order, String subject) {
        BalanceLog balanceLog = BalanceLog.find.query().where().eq("orderNo", order.orderNo)
                .orderBy().asc("id")
                .setMaxRows(1)
                .findOne();
        if (null != balanceLog) {
            long totalAmount = order.realPay - order.membershipFee;
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


    private boolean refundMoneyWithAlipay(PayParam payParam, Order order, String subject) {
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

    private boolean refundMoneyWithWechatPay(PayParam payParam, Order order) {
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

    private void setOrderRefunded(Order order) {
        if (order.totalReturnNumber >= order.productCount) {
            order.setStatus(Order.ORDER_STATUS_SYSTEM_REFUNDED);
            order.setUpdateTime(System.currentTimeMillis() / 1000);
            order.save();
        }
        //return award
        List<DealerAward> dealerAwardList = DealerAward.find.query().where()
                .eq("orderId", order.id)
                .findList();
        dealerAwardList.parallelStream().forEach((each) -> {
            each.setStatus(DealerAward.STATUS_CANCELED);
            each.save();
        });

//        retreatOrderCount(order);

        //retreat card coupon
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
     * @api {GET} /v1/o/order_stat/ 09订单数统计
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
            List<Object> unpaylist = Order.find.query().where().eq("uid", uid)
                    .eq("status", ORDER_STATUS_UNPAY).findIds();
            List<Object> toMailList = Order.find.query().where().eq("uid", uid)
                    .eq("status", ORDER_STATUS_TO_DELIEVERY).findIds();
            List<Object> toConfirmList = Order.find.query().where().eq("uid", uid)
                    .eq("status", ORDER_STATUS_TO_TAKE).findIds();
            List<Object> toCommentList = Order.find.query().where().eq("uid", uid)
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
     * @api {POST} /v1/o/order_return/:id/  11申请售后修改
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

            OrderReturns returnsApply = OrderReturns.find.byId(id);
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
                    List<OrderReturnsImage> updateList = new ArrayList<>();
                    if (imgNodes.isArray() && imgNodes.size() > 0) {
                        int currentCount = imgNodes.size();
                        if (currentCount > 9) return okCustomJson(CODE40001, "图片最多允许9张");
                        imgNodes.forEach((each) -> {
                            if (null != each) {
                                OrderReturnsImage image = new OrderReturnsImage();
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
                    List<OrderReturnsImage> imageList = OrderReturnsImage.find.query().where()
                            .eq("returnApplyId", returnsApply.id)
                            .orderBy().asc("id")
                            .findList();
                    if (imageList.size() > 0) Ebean.deleteAll(imageList);
                    if (updateList.size() > 0) Ebean.saveAll(updateList);
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
     * @api {POST} /v1/o/calc_order/ 17计算下单金额与优惠
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

            final long addressId = requestNode.findPath("addressId").asLong();
            if (null == paramList || !paramList.isArray()) return okCustomJson(CODE40001, "请选择商品");
            List<OrderDetail> detailList = new ArrayList<>();
            long totalMoney = 0;
            long realPay = 0;
            int productAmount = 0;
            double totalWeight = 0;
            long requireScore = 0;
            Map<Long, Integer> productAmountMap = new HashMap<>();
            Map<Long, Integer> shopProductAmountMap = new HashMap<>();
            Map<Long, Double> shopProductWeightMap = new HashMap<>();
            Map<Long, List<ProductLevelPrice>> productLevelPriceMap = new HashMap<>();
            for (int i = 0; i < paramList.size(); i++) {
                JsonNode node = paramList.get(i);
                OrderDetail orderDetail = new OrderDetail();
                final int amount = node.findPath("amount").asInt();
                final long productId = node.findPath("productId").asLong();
                final long skuId = node.findPath("skuId").asLong();
                final int deliveryMethod = node.findPath("deliveryMethod").asInt();
                final String remark = node.findPath("remark").asText();
                boolean joinFlashSale = node.findPath("joinFlashSale").asBoolean();
                orderDetail.joinFlashSale = joinFlashSale;
                orderDetail.deliveryMethod = deliveryMethod;
                Product product = Product.find.byId(productId);
                if (null == product) return okCustomJson(CODE40001, "该商品不存在");
                if (amount < 1) return okCustomJson(CODE40001, "请选择商品数量");
                if (product.status < STATUS_ON_SHELVE) return okCustomJson(CODE40001, "该商品不存在或已下架");
                if (skuId < 1) return okCustomJson(CODE40001, "sku参数有误");
                ProductSku productSku = ProductSku.find.byId(skuId);
                if (null == productSku) return okCustomJson(CODE40001, "该SKU不存在");
                if (productSku.productId != productId) return okCustomJson(CODE40001, "商品ID有误");

                if (joinFlashSale) {
                    if (!businessUtils.isFlashSale(productSku))
                        return okCustomJson(CODE40001, "'" + product.name + "'" + "秒杀已结束");
                    if (amount > productSku.flashLeftAmount)
                        return okCustomJson(CODE40001, "下单数量超出" + product.name + "的库存数量");
                } else {
                    if (amount > productSku.stock) {
                        return okCustomJson(CODE40001, "下单数量超出" + product.name + "的库存数量");
                    }
                }
                if (productSku.limitAmount > 0) {
                    if (amount > productSku.limitAmount)
                        return okCustomJson(CODE40001, productSku.name + "限购" + productSku.limitAmount + product.unit);
                    boolean uptoMaxAmount = checkUptoMaxAmount(productSku, uid, amount);
                    if (uptoMaxAmount)
                        return okCustomJson(CODE40001, productSku.name + "限购" + productSku.limitAmount + product.unit);
                }
                double eachWeight = productSku.weight * amount;
                totalWeight = totalWeight + eachWeight;
                setOrderDetailByShop(orderDetail, amount, remark, product, productSku);
                setProductAmountMap(productAmountMap, amount, productId);
                setShopProductAmountMap(shopProductAmountMap, amount, product.shopId);
                setShopProductWeightMap(shopProductWeightMap, eachWeight, product.shopId);
                productAmount = productAmount + amount;
                requireScore = requireScore + amount * productSku.requireScore;
                detailList.add(orderDetail);
                setProductLevelPriceMap(productLevelPriceMap, productId, product);
            }
            Map<Long, Long> shopRealPayMap = new HashMap<>();
            for (OrderDetail orderDetail : detailList) {
                List<ProductLevelPrice> levelPriceList = productLevelPriceMap.get(orderDetail.productId);
                Integer togetherAmount = productAmountMap.get(orderDetail.productId);
                if (null != levelPriceList && null != togetherAmount) {
                    long price = getPrice(levelPriceList, togetherAmount);
                    if (price > 0) orderDetail.setProductPrice(price);
                }
                long thisItemTotalMoney = orderDetail.productPrice * orderDetail.number;
                totalMoney = totalMoney + thisItemTotalMoney;
                orderDetail.setSubTotal(thisItemTotalMoney);
                realPay = realPay + thisItemTotalMoney;
                Long shopRealPay = shopRealPayMap.get(orderDetail.shopId);
                if (null != shopRealPay) shopRealPay = shopRealPay + thisItemTotalMoney;
                else shopRealPay = thisItemTotalMoney;
                shopRealPayMap.put(orderDetail.shopId, shopRealPay);
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

            long realPayForCoupon = realPay;
            ArrayNode nodes = Json.newArray();
            shopRealPayMap.put(0L, realPayForCoupon);
            shopRealPayMap.forEach((shopId, shopRealpay) -> {
                List<MemberCoupon> memberCouponAvailableList = new ArrayList<>();
                List<MemberCoupon> memberCouponNotAvailableList = new ArrayList<>();
                List<MemberCoupon> memberCouponList = MemberCoupon.find.query().where()
                        .eq("uid", uid)
                        .eq("shopId", shopId)
                        .eq("status", STATUS_NOT_USE)
                        .findList();
                memberCouponList.forEach((each) -> {
                    long realpay = shopRealpay;
                    long couponFree = businessUtils.calcCouponFree(each.id, realpay, detailList, shopId);
                    CouponConfig config = businessUtils.getCouponConfig(each.couponId);
                    each.coupon = config;
                    if (couponFree > 0) {
                        each.couponFree = couponFree;
                        memberCouponAvailableList.add(each);
                    } else memberCouponNotAvailableList.add(each);
                });
                ObjectNode node = Json.newObject();
                node.put("shopId", shopId);
                long couponFree = memberCouponAvailableList.parallelStream().mapToLong((memberCoupon) -> memberCoupon.couponFree).sum();
                node.put("couponFree", couponFree);
                node.set("memberCouponAvailableList", Json.toJson(memberCouponAvailableList));
                node.set("memberCouponNotAvailableList", Json.toJson(memberCouponNotAvailableList));
                nodes.add(node);
            });
            shopRealPayMap.remove(0L);
            resultNode.set("memberCouponAvailableList", nodes);
            long mailFee = 0;
            if (addressId > 0) {
                ContactDetail contactDetail = ContactDetail.find.byId(addressId);
                if (null == contactDetail) return okCustomJson(CODE40005, "收货地址有误，请重新选择");
                Map<Long, Long> shopMailFeeMap = new HashMap<>();
                ArrayNode mailFeeMap = Json.newArray();
                shopRealPayMap.forEach((shopId, shopRealPay) -> {
                    double eachTotalWeight = shopProductWeightMap.get(shopId);
                    int eachMailAmount = shopProductAmountMap.get(shopId);
                    long eachMailFee = businessUtils.calcMailFee(shopId, shopRealPay, contactDetail.provinceCode, eachTotalWeight, eachMailAmount);
                    ObjectNode node = Json.newObject();
                    node.put("shopId", shopId);
                    node.put("eachMailFee", eachMailFee);
                    shopMailFeeMap.put(shopId, eachMailFee);
                    mailFeeMap.add(node);
                });
                mailFee = shopMailFeeMap.values().stream().mapToLong(l -> l).sum();
                resultNode.set("mailFeeMap", mailFeeMap);
                realPay = realPay + mailFee;
            }
            resultNode.put("mailFee", mailFee);
            resultNode.put("totalMoney", totalMoney);
            resultNode.put("requireScore", requireScore);
            resultNode.put("scoreToMoney", scoreToMoney);

            if (realPay <= 0) realPay = 1;
            resultNode.put("realPayMoney", realPay);
            return ok(resultNode);
        });
    }

    private long getPrice(List<ProductLevelPrice> levelPriceList, Integer togetherAmount) {
        for (ProductLevelPrice levelPrice : levelPriceList) {
            if (togetherAmount >= levelPrice.upto) {
                return levelPrice.price;
            }
        }
        return 0;
    }

    private void setProductAmountMap(Map<Long, Integer> productAmountMap, int amount, long productId) {
        Integer togetherAmount = productAmountMap.get(productId);
        if (null != togetherAmount) togetherAmount = togetherAmount + amount;
        else togetherAmount = amount;
        productAmountMap.put(productId, togetherAmount);
    }

    private void setShopProductAmountMap(Map<Long, Integer> shopProductAmountMap, int amount, long shopId) {
        Integer togetherAmount = shopProductAmountMap.get(shopId);
        if (null != togetherAmount) togetherAmount = togetherAmount + amount;
        else togetherAmount = amount;
        shopProductAmountMap.put(shopId, togetherAmount);
    }

    private void setShopProductWeightMap(Map<Long, Double> shopProductWeightMap, double weight, long shopId) {
        Double togetherWeight = shopProductWeightMap.get(shopId);
        if (null != togetherWeight) togetherWeight = togetherWeight + weight;
        else togetherWeight = weight;
        shopProductWeightMap.put(shopId, togetherWeight);
    }

    private void setOrderDetailByShop(OrderDetail orderDetail, int amount, String remark, Product product, ProductSku productSku) {
        long currentTime = dateUtils.getCurrentTimeBySecond();
        orderDetail.setShopId(product.shopId);
        orderDetail.setProductPrice(productSku.price);
        orderDetail.setSkuName(productSku.name);
        orderDetail.setProductImgUrl(productSku.imgUrl);
        orderDetail.setBrandId(product.brandId);
        orderDetail.setNumber(amount);
        orderDetail.setProductId(product.id);
        orderDetail.setProductName(product.name);
        orderDetail.setProductSkuId(productSku.id);
        orderDetail.setNumber(amount);
        orderDetail.setRemark(remark);
        orderDetail.setUnit(product.unit);
        orderDetail.setBrandId(product.brandId);
        orderDetail.setUpdateTime(currentTime);
        orderDetail.setCreateTime(currentTime);
    }

    private void setProductLevelPriceMap(Map<Long, List<ProductLevelPrice>> productLevelPriceMap, long productId, Product product) {
        List<ProductLevelPrice> productLevelPrices = productLevelPriceMap.get(productId);
        if (null == productLevelPrices) {
            productLevelPrices = ProductLevelPrice.find.query().where()
                    .eq("productId", product.id)
                    .order().desc("upto")
                    .findList();
            productLevelPriceMap.put(productId, productLevelPrices);
        }
    }

    private long calcFavorMoney(List<OrderDetail> orderDetailList) {
        List<ProductFavor> favorList = ProductFavor.find.query().where()
                .eq("status", ProductFavor.STATUS_ENABLE)
                .orderBy().desc("id").findList();
        long totalFavor = 0;
        for (ProductFavor favor : favorList) {
            List<ProductFavorProducts> productFavorProducts = ProductFavorProducts.find.query().where()
                    .eq("favorId", favor.id)
                    .eq("status", ProductFavor.STATUS_ENABLE)
                    .findList();
            Set<Long> set = new HashSet<>();
            productFavorProducts.parallelStream().forEach((each) -> set.add(each.productId));
            if (set.size() > 0) {
                long amountToSub = 0;
                long amountToDiscount = 0;
                long moneyToSub = 0;
                long moneyToDiscount = 0;
                long seqDiscount = 0;
                long uptoAmountDiscountMoney = 0;
                for (OrderDetail orderDetail : orderDetailList) {
                    if (!orderDetail.joinFlashSale && set.contains(orderDetail.productId)) {
                        if (null != favor) {
                            switch (favor.requireType) {
                                case ProductFavor.REQUIRE_TYPE_UPTO_AMOUNT_TO_SUB: {
                                    amountToSub = amountToSub + orderDetail.number;
                                    break;
                                }
                                case ProductFavor.REQUIRE_TYPE_UPTO_AMOUNT_TO_DISCOUNT: {
                                    uptoAmountDiscountMoney = uptoAmountDiscountMoney + orderDetail.productPrice * orderDetail.number;
                                    amountToDiscount = amountToDiscount + orderDetail.number;
                                    break;
                                }
                                case ProductFavor.REQUIRE_TYPE_UPTO_MONEY_TO_SUB: {
                                    moneyToSub = moneyToSub + orderDetail.productPrice * orderDetail.number;
                                    break;
                                }
                                case ProductFavor.REQUIRE_TYPE_UPTO_MONEY_TO_DISCOUNT: {
                                    moneyToDiscount = moneyToDiscount + orderDetail.productPrice * orderDetail.number;
                                    break;
                                }

                                case ProductFavor.REQUIRE_TYPE_SEQ_DISCOUNT: {
                                    seqDiscount = seqDiscount + orderDetail.number;
                                    break;
                                }
                            }
                        }
                    }
                }
                long favorMoney = calcFavorMoneyByType(favor, amountToSub, amountToDiscount, uptoAmountDiscountMoney, moneyToSub, moneyToDiscount, seqDiscount);
                totalFavor = totalFavor + favorMoney;
            }

        }
        return totalFavor;
    }


    private long calcFavorMoneyByType(ProductFavor favor, long amountToSub, long amountToDiscount, long uptoAmountDiscountMoney,
                                      long moneyToSub, long moneyToDiscount, long seqDiscount) {
        List<ProductFavorDetail> details = ProductFavorDetail.find.query()
                .where().eq("favorId", favor.id)
                .orderBy()
                .desc("uptoAmount")
                .findList();
        if (details.size() > 0) {
            switch (favor.requireType) {
                case ProductFavor.REQUIRE_TYPE_UPTO_AMOUNT_TO_SUB: {
                    for (ProductFavorDetail detail : details) {
                        if (detail.uptoAmount >= 0 && amountToSub >= detail.uptoAmount) {
                            return detail.subAmount;
                        }
                    }
                    break;
                }
                case ProductFavor.REQUIRE_TYPE_UPTO_AMOUNT_TO_DISCOUNT: {
                    for (ProductFavorDetail detail : details) {
                        if (detail.uptoAmount >= 0 && amountToDiscount >= detail.uptoAmount) {
                            long favorMoney = (long) (uptoAmountDiscountMoney * (1 - detail.discount));
                            if (favorMoney < 0) favorMoney = 0;
                            return favorMoney;
                        }
                    }
                    break;
                }
                case ProductFavor.REQUIRE_TYPE_UPTO_MONEY_TO_SUB: {
                    for (ProductFavorDetail detail : details) {
                        if (detail.uptoMoney >= 0 && moneyToSub >= detail.uptoMoney) {
                            long favorMoney = detail.subAmount;
                            return favorMoney;
                        }
                    }
                    break;
                }
                case ProductFavor.REQUIRE_TYPE_UPTO_MONEY_TO_DISCOUNT: {
                    for (ProductFavorDetail detail : details) {
                        if (detail.uptoMoney >= 0 && moneyToDiscount >= detail.uptoMoney) {
                            long favorMoney = (long) (moneyToDiscount * (1 - detail.discount));
                            if (favorMoney < 0) favorMoney = 0;
                            return favorMoney;
                        }
                    }
                    break;
                }
                case ProductFavor.REQUIRE_TYPE_SEQ_DISCOUNT: {
//                    for (ProductFavorDetail detail : details) {
//                        if (amount >= detail.seq) {
//                            if (detail.discount > 0) {
//                                resultMoney = (long) (thisItemTotalMoney - price * (1 - detail.discount));
//                                break;
//                            } else if (detail.subAmount > 0) {
//                                resultMoney = thisItemTotalMoney - detail.subAmount;
//                                break;
//                            }
//                        }
//                    }
                    break;
                }
            }
        }
        return 0;
    }


    private long calcDefaultDiscountRule(long thisItemTotalMoney, int amount, long resultMoney, long favorId) {
        List<ProductFavorDetail> details = ProductFavorDetail.find.query()
                .where().eq("favorId", favorId)
                .orderBy().desc("uptoMoney")
                .orderBy().desc("uptoAmount")
                .findList();
        for (ProductFavorDetail detail : details) {
            if (detail.uptoAmount >= 0) {
                if (amount >= detail.uptoAmount) {
                    resultMoney = calcDiscount(thisItemTotalMoney, resultMoney, detail);
                    break;
                }
            } else if (detail.uptoMoney >= 0) {
                if (thisItemTotalMoney >= detail.uptoMoney) {
                    resultMoney = calcDiscount(thisItemTotalMoney, resultMoney, detail);
                    break;
                }
            }
        }
        return resultMoney;
    }

    private long calcDiscount(long thisItemTotalMoney, long resultMoney, ProductFavorDetail detail) {
        if (detail.discount > 0) {
            resultMoney = (long) (thisItemTotalMoney * detail.discount);
        } else if (detail.subAmount > 0) {
            resultMoney = thisItemTotalMoney - detail.subAmount;
        }
        return resultMoney;
    }


    /**
     * @api {GET} /v1/o/order_count/ 18分类订单数量
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
            List<Object> unPayList = Order.find.query().where().eq("uid", uid)
                    .eq("status", ORDER_STATUS_UNPAY).findIds();
            unpayOrders = unPayList.size();

            List<Object> paidList = Order.find.query().where().eq("uid", uid)
                    .eq("status", ORDER_STATUS_TO_DELIEVERY).findIds();
            paidOrders = paidList.size();

            List<Object> delieveredList = Order.find.query().where().eq("uid", uid)
                    .eq("status", ORDER_STATUS_TO_TAKE).findIds();
            delieveredOrders = delieveredList.size();

            List<Object> toCommentList = Order.find.query().where().eq("uid", uid)
                    .eq("status", ORDER_STATUS_TAKEN)
                    .findIds();
            toCommentOrders = toCommentList.size();

            List<Object> postServiceList = OrderReturns.find.query().where()
                    .eq("uid", uid)
                    .ge("status", OrderReturns.STATUS_RETURN_TO_AUDIT)
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

    private Order saveGrouponOrder(Member member, long totalMoney, int productAmount, long staffId, int status) {
        long currentTime = dateUtils.getCurrentTimeBySecond();
        Order order = new Order();
        order.setUid(member.id);
        String name = member.realName;
        if (ValidationUtil.isEmpty(name)) name = member.nickName;
        if (ValidationUtil.isEmpty(name)) name = member.phoneNumber;
        order.setUserName(name);
        order.setOrderShops("");
        order.setDeliveryType(0);
        order.setAddress("");
        order.setRegionPath("");
        order.setOrderNo(dateUtils.getToday() + IdGenerator.getId());
        order.setOutTradeNo("");
        order.setRefundTxId("");
        order.setPayTxNo("");
        order.setStatus(status);
        order.setTotalMoney(totalMoney);
        order.setRealPay(totalMoney);
        order.setScoreUse(0);
        order.setScoreToMoney(0);
        order.setPostServiceStatus(POST_SERVICE_STATUS_NO);
        order.setProductCount(productAmount);
        order.setLogisticsFee(0);
        order.setCommissionHandled(false);
        order.setUpdateTime(currentTime);
        order.setCreateTime(currentTime);
        order.setMix(false);
        order.setCouponFree(0);
        order.setCouponId(0);
        order.save();
        return order;
    }

    /**
     * @api {GET} /v1/o/shop_orders/?page=&status= 28店铺订单列表
     * @apiName listShopOrders
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
    public CompletionStage<Result> listShopOrders(Http.Request request, int page, int status) {
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
            ExpressionList<Order> expressionList = Order.find.query().where()
                    .eq("shopId", member.shopId);
            if (status > 0) {
                if (status == Order.ORDER_STATUS_TO_DELIEVERY) {
                    expressionList.ge("status", Order.ORDER_STATUS_TO_DELIEVERY)
                            .le("status", Order.ORDER_STATUS_DELIEVERED);
                } else if (status == Order.ORDER_STATUS_ARRIVE_SELF_TAKEN_PLACE) {
                    expressionList.ge("status", Order.ORDER_STATUS_TO_TAKE)
                            .le("status", Order.ORDER_STATUS_ARRIVE_SELF_TAKEN_PLACE);
                } else expressionList.eq("status", status);
            }
            PagedList<Order> pagedList = expressionList.orderBy().desc("id")
                    .setFirstRow((page - 1) * PAGE_SIZE_20)
                    .setMaxRows(PAGE_SIZE_20)
                    .findPagedList();
            List<Order> list = pagedList.getList();
            //读取订单详情
            list.parallelStream().forEach((each) -> {
                List<OrderDetail> details = OrderDetail.find.query().where()
                        .eq("orderId", each.id)
                        .orderBy().asc("id").findList();
                each.detailList.addAll(details);
                OrderReturns orderReturn = OrderReturns.find.query().where()
                        .eq("orderId", each.id)
                        .orderBy().desc("id")
                        .setMaxRows(1).findOne();
                each.orderReturns = orderReturn;
                if (each.status == ORDER_STATUS_TO_TAKE || each.status == ORDER_STATUS_ARRIVE_SELF_TAKEN_PLACE) {
                    setOrderServiceCount(each);
                }
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
     * @api {POST} /v1/o/subscribe_miniapp_msg/ 29订阅小程序通知
     * @apiName subscribeWeappMsg
     * @apiGroup Order
     * @apiSuccess (Success 200){int} code 200
     */
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> subscribeMiniappMsg(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            long currentTime = dateUtils.getCurrentTimeBySecond();
            Order order = Order.find.query().where().eq("uid", uid)
                    .eq("status", ORDER_STATUS_TO_DELIEVERY)
                    .ge("payTime", currentTime - 2 * 60)
                    .setMaxRows(1)
                    .findOne();
            if (null != order) {
                businessUtils.sendOrderPaidMessage(order);
            }
            return okJSON200();
        });

    }

    /**
     * @api {POST} /v1/o/change_order_address/ 39修改订单地址
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

            Order order = Order.find.byId(orderId);
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
     * @api {GET} /v1/o/shop_stat/ 42店铺统计
     * @apiName shopStat
     * @apiGroup Order
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Success 200) {long} regCount 今日注册人数
     * @apiSuccess (Success 200) {long} orders 今日订单数
     * @apiSuccess (Success 200) {long} totalMoney 今日订单总额
     * @apiSuccess (Success 200) {long} products 今日售出商品数
     * @apiSuccess (Success 200) {JsonArray} list 最新30条统计数据
     */
    public CompletionStage<Result> shopStat(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            ExpressionList<StatPlatformDaySalesOverview> expressionList = StatPlatformDaySalesOverview.find.query()
                    .where().eq("shopId", member.shopId);
            List<StatPlatformDaySalesOverview> dayStatList = expressionList
                    .orderBy().desc("id")
                    .setMaxRows(30)
                    .findList();
            long todayOrders = 0;
            double todayOrderMoney = 0;
            long todaySoldProducts = 0;
            long todayRegCount = 0;
            if (dayStatList.size() > 0) {
                StatPlatformDaySalesOverview todayOverview = dayStatList.get(0);
                String today = dateUtils.getTodayDash();
                if (todayOverview.day.equalsIgnoreCase(today)) {
                    todayOrders = todayOverview.orders;
                    todayOrderMoney = todayOverview.totalMoney;
                    todaySoldProducts = todayOverview.products;
                    todayRegCount = todayOverview.regCount;
                }
            }

            String currentMonth = dateUtils.getCurrentMonth();
            ExpressionList<StatPlatformMonthSalesOverview> monthExpressionList = StatPlatformMonthSalesOverview.find.query().where()
                    .eq("month", currentMonth)
                    .eq("shopId", member.shopId);
            StatPlatformMonthSalesOverview monthSalesOverview = monthExpressionList.orderBy().asc("id")
                    .setMaxRows(1)
                    .findOne();
            long monthOrders = 0;
            double monthOrderMoney = 0;
            long monthSoldProducts = 0;
            long monthRegCount = 0;
            if (null != monthSalesOverview) {
                monthOrders = monthSalesOverview.orders;
                monthOrderMoney = monthSalesOverview.totalMoney;
                monthRegCount = monthSalesOverview.regCount;
                monthSoldProducts = monthSalesOverview.products;
            }
            dayStatList.parallelStream().forEach((each) -> {
                if (!ValidationUtil.isEmpty(each.getDay()) && each.getDay().length() >= 8)
                    each.setDay(each.getDay().substring(5));
            });
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            result.put("regCount", todayRegCount);
            result.put("orders", todayOrders);
            result.put("totalMoney", todayOrderMoney);
            result.put("products", todaySoldProducts);

            result.put("monthOrders", monthOrders);
            result.put("monthOrderMoney", monthOrderMoney);
            result.put("monthSoldProducts", monthSoldProducts);
            result.put("monthRegCount", monthRegCount);
            result.set("dayStatList", Json.toJson(dayStatList));
            logger.info("result:" + result.toString());
            return ok(result);
        });
    }


    /**
     * @api {POST} /v1/o/exchange/ 43积分兑换
     * @apiName exchange
     * @apiGroup Order
     * @apiParam {string} [description] 备注
     * @apiParam {long} skuId 商品skuID
     * @apiParam {long} addressId 地址ID
     * @apiParam {long} amount 购买数量
     * @apiParam {String} [remark] 备注，每个单品的备注，预留
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
    public CompletionStage<Result> exchange(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            try {
                if (uid < 1) return unauth403();
                Member member = Member.find.byId(uid);
                if (null == member) return unauth403();
                JsonNode requestNode = request.body().asJson();
                if (!businessUtils.setLock(String.valueOf(uid), OPERATION_PLACE_ORDER))
                    return okCustomJson(CODE40004, "正在兑换中,请稍等");
                long currentTime = dateUtils.getCurrentTimeBySecond();
                if (null == requestNode) return okCustomJson(CODE40001, "参数错误");
                final long addressId = requestNode.findPath("addressId").asLong();
                String description = requestNode.findPath("description").asText();
                final int amount = requestNode.findPath("amount").asInt();
                final long skuId = requestNode.findPath("skuId").asLong();

                long totalMoney = 0;
                long requireScore = 0;
                int productAmount = 0;
                ProductSku sku = ProductSku.find.byId(skuId);
                if (null == sku) return okCustomJson(CODE40001, "该商品sku不存在");
                fixSkuPrice(sku);
                Product product = Product.find.byId(sku.productId);
                if (null == product) return okCustomJson(CODE40001, "该商品不存在");
                if (amount < 1) return okCustomJson(CODE40001, "请选择商品数量");
                if (product.status < STATUS_ON_SHELVE) return okCustomJson(CODE40001, "该商品不存在或已下架");
                ContactDetail contactDetail = ContactDetail.find.byId(addressId);
                if (null == contactDetail) return okCustomJson(CODE40002, "请选择收货地址");

                OrderDetail orderDetail = new OrderDetail();
                if (amount > sku.stock) {
                    return okCustomJson(CODE40001, "下单数量超出" + product.name + "的库存数量");
                }
                long thisItemTotalMoney = sku.price * amount;
                totalMoney = totalMoney + thisItemTotalMoney;
                setOrderDetail(uid, currentTime, orderDetail, amount, description, product, sku);
                orderDetail.setSubTotal(thisItemTotalMoney);
                productAmount = productAmount + amount;
                long realPayMoney = totalMoney;
                if (realPayMoney <= 0) realPayMoney = 0;
                //check score balance
                MemberBalance caseBalance = MemberBalance.find.query().where().eq("uid", uid)
                        .eq("itemId", BusinessItem.SCORE).setMaxRows(1).findOne();
                if (null == caseBalance || caseBalance.leftBalance < realPayMoney) {
                    return okCustomJson(CODE40005, "您的积分不足，无法兑换商品");
                }
                description = businessUtils.escapeHtml(description);
                OrderParam param = new OrderParam.Builder()
                        .uid(uid)
                        .addressId(addressId)
                        .deliveryType(DELIVERY_TYPE_DELIVERY)
                        .description(description)
                        .totalMoney(totalMoney)
                        .realPayMoney(realPayMoney)
                        .productAmount(productAmount)
                        .mailFee(0)
                        .shopId(0)
                        .shopName("平台")
                        .build();
                ActorProtocol.EXCHANGE orderParam = new ActorProtocol.EXCHANGE(param, member, orderDetail);
                return orderParam;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("place order:" + e.getMessage());
            } finally {
                businessUtils.unLock(String.valueOf(uid), OPERATION_PLACE_ORDER);
            }
            return okCustomJson(CODE500, "兑换发生异常，请稍后再试");
        }).thenCompose((result) -> {
            if (result instanceof ActorProtocol.EXCHANGE)
                return executeOrder((ActorProtocol.EXCHANGE) result);
            return CompletableFuture.supplyAsync(() -> (Result) result);
        });
    }

    private void fixSkuPrice(ProductSku sku) {
        sku.price = sku.price / 100;
        sku.oldPrice = sku.oldPrice / 100;
        sku.bidPrice = sku.bidPrice / 100;
        sku.flashPrice = sku.flashPrice / 100;
    }

    private CompletionStage<Result> executeOrder(ActorProtocol.EXCHANGE orderParam) {
        return FutureConverters.toJava(ask(exchangeActorRef, orderParam, 30000))
                .thenApply(response -> ok((ObjectNode) response));
    }


    /**
     * @api {POST} /v1/o/order_delievered/ 44设置订单已发货
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
            Order order = Order.find.byId(orderId);
            if (null == order) return okCustomJson(CODE40002, "找不到该订单");

            if (member.shopId != order.shopId)
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

                if (order.activityType == ORDER_ACTIVITY_TYPE_BARGAIN) {
                    Bargain bargain = Bargain.find.query().where().eq("orderId", order.id)
                            .orderBy().desc("id")
                            .setMaxRows(1)
                            .findOne();
                    if (null != bargain) {
                        bargain.setStatus(Bargain.STATUS_DELIVERED);
                        bargain.save();
                    }
                }
                LocalDateTime orderTime =
                        LocalDateTime.ofInstant(Instant.ofEpochSecond(order.createTime),
                                TimeZone.getTimeZone("GMT+8").toZoneId());
                LocalDateTime thatTimeMax = LocalDateTime.of(orderTime.getYear(),
                        orderTime.getMonthValue(), orderTime.getDayOfMonth(), 23, 59, 59);
                long maxThatTime = Timestamp.valueOf(thatTimeMax).getTime() / 1000;
                long minThatTime = maxThatTime + 1 - 24 * 3600;
                businessUtils.updateOrderDetailStatus(order);
            }
            order.save();
            return okJSON200();
        });
    }


    private boolean needUpdateOrderStatus(JsonNode jsonNode, long orderId) {
        boolean needUpdateOrderStatus = false;
        if (jsonNode.has("detailList")) {
            ArrayNode orderDetailList = (ArrayNode) jsonNode.findPath("detailList");
            int count = OrderDetail.find.query().where().eq("orderId", orderId)
                    .ge("status", Order.ORDER_STATUS_DELIEVERED)
                    .findCount();
            List<OrderDetail> list = new ArrayList<>();
            orderDetailList.forEach((each) -> {
                OrderDetail orderDetail = OrderDetail.find.byId(each.asLong());
                if (null != orderDetail) {
                    orderDetail.setStatus(Order.ORDER_STATUS_DELIEVERED);
                    list.add(orderDetail);
                }
            });
            if (list.size() > 0) Ebean.saveAll(list);
            int deliverCount = OrderDetail.find.query().where().eq("orderId", orderId)
                    .ge("status", Order.ORDER_STATUS_DELIEVERED)
                    .findCount();
            if (count == deliverCount) needUpdateOrderStatus = true;
        } else needUpdateOrderStatus = true;
        return needUpdateOrderStatus;
    }


    /**
     * @api {POST} /v1/o/delete_order/ 45删除订单
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
            Order order = Order.find.query().where().eq("id", orderId)
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
