package actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ebean.DB;
import models.order.Order;
import models.order.OrderDetail;
import models.product.ProductSku;
import models.user.ContactDetail;
import models.user.Member;
import models.user.MemberBalance;
import play.libs.Json;
import utils.*;

import javax.inject.Inject;

import static constants.BusinessConstant.TRANSACTION_TYPE_PLACE_ORDER;
import static constants.BusinessConstant.TYPE_PLACE_ORDER;
import static models.order.Order.*;

public class ExchangeActor extends AbstractLoggingActor {
    public static Props getProps() {
        return Props.create(ExchangeActor.class);
    }

    @Inject
    DateUtils dateUtils;

    @Inject
    BizUtils bizUtils;

    @Inject
    BalanceUtils balanceUtils;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ActorProtocol.EXCHANGE.class, param -> exchange(param))
                .build();
    }

    public ObjectNode okCustomJson(int code, String reason) {
        ObjectNode node = Json.newObject();
        node.put("code", code);
        node.put("reason", reason);
        return node;
    }

    private void exchange(ActorProtocol.EXCHANGE orderParam) {
        int status = ORDER_STATUS_UNPAY;
        OrderParam param = orderParam.orderParam;
        Member member = orderParam.member;
        if (null == param || null == member || null == orderParam.orderDetail) {
            ObjectNode node = okCustomJson(40001, "参数错误");
            sender().tell(node, self());
            return;
        }
        Order order = null;
        try {
            long realPay = param.realPayMoney;
            long totalMoney = param.totalMoney;
            OrderDetail orderDetail = orderParam.orderDetail;
            ProductSku productSku = ProductSku.find.byId(orderDetail.productSkuId);
            if (null == productSku) {
                ObjectNode node = okCustomJson(40001, "该商品不存在");
                sender().tell(node, self());
                return;
            }
            orderDetail.setProductPrice(productSku.price);

            if (orderDetail.number > productSku.stock) {
                ObjectNode node = okCustomJson(40001, "下单数量超出库存数量");
                sender().tell(node, self());
                return;
            }
            if (productSku.limitAmount > 0) {
                if (orderDetail.number > productSku.limitAmount) {
                    ObjectNode node = okCustomJson(40001, "'" + productSku.name + "'" + "已达到限购数量,无法购买更多");
                    sender().tell(node, self());
                    return;
                }
            }
            param.activityType = ORDER_TYPE_SCORE;
            if (realPay < 0) realPay = 0;
            if (totalMoney < 0) totalMoney = 0;
            param.realPayMoney = realPay;
            param.totalMoney = totalMoney;
            DB.beginTransaction();
            order = saveOrder(param, status, member, param.shopName, param.mailFee);
            order.setOrderType(ORDER_TYPE_SCORE);
            order.setStatus(Order.ORDER_STATUS_TO_DELIEVERY);
            order.setPayTxNo(IdGenerator.getId());
            order.setPayTime(dateUtils.getCurrentTimeBySecond());
            order.setSubOrder(true);
            order.save();
            orderDetail.setOrderId(order.id);
            orderDetail.setShopId(order.shopId);
            orderDetail.save();

            BalanceParam balanceParam = new BalanceParam.Builder()
                    .itemId(BusinessItem.SCORE)
                    .changeAmount(-realPay)
                    .leftBalance(-realPay)
                    .totalBalance(-realPay)
                    .memberId(param.uid)
                    .desc("用户兑换成功，扣除积分")
                    .orderNo(order.orderNo)
                    .bizType(TRANSACTION_TYPE_PLACE_ORDER)
                    .build();
            balanceUtils.saveChangeBalance(balanceParam, true);
            bizUtils.updateMerchantStockByOrderDetail(orderDetail, TYPE_PLACE_ORDER);
            DB.commitTransaction();
        } catch (Exception e) {
            log().error("placeOrder:" + e.getMessage());
        } finally {
            DB.endTransaction();
        }
        ObjectNode result = Json.newObject();
        //扣积分
        result.put("code", 200);
        result.put("orderId", order.id);
        result.put("orderNo", order.orderNo);
        result.put("status", status);
        result.put("useScore", order.realPay);
        MemberBalance balance = MemberBalance.find.query().where()
                .eq("uid", param.uid)
                .eq("itemId", BusinessItem.SCORE)
                .setMaxRows(1).forUpdate()
                .findOne();
        result.put("leftScore", balance.leftBalance);
        sender().tell(result, self());
    }

    private Order saveOrder(OrderParam param, int status, Member member, String orderShops, long mailFee) {
        Order order = new Order();
        order.setUid(member.id);
        String name = member.realName;
        if (ValidationUtil.isEmpty(name)) name = member.nickName;
        if (ValidationUtil.isEmpty(name)) name = member.phoneNumber;
        order.setUserName(bizUtils.limit20(name));
        setOrderDealer(member, param, order);
        order.setOrderShops(orderShops);
        order.setDeliveryType(param.deliveryType);
        order.setActivityType(param.activityType);

        ContactDetail contactDetail = ContactDetail.find.byId(param.addressId);
        if (null != contactDetail) {
            order.setAddress(Json.stringify(Json.toJson(contactDetail)));
        }
        order.setOrderType(ORDER_TYPE_NORMAL);
        order.setRegionPath("");
        order.setOrderNo(dateUtils.getCurrentTimeWithHMS() + IdGenerator.getId());
        order.setOutTradeNo("");
        order.setRefundTxId("");
        order.setPayTxNo("");
        order.setStatus(status);
        order.setShopId(param.shopId);
        order.setShopName(param.shopName);
        order.setTotalMoney(param.totalMoney);
        order.setRealPay(param.realPayMoney);
        order.setCouponFree(param.couponFree);
        order.setPlatformCouponFree(param.platformCouponFree);
        order.setDiscountMoney(param.memberFavor);
        order.setScoreUse(param.useScore);
        order.setScoreToMoney((long) (param.useScore * bizUtils.getScoreMoneyScale()));
        order.setPostServiceStatus(POST_SERVICE_STATUS_NO);
        order.setProductCount(param.productAmount);
        order.setLogisticsFee(mailFee);
        order.setCommissionHandled(false);
        long currentTime = dateUtils.getCurrentTimeBySecond();
        order.setUpdateTime(currentTime);
        order.setCreateTime(currentTime);
        order.setMix(false);
        order.setCouponId(param.memberCouponId);

        order.setDescription(param.description);
        order.setTakeTime(0);
        order.setCountStatus(Order.ORDER_COUNT_STATUS_NO_COUNT);
        order.setMembershipFee(0);
        order.setFilter("");
        order.setFilter(Json.stringify(Json.toJson(order)));
        return order;
    }


    private void setOrderDealer(Member member, OrderParam param, Order order) {
        long dealerId = param.dealerId;
        if (dealerId > 0) {
            order.setDealerUid(member.dealerId);
            Member dealer = Member.find.byId(member.dealerId);
            if (null != dealer) order.setDealerName(member.dealerName);
        } else {
            order.setDealerUid(member.dealerId);
            order.setDealerName(member.dealerName);
        }
    }


}
