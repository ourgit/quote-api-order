package actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.ebean.DB;
import models.groupon.GroupProduct;
import models.groupon.GroupProductStat;
import models.groupon.Groupon;
import models.order.GroupOrder;
import models.order.Order;
import models.user.Member;
import play.cache.SyncCacheApi;
import play.libs.Json;
import utils.BizUtils;
import utils.CacheUtils;
import utils.ValidationUtil;

import javax.inject.Inject;

/**
 * Created by win7 on 2016/7/14.
 */
public class GroupOrderActor extends AbstractLoggingActor {
    public static Props getProps() {
        return Props.create(GroupOrderActor.class);
    }

    @Inject
    BizUtils bizUtils;

    @Inject
    protected CacheUtils cacheUtils;

    @Inject
    protected SyncCacheApi cache;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ActorProtocol.PLACE_ORDER_GROUP_BY_PAID.class, orderParam -> handleGroupOrderPaid(orderParam.orderNo, orderParam.txId))
                .build();
    }

    private void handleGroupOrderPaid(String orderNo, String txId) {
        System.out.println("handleOrderPaid:" + orderNo + "======" + txId);
        if (ValidationUtil.isEmpty(orderNo)) {
            log().error("param.orderNo empty");
        }
        GroupOrder order = GroupOrder.find.query().where()
                .eq("orderNo", orderNo)
                .orderBy().asc("id")
                .setMaxRows(1)
                .findOne();
        if (null == order) {
            log().error("handleGroupOrderPaid error: can not find the order with orderNo:" + orderNo);
            return;
        }
        if (order.status >= Order.ORDER_STATUS_PAID) {
            log().error("order.status > Order.ORDER_STATUS_PAID:" + orderNo);
            return;
        }
        try {
            DB.beginTransaction();
            order.setStatus(GroupOrder.ORDER_STATUS_PAID);
            order.setAccountSettle(GroupOrder.ACCOUNT_SETTLE_NEED);
            order.setPayTxNo(txId);
            order.setPayTime(System.currentTimeMillis() / 1000);
            order.save();
            handleGrouponOrder(order);
            updateProductStat(order.productId);
            DB.commitTransaction();
        } catch (Exception e) {
            log().error("handleOrderPaid:" + e.getMessage());
        } finally {
            DB.endTransaction();
        }
    }

    private void updateProductStat(long productId) {
        GroupProductStat stat = GroupProductStat.find.query().where()
                .eq("productId", productId)
                .setMaxRows(1)
                .orderBy().asc("id")
                .findOne();
        long currentTime = System.currentTimeMillis() / 1000;
        if (null != stat) {
            stat.setOrders(stat.orders + 1);
            stat.setUpdateTime(currentTime);
            stat.save();
        }
    }

    private void handleGrouponOrder(GroupOrder order) {
        GroupProduct product = GroupProduct.find.byId(order.productId);
        if (null != product) {
            product.setSoldAmount(product.soldAmount + 1);
            product.save();
            String key = cacheUtils.getGroupProductJsonCacheKey(order.productId);
            cache.set(key, product, 4 * 3600);
        }
        if (order.grouponId > 0) {
            Groupon existGroupon = Groupon.find.byId(order.grouponId);
            if (null != existGroupon) {
                long orders = existGroupon.orders + 1;
                Member member = Member.find.byId(order.uid);
                if (null != member) {
                    ArrayNode nodes = Json.newArray();
                    if (!ValidationUtil.isEmpty(existGroupon.avatars)) {
                        nodes = (ArrayNode) Json.parse(existGroupon.avatars);
                    }
                    nodes.add(member.avatar);
                    existGroupon.setAvatars(Json.stringify(nodes));
                    String name = member.realName;
                    if (ValidationUtil.isEmpty(name)) name = member.nickName;
                    if (ValidationUtil.isEmpty(name)) name = bizUtils.hidepartialChar(member.phoneNumber);
                    existGroupon.setUsers(existGroupon.users + "," + name);
                    existGroupon.setUidList(existGroupon.uidList + member.id + "/");
                    String[] orderUids = existGroupon.uidList.trim().split("/");
                    orders = orderUids.length - 1;
                }
                existGroupon.setOrders(orders);
                existGroupon.save();
            }

        } else {
            Groupon groupon = new Groupon();
            groupon.setLauncherUid(order.uid);
            groupon.setProductId(order.productId);
            groupon.setProductName(order.productName);
            long limitTime = 60;
            long requireOrders = 2;
            if (null != product) {
                limitTime = product.requireTime;
                requireOrders = product.requireUsers;
            }
            long currentTime = System.currentTimeMillis() / 1000;
            groupon.setExpireTime(currentTime + limitTime * 60);
            groupon.setRequireOrders(requireOrders);
            groupon.setOrders(1);
            Member member = Member.find.byId(order.uid);
            if (null != member) {
                groupon.setAvatars(Json.stringify(Json.newArray().add(member.avatar)));
                String name = member.realName;
                if (ValidationUtil.isEmpty(name)) name = member.nickName;
                if (ValidationUtil.isEmpty(name)) name = bizUtils.hidepartialChar(member.phoneNumber);
                groupon.setUsers(name);
                groupon.setUidList("/" + member.id + "/");
            }
            groupon.setStatus(Groupon.STATUS_PROCESSING);
            groupon.setCreateTime(currentTime);
            groupon.save();
            order.setGrouponStatus(Groupon.STATUS_PROCESSING);
            order.setGrouponId(groupon.id);
            order.setUpdateTime(currentTime);
        }
        order.setCommission(product.commission);
        order.save();
    }
}
