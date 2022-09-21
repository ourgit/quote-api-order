package controllers;

import actor.ActorProtocol;
import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.AutoUpdateCertificatesVerifier;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import com.wechat.pay.contrib.apache.httpclient.util.RsaCryptoUtil;
import constants.BusinessConstant;
import models.order.Order;
import models.store.StoreOrder;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import play.Logger;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import utils.ValidationUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * 支付
 */
@Singleton
public class WechatPayV3Controller extends BaseNoAuthController {
    Logger.ALogger logger = Logger.of(WechatPayV3Controller.class);

    public static final int TAG_LENGTH_BIT = 128;
    @Inject
    @Named("groupOrderActor")
    private ActorRef groupOrderActorActor;
    @Inject
    @Named("storeOrderActor")
    private ActorRef storeOrderActorRef;

    public CompletionStage<Result> wechatPay(PayParam payParam) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpPost httpPost = new HttpPost("https://api.mch.weixin.qq.com/v3/pay/partner/transactions/jsapi");
                httpPost.addHeader("Accept", "application/json");
                httpPost.addHeader("Content-type", "application/json; charset=utf-8");
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectMapper objectMapper = new ObjectMapper();
                ObjectNode rootNode = objectMapper.createObjectNode();
                String spAppId = businessUtils.getWepaySpAppId();
                String spMchId = businessUtils.getWepaySpMchId();
                String subMchId = businessUtils.getWepaySubMchId();
                String miniAppId = businessUtils.getWechatMiniAppId();
                rootNode.put("sp_appid", spAppId)
                        .put("sp_mchid", spMchId)
                        .put("sub_appid", miniAppId)
                        .put("sub_mchid", subMchId)
                        .put("description", payParam.subject)
                        .put("out_trade_no", payParam.tradeNo.trim())
                        .put("notify_url", "https://fuzuc.com/v1/o/wechat/pay_notify/");
                //time_expire 失效 attach自定义参数
                rootNode.putObject("amount")
                        .put("total", payParam.realPayMoney);
                rootNode.putObject("payer")
                        .put("sub_openid", payParam.openId);
                rootNode.putObject("settle_info")
                        .put("profit_sharing", true);
                objectMapper.writeValue(bos, rootNode);
                httpPost.setEntity(new StringEntity(bos.toString("UTF-8"), "UTF-8"));

                String merchantSerialNumber = businessUtils.getWepaySubKeySerialNo();
                String apiV3Key = businessUtils.getWepayAPIV3Key();
                String privateKey = businessUtils.getWepayPrivateKey();
                PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(
                        new ByteArrayInputStream(privateKey.getBytes("utf-8")));

                //不需要传入微信支付证书了
                AutoUpdateCertificatesVerifier verifier = new AutoUpdateCertificatesVerifier(
                        new WechatPay2Credentials(spMchId, new PrivateKeySigner(merchantSerialNumber, merchantPrivateKey)),
                        apiV3Key.getBytes("utf-8"));

                WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                        .withMerchant(spMchId, merchantSerialNumber, merchantPrivateKey)
                        .withValidator(new WechatPay2Validator(verifier));
                // ... 接下来，你仍然可以通过builder设置各种参数，来配置你的HttpClient
                // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签，并进行证书自动更新
                HttpClient httpClient = builder.build();
                // 后面跟使用Apache HttpClient一样
                CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpPost);
                String bodyAsString = EntityUtils.toString(response.getEntity());
                response.close();
                logger.info("bodyAsString:" + bodyAsString);
                if (!ValidationUtil.isEmpty(bodyAsString)) {
                    JsonNode resultNode = Json.parse(bodyAsString);
                    String prepayId = resultNode.get("prepay_id").asText();
                    String packageStr = "prepay_id=" + prepayId;
                    if (!ValidationUtil.isEmpty(prepayId)) {
                        String KEY_APPID = "appid";
                        String KEY_TIMESTAMP = "timeStamp";
                        String KEY_NONCESTR = "nonceStr";
                        String KEY_PACKAGE = "package";
                        StringBuilder sb = new StringBuilder();
                        String timestamp = dateUtils.getCurrentTimeBySecond() + "";
                        String nonceStr = UUID.randomUUID().toString().replace("-", "").toUpperCase();
                        sb.append(miniAppId).append("\n")
                                .append(timestamp).append("\n")
                                .append(nonceStr).append("\n")
                                .append(packageStr).append("\n");
                        String paySign = signWithRSA(sb.toString(), privateKey);
                        ObjectNode dataNode = Json.newObject();
                        dataNode.put(CODE, CODE200);
                        ObjectNode data = Json.newObject();
                        data.put(KEY_TIMESTAMP, timestamp);
                        data.put(KEY_NONCESTR, nonceStr);
                        data.put(KEY_PACKAGE, packageStr);
                        data.put(KEY_APPID, miniAppId);
                        data.put("signType", "RSA");
                        data.put("paySign", paySign);
                        dataNode.set("data", data);
                        return ok(dataNode);
                    }
                }
            } catch (Exception e) {
                logger.error("wechatPay:" + e.getMessage());
            }
            return okCustomJson(CODE500, "调用微信支付产生异常，请微后再试");
        });
    }

    public String signWithRSA(String toSign, String privateKey) {
        try {
            // Remove markers and new line characters in private key
            String realPK = privateKey.replaceAll("-----END PRIVATE KEY-----", "")
                    .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll("\n", "");
            byte[] b1 = Base64.getDecoder().decode(realPK);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(b1);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            Signature privateSignature = Signature.getInstance("SHA256withRSA");
            privateSignature.initSign(kf.generatePrivate(spec));
            privateSignature.update(toSign.getBytes("UTF-8"));
            byte[] s = privateSignature.sign();
            return Base64.getEncoder().encodeToString(s);
        } catch (Exception e) {
            logger.error("signWithRSA:" + e.getMessage());
        }
        return "";
    }

    /**
     * @api {POST} /v1/p/wechat/pay_notify/ 01支付回调接口
     * @apiName handleWechatPayNotify
     * @apiGroup Pay
     * @apiSuccess (Success 200){int} code 200
     */
    @Transactional
    public CompletionStage<Result> handleWechatPayNotify(Http.Request request) {
        return CompletableFuture.supplyAsync(() -> {
            Http.RequestBody body = request.body();
            if (null == body) return ok("FAIL");
            JsonNode resultNode = body.asJson();
            if (null == resultNode) {
                logger.error("json node null");
                return ok("FAIL");
            }
            String eventType = resultNode.findPath("event_type").asText();
            if (ValidationUtil.isEmpty(eventType)) {
                logger.error("eventType empty");
                return ok("FAIL");
            }
            if (eventType.equalsIgnoreCase("TRANSACTION.SUCCESS")) {
                JsonNode resource = resultNode.findPath("resource");
                if (null == resource) {
                    logger.error("resource empty");
                    return ok("FAIL");
                }
                String associatedData = resource.findPath("associated_data").asText();
                String ciphertext = resource.findPath("ciphertext").asText();
                String nonce = resource.findPath("nonce").asText();
                String apiv3Key = businessUtils.getWepayAPIV3Key();
                String result = decryptToString(apiv3Key, associatedData, nonce, ciphertext);
                if (!ValidationUtil.isEmpty(result)) {
                    JsonNode realData = Json.parse(result);
                    String orderNo = realData.findPath("out_trade_no").asText();
                    String txId = realData.findPath("transaction_id").asText();
                    JsonNode amount = realData.findPath("amount");
                    if (!ValidationUtil.isEmpty(orderNo) && null != amount) {
                        long payerTotal = amount.findPath("payer_total").asLong();

                        if (orderNo.contains("G")) {
                            ActorProtocol.PLACE_ORDER_GROUP_BY_PAID param = new ActorProtocol.PLACE_ORDER_GROUP_BY_PAID(orderNo, txId);
                            groupOrderActorActor.tell(param, ActorRef.noSender());
                        } else if (orderNo.contains("S")) {
                            StoreOrder order = StoreOrder.find.query().where().eq("orderNo", orderNo)
                                    .orderBy().asc("id")
                                    .setMaxRows(1)
                                    .findOne();
                            if (null != order && order.status == Order.ORDER_STATUS_UNPAY && payerTotal >= order.realPay) {
                                order.setPayTxNo(txId);
                                long currentTime = dateUtils.getCurrentTimeBySecond();
                                order.setUpdateTime(currentTime);
                                order.setPayTime(currentTime);
                                order.setStatus(Order.ORDER_STATUS_TO_DELIEVERY);
                                order.setPayDetail(result);
                                order.setAccountSettle(0);
                                order.save();
                            }
                        } else {
                            Order order = Order.find.query().where().eq("orderNo", orderNo)
                                    .orderBy().asc("id")
                                    .setMaxRows(1)
                                    .findOne();
                            if (null != order && order.status == Order.ORDER_STATUS_UNPAY && payerTotal >= order.realPay) {
                                order.setPayTxNo(txId);
                                long currentTime = dateUtils.getCurrentTimeBySecond();
                                order.setUpdateTime(currentTime);
                                order.setPayTime(currentTime);
                                order.setStatus(Order.ORDER_STATUS_PAID);
                                order.setPayDetail(result);
                                order.setAccountSettle(Order.ACCOUNT_SETTLE_NEED);
                                order.save();
                                businessUtils.handleOrderPaid(order.orderNo, txId, BusinessConstant.PAYMENT_WEPAY, "");
                            }
                        }
                    }
                }
            }
            return ok("FAIL");
        });
    }

    public String decryptToString(String apiV3Key, String associatedData, String nonce, String ciphertext) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            SecretKeySpec key = new SecretKeySpec(apiV3Key.getBytes(), "AES");
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BIT, nonce.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            cipher.updateAAD(associatedData.getBytes());
            return new String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)), "utf-8");
        } catch (Exception e) {
            logger.error("decryptToString:" + e.getMessage());
        }
        return "";
    }

    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> addToReceivers(Http.Request request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonNode param = request.body().asJson();
                //MERCHANT_ID：商户ID  PERSONAL_OPENID：个人openid（由父商户APPID转换得到     PERSONAL_SUB_OPENID：个人sub_openid（由子商户APPID转换得到）
                String type = param.findPath("type").asText();
                String account = param.findPath("account").asText();
                String name = param.findPath("name").asText();
                HttpPost httpPost = new HttpPost("https://api.mch.weixin.qq.com/v3/profitsharing/receivers/add");
                httpPost.addHeader("Accept", "application/json");
                httpPost.addHeader("Content-type", "application/json; charset=utf-8");

                String spAppId = businessUtils.getWepaySpAppId();
                String spMchId = businessUtils.getWepaySpMchId();
                String subMchId = businessUtils.getWepaySubMchId();
                String miniAppId = businessUtils.getWechatMiniAppId();

                String merchantSerialNumber = businessUtils.getWepaySubKeySerialNo();
                String apiV3Key = businessUtils.getWepayAPIV3Key();
                String privateKey = businessUtils.getWepayPrivateKey();
                PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(
                        new ByteArrayInputStream(privateKey.getBytes("utf-8")));
                AutoUpdateCertificatesVerifier verifier = new AutoUpdateCertificatesVerifier(
                        new WechatPay2Credentials(spMchId, new PrivateKeySigner(merchantSerialNumber, merchantPrivateKey)),
                        apiV3Key.getBytes("utf-8"));
                WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                        .withMerchant(spMchId, merchantSerialNumber, merchantPrivateKey)
                        .withValidator(new WechatPay2Validator(verifier));

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectMapper objectMapper = new ObjectMapper();
                ObjectNode rootNode = objectMapper.createObjectNode();
                rootNode.put("appid", spAppId)
                        .put("sub_appid", miniAppId)
                        .put("sub_mchid", subMchId)
                        .put("type", type)
                        .put("account", account);
                if (type.equalsIgnoreCase("MERCHANT_ID")) {
                    X509Certificate wechatpayCertificate = verifier.getValidCertificate();
                    String ciphertext = RsaCryptoUtil.encryptOAEP(name, wechatpayCertificate);
                    rootNode.put("name", ciphertext);//need encrypt
                    rootNode.put("relation_type", "SERVICE_PROVIDER");
                } else {
                    rootNode.put("relation_type", "PARTNER");
                }
                objectMapper.writeValue(bos, rootNode);
                httpPost.setEntity(new StringEntity(bos.toString("UTF-8"), "UTF-8"));
                String serial = verifier.getValidCertificate().getSerialNumber().toString(16);
                logger.info("serial:" + serial);
                httpPost.setHeader("Wechatpay-Serial", serial);
                HttpClient httpClient = builder.build();
                // 后面跟使用Apache HttpClient一样
                CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpPost);
                String bodyAsString = EntityUtils.toString(response.getEntity());
                response.close();
                if (!ValidationUtil.isEmpty(bodyAsString)) {
                    JsonNode resultNode = Json.parse(bodyAsString);
                    logger.info("addToReceivers:" + resultNode.toString());
                    return okJSON200();
                }
            } catch (Exception e) {
                logger.error("addToReceivers:" + e.getMessage());
            }
            return okCustomJson(CODE500, "调用addToReceivers产生异常，请微后再试");
        });
    }

    private WechatPayHttpClientBuilder getBuilder(String spMchId) throws UnsupportedEncodingException {
        String merchantSerialNumber = businessUtils.getWepaySubKeySerialNo();
        String apiV3Key = businessUtils.getWepayAPIV3Key();
        String privateKey = businessUtils.getWepayPrivateKey();
        PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(
                new ByteArrayInputStream(privateKey.getBytes("utf-8")));
        //不需要传入微信支付证书了
        AutoUpdateCertificatesVerifier verifier = new AutoUpdateCertificatesVerifier(
                new WechatPay2Credentials(spMchId, new PrivateKeySigner(merchantSerialNumber, merchantPrivateKey)),
                apiV3Key.getBytes("utf-8"));
        WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                .withMerchant(spMchId, merchantSerialNumber, merchantPrivateKey)
                .withValidator(new WechatPay2Validator(verifier));
        return builder;
    }


    public CompletionStage<Result> nativePay(PayParam payParam) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpPost httpPost = new HttpPost("https://api.mch.weixin.qq.com/v3/pay/partner/transactions/native");
                httpPost.addHeader("Accept", "application/json");
                httpPost.addHeader("Content-type", "application/json; charset=utf-8");
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectMapper objectMapper = new ObjectMapper();
                ObjectNode rootNode = objectMapper.createObjectNode();
                String spAppId = businessUtils.getWepaySpAppId();
                String spMchId = businessUtils.getWepaySpMchId();
                String subMchId = businessUtils.getWepaySubMchId();

                logger.info(spAppId);
                logger.info(spMchId);
                logger.info(subMchId);
                rootNode.put("sp_appid", spAppId)
                        .put("sp_mchid", spMchId)
                        .put("sub_mchid", subMchId)
                        .put("description", payParam.subject)
                        .put("out_trade_no", payParam.tradeNo.trim())
                        .put("notify_url", "https://fuzubang.com/v1/o/wechat/pay_notify/");
                //time_expire 失效 attach自定义参数
                rootNode.putObject("amount")
                        .put("total", payParam.realPayMoney);
                objectMapper.writeValue(bos, rootNode);
                httpPost.setEntity(new StringEntity(bos.toString("UTF-8"), "UTF-8"));

                String merchantSerialNumber = businessUtils.getWepaySubKeySerialNo();
                String apiV3Key = businessUtils.getWepayAPIV3Key();
                String privateKey = businessUtils.getWepayPrivateKey();

                logger.info(merchantSerialNumber);
                logger.info(apiV3Key);
                logger.info(privateKey);

                PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(
                        new ByteArrayInputStream(privateKey.getBytes("utf-8")));

                //不需要传入微信支付证书了
                AutoUpdateCertificatesVerifier verifier = new AutoUpdateCertificatesVerifier(
                        new WechatPay2Credentials(spMchId, new PrivateKeySigner(merchantSerialNumber, merchantPrivateKey)),
                        apiV3Key.getBytes("utf-8"));

                WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                        .withMerchant(spMchId, merchantSerialNumber, merchantPrivateKey)
                        .withValidator(new WechatPay2Validator(verifier));
                // ... 接下来，你仍然可以通过builder设置各种参数，来配置你的HttpClient
                // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签，并进行证书自动更新
                HttpClient httpClient = builder.build();
                // 后面跟使用Apache HttpClient一样
                CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpPost);
                String bodyAsString = EntityUtils.toString(response.getEntity());
                response.close();
                logger.info(bodyAsString);
                if (!ValidationUtil.isEmpty(bodyAsString)) {
                    JsonNode resultNode = Json.parse(bodyAsString);
                    String codeUrl = resultNode.get("code_url").asText();
                    ObjectNode dataNode = Json.newObject();
                    dataNode.put(CODE, CODE200);
                    dataNode.put("codeUrl", codeUrl);
                    return ok(dataNode);
                }
            } catch (Exception e) {
                logger.error("nativePay:" + e.getMessage());
            }
            return okCustomJson(CODE500, "调用微信支付产生异常，请微后再试");
        });
    }

}
