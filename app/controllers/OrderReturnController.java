package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.AutoUpdateCertificatesVerifier;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import constants.BusinessConstant;
import io.ebean.DB;
import io.ebean.PagedList;
import models.dealer.DealerAward;
import models.log.BalanceLog;
import models.order.Order;
import models.order.OrderDetail;
import models.order.OrderLog;
import models.postservice.*;
import models.stat.StatMemberSalesOverview;
import models.user.Member;
import models.user.MemberCoupon;
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
import utils.*;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static constants.BusinessConstant.*;
import static models.order.Order.*;
import static models.postservice.OrderReturns.*;
import static models.user.MemberCoupon.STATUS_NOT_USE;

/**
 * 用户控制类
 */
public class OrderReturnController extends BaseController {

    Logger.ALogger logger = Logger.of(OrderReturnController.class);

    @Inject
    BalanceUtils balanceUtils;

    /**
     * @api {POST} /v1/o/cancel_order/ 01取消订单
     * @apiName cancelOrder
     * @apiGroup OrderReturn
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
            JsonNode requestNode = request.body().asJson();
            if (null == requestNode) return okCustomJson(CODE40001, "参数错误");
            String operation = requestNode.findPath("operation").asText();
            long orderId = requestNode.findPath("orderId").asLong();
            if (ValidationUtil.isEmpty(operation) || !operation.equals("cancel") || orderId < 1)
                return okCustomJson(CODE40001, "参数错误");
            Order order = Order.find.query().where().eq("id", orderId)
                    .eq("uid", uid).setMaxRows(1).findOne();
            if (null == order) return okCustomJson(CODE40002, "订单不存在");
            if (order.orderType == ORDER_TYPE_MEMBERSHIP) return okCustomJson(CODE40002, "购买会员的订单不可取消");
            if (order.orderType == ORDER_TYPE_NORMAL) {
                if (order.status < ORDER_STATUS_UNPAY || order.status > ORDER_STATUS_TO_DELIEVERY)
                    return okCustomJson(CODE40003, "订单处于不可取消状态，如有需要请申请售后");
            } else {
                if (order.status < ORDER_STATUS_UNPAY || order.status > ORDER_STATUS_ARRIVE_SELF_TAKEN_PLACE)
                    return okCustomJson(CODE40003, "订单处于不可取消状态，如有需要请申请售后");
            }
            long currentTime = dateUtils.getCurrentTimeBySecond();
            try {
                if (!businessUtils.setLock(String.valueOf(orderId), OPERATION_CANCEL_ORDER))
                    return okCustomJson(CODE40004, "正在取消订单中,请稍等");
                if (order.status == ORDER_STATUS_UNPAY) {
                    setOrderCancelled(order, currentTime, uid + "", true);
                    returnCoupon(order);
                    businessUtils.unLock(String.valueOf(orderId), OPERATION_CANCEL_ORDER);
                    return okJSON200();
                } else {
                    String result = "";
                    if (order.realPay > 0) {
                        result = refundMoney(order, "用户取消订单，退款", "" + order.id, order.totalReturnMoney);
                    } else result = "OK";
                    if (!ValidationUtil.isEmpty(result)) {
                        setOrderCancelled(order, currentTime, uid + "", true);
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

    private void setOrderCancelled(Order order, long currentTime, String operatorName, boolean needRestoreStock) {
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
        if (order.activityType != ORDER_ACTIVITY_TYPE_FLASH_SALE && needRestoreStock)
            businessUtils.updateMerchantStock(order, BusinessConstant.TYPE_CANCEL_ORDER);
    }

    private void setOrderRefund(Order order, OrderReturns returns, String operatorName) {
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderId(order.id);
        orderLog.setOldStatus(order.status);
        orderLog.setNewStatus(order.status);
        orderLog.setOperatorName(operatorName);
        long currentTime = dateUtils.getCurrentTimeBySecond();
        orderLog.setCreateTime(currentTime);
        orderLog.setNote("用户申请退款");
        orderLog.save();
        List<OrderReturnsDetail> detailList = OrderReturnsDetail.find.query().where()
                .eq("returnId", returns.id)
                .findList();
        if (detailList.size() > 0) {
            detailList.forEach((each) -> {
                OrderDetail orderDetail = OrderDetail.find.byId(each.orderDetailId);
                if (null != orderDetail) {
                    businessUtils.updateProductSkuSoldAmountAndStock(orderDetail.productSkuId, each.returnAmount * -1);
                }
            });
        }
        if (order.totalReturnNumber >= order.productCount) {
            //如果有优惠券，退还
            if (order.couponId > 0) {
                MemberCoupon coupon = MemberCoupon.find.byId(order.couponId);
                if (coupon.status == MemberCoupon.STATUS_USED) {
                    coupon.setStatus(STATUS_NOT_USE);
                    coupon.save();
                }
            }
        }
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
            BalanceLog balanceLog = BalanceLog.find.query().where()
                    .eq("orderNo", order.orderNo)
                    .eq("bizType", TRANSACTION_TYPE_SUBTRACT_SCORE_GAVE_FOR_CANCEL_ORDER)
                    .setMaxRows(1)
                    .findOne();
            if (null == balanceLog) {
                BalanceParam param = new BalanceParam.Builder()
                        .itemId(BusinessItem.SCORE)
                        .changeAmount(-order.scoreGave)
                        .leftBalance(-order.scoreGave)
                        .totalBalance(-order.scoreGave)
                        .memberId(order.uid)
                        .desc("取消订单/申请退款扣除赠送的消费积分:" + order.scoreGave)
                        .bizType(TRANSACTION_TYPE_SUBTRACT_SCORE_GAVE_FOR_CANCEL_ORDER).build();
                balanceUtils.saveChangeBalance(param, true);
            }

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
    public String refundMoney(Order order, String subject, String productionCode, long returnMoney) {
        if (order.realPay <= 0) return "OK";
        PayParam payParam = new PayParam.Builder()
                .tradeNo(order.orderNo).subject(subject)
                .productionCode(productionCode)
                .totalAmount(order.realPay)
                .returnMoney(returnMoney)
                .uid(order.uid).build();
        switch (order.payMethod) {
            case BusinessConstant.PAYMENT_BALANCE_PAY:
                return refundMoneyWithBalancePayment(order, subject, returnMoney);
            case BusinessConstant.PAYMENT_WEPAY:
                return refundMoneyWithWechatPay(payParam, order, null);
            case BusinessConstant.PAYMENT_WEPAY_MINIAPP:
                return refundMoneyWithWechatPay(payParam, order, null);
        }
        return "";
    }

    public String refundMoneyForOrderReturns(OrderReturns orderReturns, String subject, String productionCode, long returnMoney, Order order) {
        if (returnMoney <= 0) return "OK";
        PayParam payParam = new PayParam.Builder()
                .tradeNo(order.orderNo)
                .subject(subject)
                .productionCode(productionCode)
                .totalAmount(order.realPay)
                .returnMoney(returnMoney)
                .uid(order.uid).build();
        switch (order.payMethod) {
            case BusinessConstant.PAYMENT_BALANCE_PAY:
                BalanceParam param = new BalanceParam.Builder()
                        .itemId(BusinessItem.CASH)
                        .changeAmount(returnMoney)
                        .leftBalance(returnMoney)
                        .totalBalance(returnMoney)
                        .days(0)
                        .desc(subject)
                        .memberId(order.uid)
                        .orderNo(order.orderNo)
                        .bizType(TRANSACTION_TYPE_REFUND_MONEY).build();
                balanceUtils.saveChangeBalance(param, true);
                setOrderRefunded(order);
                return "OK";
            case BusinessConstant.PAYMENT_WEPAY:
                return refundMoneyWithWechatPay(payParam, order, orderReturns);
            case BusinessConstant.PAYMENT_WEPAY_MINIAPP:
                return refundMoneyWithWechatPay(payParam, order, orderReturns);
        }
        return "";
    }

    private String refundMoneyWithBalancePayment(Order order, String subject, long returnMoney) {
        BalanceLog balanceLog = BalanceLog.find.query().where().eq("orderNo", order.orderNo)
                .orderBy().asc("id")
                .setMaxRows(1)
                .findOne();
        if (null != balanceLog) {
            BalanceParam param = new BalanceParam.Builder()
                    .itemId(balanceLog.itemId)
                    .changeAmount(returnMoney)
                    .leftBalance(returnMoney)
                    .totalBalance(returnMoney)
                    .days(0)
                    .desc(subject)
                    .memberId(order.uid)
                    .orderNo(order.orderNo)
                    .bizType(TRANSACTION_TYPE_REFUND_MONEY).build();
            balanceUtils.saveChangeBalance(param, true);
            setOrderRefunded(order);
            return "OK";
        }
        return "";
    }

    private String refundMoneyWithWechatPay(PayParam payParam, Order order, OrderReturns orderReturns) {
        try {
            HttpPost httpPost = new HttpPost("https://api.mch.weixin.qq.com/v3/refund/domestic/refunds");
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Content-type", "application/json; charset=utf-8");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode rootNode = objectMapper.createObjectNode();
            String spMchId = businessUtils.getWepaySpMchId();
            String subMchId = businessUtils.getWepaySubMchId();
            rootNode.put("sub_mchid", subMchId)
                    .put("out_trade_no", payParam.tradeNo.trim())
                    .put("out_refund_no", payParam.tradeNo.trim());
            //time_expire 失效 attach自定义参数
            rootNode.putObject("amount")
                    .put("refund", payParam.returnMoney)
                    .put("total", payParam.totalAmount)
                    .put("currency", "CNY");
            objectMapper.writeValue(bos, rootNode);
            httpPost.setEntity(new StringEntity(bos.toString("UTF-8"), "UTF-8"));

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
            HttpClient httpClient = builder.build();
            CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpPost);
            String bodyAsString = EntityUtils.toString(response.getEntity());
            response.close();
            logger.info("refundMoneyWithWechatPay:" + bodyAsString);
            if (!ValidationUtil.isEmpty(bodyAsString)) {
                JsonNode resultNode = Json.parse(bodyAsString);
                String refundId = resultNode.findPath("refund_id").asText();
                String status = resultNode.findPath("status").asText();
                if (!ValidationUtil.isEmpty(status) && status.equalsIgnoreCase("SUCCESS")) {
                    if (null != orderReturns) {
                        orderReturns.setStatus(STATUS_REFUND);
                        orderReturns.setReturnTx(Json.stringify(resultNode));
                        orderReturns.setUpdateTime(dateUtils.getCurrentTimeBySecond());
                        orderReturns.save();
                    } else {
                        order.setRefundTxId(refundId);
                        order.setUpdateTime(dateUtils.getCurrentTimeBySecond());
                        if (order.totalReturnNumber >= order.productCount)
                            order.setStatus(Order.ORDER_STATUS_SYSTEM_REFUNDED);
                        order.setPostServiceStatus(STATUS_FINISHED);
                        order.save();
                    }
                    return Json.stringify(resultNode);
                }
            }
        } catch (Exception e) {
            logger.error("wechatPay:" + e.getMessage());
        }
        return "";
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
    }

    /**
     * @api {POST} /v1/o/part_return_order_detail/ 02申请/取消部分退货
     * @apiName applyReturnOrderDetail
     * @apiGroup OrderReturn
     * @apiParam {long} orderId 订单ID
     * @apiParam {long} [returnOrderId] 售后ID
     * @apiParam {int} status 10申请售后 -1取消售后
     * @apiParam {Array} list list
     * @apiParam {long} orderDetailId orderDetailId
     * @apiParam {long} returnNumber 退货数量
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Error 40001){int} code 40001 参数错误
     */
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> applyReturnOrderDetail(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            JsonNode node = request.body().asJson();
            if (null == node) return okCustomJson(CODE40001, "参数错误");
            long orderId = node.findPath("orderId").asLong();
            long returnOrderId = node.findPath("returnOrderId").asLong();
            int status = node.findPath("status").asInt();
            int state = node.findPath("state").asInt();

            String reason = node.findPath("reason").asText();
            String note = node.findPath("note").asText();
            long returnMoneyByUser = node.findPath("returnMoney").asLong();

            Order order = Order.find.byId(orderId);
            if (null == order) return okCustomJson(CODE40003, "该订单不存在");
            long currentTime = dateUtils.getCurrentTimeBySecond();
            try {
                if (!businessUtils.setLock(String.valueOf(uid), OPERATION_APPLY_ORDER_RETURN))
                    return okCustomJson(CODE40004, "正在处理中,请稍等");
                DB.beginTransaction();
                if (status == STATUS_RETURN_TO_AUDIT) {
                    if (!node.has("list")) return okCustomJson(CODE40001, "请选择退货商品");
                    ArrayNode list = (ArrayNode) node.findPath("list");
                    if (list.size() < 1) return okCustomJson(CODE40001, "请选择退货商品");
                    long returnMoney = 0;

                    long favor = order.totalMoney - order.realPay;
                    if (favor <= 0) favor = 0;
                    long totalReturnNumber = 0;
                    List<OrderReturnsDetail> returnsDetailList = new ArrayList<>();
                    for (int i = 0; i < list.size(); i++) {
                        JsonNode each = list.get(i);
                        if (null != each) {
                            long orderDetailId = each.findPath("orderDetailId").asLong();
                            long returnNumber = each.findPath("returnNumber").asLong();
                            if (returnNumber < 1) {
                                return okCustomJson(CODE40001, "请选择退货数量");
                            }
                            if (orderDetailId < 1) {
                                return okCustomJson(CODE40001, "参数错误");
                            }
                            OrderDetail orderDetail = OrderDetail.find.byId(orderDetailId);
                            if (null == orderDetail) {
                                return okCustomJson(CODE40003, "该订单不存在");
                            }
                            if (orderDetail.returnStatus == POST_SERVICE_STATUS_APPLYING) {
                                return okCustomJson(CODE40001, "该商品已申请退货，请等待审核");
                            }
                            orderDetail.setReturnStatus(POST_SERVICE_STATUS_APPLYING);
                            orderDetail.setReturnNumber(orderDetail.returnNumber + returnNumber);
                            totalReturnNumber = totalReturnNumber + returnNumber;
                            orderDetail.setUpdateTime(currentTime);
                            long subReturn = orderDetail.productPrice * returnNumber;
                            orderDetail.setSubReturn(subReturn);
                            orderDetail.save();
                            returnMoney = returnMoney + subReturn;
                            OrderReturnsDetail orderReturnsDetail = newOrderReturnsDetail(currentTime, returnNumber, orderDetail);
                            returnsDetailList.add(orderReturnsDetail);
                        }
                    }
                    if (returnMoney < 1) return okCustomJson(CODE40001, "您的订单可退款金额不足，如有疑问，请联系客服");
                    order.setTotalReturnNumber(order.totalReturnNumber + totalReturnNumber);
                    if (order.totalReturnNumber >= order.productCount) {
                        returnMoney = returnMoney + order.logisticsFee;
                    }
                    if (returnMoneyByUser < returnMoney) returnMoney = returnMoneyByUser;
                    order.setTotalReturnMoney(order.totalReturnMoney + returnMoney);
                    if (order.totalReturnMoney > order.realPay) {
                        return okCustomJson(CODE40001, "您的订单可退款金额不足，如有疑问，请联系客服");
                    }
                    order.setPostServiceStatus(POST_SERVICE_STATUS_APPLYING);
                    order.save();
                    OrderReturns returnsApply = new OrderReturns();
                    returnsApply.setShopId(order.shopId);
                    returnsApply.setOrderId(orderId);
                    returnsApply.setOrderNo(order.orderNo);
                    returnsApply.setOrderDetailId(0);
                    returnsApply.setUid(uid);
                    returnsApply.setReturnsNo("R" + IdGenerator.getId());
                    returnsApply.setReason(reason);
                    returnsApply.setState(state);
                    returnsApply.setUpdateTime(currentTime);
                    returnsApply.setCreateTime(currentTime);
                    returnsApply.setStatus(status);
                    returnsApply.setReturnSubmitTime(currentTime);
                    returnsApply.setRemark(note);
                    returnsApply.setReturnMoney(returnMoney);
                    returnsApply.setTotalReturnNumber(totalReturnNumber);
                    returnsApply.setRemark(note);
                    handlePostServiceTime(returnsApply, currentTime);
                    returnsApply.save();
                    if (returnsDetailList.size() > 0) {
                        returnsDetailList.parallelStream().forEach((each) -> {
                            each.setReturnId(returnsApply.id);
                        });
                        DB.saveAll(returnsDetailList);
                    }
                    if (node.has("imgList")) {
                        JsonNode imgList = node.findPath("imgList");
                        if (null != imgList) {
                            ArrayNode imgNodes = (ArrayNode) node.findPath("imgList");
                            List<OrderReturnsImage> updateList = new ArrayList<>();
                            if (imgNodes.isArray() && imgNodes.size() > 0) {
                                int currentCount = imgNodes.size();
                                if (currentCount > 9) return okCustomJson(CODE40001, "图片最多允许9张");
                                List<Long> orderReturnsImageList = OrderReturnsImage.find.query().where()
                                        .eq("uid", uid)
                                        .eq("returnApplyId", returnsApply.id)
                                        .findIds();
                                int existImgCount = orderReturnsImageList.size();
                                if (existImgCount + currentCount > 9) {
                                    int availableCount = 9 - existImgCount;
                                    if (availableCount < 0) availableCount = 0;
                                    return okCustomJson(CODE40001, "还可上传" + availableCount + "个图片/视频");
                                }
                                imgNodes.forEach((each) -> {
                                    if (null != each) {
                                        OrderReturnsImage image = new OrderReturnsImage();
                                        image.setOrderId(orderId);
                                        image.setReturnApplyId(returnsApply.id);
                                        image.setOrderDetailId(0);
                                        image.setUid(uid);
                                        String imgUrl = businessUtils.escapeHtml(each.asText());
                                        image.setImgUrl(imgUrl);
                                        image.setCreateTime(currentTime);
                                        updateList.add(image);
                                    }
                                });
                            }
                            if (updateList.size() > 0) DB.saveAll(updateList);
                        }
                    }
                    DB.commitTransaction();
                    ObjectNode result = Json.newObject();
                    result.put(CODE, CODE200);
                    String returnsNo = returnsApply.getReturnsNo();
                    result.put("returnsNo", returnsNo);
                    ObjectNode pushNode = Json.newObject();
                    pushNode.put("type", TASK_NEW_POST_SERVICE);
                    pushNode.put("orderId", orderId);
                    pushNode.put("returnsNo", returnsNo);
                    return ok(result);
                } else {
                    OrderReturns orderReturns = OrderReturns.find.byId(returnOrderId);
                    if (null == orderReturns) {
                        return okCustomJson(CODE40004, "该退货申请不存在");
                    }
                    orderReturns.setStatus(status);
                    orderReturns.setUpdateTime(currentTime);
                    orderReturns.save();
                    updateOrderReturnNumbersForReject(order, orderReturns);
                    cancelOrderReturnsDetail(orderReturns.id, false);
                    DB.commitTransaction();
                }
                return okJSON200();
            } catch (Exception e) {
                logger.error("applyReturnOrderDetail:" + e.getMessage());
            } finally {
                DB.endTransaction();
                businessUtils.unLock(String.valueOf(orderId), OPERATION_APPLY_ORDER_RETURN);
            }
            return okCustomJson(CODE500, "退货发生异常，请稍后再试");
        });
    }

    private void updateOrderReturnNumbersForReject(Order order, OrderReturns orderReturns) {
        order.setTotalReturnNumber(order.totalReturnNumber - orderReturns.totalReturnNumber);
        order.setTotalReturnMoney(order.totalReturnMoney - orderReturns.returnMoney);
        order.setPostServiceStatus(Order.POST_SERVICE_STATUS_NO);
        order.save();
    }

    private OrderReturnsDetail newOrderReturnsDetail(long currentTime, long returnNumber, OrderDetail orderDetail) {
        OrderReturnsDetail orderReturnsDetail = new OrderReturnsDetail();
        orderReturnsDetail.setOrderDetailId(orderDetail.id);
        orderReturnsDetail.setProductPrice(orderDetail.productPrice);
        orderReturnsDetail.setOldPrice(orderDetail.oldPrice);
        orderReturnsDetail.setCreateTime(currentTime);
        orderReturnsDetail.setProductName(orderDetail.productName);
        orderReturnsDetail.setSkuName(orderDetail.skuName);
        orderReturnsDetail.setReturnAmount(returnNumber);
        orderReturnsDetail.setProductImgUrl(orderDetail.productImgUrl);
        return orderReturnsDetail;
    }

    /**
     * @api {POST} /v1/o/calc_part_return_money/ 03计算退货金额
     * @apiName calcReturnMoney
     * @apiGroup OrderReturn
     * @apiParam {long} orderId 订单ID
     * @apiParam {Array} list list
     * @apiParam {long} orderDetailId orderDetailId
     * @apiParam {long} returnNumber 退货数量
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Error 40001){int} code 40001 参数错误
     */
    @Transactional
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> calcReturnMoney(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            JsonNode node = request.body().asJson();
            if (null == node) return okCustomJson(CODE40001, "参数错误");
            long orderId = node.findPath("orderId").asLong();
            Order order = Order.find.byId(orderId);
            if (null == order) return okCustomJson(CODE40003, "该订单不存在");
            if (!node.has("list")) return okCustomJson(CODE40001, "请选择退货商品");
            ArrayNode list = (ArrayNode) node.findPath("list");
            if (list.size() < 1) return okCustomJson(CODE40001, "请选择退货商品");
            long favor = order.totalMoney - order.realPay;
            if (favor <= 0) favor = 0;
            long totalSubReturn = 0;
            long totalReturnNumber = 0;
            for (int i = 0; i < list.size(); i++) {
                JsonNode each = list.get(i);
                if (null != each) {
                    long orderDetailId = each.findPath("orderDetailId").asLong();
                    long returnNumber = each.findPath("returnNumber").asLong();
                    if (returnNumber < 1) {
                        businessUtils.unLock(String.valueOf(uid), OPERATION_APPLY_ORDER_RETURN);
                        return okCustomJson(CODE40001, "请选择退货数量");
                    }
                    if (orderDetailId < 1) {
                        businessUtils.unLock(String.valueOf(uid), OPERATION_APPLY_ORDER_RETURN);
                        return okCustomJson(CODE40001, "参数错误");
                    }
                    OrderDetail orderDetail = OrderDetail.find.byId(orderDetailId);
                    if (null == orderDetail) {
                        businessUtils.unLock(String.valueOf(uid), OPERATION_APPLY_ORDER_RETURN);
                        return okCustomJson(CODE40003, "该订单不存在");
                    }
                    totalReturnNumber = totalReturnNumber + returnNumber;
                    long subReturn = orderDetail.productPrice * returnNumber;
                    totalSubReturn = totalSubReturn + subReturn;
                }
            }
            if ((order.totalReturnNumber + totalReturnNumber) >= order.productCount) {
                totalSubReturn = totalSubReturn + order.logisticsFee;
            }
            ObjectNode result = Json.newObject();
            result.put("totalMoney", order.totalMoney);
            result.put("realPay", order.realPay);
            result.put("favor", favor);
            result.put("returnMoney", totalSubReturn);
            result.put("totalSubReturn", totalSubReturn);
            result.put(CODE, CODE200);
            return ok(result);
        });
    }


    private void handlePostServiceTime(OrderReturns returnsApply, long currentTime) {
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
    }


    /**
     * @api {POST} /v1/o/handle_order_return/ 05处理退货
     * @apiName handleOrderReturn
     * @apiGroup OrderReturn
     * @apiParam {long} returnOrderId  退货id
     * @apiParam {int} returnOrderStatus 20同意退货 -2拒绝退货
     * @apiParam {String} auditReason 拒绝原因
     * @apiParam {String} auditRemark　拒绝备注
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Error 40001) {int} code 40001 参数错误
     * @apiSuccess (Error 40002) {int} code 40002 该退货不存在
     * @apiSuccess (Error 40004) {int} code 40004 该退货申请不是处于可同意退货的状态
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public CompletionStage<Result> handleOrderReturn(Http.Request request) {
        JsonNode requestNode = request.body().asJson();
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            if (null == requestNode) return okCustomJson(CODE40001, "参数错误");
            long returnOrderId = requestNode.findPath("returnOrderId").asLong();
            int returnOrderStatus = requestNode.findPath("returnOrderStatus").asInt();
            String auditReason = requestNode.findPath("auditReason").asText();
            String auditRemark = requestNode.findPath("auditRemark").asText();
            if (returnOrderStatus != OrderReturns.STATUS_RETURN_TO_DELIVERY_BACK
                    && returnOrderStatus != OrderReturns.STATUS_REJECT
            ) return okCustomJson(CODE40001, "状态有错");
            if (returnOrderId < 1) return okCustomJson(CODE40001, "参数错误");
            OrderReturns returnOrder = OrderReturns.find.byId(returnOrderId);
            if (null == returnOrder) return okCustomJson(CODE40002, "该退货不存在");
            Order order = Order.find.byId(returnOrder.orderId);
            if (null == order) return okCustomJson(CODE40001, "订单不存在");
            if (member.shopId < 1 || order.shopId != member.shopId)
                return okCustomJson(CODE40003, "无权操作该订单");
            String operatorName = businessUtils.getMemberName(member);

            returnOrder.setStatus(returnOrderStatus);
            returnOrder.setOperatorId(member.id);
            returnOrder.setOperatorName(operatorName);
            returnOrder.setUpdateTime(dateUtils.getCurrentTimeBySecond());
            if (returnOrderStatus == OrderReturns.STATUS_RETURN_TO_DELIVERY_BACK) {
                String consigneeRealname = "";
                String consigneePhoneNumber = "";
                String consigneeAddress = "";
                String consigneePostcode = "";
                ReturnContactDetail contactDetail = ReturnContactDetail.find.query().where().eq("shopId", order.shopId)
                        .setMaxRows(1).findOne();
                if (null != contactDetail) {
                    consigneeRealname = contactDetail.name;
                    consigneePhoneNumber = contactDetail.telephone;
                    consigneeAddress = contactDetail.province + contactDetail.city + contactDetail.area + contactDetail.details;
                    consigneePostcode = contactDetail.postcode;
                }
                returnOrder.setOrderNo(returnOrder.orderNo);
                returnOrder.setUid(returnOrder.uid);
                returnOrder.setReturnsNo(returnOrder.returnsNo);
                returnOrder.setHandlingWay("");
                returnOrder.setOperatorId(member.id);
                returnOrder.setOperatorName(operatorName);
                returnOrder.setPreStatus(returnOrder.status);
                returnOrder.setStatus(STATUS_RETURN_TO_DELIVERY_BACK);
                returnOrder.setConsigneeRealname(consigneeRealname);
                returnOrder.setConsigneePhoneNumber(consigneePhoneNumber);
                returnOrder.setConsigneeAddress(consigneeAddress);
                returnOrder.setConsigneePostcode(consigneePostcode);
                long currentTime = dateUtils.getCurrentTimeBySecond();
                long maxTime = DEFAULT_MAX_TIME_FOR_RETURN_GOODS;
                String maxTimeStr = businessUtils.getConfigValue(PARAM_KEY_MAX_TIME_FOR_RETURN_GOODS);
                if (!ValidationUtil.isEmpty(maxTimeStr)) maxTime = Long.parseLong(maxTimeStr);
                returnOrder.setReturnSubmitEndTime(currentTime + maxTime);
                if (returnOrder.state == STATE_RETURN_REFUND) {
                    saveOrderReturnDeliveryForm(order, returnOrder, member);
                }
            } else {
                returnOrder.setAuditReason(auditReason);
                returnOrder.setAuditRemark(auditRemark);
                returnOrder.setStatus(OrderReturns.STATUS_REJECT);
                updateOrderReturnNumbersForReject(order, returnOrder);
                //subtract return number
                cancelOrderReturnsDetail(returnOrderId, false);
            }
            returnOrder.setHandlingApplyEndTime(dateUtils.getCurrentTimeBySecond());
            returnOrder.save();
            return okJSON200();
        });
    }

    private void saveOrderReturnRefundForm(Order order, OrderReturns orderReturns, Member operator) {
        OrderReturnRefundForm refundForm = new OrderReturnRefundForm();
        refundForm.setRefundNo("RF" + IdGenerator.getId());
        refundForm.setReturnApplyId(orderReturns.id);
        refundForm.setReturnNo(orderReturns.returnsNo);
        refundForm.setOrderId(order.id);
        refundForm.setOrderNo(order.orderNo);
        refundForm.setUid(order.uid);
        refundForm.setUserName(order.userName);
        refundForm.setOperatorId(operator.id);
        refundForm.setOperatorName(businessUtils.getMemberName(operator));
        refundForm.setRefundMoney(orderReturns.returnMoney);
        refundForm.setRefundMethod(order.payMethod);
        refundForm.setStatus(STATUS_AGREE_REFUND);
        refundForm.setPayMethod(order.payMethod);
        refundForm.setTxData("");
        long currentTime = dateUtils.getCurrentTimeBySecond();
        refundForm.setUpdateTime(currentTime);
        refundForm.setCreateTime(currentTime);
        refundForm.save();
    }

    private OrderReturnDeliveryForm saveOrderReturnDeliveryForm(Order order, OrderReturns orderReturns, Member operator) {
        OrderReturnDeliveryForm returnDeliveryForm = new OrderReturnDeliveryForm();
        returnDeliveryForm.setDeliveryNo("RD" + IdGenerator.getId());
        returnDeliveryForm.setReturnApplyId(orderReturns.id);
        returnDeliveryForm.setReturnNo(orderReturns.returnsNo);
        returnDeliveryForm.setOrderId(order.id);
        returnDeliveryForm.setOrderNo(order.orderNo);
        returnDeliveryForm.setUid(order.uid);
        returnDeliveryForm.setUserName(order.userName);
        returnDeliveryForm.setOperatorId(operator.id);
        returnDeliveryForm.setOperatorName(businessUtils.getMemberName(operator));
        returnDeliveryForm.setExpressNo("");
        returnDeliveryForm.setExpressCompany("");
        returnDeliveryForm.setContactName("");
        returnDeliveryForm.setContactAddress("");
        returnDeliveryForm.setContactPhoneNumber("");
        long currentTime = dateUtils.getCurrentTimeBySecond();
        returnDeliveryForm.setUpdateTime(currentTime);
        returnDeliveryForm.setCreateTime(currentTime);
        returnDeliveryForm.save();
        return returnDeliveryForm;
    }

    private void cancelOrderReturnsDetail(long returnOrderId, boolean hasRefund) {
        List<OrderReturnsDetail> orderReturnsDetailList = OrderReturnsDetail.find.query().where()
                .eq("returnId", returnOrderId)
                .findList();
        List<OrderDetail> orderDetailList = new ArrayList<>();
        if (orderReturnsDetailList.size() > 0) {
            orderReturnsDetailList.parallelStream().forEach((each) -> {
                OrderDetail orderDetail = OrderDetail.find.byId(each.orderDetailId);
                if (null != orderDetail) {
                    if (!hasRefund) {
                        long leftNumber = orderDetail.returnNumber - each.returnAmount;
                        if (leftNumber < 1) leftNumber = 0;
                        orderDetail.setReturnNumber(leftNumber);
                        orderDetail.setSubReturn(leftNumber * orderDetail.productPrice);
                    }
                    orderDetail.setReturnStatus(POST_SERVICE_STATUS_NO);
                    orderDetailList.add(orderDetail);
                }
            });
            DB.saveAll(orderDetailList);
        }
    }

    /**
     * @api {POST} /v1/o/return_order_logis/ 06填写退货物流
     * @apiName fillOutLogisForReturnOrder
     * @apiGroup OrderReturn
     * @apiParam {String} returnsNo 退货ID
     * @apiParam {String} logisNo 物流单号
     * @apiParam {String} [logisName] 物流公司
     * @apiParam {String} note 备注
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Error 40001){int} code 40001 参数错误
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public CompletionStage<Result> fillOutLogisForReturnOrder(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            JsonNode requestNode = request.body().asJson();
            String logisNo = requestNode.findPath("logisNo").asText();
            String returnsNo = requestNode.findPath("returnsNo").asText();
            String logisName = requestNode.findPath("logisName").asText();
            String note = requestNode.findPath("note").asText();
            if (ValidationUtil.isEmpty(returnsNo)) return okCustomJson(CODE40001, "请填写退货单号");
            if (ValidationUtil.isEmpty(logisNo)) return okCustomJson(CODE40001, "请填写物流单号");

            OrderReturns returnOrder = OrderReturns.find.query().where()
                    .eq("returnsNo", returnsNo)
                    .eq("uid", uid)
                    .orderBy().desc("id")
                    .setMaxRows(1).findOne();
            if (null == returnOrder) return okCustomJson(CODE40002, "该退货单不存在");
            if (returnOrder.status != STATUS_RETURN_TO_DELIVERY_BACK) return okCustomJson(CODE40003, "该售后单不是处于填写物流的流程");

            Order order = Order.find.query().where()
                    .eq("id", returnOrder.orderId)
                    .setMaxRows(1).findOne();
            if (null == order) return okCustomJson(CODE40002, "订单不存在");
            try {
                DB.beginTransaction();
                returnOrder.setLogisName(logisName);
                returnOrder.setExpressNo(logisNo);
                returnOrder.setUpdateTime(dateUtils.getCurrentTimeBySecond());
                returnOrder.setPreStatus(returnOrder.status);
                returnOrder.setStatus(OrderReturns.STATUS_RETURN_TO_REFUND);
                returnOrder.setRemark(note);
                long maxTime = DEFAULT_MAX_TIME_FOR_REFUND;
                String maxTimeStr = businessUtils.getConfigValue(PARAM_KEY_MAX_TIME_FOR_REFUND);
                if (!ValidationUtil.isEmpty(maxTimeStr)) maxTime = Long.parseLong(maxTimeStr);
                long currentTime = dateUtils.getCurrentTimeBySecond();
                returnOrder.setHandlingRefundEndTime(currentTime + maxTime);
                returnOrder.save();
                OrderReturnDeliveryForm returnDeliveryForm = OrderReturnDeliveryForm.find.query()
                        .where().eq("returnApplyId", returnOrder.id)
                        .orderBy().asc("id")
                        .setMaxRows(1)
                        .findOne();
                if (null == returnDeliveryForm) {
                    returnDeliveryForm = saveOrderReturnDeliveryForm(order, returnOrder, member);
                }
                returnDeliveryForm.setExpressNo(logisNo);
                returnDeliveryForm.setExpressCompany(logisName);
//                returnDeliveryForm.setContactName("");
//                returnDeliveryForm.setContactAddress("");
//                returnDeliveryForm.setContactPhoneNumber("");
                returnDeliveryForm.save();
                DB.commitTransaction();
            } finally {
                DB.endTransaction();
            }
            return okJSON200();
        });
    }

    /**
     * @api {POST} /v1/o/handle_order_refund/ 07处理退款
     * @apiName handleOrderRefund
     * @apiGroup OrderReturn
     * @apiParam {long} returnOrderId  退货id
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Error 40001) {int} code 40001 参数错误
     * @apiSuccess (Error 40002) {int} code 40002 该退货不存在
     * @apiSuccess (Error 40004) {int} code 40004 该退货申请不是处于可同意退货的状态
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public CompletionStage<Result> handleOrderRefund(Http.Request request) {
        JsonNode requestNode = request.body().asJson();
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            if (null == requestNode) return okCustomJson(CODE40001, "参数错误");
            long returnOrderId = requestNode.findPath("returnOrderId").asLong();
            if (returnOrderId < 1) return okCustomJson(CODE40001, "参数错误");
            OrderReturns returnOrder = OrderReturns.find.byId(returnOrderId);
            if (null == returnOrder) return okCustomJson(CODE40002, "该退货不存在");
            if (returnOrder.status >= OrderReturns.STATUS_AGREE_REFUND) return okCustomJson(CODE40001, "已执行退款,不可重复操作");
            Order order = Order.find.byId(returnOrder.orderId);
            if (null == order) return okCustomJson(CODE40001, "订单不存在");
            if (order.uid != member.id && order.shopId != member.shopId)
                return okCustomJson(CODE40003, "无权操作该订单");
            String operatorName = businessUtils.getMemberName(member);
            try {
                DB.beginTransaction();
                long currentTime = dateUtils.getCurrentTimeBySecond();
                returnOrder.setOperatorId(member.id);
                returnOrder.setOperatorName(operatorName);
                returnOrder.setUpdateTime(currentTime);
                returnOrder.setOperatorId(member.id);
                returnOrder.setOperatorName(operatorName);
                returnOrder.setPreStatus(returnOrder.status);
                //change to order detail status
                cancelOrderReturnsDetail(returnOrderId, true);
                //return award
                List<DealerAward> dealerAwardList = DealerAward.find.query().where().eq("orderId", order.id)
                        .orderBy().asc("id")
                        .findList();
                dealerAwardList.parallelStream().forEach((dealerAward) -> {
                    dealerAward.setStatus(DealerAward.STATUS_CANCELED);
                    dealerAward.save();
                });
                balanceUtils.subtractScoreGave(order, member.id);
                returnOrder.setStatus(OrderReturns.STATUS_REFUND);
                returnOrder.setHandlingRefundTime(dateUtils.getCurrentTimeBySecond());
                returnOrder.save();
                order.setPostServiceStatus(POST_SERVICE_STATUS_REFUND);
                order.setUpdateTime(currentTime);
                if (order.totalReturnNumber >= order.productCount) {
                    order.setStatus(Order.ORDER_STATUS_SYSTEM_REFUNDED);
                }
                order.save();
                saveOrderReturnRefundForm(order, returnOrder, member);
                setOrderRefund(order, returnOrder, member.realName + member.id);
                //扣除赠送的积分
                if (order.scoreGave > 0) subtractScoreGave(order);
                updateMemberSalesOverview(order);
                String refundTxNo = refundMoneyForOrderReturns(returnOrder, "同意退款", "" + order.id, returnOrder.returnMoney, order);
                if (!ValidationUtil.isEmpty(refundTxNo)) {
                    updateOrderRefundForm(returnOrder, refundTxNo);
                }
                DB.commitTransaction();
            } finally {
                DB.endTransaction();
            }
            return okJSON200();
        });
    }

    private void updateOrderRefundForm(OrderReturns returnOrder, String refundTxNo) {
        OrderReturnRefundForm orderReturnRefundForm = OrderReturnRefundForm.find.query().where()
                .eq("returnApplyId", returnOrder.id)
                .orderBy().asc("id")
                .setMaxRows(1)
                .findOne();
        if (null != orderReturnRefundForm) {
            orderReturnRefundForm.setStatus(STATUS_REFUND);
            orderReturnRefundForm.setTxData(refundTxNo);
            orderReturnRefundForm.save();
        }
    }

    /**
     * @api {POST} /v1/o/reject_order_refund/ 08驳回退款请求
     * @apiName rejectOrderRefund
     * @apiGroup OrderReturn
     * @apiParam {long} returnOrderId  退货id
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Error 40001) {int} code 40001 参数错误
     * @apiSuccess (Error 40002) {int} code 40002 该退货不存在
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public CompletionStage<Result> rejectOrderRefund(Http.Request request) {
        JsonNode requestNode = request.body().asJson();
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            if (null == requestNode) return okCustomJson(CODE40001, "参数错误");
            long returnOrderId = requestNode.findPath("returnOrderId").asLong();
            if (returnOrderId < 1) return okCustomJson(CODE40001, "参数错误");
            OrderReturns returnOrder = OrderReturns.find.byId(returnOrderId);
            if (null == returnOrder) return okCustomJson(CODE40002, "该退货不存在");
            if (returnOrder.status >= OrderReturns.STATUS_AGREE_REFUND)
                return okCustomJson(CODE40001, "已执行退款,不可驳回退款请求");
            Order order = Order.find.byId(returnOrder.orderId);
            if (null == order) return okCustomJson(CODE40001, "订单不存在");
            if (order.shopId != member.shopId) return okCustomJson(CODE40003, "无权操作该订单");
            String operatorName = businessUtils.getMemberName(member);
            try {
                DB.beginTransaction();
                returnOrder.setStatus(STATUS_REJECT_REFUND);
                returnOrder.setOperatorId(member.id);
                returnOrder.setOperatorName(operatorName);
                returnOrder.setUpdateTime(dateUtils.getCurrentTimeBySecond());
                order.setPostServiceStatus(POST_SERVICE_STATUS_NO);
                updateOrderReturnNumbersForReject(order, returnOrder);
                cancelOrderReturnsDetail(returnOrderId, false);
                returnOrder.setStatus(STATUS_REJECT_REFUND);
                returnOrder.setHandlingRefundTime(dateUtils.getCurrentTimeBySecond());
                returnOrder.save();
                DB.commitTransaction();
            } finally {
                DB.endTransaction();
            }
            return okJSON200();
        });
    }

    /**
     * @api {GET} /v1/o/return_orders_lists/  08我的退货列表
     * @apiName listReturnOrders
     * @apiGroup OrderReturn
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Success 200){int} pages 页数
     * @apiSuccess (Success 200){JsonArray} list 订单列表
     * @apiSuccess (Success 200){String} returnsNo 退货单号
     * @apiSuccess (Success 200){String} expressNo 快递单号
     * @apiSuccess (Success 200){String} logisName 物流公司
     * @apiSuccess (Success 200){String} logisticsLastDesc 物流最后状态描述
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
     * @apiSuccess (Success 200){int} status  0未付款,1已付款,2已发货,3已签收,-1退货申请,-2退货中,-3已退货,-4取消交易 -5撤销申请
     */
    public CompletionStage<Result> listReturnOrders(Http.Request request, int page) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            PagedList<OrderReturns> pagedList = OrderReturns.find.query().where()
                    .eq("uid", uid)
                    .orderBy().desc("id")
                    .setFirstRow((page - 1) * PAGE_SIZE_10)
                    .setMaxRows(PAGE_SIZE_10)
                    .findPagedList();
            List<OrderReturns> list = pagedList.getList();
            list.parallelStream().forEach((each) -> {
                each.setOperatorId(0);
                each.setOperatorName("");
                Order order = Order.find.byId(each.orderId);
                if (null != order) {
                    each.order = order;
                    List<OrderDetail> orderDetails = OrderDetail.find.query().where().eq("orderId", order.id)
                            .orderBy().asc("id").findList();
                    each.order.detailList.addAll(orderDetails);
                }
                setOrderReturnsDetailList(each);
            });
            ObjectNode resultNode = Json.newObject();
            resultNode.put(CODE, CODE200);
            resultNode.put("pages", pagedList.getTotalPageCount());
            boolean hasNext = pagedList.hasNext();
            resultNode.put("hasNext", hasNext);
            resultNode.set("list", Json.toJson(list));
            return ok(resultNode);
        });
    }

    /**
     * @api {GET} /v1/o/shop_return_orders/  09店铺退货列表
     * @apiName listShopReturnOrders
     * @apiGroup OrderReturn
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Success 200){int} pages 页数
     * @apiSuccess (Success 200){JsonArray} list 订单列表
     * @apiSuccess (Success 200){String} returnsNo 退货单号
     * @apiSuccess (Success 200){String} expressNo 快递单号
     * @apiSuccess (Success 200){String} logisName 物流公司
     * @apiSuccess (Success 200){String} logisticsLastDesc 物流最后状态描述
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
     * @apiSuccess (Success 200){int} status  0未付款,1已付款,2已发货,3已签收,-1退货申请,-2退货中,-3已退货,-4取消交易 -5撤销申请
     */
    public CompletionStage<Result> listShopReturnOrders(Http.Request request, int page) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            PagedList<OrderReturns> pagedList = OrderReturns.find.query().where()
                    .eq("shopId", member.shopId)
                    .orderBy().desc("id")
                    .setFirstRow((page - 1) * PAGE_SIZE_10)
                    .setMaxRows(PAGE_SIZE_10)
                    .findPagedList();
            List<OrderReturns> list = pagedList.getList();
            list.parallelStream().forEach((each) -> {
                Order order = Order.find.byId(each.orderId);
                if (null != order) {
                    each.order = order;
                    List<OrderDetail> orderDetails = OrderDetail.find.query().where().eq("orderId", order.id)
                            .orderBy().asc("id").findList();
                    each.order.detailList.addAll(orderDetails);
                }
                setOrderReturnsDetailList(each);
            });
            ObjectNode resultNode = Json.newObject();
            resultNode.put(CODE, CODE200);
            resultNode.put("pages", pagedList.getTotalPageCount());
            boolean hasNext = pagedList.hasNext();
            resultNode.put("hasNext", hasNext);
            resultNode.set("list", Json.toJson(list));
            return ok(resultNode);
        });
    }

    /**
     * @api {GET} /v1/o/return_orders_list/:returnOrderId/  10退货单详情
     * @apiName getReturnOrderDetail
     * @apiGroup OrderReturn
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Success 200){String} returnsNo 退货单号
     * @apiSuccess (Success 200){String} expressNo 快递单号
     * @apiSuccess (Success 200){String} logisName 物流公司
     * @apiSuccess (Success 200){String} logisticsLastDesc 物流最后状态描述
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
     * @apiSuccess (Success 200){long} returnSubmitTime 申请时间
     * @apiSuccess (Success 200){long} handlingReturnEndTime 售后申请最长处理时间
     * @apiSuccess (Success 200){long} returnSubmitEndTime 退货最长寄回时间
     * @apiSuccess (Success 200){long} handlingRefundTime 退款时间
     * @apiSuccess (Success 200){long} handlingRefundEndTime 退款最长处理时间
     * @apiSuccess (Success 200){int} status -3拒绝退款  -2 申请被驳回  -1取消退货  10退货退款待审核  20待退回  30退款待审核  40已退款  200处理完毕
     * @apiSuccess (Success 200){int} state  1仅退款 2退货退款
     */
    public CompletionStage<Result> getReturnOrderDetail(Http.Request request, long returnOrderId) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            if (returnOrderId < 1) return okCustomJson(CODE40001, "参数错误");
            OrderReturns returnOrder = OrderReturns.find.byId(returnOrderId);
            if (null == returnOrder) return okCustomJson(CODE40001, "该退货单不存在");
            setOrderReturnsDetailList(returnOrder);
            Order order = Order.find.byId(returnOrder.orderId);
            if (null != order) {
                returnOrder.order = order;
                List<OrderDetail> orderDetails = OrderDetail.find.query().where().eq("orderId", order.id)
                        .orderBy().asc("id").findList();
                returnOrder.setOperatorId(0);
                returnOrder.setOperatorName("");
                returnOrder.order.detailList.addAll(orderDetails);
            }
            List<OrderReturnsImage> imageList = OrderReturnsImage.find.query().where()
                    .eq("returnApplyId", returnOrder.id)
                    .orderBy().asc("id")
                    .findList();
            ObjectNode resultNode = (ObjectNode) Json.toJson(returnOrder);
            resultNode.put(CODE, CODE200);
            ReturnContactDetail contactDetail = ReturnContactDetail.find.query().where()
                    .eq("shopId", order.shopId)
                    .setMaxRows(1).findOne();
            if (null != contactDetail) {
                resultNode.set("contactDetail", Json.toJson(contactDetail));
            }
            resultNode.set("imgList", Json.toJson(imageList));
            return ok(resultNode);
        });

    }

    private void setOrderReturnsDetailList(OrderReturns each) {
        List<OrderReturnsDetail> orderReturnsDetailList = OrderReturnsDetail.find.query().where()
                .eq("returnId", each.id)
                .findList();
        each.orderReturnsDetailList.addAll(orderReturnsDetailList);
    }

}
