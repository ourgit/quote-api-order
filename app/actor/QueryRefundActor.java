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
import models.order.Order;
import models.postservice.OrderReturns;
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

public class QueryRefundActor extends AbstractLoggingActor {
    public static Props getProps() {
        return Props.create(QueryRefundActor.class);
    }

    public static final String CHECKING_QUERY_REFUND_RESULT = "CHECKING_QUERY_REFUND_RESULT";

    @Inject
    DateUtils dateUtils;

    @Inject
    BizUtils bizUtils;

    @Inject
    AsyncCacheApi asyncCacheApi;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ActorProtocol.QUERY_REFUND_RESULT.class, str -> {
                    Optional optional = asyncCacheApi.sync().get(CHECKING_QUERY_REFUND_RESULT);
                    if (optional.isPresent()) return;
                    asyncCacheApi.set(CHECKING_QUERY_REFUND_RESULT, CHECKING_QUERY_REFUND_RESULT, 5);
                    checkRefundResult();
                    asyncCacheApi.remove(CHECKING_QUERY_REFUND_RESULT);
                })
                .build();
    }

    public ObjectNode okCustomJson(int code, String reason) {
        ObjectNode node = Json.newObject();
        node.put("code", code);
        node.put("reason", reason);
        return node;
    }


    private void checkRefundResult() {
        List<OrderReturns> orders = OrderReturns.find.query().where()
                .eq("status", OrderReturns.STATUS_AGREE_REFUND)
                .findList();
        orders.forEach((each) -> {
            if (!ValidationUtil.isEmpty(each.orderNo)) queryRefundOrder(each);
        });
    }

    public void queryRefundOrder(OrderReturns orderReturns) {
        try {
            String spMchId = bizUtils.getWepaySpMchId();
            String subMchId = bizUtils.getWepaySubMchId();
            HttpGet httpGet = new HttpGet("https://api.mch.weixin.qq.com/v3/refund/domestic/refunds/" + orderReturns.orderNo
                    + "?sub_mchid=" + subMchId);
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
                String status = resultNode.findPath("status").asText();
                String refundId = resultNode.findPath("refund_id").asText();
                String txId = resultNode.findPath("transaction_id").asText();
                JsonNode amount = resultNode.findPath("amount");
                if (status.equalsIgnoreCase("SUCCESS") && null != amount && !ValidationUtil.isEmpty(txId)) {
                    long payerRefund = amount.findPath("payer_refund").asLong();
                    Order latestOrder = Order.find.query().where().eq("orderNo", orderReturns.orderNo)
                            .orderBy().asc("id")
                            .setMaxRows(1)
                            .findOne();
                    if (null != latestOrder && payerRefund > 0) {
                        long currentTime = dateUtils.getCurrentTimeBySecond();
                        latestOrder.setRefundTxId(refundId);
                        latestOrder.setStatus(Order.ORDER_STATUS_SYSTEM_REFUNDED);
                        latestOrder.setPostServiceStatus(OrderReturns.STATUS_FINISHED);
                        latestOrder.setUpdateTime(currentTime);
                        latestOrder.setFilter("");
                        latestOrder.setFilter(Json.stringify(Json.toJson(latestOrder)));
                        latestOrder.save();
                        orderReturns.setStatus(OrderReturns.STATUS_REFUND);
                        orderReturns.setUpdateTime(currentTime);
                        orderReturns.save();
                    }
                }
            }
        } catch (Exception e) {
            log().error("queryRefundOrder:" + e.getMessage());
        }

    }
}
