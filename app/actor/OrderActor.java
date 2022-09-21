package actor;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ebean.DB;
import models.cart.ShoppingCart;
import models.order.Order;
import models.order.OrderDetail;
import models.product.MailFee;
import models.product.Product;
import models.product.ProductLevelPrice;
import models.product.ProductSku;
import models.shop.Shop;
import models.user.ContactDetail;
import models.user.Member;
import models.user.MemberBalance;
import play.libs.Json;
import utils.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static constants.BusinessConstant.*;
import static models.order.Order.*;

public class OrderActor extends AbstractLoggingActor {
    public static Props getProps() {
        return Props.create(OrderActor.class);
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
                .match(ActorProtocol.PLACE_ORDER.class, orderParam -> placeOrder(orderParam))
                .match(ActorProtocol.EXCHANGE.class, param -> exchange(param))
                .build();
    }

    public ObjectNode okCustomJson(int code, String reason) {
        ObjectNode node = Json.newObject();
        node.put("code", code);
        node.put("reason", reason);
        return node;
    }

    private void placeOrder(ActorProtocol.PLACE_ORDER orderParam) {
        long currentTime = System.currentTimeMillis() / 1000;
        int status = ORDER_STATUS_UNPAY;
        Member member = orderParam.member;
        JsonNode param = orderParam.paramNode;
        Map<Long, Product> productMap = orderParam.productMap;
        if (null == param || null == member) {
            ObjectNode node = okCustomJson(40001, "参数错误");
            sender().tell(node, self());
            return;
        }
        ArrayNode nodes = (ArrayNode) param.findPath("list");
        long platformCouponId = param.findPath("platformCouponId").asLong();
        long addressId = param.findPath("addressId").asLong();
        String dealerCode = param.findPath("dealerCode").asText();
        long dealerUid = param.findPath("dealerUid").asLong();

        if (addressId < 1) {
            ObjectNode node = okCustomJson(40001, "请选择收货地址");
            sender().tell(node, self());
            return;
        }
        ContactDetail contactDetail = ContactDetail.find.byId(addressId);
        if (null == contactDetail) {
            ObjectNode node = okCustomJson(40001, "请选择收货地址");
            sender().tell(node, self());
            return;
        }
        if (null == nodes || nodes.size() < 1) {
            ObjectNode node = okCustomJson(40001, "列表为空");
            sender().tell(node, self());
            return;
        }
        long totalMoney = 0;
        long totalRealpay = 0;
        int totalProductAmount = 0;
        Map<Long, List<ProductLevelPrice>> levelPriceMap = new HashMap<>();
        Map<String, List<OrderDetail>> orderDetailMap = new HashMap<>();
        List<Order> orderList = new ArrayList<>();
        List<OrderDetail> allDetailList = new ArrayList<>();
        try {
            DB.beginTransaction();
            for (JsonNode node : nodes) {
                long shopId = node.findPath("shopId").asLong();
                long memberShopCouponId = node.findPath("memberCouponId").asLong();
                final String remark = node.findPath("description").asText();
                JsonNode list = node.findPath("productList");
                int mailAmount = 0;
                List<OrderDetail> detailList = new ArrayList<>();
                double totalWeight = 0;
                long thisItemTotalMoney = 0;
                long thisItemRealpay = 0;
                boolean oneProductEnbableFreeMail = true;
                long lastProductId = 0;
                String shopName = "";
                Shop shop = Shop.find.byId(shopId);
                if (null != shop) shopName = shop.name;

                int thisItemTotalProductAmount = 0;
                for (JsonNode productNode : list) {
                    OrderDetail orderDetail = new OrderDetail();
                    final int amount = productNode.findPath("amount").asInt();
                    final long productId = productNode.findPath("productId").asLong();
                    final long skuId = productNode.findPath("skuId").asLong();
                    orderDetail.deliveryMethod = MailFee.METHOD_MAIL;
                    Product product = productMap.get(productId);
                    ProductSku productSku = ProductSku.find.byId(skuId);
                    orderDetail.setProductPrice(autoRetrievePrice(levelPriceMap, orderDetail, product, productSku));
                    long subTotal = orderDetail.productPrice * amount;
                    thisItemTotalMoney = thisItemTotalMoney + subTotal;
                    setOrderDetail(member, currentTime, orderDetail, amount, remark, product, productSku);
                    orderDetail.setSubTotal(subTotal);
                    detailList.add(orderDetail);
                    if (lastProductId == 0) {
                        lastProductId = productId;
                        if (!product.enableFreeMail) oneProductEnbableFreeMail = false;
                    }
                    if (oneProductEnbableFreeMail && lastProductId != productId) oneProductEnbableFreeMail = false;

                    if (product.weightMethod == Product.WEIGHT_METHOD_BY_WEIGHT) {
                        totalWeight = totalWeight + productSku.weight * amount;
                    } else {
                        mailAmount = mailAmount + amount;
                    }
                    thisItemTotalProductAmount = thisItemTotalProductAmount + amount;
                    totalProductAmount = totalProductAmount + amount;
                }
                long couponFree = bizUtils.calcCouponFree(memberShopCouponId, thisItemTotalMoney, detailList, shopId);
                if (couponFree > 0) {
                    long thisShopTotal = thisItemTotalMoney;
                    updateOrderDetailForCouponFree(currentTime, detailList, thisShopTotal, couponFree);
                }
                allDetailList.addAll(detailList);
                //calc mailfee
                long mailFee = 0;
                mailFee = mailFee + bizUtils.calcMailFee(shopId, thisItemTotalMoney, contactDetail.provinceCode, totalWeight, mailAmount);
                thisItemRealpay = thisItemTotalMoney - couponFree + mailFee;
                totalRealpay = totalRealpay + thisItemRealpay;
                totalMoney = totalMoney + thisItemTotalMoney;
                OrderParam eachOrderParam = new OrderParam.Builder()
                        .uid(member.id)
                        .addressId(addressId)
                        .deliveryType(DELIVERY_TYPE_DELIVERY)
                        .description(remark)
                        .totalMoney(thisItemTotalMoney)
                        .realPayMoney(thisItemRealpay)
                        .productAmount(thisItemTotalProductAmount)
                        .memberCouponId(memberShopCouponId)
                        .couponFree(couponFree)
                        .activityType(ORDER_ACTIVITY_TYPE_NORMAL)
                        .shopId(shopId)
                        .shopName(shopName)
                        .dealerCode(dealerCode)
                        .dealerId(dealerUid)
                        .mailFee(mailFee)
                        .build();
                Order order = saveOrder(eachOrderParam, status, member, shopName, mailFee);
                order.setSubOrder(true);
                orderDetailMap.put(order.orderNo, detailList);
                orderList.add(order);
            }
            Order targetOrder = null;
            long platformCouponFree = 0;
            if (platformCouponId > 0) {
                platformCouponFree = bizUtils.calcCouponFree(platformCouponId, totalMoney, allDetailList, 0);
                if (platformCouponFree > 0) {
                    long platformTotal = allDetailList.parallelStream().mapToLong((each) -> each.subTotal).sum();
                    long finalCouponFree = platformCouponFree;
                    updateOrderDetailForCouponFree(currentTime, allDetailList, platformTotal, finalCouponFree);
                }
            }
            if (orderList.size() == 1) {
                saveOrderList(orderDetailMap, orderList, member);
                targetOrder = orderList.get(0);
                if (platformCouponFree > 0) {
                    long realpay = targetOrder.realPay - platformCouponFree;
                    if (realpay < 0) realpay = 0;
                    targetOrder.setRealPay(realpay);
                    targetOrder.setPlatformCouponId(platformCouponId);
                    targetOrder.setPlatformCouponFree(platformCouponFree);
                    targetOrder.save();
                }
            } else {
                OrderParam parentOrderParam = new OrderParam.Builder()
                        .uid(member.id)
                        .addressId(addressId)
                        .deliveryType(DELIVERY_TYPE_DELIVERY)
                        .totalMoney(totalMoney)
                        .realPayMoney(totalRealpay - platformCouponFree)
                        .platformCouponFree(platformCouponFree)
                        .platformCouponId(platformCouponId)
                        .productAmount(totalProductAmount)
                        .activityType(ORDER_ACTIVITY_TYPE_NORMAL)
                        .dealerCode(dealerCode)
                        .dealerId(dealerUid)
                        .mailFee(0)
                        .build();
                Order parentOrder = saveOrder(parentOrderParam, status, member, "", 0);
                parentOrder.setSubOrder(false);
                parentOrder.save();
                targetOrder = parentOrder;
                orderList.parallelStream().forEach((each) -> each.setParentId(parentOrder.id));
                saveOrderList(orderDetailMap, orderList, member);
            }
            DB.commitTransaction();
            ObjectNode result = Json.newObject();
            //扣积分
            result.put("code", 200);
            result.set("order", Json.toJson(targetOrder));
            result.put("orderId", targetOrder.id);
            long maxTimeToPay = MAX_TIME_TO_WAIT_PAY;
            result.put("maxTimeToPay", maxTimeToPay);
            result.put("orderNo", targetOrder.orderNo);
            result.put("address", targetOrder.address);
            result.put("status", status);
            sender().tell(result, self());
        } finally {
            DB.endTransaction();
            productMap.clear();
            bizUtils.unLock(String.valueOf(member.id), OPERATION_PLACE_ORDER);
        }
    }

