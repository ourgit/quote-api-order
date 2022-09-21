package actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ebean.DB;
import models.enroll.EnrollConfig;
import models.enroll.EnrollContentUserInfo;
import models.order.Order;
import models.product.Product;
import models.product.ProductSku;
import models.promotion.Bargain;
import models.user.Member;
import play.libs.Json;
import utils.*;

import javax.inject.Inject;
import java.util.List;

import static constants.BusinessConstant.TRANSACTION_TYPE_SCORE_PAY_FOR_ORDER;
import static models.order.Order.*;

public class HandleOrderPaidActor extends AbstractLoggingActor {
    public static Props getProps() {
        return Props.create(HandleOrderPaidActor.class);
    }

    public static final String SMS_TEMPLATE = "【晴松】恭喜您已报名（***）成功，详情请打开小程序查看";
    @Inject
    DateUtils dateUtils;
    @Inject
    BizUtils bizUtils;
    @Inject
    BalanceUtils balanceUtils;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ActorProtocol.HANDLE_ORDER_PAID.class, param -> handleOrderPaid(param))
                .build();
    }

    public ObjectNode okCustomJson(int code, String reason) {
        ObjectNode node = Json.newObject();
        node.put("code", code);
        node.put("reason", reason);
        return node;
    }

    private void handleOrderPaid(ActorProtocol.HANDLE_ORDER_PAID param) {
        System.out.println("handleOrderPaid:" + param.toString());
        if (ValidationUtil.isEmpty(param.orderNo)) {
            log().error("param.orderNo empty");
        }
        Order order = Order.find.query().where()
                .eq("orderNo", param.orderNo)
                .orderBy().asc("id")
                .setMaxRows(1)
                .findOne();
        if (null == order) {
            log().error("handleOrderPaid error: can not find the order with orderNo:" + param.orderNo);
            return;
        }
        if (order.status > Order.ORDER_STATUS_PAID) {
            log().error("order.status > Order.ORDER_STATUS_PAID:" + param.orderNo);
            return;
        }
        try {
            DB.beginTransaction();
            int payType = param.payType;
            String payTxId = param.payTxId;
            order.setStatus(Order.ORDER_STATUS_TO_DELIEVERY);
            order.setPayMethod(payType);
            order.setPayTxNo(payTxId);
            order.setPayTime(System.currentTimeMillis() / 1000);
            if (!ValidationUtil.isEmpty(param.outTradeNo)) {
                order.setOutTradeNo(param.outTradeNo);
            }
            if (order.orderType == Order.ORDER_TYPE_ENROLL) {
                order.setStatus(Order.ORDER_STATUS_TAKEN);
            } else if (order.orderType == Order.ORDER_TYPE_SERVICE) {
                order.setStatus(Order.ORDER_STATUS_TO_TAKE);
            } else if (order.orderType == Order.ORDER_TYPE_MEMBERSHIP) {
                order.setStatus(Order.ORDER_STATUS_TAKEN);
            } else if (order.orderType == Order.ORDER_TYPE_THIRD_DISCOUNT) {
                order.setStatus(Order.ORDER_STATUS_ARRIVE_SELF_TAKEN_PLACE);
            }
            //check score use
            if (order.scoreUse > 0 && order.realPay <= 0) {
                //pay order succeed and need to sub score from freeze balance and totalbalance
                BalanceParam balanceParam = new BalanceParam.Builder()
                        .itemId(BusinessItem.SCORE)
                        .changeAmount(-order.scoreUse)
                        .freezeBalance(order.scoreUse)
                        .totalBalance(-order.scoreUse)
                        .memberId(order.uid)
                        .desc("订单支付成功，从冻结和总额中扣除需要使用的积分")
                        .bizType(TRANSACTION_TYPE_SCORE_PAY_FOR_ORDER)
                        .build();
                balanceUtils.saveChangeBalance(balanceParam, true);
            }
            //添加积分
            long score = bizUtils.giveCreditScoreForOrder(order);
            order.setScoreGave(score);
            order.save();
            List<Order> subOrderList = Order.find.query().where().eq("parentId", order.id).findList();
            if (subOrderList.size() > 0) {
                subOrderList.forEach((each) -> {
                    each.setStatus(order.status);
                });
                DB.saveAll(subOrderList);
            }
            if (order.activityType == ORDER_ACTIVITY_TYPE_BARGAIN) {
                Bargain bargain = Bargain.find.query().where().eq("orderId", order.id)
                        .orderBy().desc("id")
                        .setMaxRows(1)
                        .findOne();
                if (null != bargain) {
                    bargain.setStatus(Bargain.STATUS_PAID);
                    bargain.setPayNo(order.payTxNo);
                    bargain.save();
                }
            }
            bizUtils.handleOtherAfterOrderPaid(order);
            DB.commitTransaction();
        } catch (Exception e) {
            log().error("handleOrderPaid:" + e.getMessage());
        } finally {
            DB.endTransaction();
        }
    }


}
