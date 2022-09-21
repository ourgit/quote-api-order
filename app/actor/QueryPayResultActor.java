package actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.AutoUpdateCertificatesVerifier;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import constants.BusinessConstant;
import models.order.Order;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import play.cache.AsyncCacheApi;
import play.libs.Json;
import utils.BizUtils;
import utils.DateUtils;
import utils.ValidationUtil;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.util.List;
import java.util.Optional;

import static models.order.Order.ORDER_STATUS_UNPAY_CLOSE;

public class QueryPayResultActor extends AbstractLoggingActor {
    public static Props getProps() {
        return Props.create(QueryPayResultActor.class);
    }
    public static final String CHECKING_PAY_RESULT = "CHECKING_PAY_RESULT";

    @Inject
    DateUtils dateUtils;

    @Inject
    BizUtils bizUtils;


    @Inject
    AsyncCacheApi asyncCacheApi;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ActorProtocol.CHECK_PAY_RESULT.class, str -> {
                    Optional optional = asyncCacheApi.sync().get(CHECKING_PAY_RESULT);
                    if (optional.isPresent()) return;
                    asyncCacheApi.set(CHECKING_PAY_RESULT, CHECKING_PAY_RESULT, 5);
                    checkPayResult();
                    asyncCacheApi.remove(CHECKING_PAY_RESULT);
                })
                .build();
    }

    public ObjectNode okCustomJson(int code, String reason) {
        ObjectNode node = Json.newObject();
        node.put("code", code);
        node.put("reason", reason);
        return node;
    }


    private void checkPayResult() {
        long currentTime = dateUtils.getCurrentTimeBySecond();
        List<Order> orders = Order.find.query().where()
                .eq("status", Order.ORDER_STATUS_UNPAY)
                .eq("payMethod", BusinessConstant.PAYMENT_WEPAY)
                .le("updateTime", currentTime - 2 * 60)
                .findList();
        orders.forEach((each) -> queryPayOrder(each));
    }

    public void queryPayOrder(Order order) {
        try {
            String spMchId = bizUtils.getWepaySpMchId();
            String subMchId = bizUtils.getWepaySubMchId();
            HttpGet httpGet = new HttpGet("https://api.mch.weixin.qq.com/v3/pay/partner/transactions/out-trade-no/" + order.orderNo
                    + "?sp_mchid=" + spMchId + "&sub_mchid=" + subMchId);
            httpGet.addHeader("Accept", "application/json");
            httpGet.addHeader("Content-type", "application/json; charset=utf-8");

            String merchantSerialNumber = bizUtils.getWepaySubKeySerialNo();
            String apiV3Key = bizUtils.getWepayAPIV3Key();
            String privateKey = bizUtils.getWepayPrivateKey();
            PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(
                    new ByteArrayInputStream(privateKey.getBytes("utf-8")));
            AutoUpdateCertificatesVerifier verifier = new AutoUpdateCertificatesVerifier(
                    new WechatPay2Credentials(spMchId, new PrivateKeySigner(merchantSerialNumber, merchantPrivateKey)),
                    apiV3Key.getBytes("utf-8"));
            WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                    .withMerchant(spMchId, merchantSerialNumber, merchantPrivateKey)
                    .withValidator(new WechatPay2Validator(verifier));
            HttpClient httpClient = builder.build();
            CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpGet);
            String bodyAsString = EntityUtils.toString(response.getEntity());
            response.close();
            if (!ValidationUtil.isEmpty(bodyAsString)) {
                JsonNode resultNode = Json.parse(bodyAsString);
                String tradeState = resultNode.findPath("trade_state").asText();
                String txId = resultNode.findPath("transaction_id").asText();
                JsonNode amount = resultNode.findPath("amount");
                if (tradeState.equalsIgnoreCase("SUCCESS") && null != amount && !ValidationUtil.isEmpty(txId)) {
                    Order latestOrder = Order.find.query().where().eq("orderNo", order.orderNo)
                            .orderBy().asc("id")
                            .setMaxRows(1)
                            .findOne();
                    if (null != latestOrder && latestOrder.status <= Order.ORDER_STATUS_UNPAY) {
                        log().info("支付返回成功，更新订单支付状态:" + txId);
                        bizUtils.handleOrderPaid(order.orderNo, txId, BusinessConstant.PAYMENT_WEPAY,"");
                    }
                } else {
                    long currentTime = dateUtils.getCurrentTimeBySecond();
                    if (currentTime - order.createTime > 5 * 3600) {
                        //5小时前的，全部关闭
                        order.setStatus(ORDER_STATUS_UNPAY_CLOSE);
                        order.setNote("超时自动关闭");
                        order.save();
                    }
                    log().info("messageDecode: " + bodyAsString);
                }
            }
        } catch (Exception e) {
            log().error("queryPayOrder:" + e.getMessage());
        }
    }

}