    private void updateOrderDetailForCouponFree(long currentTime, List<OrderDetail> detailList, long thisShopTotal, long finalCouponFree) {
        detailList.forEach((each) -> {
            long free = each.subTotal * finalCouponFree / thisShopTotal;
            long resultFee = each.subTotal - free;
            each.setSubTotal(resultFee);
            each.setProductPrice(resultFee / each.number);
            each.setUpdateTime(currentTime);
        });
    }


    private void saveOrderList(Map<String, List<OrderDetail>> orderDetailMap, List<Order> orderList, Member member) {
        DB.saveAll(orderList);
        Map<String, Long> orderIdMap = new HashMap<>();
        orderList.parallelStream().forEach((order) -> {
            orderIdMap.put(order.orderNo, order.id);
        });
        List<OrderDetail> updateOrderDetailList = new ArrayList<>();
        orderDetailMap.forEach((orderNo, detailList) -> {
            long orderId = orderIdMap.get(orderNo);
            detailList.forEach((orderDetail) -> {
                orderDetail.setOrderId(orderId);
                updateOrderDetailList.add(orderDetail);
            });
        });
        DB.saveAll(updateOrderDetailList);
        removeFromCart(member.id, updateOrderDetailList);
        batchSubStock(updateOrderDetailList);
    }

