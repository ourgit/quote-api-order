package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import models.order.Order;
import models.order.OrderDetail;
import org.apache.commons.collections.map.HashedMap;
import play.Logger;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import utils.MD5;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static constants.BusinessConstant.DF_TWO_DIGIT;

/**
 * 订单管理
 */
@Singleton
public class DADAUtil extends BaseSecurityController {
    Logger.ALogger logger = Logger.of(DADAUtil.class);

    public static final String DADA_APP_KEY = "dada87a798ece4db4c0";
    public static final String DADA_APP_SECKET = "dc3a8fca5e822d500c958bd8c0f4f122";
    public final static String DADA_FORMAT = "json";
    public final static String DATA_V = "1.0";
    public final static String DADA_CITY_CODE = "0591";
    //    public static final String DATA_API_URL = "http://newopen.qa.imdada.cn";
    public static final String DATA_API_URL = "https://newopen.imdada.cn";
    public static final String DATA_PLACE_ORDER_API = "/api/order/addOrder";
    public static final String DATA_RE_PLACE_ORDER_API = "/api/order/reAddOrder";
    public static final String DATA_QUERY_ORDER_API = "/api/order/status/query";
    public static final String DATA_CANCEL_ORDER_API = "/api/order/formalCancel";
    public static final String DATA_CONFIRM_ORDER_EXCEPTION_API = "/api/order/confirm/goods";
    //    public static final String DATA_USER_NO = "99453";
    public static final String CALL_BACK_URL = "https://qd.starnew.cn/v1/cp/dada_call_backsudljowiejlk33423/";
//    public static final String DATA_SHOP_ID = "99453-1120937";

    @Inject
    WSClient wsClient;

    public CompletionStage<WSResponse> placeDadaOrder(Order order, String dadaShopId, String dadaUserNo) {
        ObjectNode param = buildPlaceOrderParam(order, dadaShopId);
        ObjectNode signDataParam = signDataParam(param, dadaUserNo);
        return wsClient.url(DATA_API_URL + DATA_PLACE_ORDER_API).post(signDataParam);
    }

    public CompletionStage<WSResponse> replaceDadaOrder(Order order, String dadaShopId, String dadaUserNo) {
        ObjectNode param = buildPlaceOrderParam(order, dadaShopId);
        ObjectNode signDataParam = signDataParam(param, dadaUserNo);
        return wsClient.url(DATA_API_URL + DATA_RE_PLACE_ORDER_API).post(signDataParam);
    }

    public CompletionStage<WSResponse> queryDadaOrder(Order order, String dadaUserNo) {
        ObjectNode param = buildQueryOrderParam(order);
        ObjectNode signDataParam = signDataParam(param, dadaUserNo);
        return wsClient.url(DATA_API_URL + DATA_QUERY_ORDER_API).post(signDataParam);
    }

    public CompletionStage<WSResponse> cancelDadaOrder(Order order, String dadaUserNo, int cancelReasonId, String cancelReason) {
        ObjectNode param = buildCancelOrderParam(order, cancelReasonId, cancelReason);
        ObjectNode signDataParam = signDataParam(param, dadaUserNo);
        return wsClient.url(DATA_API_URL + DATA_CANCEL_ORDER_API).post(signDataParam);
    }

    private ObjectNode buildPlaceOrderParam(Order order, String dadaShopId) {
        ObjectNode node = Json.newObject();
        node.put("shop_no", dadaShopId);
        node.put("origin_id", order.orderNo);
        node.put("city_code", DADA_CITY_CODE);
        node.put("cargo_price", DF_TWO_DIGIT.format(order.realPay / 100.00));
        node.put("is_prepay", "0");
        ObjectNode address = (ObjectNode) Json.parse(order.address);
        node.put("receiver_name", address.findPath("name").asText());
        node.put("receiver_address", address.findPath("address").asText());
        node.put("receiver_phone", address.findPath("telephone").asText());
        node.put("receiver_lat", address.findPath("lat").asDouble());
        node.put("receiver_lng", address.findPath("lng").asDouble());
        node.put("callback", CALL_BACK_URL);
        node.put("cargo_weight", "2");
        node.put("cargo_num", "" + order.productCount);
        ArrayNode productList = Json.newArray();
        List<OrderDetail> detailList = OrderDetail.find.query().where().eq("orderId", order.id)
                .orderBy().asc("id").findList();
        detailList.forEach((each) -> {
            ObjectNode eachNode = Json.newObject();
            eachNode.put("sku_name", each.productName);
            eachNode.put("src_product_no", each.productId);
            eachNode.put("count", each.number);
            eachNode.put("unit", each.unit);
            productList.add(eachNode);
        });
        node.set("product_list", productList);
        return node;
    }

    private ObjectNode buildQueryOrderParam(Order order) {
        ObjectNode node = Json.newObject();
        node.put("order_id", order.orderNo);
        return node;
    }

    private ObjectNode buildCancelOrderParam(Order order, int cancelReasonId, String cancelReason) {
        ObjectNode node = Json.newObject();
        node.put("order_id", order.orderNo);
        node.put("cancel_reason_id", cancelReasonId);
        node.put("cancel_reason", cancelReason);
        return node;
    }

    private ObjectNode signDataParam(JsonNode param, String dadaUserNo) {
        Map<String, String> map = new HashedMap();
        map.put("body", Json.stringify(param));
        map.put("format", DADA_FORMAT);
        map.put("timestamp", dateUtils.getCurrentTimeBySecond() + "");
        map.put("app_key", DADA_APP_KEY);
        map.put("v", DATA_V);
        map.put("source_id", dadaUserNo);
        Collection<String> keySet = map.keySet();
        List<String> sortedList = keySet.parallelStream().sorted().collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        sortedList.forEach((key) -> sb.append(key + map.get(key)));
        String result = MD5.MD5Encode(DADA_APP_SECKET + sb.toString() + DADA_APP_SECKET).toUpperCase();
        map.put("signature", result);
        ObjectNode node = (ObjectNode) Json.toJson(map);
        return node;
    }

}
