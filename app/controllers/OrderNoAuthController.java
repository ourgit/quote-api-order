package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import constants.BusinessConstant;
import models.order.Order;
import models.order.OrderDetail;
import models.postservice.OrderReturns;
import play.Logger;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import utils.ValidationUtil;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.*;

import static constants.BusinessConstant.OPERATION_PAY_ORDER;
import static models.order.Order.ORDER_STATUS_UNPAY;

/**
 * 用户控制类
 */
public class OrderNoAuthController extends BaseController {

    Logger.ALogger logger = Logger.of(OrderNoAuthController.class);

    @Inject
    WechatPayV3Controller wechatPayController;

    @Inject
    AliPayController aliPayController;


    /**
     * @api {POST} /v1/o/pay_order_no/  01支付代客订单
     * @apiName payOrder
     * @apiGroup Order_H5
     * @apiParam {long} orderId 订单ID
     * @apiParam {int} payMethod 1银行卡支付2支付宝支付3微信支付 6微信支付H5 7支付宝H5支付8小程序支付
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
        JsonNode requestNode = request.body().asJson();
        return CompletableFuture.supplyAsync(() -> {
            if (null == requestNode) return okCustomJson(CODE40001, "参数错误");
            long orderId = requestNode.findPath("orderId").asLong();
            int payMethod = requestNode.findPath("payMethod").asInt();
            String openId = requestNode.findPath("openId").asText();
            if (orderId < 1 || payMethod < 1) return okCustomJson(CODE40001, "参数错误");
            String key = "ORDER:" + openId;
            if (!businessUtils.setLock(String.valueOf(orderId), OPERATION_PAY_ORDER))
                return okCustomJson(CODE40004, "正在支付中,请稍等");
            try {
                businessUtils.setLock(key, OPERATION_PAY_ORDER);
                Order order = Order.find.byId(orderId);
                if (null == order) return okCustomJson(CODE40002, "该订单不存在");
                if (order.status != ORDER_STATUS_UNPAY) return okCustomJson(CODE40003, "该订单不是待支付状态,不能支付");
                PayParam payParam = new PayParam.Builder()
                        .tradeNo(order.orderNo)
                        .subject("麦芽仓订单支付")
                        .productionCode("oid" + order.id)
                        .realPayMoney(order.realPay)
                        .totalAmount(order.realPay)
                        .uid(order.uid)
                        .openId(openId)
                        .payMethod(payMethod)
                        .useScore(0)
                        .build();
                Result result = null;
                switch (payMethod) {
                    case BusinessConstant.PAYMENT_BANK:
                        result = okCustomJson(CODE40005, "正在申请中...");
                        break;
                    case BusinessConstant.PAYMENT_ALIPAY:
                        result = aliPayController.aliPagePay(payParam);
                        break;
                    case BusinessConstant.PAYMENT_ALIPAY_WAP:
                        result = aliPayController.aliWapPay(payParam);
                        break;
                    case BusinessConstant.PAYMENT_WEPAY:
                        result = invokeWechatPay(request, order, payParam, payMethod);
                        break;
                    case BusinessConstant.PAYMENT_WEPAY_MINIAPP:
                        result = invokeWechatPay(request, order, payParam, payMethod);
                        break;
                    case BusinessConstant.PAYMENT_WEPAY_H5:
                        if (ValidationUtil.isEmpty(openId)) {
                            return okCustomJson(CODE40001, "公众号支付需要openId");
                        }
                        result = invokeWechatPay(request, order, payParam, payMethod);
                        break;
                    default:
                        result = okCustomJson(CODE40004, "该支付方式尚未支持,请换成其他支付方式");
                        break;
                }
                businessUtils.unLock(key, OPERATION_PAY_ORDER);
                return result;
            } catch (Exception e) {
                logger.error("payOrder:" + e.getMessage());
                businessUtils.unLock(key, OPERATION_PAY_ORDER);
                return okCustomJson(CODE500, "支付发生异常，请稍后再试");
            }
        });
    }

    private Result invokeWechatPay(Http.Request request, Order order, PayParam payParam, int payMethod) {
        logger.info("wechat payparam:" + payParam.toString());
        String ip = businessUtils.getRequestIP(request);
        try {
            payParam.tradeNo = order.orderNo;
            payParam.realPayMoney = payParam.realPayMoney * 100;//微信支付是以分为单位，需要乘以100
            payParam.totalAmount = payParam.totalAmount * 100;//微信支付是以分为单位，需要乘以100
            return wechatPayController.wechatPay(payParam).toCompletableFuture().get(20, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("invokeWechatPay:" + e.getMessage());
            return okCustomJson(CODE500, "微信支付发生异常，请稍后再试");
        }
    }

    /**
     * @api {GET} /v1/o/orders_by_no/:orderNo/ 02订单详情
     * @apiName getOrderDetailByOrderNo
     * @apiGroup Order_H5
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
    public CompletionStage<Result> getOrderDetailByOrderNo(Http.Request request, String orderNo) {
        return CompletableFuture.supplyAsync(() -> {
            if (ValidationUtil.isEmpty(orderNo)) return okCustomJson(CODE40001, "订单参数错误");
            Order order = Order.find.query().where()
                    .eq("orderNo", orderNo)
                    .setMaxRows(1)
                    .findOne();
            if (null == order) return okCustomJson(CODE40002, "找不到该订单");
            List<OrderDetail> list = OrderDetail.find.query().where()
                    .eq("orderId", order.id)
                    .orderBy().asc("id")
                    .findList();
            order.detailList.addAll(list);
            ObjectNode result = (ObjectNode) Json.toJson(order);
            List<OrderReturns> orderReturnList = OrderReturns.find.query().where()
                    .ge("status", OrderReturns.STATUS_RETURN_TO_AUDIT)
                    .orderBy().asc("id")
                    .findList();
            result.set("orderReturnList", Json.toJson(orderReturnList));
            result.put(CODE, CODE200);
            return ok(result);
        });
    }



}