    private long autoRetrievePrice(Map<Long, List<ProductLevelPrice>> levelPriceMap, OrderDetail orderDetail, Product product, ProductSku productSku) {
        if (productSku.price > 0) {
            return productSku.price;
        } else {
            List<ProductLevelPrice> levelPriceList = getProductLevelPrice(levelPriceMap, product.id);
            if (levelPriceList.size() > 0) {
                for (ProductLevelPrice productLevelPrice : levelPriceList) {
                    if (product.buyAmount >= productLevelPrice.upto) {
                        return productLevelPrice.price;
                    }
                }
            } else {
                return productSku.price;
            }
        }
        return productSku.price;
    }

    private void setOrderDetail(Member member, long currentTime, OrderDetail orderDetail, int amount, String remark, Product product, ProductSku productSku) {
        orderDetail.setUid(member.id);
        orderDetail.setSkuName(productSku.name);
        orderDetail.setProductImgUrl(productSku.imgUrl);
        orderDetail.setBrandId(product.brandId);
        orderDetail.setNumber(amount);
        orderDetail.setProductId(product.id);
        orderDetail.setProductName(product.name);
        orderDetail.setProductSkuId(productSku.id);
        orderDetail.setRemark(remark);
        orderDetail.setUnit(product.unit);
        orderDetail.setUpdateTime(currentTime);
        orderDetail.setCreateTime(currentTime);
        orderDetail.setShopId(product.shopId);
        orderDetail.setSupplierId(product.supplierUid);
        orderDetail.setSource(productSku.source);
        orderDetail.setOldPrice(productSku.oldPrice);
        orderDetail.setBidPrice(productSku.bidPrice);
        orderDetail.setTakeTime(currentTime + productSku.deliverHours * 3600);
    }

    private List<ProductLevelPrice> getProductLevelPrice(Map<Long, List<ProductLevelPrice>> levelPriceMap, long productId) {
        List<ProductLevelPrice> levelPriceList = levelPriceMap.get(productId);
        if (null == levelPriceList) {
            levelPriceList = ProductLevelPrice.find.query().where()
                    .eq("productId", productId)
                    .order().desc("upto")
                    .findList();
            levelPriceMap.put(productId, levelPriceList);
        }
        return levelPriceList;
    }


    private void batchSubStock(List<OrderDetail> detailList) {
        detailList.forEach((each) -> bizUtils.updateMerchantStockByOrderDetail(each, TYPE_PLACE_ORDER));
    }

    private void subScore(long uid, OrderParam param, Order order) {
        if (param.useScore > 0) {
            if (param.realPayMoney == 0) {
                BalanceParam balanceParam = new BalanceParam.Builder()
                        .itemId(BusinessItem.SCORE)
                        .changeAmount(-param.useScore)
                        .leftBalance(-param.useScore)
                        .freezeBalance(param.useScore)
                        .memberId(param.uid)
                        .desc("用户下单成功，扣除积分")
                        .bizType(TRANSACTION_TYPE_PLACE_ORDER)
                        .build();
                balanceUtils.saveChangeBalance(balanceParam, true);
                order.setStatus(Order.ORDER_STATUS_PAID);
                order.save();
            } else frozeScore(uid, param.useScore);
        }
    }

    //冻结积分
    private void frozeScore(long memberId, long useScore) {
        BalanceParam param = new BalanceParam.Builder()
                .itemId(BusinessItem.SCORE)
                .changeAmount(-useScore)
                .leftBalance(-useScore)
                .freezeBalance(useScore)
                .memberId(memberId)
                .desc("用户下单时冻结积分")
                .bizType(TRANSACTION_TYPE_PLACE_ORDER)
                .build();
        balanceUtils.saveChangeBalance(param, true);
    }

    private Order saveOrder(OrderParam param, int status, Member member, String orderShops, long mailFee) {
        Order order = new Order();
        order.setUid(member.id);
        String name = member.realName;
        if (ValidationUtil.isEmpty(name)) name = member.nickName;
        if (ValidationUtil.isEmpty(name)) name = member.phoneNumber;
        order.setUserName(bizUtils.limit20(name));
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

    /**
     * 将下单的商品从购物车移除
     *
     * @param uid
     */
    private void removeFromCart(long uid, List<OrderDetail> detailList) {
        detailList.forEach((each) -> {
            List<ShoppingCart> shoppingCartList = ShoppingCart.find.query().where()
                    .eq("uid", uid)
                    .eq("skuId", each.productSkuId).findList();
            if (shoppingCartList.size() > 0) DB.deleteAll(shoppingCartList);
        });
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

}
