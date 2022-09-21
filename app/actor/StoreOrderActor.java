package actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ebean.DB;
import models.cart.ShoppingCart;
import models.shop.Shop;
import models.store.StoreOrder;
import models.store.StoreOrderDetail;
import models.user.ContactDetail;
import models.user.Member;
import models.user.MemberCardCoupon;
import models.user.MemberCoupon;
import play.libs.Json;
import utils.*;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static constants.BusinessConstant.OPERATION_PLACE_ORDER;
import static models.order.Order.*;

public class StoreOrderActor extends AbstractLoggingActor {
    public static Props getProps() {
        return Props.create(StoreOrderActor.class);
    }

    @Inject
    DateUtils dateUtils;

    @Inject
    BizUtils bizUtils;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ActorProtocol.PLACE_STORE_ORDER.class, orderParam -> placeOrder(orderParam))
                .build();
    }

    public ObjectNode okCustomJson(int code, String reason) {
        ObjectNode node = Json.newObject();
        node.put("code", code);
        node.put("reason", reason);
        return node;
    }

    private void placeOrder(ActorProtocol.PLACE_STORE_ORDER orderParam) {
        long currentTime = System.currentTimeMillis() / 1000;
        int status = ORDER_STATUS_UNPAY;
        OrderParam param = orderParam.orderParam;
        Member member = orderParam.member;
        if (null == param || null == member || null == orderParam.detailList) {
            ObjectNode node = okCustomJson(40001, "参数错误");
            sender().tell(node, self());
            return;
        }
        try {
            long realPay = param.realPayMoney;
            long totalMoney = param.totalMoney;
            if (param.useScore == 0 && param.realPayMoney == 0) status = ORDER_STATUS_PAID;
            if (realPay < 0) realPay = 0;
            if (totalMoney < 0) totalMoney = 0;
            param.realPayMoney = realPay;
            param.totalMoney = totalMoney;
            DB.beginTransaction();
            StoreOrder order = saveOrder(param, status, member, param.mailFee, orderParam.memberCardCouponId);
            order.setFilter("");
            order.setFilter(Json.stringify(Json.toJson(order)));
            order.save();
            MemberCoupon coupon = orderParam.memberCoupon;
            if (null != coupon) {
                coupon.setStatus(MemberCoupon.STATUS_USED);
                coupon.setUseTime(currentTime);
                coupon.save();
            }
            if (orderParam.memberCardCouponId > 0) {
                MemberCardCoupon memberCardCoupon = MemberCardCoupon.find.byId(orderParam.memberCardCouponId);
                if (null != memberCardCoupon) {
                    memberCardCoupon.setStatus(MemberCardCoupon.STATUS_USED);
                    memberCardCoupon.setUseTime(currentTime);
                    memberCardCoupon.save();
                }
            }
            if (null != orderParam.detailList && orderParam.detailList.size() > 0) {
                orderParam.detailList.forEach((each) -> {
                    each.setOrderId(order.id);
                    each.setStoreId(order.storeId);
                });
                DB.saveAll(orderParam.detailList);
                //将对应商品从购物车移除
                removeFromCart(param.uid, orderParam.detailList);
                //根据情况，这里要求线上下单不扣库存，由线下人工协调
//                if (!param.joinFlashSale) batchSubStock(orderParam.detailList);
            }
            DB.commitTransaction();
            ObjectNode result = Json.newObject();
            //扣积分
            result.put("code", 200);
            result.put("orderId", order.id);
            result.put("orderNo", order.orderNo);
            result.put("status", status);
            sender().tell(result, self());
        } catch (Exception e) {
            log().error("StoreOrderActor placeOrder:" + e.getMessage());
        } finally {
            DB.endTransaction();
            bizUtils.unLock(String.valueOf(member.id), OPERATION_PLACE_ORDER);
        }
    }


    private StoreOrder saveOrder(OrderParam param, int status, Member member,
                                 long mailFee, long memberCardCouponId) {
        StoreOrder order = new StoreOrder();
        order.setUid(member.id);
        String name = member.realName;
        if (ValidationUtil.isEmpty(name)) name = member.nickName;
        if (ValidationUtil.isEmpty(name)) name = member.phoneNumber;
        order.setUserName(bizUtils.limit20(name));
        order.setDeliveryType(param.deliveryType);
        ContactDetail contactDetail = ContactDetail.find.byId(param.addressId);
        if (null != contactDetail) {
            order.setAddress(Json.stringify(Json.toJson(contactDetail)));
        }
        order.setOrderType(ORDER_TYPE_NORMAL);
        order.setOrderNo(dateUtils.getCurrentTimeWithHMS() + IdGenerator.getId() + "S");
        order.setOutTradeNo("");
        order.setRefundTxId("");
        order.setPayTxNo("");
        order.setStatus(status);
        order.setTotalMoney(param.totalMoney);
        order.setRealPay(param.realPayMoney);
        order.setCouponFree(param.couponFree);
        long discountMoney = param.totalMoney - param.realPayMoney + order.logisticsFee;
        if (discountMoney <= 0) discountMoney = 0;
        order.setDiscountMoney(discountMoney);
        order.setScoreUse(param.useScore);
        order.setScoreToMoney((long) (param.useScore * bizUtils.getScoreMoneyScale()));
        order.setPostServiceStatus(POST_SERVICE_STATUS_NO);
        order.setProductCount(param.productAmount);
        order.setLogisticsFee(mailFee);
        order.setCommissionHandled(false);
        long currentTime = dateUtils.getCurrentTimeBySecond();
        order.setUpdateTime(currentTime);
        order.setCreateTime(currentTime);
        order.setCouponId(param.memberCouponId);
        order.setDescription(param.description);
        order.setTakeTime(0);
        order.setAccountSettle(0);
        order.setMemberCardCouponId(memberCardCouponId);
        order.setStoreId(param.shopId);
        order.setStoreName(param.shopName);
        return order;
    }


    private void removeFromCart(long uid, List<StoreOrderDetail> detailList) {
        Set<Long> skuIdSet = new HashSet<>();
        detailList.parallelStream().forEach((each) -> {
            skuIdSet.add(each.productSkuId);
        });
        List<ShoppingCart> shoppingCartList = ShoppingCart.find.query().where()
                .eq("uid", uid)
                .in("skuId", skuIdSet)
                .findList();
        if (shoppingCartList.size() > 0) DB.deleteAll(shoppingCartList);
    }

}
