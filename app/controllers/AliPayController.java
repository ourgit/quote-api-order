package controllers;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayConstants;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.fasterxml.jackson.databind.node.ObjectNode;
import constants.BusinessConstant;
import models.order.Order;
import models.order.OrderLog;
import play.Logger;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import utils.ValidationUtil;
import utils.alipay.AlipayConfig;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * 支付宝支付接口
 */
@Singleton
public class AliPayController extends BaseNoAuthController {

    Logger.ALogger logger = Logger.of(AliPayController.class);

    public static final String KEY_OUT_TRADE_NO = "out_trade_no";
    public static final String KEY_SELLER_ID = "seller_id";
    public static final String KEY_TOTAL_FEE = "total_fee";

    /**
     * 生成支付宝支付请求，一般由业务逻辑直接调用，不通过客户端直接调用
     * H5
     *
     * @return 支付请求成功，返回支付宝的付款表单，失败返回错误代码
     */
    public Result aliAppPay(PayParam payParam) {
        //实例化客户端
        String appId = businessUtils.getAlipayAppId();
        String privateKey = businessUtils.getAlipayAppPrivateKey();
        String alipayAliPublicKeyRSA2 = businessUtils.getAlipayAliPublicKey();
        String directPayNotifyUrl = businessUtils.getAlipayWapPayNotifyUrl();
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.ALI_PAY_URL,
                appId,
                privateKey,
                AlipayConstants.FORMAT_JSON,
                AlipayConstants.CHARSET_UTF8,
                alipayAliPublicKeyRSA2,
                AlipayConstants.SIGN_TYPE_RSA2);
        //实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        //SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setBody(payParam.subject);
        model.setSubject(payParam.subject);
        model.setOutTradeNo(payParam.tradeNo);
        model.setTotalAmount(String.valueOf(payParam.totalAmount / 100.00));
        model.setProductCode(payParam.productionCode);
        request.setBizModel(model);
        request.setNotifyUrl(directPayNotifyUrl);
        ObjectNode result = Json.newObject();
        result.put(CODE, CODE200);
        try {
            //这里和普通的接口调用不同，使用的是sdkExecute
            AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
            String responseResult = response.getBody();
            result.put("data", responseResult);
            return ok(result);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return okCustomJson(CODE500, "支付宝支付发生异常，请稍后再试");
    }

    /**
     * for pc
     *
     * @param payParam
     * @return
     */
    public Result aliPagePay(PayParam payParam) {
        String directPayNotifyUrl = businessUtils.getAlipayDirectPayNotifyUrl();
        String returnUrl = businessUtils.getAlipayReturnUrl();
        String appId = businessUtils.getAlipayAppId();
        String privateKey = businessUtils.getAlipayAppPrivateKey();
        String aliPublicKey = businessUtils.getAlipayAliPublicKey();
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.ALI_PAY_URL,
                appId, privateKey,
                "json", AlipayConstants.CHARSET_UTF8,
                aliPublicKey,
                AlipayConstants.SIGN_TYPE_RSA2);

        //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(returnUrl);
        alipayRequest.setNotifyUrl(directPayNotifyUrl);

        ObjectNode node = Json.newObject();
        node.put(KEY_OUT_TRADE_NO, payParam.tradeNo);
        node.put("total_amount", String.valueOf(payParam.totalAmount / 100.00));
        node.put("subject", payParam.subject);
        node.put("product_code", "FAST_INSTANT_TRADE_PAY");
        alipayRequest.setBizContent(Json.stringify(node));
        //请求
        try {
            String form = alipayClient.pageExecute(alipayRequest).getBody();
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            result.put("data", form);
            return ok(result);
        } catch (AlipayApiException e) {
            logger.error(e.getMessage());
        }
        return okCustomJson(CODE500, "支付请求发生异常，请重试");
    }

    /**
     * @api {POST} /v1/alipay_notify/ 01支付宝回调接口
     * @apiName handlePagePayNotify
     * @apiGroup Pay
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Error 40008){int} code 40008 交易密码错误
     */
    @Transactional
    public CompletionStage<Result> handleAliPayNotify(Http.Request request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, String> paramMap = new HashMap();
                Map<String, String[]> map = request.body().asFormUrlEncoded();
                if (null != map) map.forEach((k, v) -> paramMap.put(k, v[0]));
                String publicKey = businessUtils.getAlipayAliPublicKey();
                boolean result = AlipaySignature.rsaCheckV1(paramMap, publicKey, AlipayConstants.CHARSET_UTF8,
                        AlipayConstants.SIGN_TYPE_RSA2);
                String tradeStatus = paramMap.get("trade_status");
                String sellerId = paramMap.get(KEY_SELLER_ID);
                String totalFee = paramMap.get(KEY_TOTAL_FEE);
                if (ValidationUtil.isEmpty(totalFee)) totalFee = paramMap.get("total_amount");
                String outTradeNo = paramMap.get(KEY_OUT_TRADE_NO);
                if (result) {
                    if (ValidationUtil.isEmpty(tradeStatus)) logger.error("请求支付状态结果有误");
                    //交易状态TRADE_SUCCESS的通知触发条件是商户签约的产品支持退款功能的前提下，买家付款成功；
                    //交易状态TRADE_FINISHED的通知触发条件是商户签约的产品不支持退款功能的前提下，买家付款成功；
                    //或者，商户签约的产品支持退款功能的前提下，交易已经成功并且已经超过可退款期限；
                    String partnerNo = businessUtils.getAlipayPartnerNo();
                    if (tradeStatus.equals("TRADE_FINISHED") || tradeStatus.equals("TRADE_SUCCESS")) {
                        if (!ValidationUtil.isEmpty(totalFee) && sellerId.equals(partnerNo)) {
                            updateOrderPayStatus(paramMap, totalFee, outTradeNo);
                            return ok("success");
                        }
                    } else if (tradeStatus.equals("TRADE_CLOSED")) {
                        Order order = Order.find.query().where().eq("orderNo", outTradeNo)
                                .orderBy().desc("id").setMaxRows(1).findOne();
                        if (null != order) {
                            long currentTime = dateUtils.getCurrentTimeBySecond();
                            OrderLog orderLog = new OrderLog();
                            orderLog.setOrderId(order.id);
                            orderLog.setOldStatus(order.status);
                            orderLog.setNewStatus(Order.ORDER_STATUS_SYSTEM_REFUNDED);
                            orderLog.setOperatorName("支付宝API通知");
                            orderLog.setCreateTime(currentTime);
                            orderLog.save();
                            if (order.totalReturnNumber >= order.productCount) {
                                order.setStatus(Order.ORDER_STATUS_SYSTEM_REFUNDED);
                                order.setUpdateTime(currentTime);
                                order.save();
                            }
                            businessUtils.updateOrderDetailStatus(order);
                        }
                        return ok("success");
                    }
                }
            } catch (AlipayApiException e) {
                logger.error("handlePagePayNotify:" + e.getMessage());
            }
            return ok("fail");
        });
    }

    private void updateOrderPayStatus(Map<String, String> paramMap, String totalFee, String outTradeNo) {
        logger.info("updateOrderPayStatus:");
        businessUtils.handleOrderPaid(outTradeNo, paramMap.get("trade_no"),
                BusinessConstant.PAYMENT_ALIPAY, "");
    }


    /**
     * @api {GET} /v1/alipay_verify/ 02支付宝回调验签接口
     * @apiName verifySign
     * @apiGroup Pay
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Success 200){boolean} result true为正确签名，false为假签名
     */
    @Transactional
    public CompletionStage<Result> verifySign(Http.Request request) {
        Map<String, String> paramMap = new HashMap();
        Map<String, String[]> queryString = request.queryString();
        if (null != queryString) queryString.forEach((k, v) -> {
            if (!k.equalsIgnoreCase("time")) paramMap.put(k, v[0]);
        });
        return CompletableFuture.supplyAsync(() -> {
            ObjectNode resultNode = Json.newObject();
            resultNode.put(CODE, CODE200);
            String orderNo = paramMap.get(KEY_OUT_TRADE_NO);
            resultNode.put("payType", "pay");
            Order order = Order.find.query().where().eq("orderNo", orderNo).findOne();
            if (null != order) {
                resultNode.put("orderNo", order.orderNo);
            }
            return ok(resultNode);
        });
    }

    /**
     * 手机端支付请求
     *
     * @param payParam
     * @return
     */
    public Result aliWapPay(PayParam payParam) {
        String appId = businessUtils.getAlipayAppId();
        String privateKey = businessUtils.getAlipayAppPrivateKey();
        String alipayAliPublicKeyRSA2 = businessUtils.getAlipayAliPublicKey();
        String returnUrl = businessUtils.getAlipayReturnUrl();
        String wapPayNotifyUrl = businessUtils.getAlipayWapPayNotifyUrl();
        String partnerNo = businessUtils.getAlipayPartnerNo();
        String sellAccountName = businessUtils.getAlipaySellAccountName();
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.ALI_PAY_URL,
                appId, privateKey, AlipayConstants.FORMAT_JSON,
                AlipayConstants.CHARSET_UTF8,
                alipayAliPublicKeyRSA2, AlipayConstants.SIGN_TYPE_RSA2); //获得初始化的AlipayClient
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();//创建API对应的request
        alipayRequest.setReturnUrl(returnUrl);
        alipayRequest.setNotifyUrl(wapPayNotifyUrl);
        alipayRequest.setApiVersion("1.0");
        ObjectNode node = Json.newObject();
        node.put("total_amount", String.valueOf(payParam.totalAmount));
        node.put("subject", payParam.subject);
        node.put("seller_id", partnerNo);
        node.put("product_code", payParam.productionCode);
        node.put("partner", partnerNo);
        node.put(KEY_OUT_TRADE_NO, payParam.tradeNo);
        node.put("payment_type", AlipayConfig.payment_type);
        node.put(KEY_TOTAL_FEE, String.valueOf(payParam.totalAmount / 100));//支付宝以元为单位,我们是以为分为单位，需要除以100
        node.put(KEY_SELLER_ID, partnerNo);
        node.put("seller_email", sellAccountName);
        node.put("seller_account_name", sellAccountName);
        alipayRequest.setBizContent(Json.stringify(node));
        try {
            String form = alipayClient.pageExecute(alipayRequest).getBody(); //调用SDK生成表单
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            result.put("data", form);
            return ok(result);
        } catch (AlipayApiException e) {
            logger.error(e.getMessage());
        }
        return okCustomJson(CODE500, "支付请求发生异常，请重试");
    }


}
