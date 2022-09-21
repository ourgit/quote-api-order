package utils;

import actor.ActorProtocol;
import akka.actor.ActorRef;
import akka.util.ByteString;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import constants.BusinessConstant;
import controllers.AliyunUploadController;
import io.ebean.Ebean;
import io.ebean.SqlUpdate;
import models.dealer.DealerAward;
import models.dealer.DealerAwardDetail;
import models.enroll.EnrollContentUserInfo;
import models.groupon.Groupon;
import models.groupon.GrouponOrder;
import models.log.SmsLog;
import models.order.*;
import models.product.*;
import models.promotion.CardCouponConfig;
import models.promotion.CouponConfig;
import models.shop.Shop;
import models.stat.StatMemberSalesOverview;
import models.stat.StatPlatformDaySalesOverview;
import models.system.ParamConfig;
import models.user.*;
import play.Environment;
import play.Logger;
import play.cache.NamedCache;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import play.mvc.Http;
import utils.wechat.WechatConfig;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static constants.BusinessConstant.*;
import static constants.RedisKeyConstant.*;
import static controllers.BaseController.CODE;
import static controllers.BaseController.CODE200;
import static models.order.Order.*;
import static models.user.MemberCoupon.STATUS_NOT_USE;
import static utils.wechat.WechatConfig.*;

@Singleton
public class BizUtils {
    public static final int HOT_VIEW_LIST = 1;
    public static final int HOT_VIEW_DETAIL = 2;
    public static final int HOT_VIEW_SHOPPING_CART = 5;
    public static final int HOT_VIEW_ORDER = 100;
    Logger.ALogger logger = Logger.of(BizUtils.class);
    public static final int TOKEN_EXPIRE_TIME = 2592000;
    public static final int DEFAULT_MAIL_FEE = 1000;
    public static final int DEFAULT_LEVEL3_TOTAL_AWARD_COUNT = 9;
    public static final double SCORE_TO_ONE_TENTH = 0.02;//积分对分的比例，1积分=2分钱

    public static final int DEALER_TYPE_DIRECT = 1;
    public static final int DEALER_TYPE_INDIRECT = 2;
    public static final int DEALER_TYPE_SELF_TAKE_PLACE = 3;
    public static final int DEALER_TYPE_COUNT_AWARD = 4;
    @Inject
    CacheUtils cacheUtils;

    @Inject
    Config config;

    @Inject
    BalanceUtils balanceUtils;

    @Inject
    DateUtils dateUtils;

    @Inject
    EncodeUtils encodeUtils;

    @Inject
    WSClient wsClient;

    @Inject
    AliyunUploadController aliyunUploadController;

    @Inject
    private Environment play;

    @Inject
    @Named("handleOrderPaidActor")
    private ActorRef handleOrderPaidActor;

    @Inject
    @NamedCache("redis")
    protected play.cache.redis.AsyncCacheApi redis;

    public static DecimalFormat DF = new DecimalFormat("0.0");

    public CompletionStage<Long> getUserIdByAuthToken(Http.Request request) {
        return CompletableFuture.supplyAsync(() -> getUserIdByAuthToken2(request));
    }


    public String getUIDFromRequest(Http.Request request) {
        Optional<String> authTokenHeaderValues = request.getHeaders().get(KEY_AUTH_TOKEN_UID);
        if (authTokenHeaderValues.isPresent()) {
            String authToken = authTokenHeaderValues.get();
            return authToken;
        }
        return "";
    }

    public long getUserIdByAuthToken2(Http.Request request) {
        String uidToken = getUIDFromRequest(request);
        if (ValidationUtil.isEmpty(uidToken)) return 0L;
        Optional<String> tokenOptional = redis.sync().get(uidToken);
        if (!tokenOptional.isPresent()) return 0L;

        String authToken = tokenOptional.get();//uid token对应的是用户uid
        if (ValidationUtil.isEmpty(authToken)) return 0L;
        Optional<String> platformKeyOptional = redis.sync().get(authToken);
        if (!platformKeyOptional.isPresent()) return 0L;
        String platformKey = platformKeyOptional.get();
        if (ValidationUtil.isEmpty(platformKey)) return 0L;
        Optional<Long> optional = redis.sync().get(platformKey);
        if (!optional.isPresent()) return 0L;
        Long uid = optional.get();
        return uid;
    }

    public long getCurrentTimeBySecond() {
        return System.currentTimeMillis() / 1000;
    }

    public int getTokenExpireTime() {
        int tokenExpireTime = config.getInt("token_expire_time");
        if (tokenExpireTime < 1) tokenExpireTime = TOKEN_EXPIRE_TIME;
        return tokenExpireTime;
    }

    public String getRequestIP(Http.Request request) {
        String ip = null;
        try {
            String remoteAddr = request.remoteAddress();
            String forwarded = request.getHeaders().get("X-Forwarded-For").get();
            String realIp = request.getHeaders().get(BusinessConstant.X_REAL_IP_HEADER).get();
            if (forwarded != null) {
                ip = forwarded.split(",")[0];
            }
            if (ValidationUtil.isEmpty(ip)) {
                ip = realIp;
            }
            if (ValidationUtil.isEmpty(ip)) {
                ip = remoteAddr;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return ip == null ? "" : escapeHtml(ip);
    }

    public boolean checkVcode(String accountName, String vcode) {
        if (ValidationUtil.isPhoneNumber(accountName)) {
            String key = cacheUtils.getSMSLastVerifyCodeKey(accountName);
            Optional<String> optional = redis.sync().get(key);
            if (optional.isPresent()) {
                String correctVcode = optional.get();
                if (!ValidationUtil.isEmpty(correctVcode)) {
                    if (ValidationUtil.isVcodeCorrect(vcode) && ValidationUtil.isVcodeCorrect(correctVcode) && vcode.equals(correctVcode))
                        return true;
                }
            }
        } else return false;
        return false;
    }


    /**
     * 转义html脚本
     *
     * @param value
     * @return
     */
    public String escapeHtml(String value) {
        if (ValidationUtil.isEmpty(value)) return "";
        value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        value = value.replaceAll("\\(", "（").replaceAll("\\)", "）");
        value = value.replaceAll("eval\\((.*)\\)", "");
        value = value.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
        value = value.replaceAll("script", "");
        value = value.replaceAll("select", "");
        value = value.replaceAll("insert", "");
        value = value.replaceAll("update", "");
        value = value.replaceAll("delete", "");
        value = value.replaceAll("%", "\\%");
        value = value.replaceAll("union", "");
        value = value.replaceAll("load_file", "");
        value = value.replaceAll("outfile", "");
        return value;
    }

    public boolean setLock(String id, String operationType) {
        String key = operationType + ":" + id;
        try {
            if (redis.exists(key).toCompletableFuture().get(10, TimeUnit.SECONDS)) return false;
            return redis.setIfNotExists(key, 1, 5).toCompletableFuture().get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("getLock:" + e.getMessage());
            redis.remove(key);
        }
        return true;
    }


    /**
     * 解锁
     *
     * @param uid
     * @param operationType
     */
    public void unLock(String uid, String operationType) {
        redis.remove(operationType + ":" + uid);
    }

    /**
     * 更新库存
     *
     * @param type
     */
    public void updateMerchantStock(Order order, int type) {
        if (null != order) {
            List<OrderDetail> detailList = OrderDetail.find.query().where().eq("orderId", order.id).findList();
            if (detailList.size() > 0) {
                detailList.forEach((each) -> {
                    if (null != each) {
                        updateMerchantStockByOrderDetail(each, type);
                    }
                });
            }
        }
    }

    public void updateMerchantStockByOrderDetail(OrderDetail orderDetail, int type) {
        int addOrSub = 1;
        if (type == BusinessConstant.TYPE_CANCEL_ORDER) {
            addOrSub = -1;
            orderDetail.setStatus(Order.ORDER_STATUS_CANCELED);
            orderDetail.save();
        } else if (type == TYPE_PLACE_ORDER) addOrSub = 1;
        if (orderDetail.productSkuId > 0) {
            updateProductSkuSoldAmountAndStock(orderDetail.productSkuId, orderDetail.number * addOrSub);
        }
        updateProductSoldAmountAndStock(orderDetail.productId, orderDetail.number * addOrSub);
    }

    private void updateProductSoldAmountAndStock(long productId, long soldAmount) {
        String sql = "UPDATE " +
                "    `v1_product` AS `dest`, " +
                "    ( SELECT * FROM `v1_product` " +
                "        WHERE id = :id  limit 1" +
                "    ) AS `src`" +
                "   SET" +
                "       `dest`.`sold_amount` = `src`.`sold_amount`+ :soldAmount " +
                "   WHERE  `dest`.`id` = :id " +
                ";";
        SqlUpdate sqlUpdate = Ebean.createSqlUpdate(sql);
        sqlUpdate.setParameter("id", productId);
        sqlUpdate.setParameter("soldAmount", soldAmount);
        sqlUpdate.executeNow();
    }

    public void updateProductSkuSoldAmountAndStock(long skuId, long soldAmount) {
        String sql = "UPDATE " +
                "    `v1_product_sku` AS `dest`, " +
                "    ( SELECT * FROM `v1_product_sku` " +
                "        WHERE id = :id  limit 1" +
                "    ) AS `src`" +
                "   SET" +
                "       `dest`.`sold_amount` =  `dest`.`sold_amount` + :soldAmount," +
                "       `dest`.`stock` = `dest`.`stock` +  :stock" +
                "   WHERE  `dest`.`id` = :id " +
                ";";
        SqlUpdate sqlUpdate = Ebean.createSqlUpdate(sql);
        sqlUpdate.setParameter("id", skuId);
        sqlUpdate.setParameter("soldAmount", soldAmount);
        sqlUpdate.setParameter("stock", -soldAmount);
        sqlUpdate.executeNow();
    }

    public void handleOrderPaid(String orderNo, String payTxId, int payType, String outTradeNo) {
        ActorProtocol.HANDLE_ORDER_PAID param = new ActorProtocol.HANDLE_ORDER_PAID(orderNo, payTxId, payType, outTradeNo);
        handleOrderPaidActor.tell(param, ActorRef.noSender());
    }


    private void giveCardCoupon(long configId, Order order, OrderDetail orderDetail) {
        CardCouponConfig cardCouponConfig = CardCouponConfig.find.byId(configId);
        long currentTime = dateUtils.getCurrentTimeBySecond();
        if (null != cardCouponConfig) {
            List<MemberCardCoupon> list = new ArrayList<>();
            long amount = 1;
            if (null != orderDetail) amount = orderDetail.number;
            long finalAmount = amount;
            LongStream.range(0, cardCouponConfig.giveCount * finalAmount).forEach((index) -> {
                MemberCardCoupon memberCardCoupon = generateMemberCardCoupon(order, orderDetail, cardCouponConfig, currentTime);
                list.add(memberCardCoupon);
            });
            if (list.size() > 0) Ebean.saveAll(list);
        }
    }

    private MemberCardCoupon generateMemberCardCoupon(Order order, OrderDetail orderDetail, CardCouponConfig cardCouponConfig, long currentTime) {
        MemberCardCoupon memberCardCoupon = new MemberCardCoupon();
        memberCardCoupon.setUid(order.uid);
        memberCardCoupon.setOrderId(order.id);
        memberCardCoupon.setNoLimitCount(cardCouponConfig.noLimitCount);
        if (null != orderDetail) {
            memberCardCoupon.setProductId(orderDetail.productId);
            memberCardCoupon.setSkuId(orderDetail.productSkuId);
        } else {
            memberCardCoupon.setProductId(cardCouponConfig.productId);
            memberCardCoupon.setSkuId(cardCouponConfig.skuId);
        }
        String userName = "";
        Member member = Member.find.byId(order.uid);
        if (null != member) {
            userName = member.realName;
            if (ValidationUtil.isEmpty(userName)) userName = member.nickName;
        }
        memberCardCoupon.setUserName(userName);
        memberCardCoupon.setCardCouponId(cardCouponConfig.id);
        memberCardCoupon.setShopId(cardCouponConfig.shopId);
        Shop shop = Shop.find.byId(cardCouponConfig.shopId);
        if (null != shop) {
            memberCardCoupon.setShopAddress(shop.contactAddress);
            memberCardCoupon.setShopName(shop.name);
            memberCardCoupon.setShopAvatar(shop.avatar);
        }
        memberCardCoupon.setTitle(cardCouponConfig.title);
        memberCardCoupon.setContent(cardCouponConfig.content);
        memberCardCoupon.setDigest(cardCouponConfig.digest);
        memberCardCoupon.setEndTime(currentTime + cardCouponConfig.days * BusinessConstant.HOUR24_TO_SECONDS);
        memberCardCoupon.setStatus(MemberCoupon.STATUS_NOT_USE);
        memberCardCoupon.setUpdateTime(currentTime);
        memberCardCoupon.setTransactionId("");
        memberCardCoupon.setSubId("");
        memberCardCoupon.setRealPay(0);
        memberCardCoupon.setPayType(0);
        return memberCardCoupon;
    }

    private void giveCoupon(long couponConfigId, long uid) {
        CouponConfig couponConfig = CouponConfig.find.byId(couponConfigId);
        long currentTime = dateUtils.getCurrentTimeBySecond();
        if (null != couponConfig) {
            MemberCoupon memberCoupon = new MemberCoupon();
            memberCoupon.setUid(uid);
            memberCoupon.setCouponId(couponConfig.id);
            memberCoupon.setCouponName(couponConfig.couponTitle);
            memberCoupon.setBeginTime(currentTime);
            memberCoupon.setEndTime(currentTime + couponConfig.expireDays * BusinessConstant.HOUR24_TO_SECONDS);
            memberCoupon.setStatus(MemberCoupon.STATUS_NOT_USE);
            memberCoupon.setUpdateTime(currentTime);
            memberCoupon.setTransactionId("");
            memberCoupon.setSubId("");
            memberCoupon.setRealPay(0);
            memberCoupon.setPayType(0);
            Member member = Member.find.byId(uid);
            String userName = "";
            if (null != member) {
                userName = member.realName;
                if (ValidationUtil.isEmpty(userName)) userName = member.nickName;
            }
            memberCoupon.setUserName(userName);
            memberCoupon.save();
        }
    }

    public String hidepartialChar(String userName) {
        if (ValidationUtil.isEmpty(userName)) return "";
        if (ValidationUtil.isValidEmailAddress(userName)) {
            int index = userName.indexOf("@");
            if (index < 2) return userName;
            if (index > 5) {
                return "*" + userName.substring(8);
            } else {
                String toReplaced = userName.substring(1, index);
                String result = toReplaced.replaceAll(".", "*");
                return userName.substring(0, 1) + result + userName.substring(index);
            }
        }
        if (ValidationUtil.isPhoneNumber(userName)) {
            return "*" + userName.substring(8);
        }
        String toReplaced = userName.substring(1);
        String result = toReplaced.replaceAll(".", "*");
        return userName.substring(0, 1) + result;
    }

    private void saveGrouponOrder(Order order, long id) {
        GrouponOrder grouponOrder = new GrouponOrder();
        grouponOrder.setGrouponId(id);
        grouponOrder.setOrderId(order.id);
        grouponOrder.save();
    }

    public String getUserName(Member member) {
        String userName = "";
        if (null != member) {
            userName = member.realName;
            if (ValidationUtil.isEmpty(userName)) userName = member.nickName;
        }
        return userName;
    }

    public void handleOtherAfterOrderPaid(Order order) {
        ObjectNode pushNode = Json.newObject();
        pushNode.put("type", TASK_NEW_ORDER);
        pushNode.put("orderId", order.id);
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderId(order.id);
        orderLog.setOldStatus(Order.ORDER_STATUS_UNPAY);
        orderLog.setNewStatus(Order.ORDER_STATUS_PAID);
        orderLog.setOperatorName("支付成功回调通知");
        orderLog.setCreateTime(dateUtils.getCurrentTimeBySecond());
        orderLog.save();
        sendOrderPaidMessage(order);
        List<OrderDetail> detailList = OrderDetail.find.query().where()
                .eq("orderId", order.id)
                .orderBy().asc("id")
                .findList();
        if (detailList.size() > 0) {
            detailList.parallelStream().forEach((each) -> {
                each.setStatus(order.status);
                increaseProductViews(each.productId, HOT_VIEW_ORDER);
                ProductSku sku = ProductSku.find.byId(each.productSkuId);
                if (null != sku) {
                    if (!ValidationUtil.isEmpty(sku.cardCoupons)) {
                        ArrayNode cardCoupons = (ArrayNode) Json.parse(sku.cardCoupons);
                        cardCoupons.forEach((cardCouponId) -> giveCardCoupon(cardCouponId.asLong(), order, each));
                    }
                }
            });
            Ebean.saveAll(detailList);
        }
        awardTransactions(order, detailList);
        updateMaterialForOrder(order);
        addToTodayPlatformStat(order);
        setBuyAvatarList(order, detailList);
        updateMemberSalesOverview(order);
//        if (detailList.size() > 0) {
//            if (order.orderType != Order.ORDER_TYPE_SERVICE)
//                updateSupplierOrderStat(detailList, order);
//        } else
        if (order.orderType == Order.ORDER_TYPE_THIRD_DISCOUNT) updateSupplierDiscountOrderStat(order);
//        updateSelfTakeOrderStat(order);
        geneOrderBarCode(order);
    }


    public void updateSupplierOrderStat(Order order) {
        long currentTime = dateUtils.getCurrentTimeBySecond();
        List<SupplierOrderStat> updateList = new ArrayList<>();
        List<OrderDetail> detailList = OrderDetail.find.query().where().eq("orderId", order.id)
                .findList();
        detailList.parallelStream().forEach((each) -> {
            SupplierOrderStat supplierOrderStat = SupplierOrderStat.find.query().where()
                    .eq("supplierId", each.supplierId)
                    .eq("productId", each.productId)
                    .eq("skuId", each.productSkuId)
                    .orderBy().asc("id")
                    .setMaxRows(1)
                    .findOne();
            if (null == supplierOrderStat) {
                supplierOrderStat = new SupplierOrderStat();
                supplierOrderStat.setSupplierId(each.supplierId);
                supplierOrderStat.setOrderId(order.id);
                supplierOrderStat.setProductId(each.productId);
                supplierOrderStat.setSkuId(each.productSkuId);
                supplierOrderStat.setProductName(each.productName);
                supplierOrderStat.setSkuName(each.skuName);
                supplierOrderStat.setCreateTime(currentTime);
                supplierOrderStat.setImgUrl(each.productImgUrl);
                supplierOrderStat.setStatus(SupplierOrderStat.STATUS_NOT_SELLTE);
            }
            supplierOrderStat.setTotalCount(supplierOrderStat.totalCount + each.number);
            supplierOrderStat.setTotalMoney(supplierOrderStat.totalMoney + each.number * each.bidPrice);
            updateList.add(supplierOrderStat);
        });
        if (updateList.size() > 0) Ebean.saveAll(updateList);
    }

    private void updateSupplierDiscountOrderStat(Order order) {
        if (order.shopId > 0) {
            long currentTime = dateUtils.getCurrentTimeBySecond();
            SupplierOrderStat supplierOrderStat = SupplierOrderStat.find.query().where()
                    .eq("supplierId", order.shopId)
                    .eq("productId", 0)
                    .eq("skuId", 0)
                    .orderBy().asc("id")
                    .setMaxRows(1)
                    .findOne();
            Shop shop = Shop.find.byId(order.shopId);
            String productImgUrl = "";
            if (null != shop) productImgUrl = shop.avatar;
            if (null == supplierOrderStat) {
                supplierOrderStat = new SupplierOrderStat();
                supplierOrderStat.setSupplierId(order.shopId);
                supplierOrderStat.setOrderId(order.id);
                supplierOrderStat.setProductId(0);
                supplierOrderStat.setSkuId(0);
                supplierOrderStat.setProductName("线下折扣订单");
                supplierOrderStat.setSkuName("线下折扣订单");
                supplierOrderStat.setCreateTime(currentTime);
                supplierOrderStat.setImgUrl(productImgUrl);
                supplierOrderStat.setStatus(SupplierOrderStat.STATUS_NOT_SELLTE);
            }
            supplierOrderStat.setTotalCount(supplierOrderStat.totalCount + 1);
            supplierOrderStat.setTotalMoney(supplierOrderStat.totalMoney + order.totalMoney * shop.bidDiscount / 100);
            supplierOrderStat.save();
        }
    }

    private void updateMemberSalesOverview(Order order) {
        //用户总消费额
        StatMemberSalesOverview memberSalesOverview = StatMemberSalesOverview.find.query().where()
                .eq("uid", order.uid).setMaxRows(1).findOne();
        if (null == memberSalesOverview) {
            memberSalesOverview = new StatMemberSalesOverview();
            memberSalesOverview.setUserName(order.userName);
            memberSalesOverview.setCommission(0);
            memberSalesOverview.setOrderAmount(order.realPay);
            memberSalesOverview.setUid(order.uid);
            memberSalesOverview.setCreatedTime(dateUtils.getCurrentTimeBySecond());
        }
        memberSalesOverview.setOrderAmount(memberSalesOverview.orderAmount + order.realPay);
        memberSalesOverview.setOrders(memberSalesOverview.orders + 1);
        memberSalesOverview.save();
    }


    private void awardTransactions(Order order, List<OrderDetail> detailList) {
        if (order.activityType != Order.ORDER_ACTIVITY_WHOLESALE && order.orderType != Order.ORDER_TYPE_MEMBERSHIP
                && order.activityType != Order.ORDER_ACTIVITY_TYPE_FLASH_SALE) {
            if (order.dealerUid > 0) {
                //直推奖励
                Member directDealer = Member.find.byId(order.dealerUid);
                if (null != directDealer) {
                    awardToDealer(order, directDealer, DEALER_TYPE_DIRECT, detailList);
                    if (directDealer.dealerId > 0 && directDealer.dealerId != order.dealerUid) {
                        Member inDirectDealer = Member.find.byId(directDealer.dealerId);
                        if (null != inDirectDealer) {
                            awardToDealer(order, inDirectDealer, DEALER_TYPE_INDIRECT, detailList);
                        }
                    }
                }
            }
            awardMembershipTimeForShare(order);
            //自提点奖励
//            String address = order.address;
//            if (!ValidationUtil.isEmpty(address) && order.deliveryType == Order.DELIVERY_TYPE_PICK_UP) {
//                JsonNode node = Json.parse(address);
//                if (null != node) {
//                    SelfTakeDetail selfTakeDetail = Json.fromJson(node, SelfTakeDetail.class);
//                    if (null != selfTakeDetail) {
//                        Member placer = Member.find.byId(selfTakeDetail.uid);
//                        if (null != placer) {
//                            awardToSelfTakePlace(order, placer);
//                        }
//                    }
//                }
//            }
        }
    }

    private void awardMembershipTimeForShare(Order order) {
        MembershipLog membershipLog = MembershipLog.find.query().where()
                .eq("uid", order.uid)
                .ne("orderId", order.id)
                .eq("status", MembershipLog.STATUS_TAKEN)
                .orderBy().asc("id")
                .setMaxRows(1)
                .findOne();
        if (null != membershipLog) {
            membershipLog.setOrderId(order.id);
            membershipLog.setStatus(MembershipLog.STATUS_ORDER);
            membershipLog.save();
        }
    }

    public void awardToDealer(Order order, Member dealer, int dealerType, List<OrderDetail> detailList) {
        List<DealerAwardDetail> list = new ArrayList<>();
        detailList.forEach((each) -> {
            ProductSku productSku = ProductSku.find.byId(each.productSkuId);
            if (null != productSku) {
                DealerAwardDetail detail = new DealerAwardDetail();
                detail.setOrderDetailId(each.id);
                int awardPercentage = 0;
                if (dealerType == DEALER_TYPE_DIRECT) {
                    awardPercentage = getDirectDealerAwardPercentage();
                } else if (dealerType == DEALER_TYPE_INDIRECT) {
                    awardPercentage = getInDirectDealerAwardPercentage();
                }
                long award = productSku.award * awardPercentage / 100;
                detail.setAwardMoney(each.number * award);
                detail.setItemAward(award);
                detail.setAmount(each.number);
                list.add(detail);
            }
        });
        if (list.size() > 0) {
            DealerAward dealerAward = new DealerAward();
            dealerAward.setAwardType(dealerType);
            dealerAward.setOrderId(order.id);
            dealerAward.setOrderNo(order.orderNo);
            dealerAward.setUid(order.uid);
            dealerAward.setUserName(order.userName);
            String dealerName = dealer.realName;
            if (ValidationUtil.isEmpty(dealerName)) dealerName = dealer.nickName;
            dealerAward.setDealerName(dealerName);
            dealerAward.setDealerId(dealer.id);
            dealerAward.setOrderRealPay(order.realPay);
            dealerAward.setStatus(DealerAward.STATUS_NOT_SELLTE);
            long currentTime = dateUtils.getCurrentTimeBySecond();
            dealerAward.setCreateTime(currentTime);
            long orderAward = 0;
            if (dealerType != DEALER_TYPE_COUNT_AWARD) {
                orderAward = list.parallelStream().filter((each) -> null != each).mapToLong((each) -> each.awardMoney).sum();
            }
            dealerAward.setOrderAward(orderAward);
            dealerAward.save();
            list.forEach((each) -> each.setAwardId(dealerAward.id));
            if (list.size() > 0) Ebean.saveAll(list);
        }
    }

    public void awardToSelfTakePlace(Order order, Member dealer) {
        if (order.realPay > 0) {
            int awardPercentage = getSelfTakenPlaceAwardPercentage();
            DealerAward dealerAward = new DealerAward();
            dealerAward.setAwardType(DEALER_TYPE_SELF_TAKE_PLACE);
            dealerAward.setOrderId(order.id);
            dealerAward.setOrderNo(order.orderNo);
            dealerAward.setUid(order.uid);
            dealerAward.setUserName(order.userName);
            String dealerName = dealer.realName;
            if (ValidationUtil.isEmpty(dealerName)) dealerName = dealer.nickName;
            dealerAward.setDealerName(dealerName);
            dealerAward.setDealerId(dealer.id);
            dealerAward.setOrderRealPay(order.realPay);
            dealerAward.setStatus(DealerAward.STATUS_NOT_SELLTE);
            long currentTime = dateUtils.getCurrentTimeBySecond();
            dealerAward.setCreateTime(currentTime);
            long orderAward = order.realPay * awardPercentage / 100;
            dealerAward.setOrderAward(orderAward);
            dealerAward.save();
        }
    }

    private void setBuyAvatarList(Order order, List<OrderDetail> detailList) {
        Member member = Member.find.byId(order.uid);
        if (null != member) {
            List<ProductSkuAvatar> avatars = new ArrayList<>();
            long currentTime = dateUtils.getCurrentTimeBySecond();
            detailList.parallelStream().forEach((each) -> {
                ProductSkuAvatar productSkuAvatar = ProductSkuAvatar.find.query().where()
                        .eq("uid", member.id)
                        .eq("productId", each.productId)
                        .orderBy().desc("id")
                        .setMaxRows(1)
                        .findOne();
                if (null == productSkuAvatar) {
                    productSkuAvatar = new ProductSkuAvatar();
                    productSkuAvatar.setUid(member.id);
                    productSkuAvatar.setUserName(getUserName(member));
                    productSkuAvatar.setSkuId(each.productSkuId);
                    productSkuAvatar.setProductId(each.productId);
                    productSkuAvatar.setAvatar(member.avatar);
                    productSkuAvatar.setCreateTime(currentTime);
                }
                productSkuAvatar.setAmount(productSkuAvatar.amount + each.number);
                avatars.add(productSkuAvatar);
            });
            if (avatars.size() > 0) Ebean.saveAll(avatars);
        }
    }

    private void addToTodayPlatformStat(Order order) {
        CompletableFuture.runAsync(() -> {
            statDaySales(order, order.shopId);
            statDaySales(order, 0);
        });
    }

    private void statDaySales(Order order, long shopId) {
        String date = dateUtils.formatToYMDDashBySecond(dateUtils.getCurrentTimeBySecond());
        StatPlatformDaySalesOverview daySalesOverview = StatPlatformDaySalesOverview.find.query().where()
                .eq("day", date)
                .eq("shopId", shopId)
                .orderBy().asc("id")
                .setMaxRows(1).forUpdate().findOne();
        if (null == daySalesOverview) {
            daySalesOverview = new StatPlatformDaySalesOverview();
            daySalesOverview.setTotalMoney(order.realPay);
            daySalesOverview.setOrders(1);
            daySalesOverview.setProducts(order.productCount);
            daySalesOverview.setDay(date);
            daySalesOverview.setShopId(shopId);
            daySalesOverview.setCreatedTime(dateUtils.getCurrentTimeBySecond());
            daySalesOverview.save();
        } else {
            daySalesOverview.setTotalMoney(daySalesOverview.totalMoney + order.realPay);
            daySalesOverview.setOrders(daySalesOverview.orders + 1);
            daySalesOverview.setProducts(daySalesOverview.products + order.productCount);
            daySalesOverview.save();
        }
    }

    public void updateMaterialForOrder(Order order) {
        if (null == order) return;
        if (order.couponId > 0) {
            setCouponUsed(order.couponId);
        }
        if (order.platformCouponId > 0) {
            setCouponUsed(order.platformCouponId);
        }
    }

    private void setCouponUsed(long couponId) {
        MemberCoupon memberCoupon = MemberCoupon.find.byId(couponId);
        if (null != memberCoupon) {
            memberCoupon.setStatus(MemberCoupon.STATUS_USED);
            memberCoupon.setUpdateTime(System.currentTimeMillis() / 1000);
            memberCoupon.save();
        }
    }


    /**
     * 下订单送积分
     */
    public long giveCreditScoreForOrder(Order order) {
        Member member = Member.find.byId(order.uid);
        long score = order.realPay / 100;
        if (null != member) {
            if (score > 0) {
                BalanceParam param = new BalanceParam.Builder()
                        .changeAmount(score)
                        .itemId(BusinessItem.SCORE)
                        .leftBalance(score)
                        .totalBalance(score)
                        .memberId(order.uid)
                        .desc("下单赠送积分:" + score)
                        .bizType(TRANSACTION_TYPE_GIVE_SCORE_FOR_ORDER).build();
                balanceUtils.saveChangeBalance(param, true);
            }
        }
        return score;
    }

    public String getAccessToken() {
        String token = "";
        Optional<Object> optional = redis.sync().get(MINI_APP_ACCESS_TOKEN);
        if (optional.isPresent()) {
            token = (String) optional.get();
        }
        return token;
    }

    public double getScoreMoneyScale() {
        String value = getConfigValue(PARAM_KEY_SCORE_TO_ONE_TENTH);
        if (!ValidationUtil.isEmpty(value)) return Double.parseDouble(value);
        return SCORE_TO_ONE_TENTH;
    }

    public int getDirectDealerAwardPercentage() {
        int directAwardPercentage = 80;
        String value = getConfigValue(PARAM_KEY_AWARD_DIRECT_DEALER_PERCENTAGE);
        if (!ValidationUtil.isEmpty(value)) {
            directAwardPercentage = Integer.parseInt(value);
        }
        return directAwardPercentage;
    }

    public int getInDirectDealerAwardPercentage() {
        int directAwardPercentage = 20;
        String value = getConfigValue(PARAM_KEY_AWARD_INDIRECT_DEALER_PERCENTAGE);
        if (!ValidationUtil.isEmpty(value)) {
            directAwardPercentage = Integer.parseInt(value);
        }
        return directAwardPercentage;
    }

    public int getSelfTakenPlaceAwardPercentage() {
        int directAwardPercentage = 3;
        String value = getConfigValue(PARAM_KEY_AWARD_SELF_TAKEN_PLACE_PERCENTAGE);
        if (!ValidationUtil.isEmpty(value)) {
            directAwardPercentage = Integer.parseInt(value);
        }
        return directAwardPercentage;
    }

    public String getConfigValue(String key) {
        String value = "";
        Optional<Object> accountOptional = redis.sync().get(key);
        if (accountOptional.isPresent()) {
            value = (String) accountOptional.get();
            if (!ValidationUtil.isEmpty(value)) return value;
        }
        if (ValidationUtil.isEmpty(value)) {
            ParamConfig config = ParamConfig.find.query().where()
                    .eq("key", key)
                    .orderBy().asc("id")
                    .setMaxRows(1).findOne();
            if (null != config) {
                value = config.value;
                redis.set(key, value, 30 * 3600 * 24);
            }
        }
        return value;
    }

    public CouponConfig getCouponConfig(long couponId) {
        String key = cacheUtils.getCouponConfigCacheKey(couponId);
        Optional<CouponConfig> optional = redis.sync().get(key);
        if (optional.isPresent()) return optional.get();
        CouponConfig config = CouponConfig.find.byId(couponId);
        redis.set(key, config, 120);
        return config;
    }

    public int getGrouponRequireOrders() {
        String value = getConfigValue(PARAM_KEY_GROUPON_REQUIRE_ORDERS);
        if (!ValidationUtil.isEmpty(value)) {
            return Integer.parseInt(value);
        }
        return GROUPON_REQUIRE_ORDERS;
    }

    public boolean checkBalanceEnough(long uid, long totalAmount) {
        MemberBalance cashBalance = MemberBalance.find.query().where().eq("uid", uid)
                .eq("itemId", BusinessItem.CASH).setMaxRows(1).findOne();
        if (null != cashBalance) {
            if (cashBalance.leftBalance >= totalAmount) return true;
        }
        return false;
    }

    public void sendOrderPaidMessage(Order order) {
        if (null != order) {
            String resultUrl = SEND_TEMPLATE_MESSAGE_URL.replace("ACCESS_TOKEN", getAccessToken());
            Member member = Member.find.byId(order.uid);
            if (null != member) {
                String openId = member.openId;
                if (!ValidationUtil.isEmpty(openId)) {
                    ObjectNode resultNode = prepareOrderPaidParam(openId, order);
                    logger.info("resultNode:" + resultNode.toString());
                    wsClient.url(resultUrl).post(resultNode).thenAccept((response) -> {
                        JsonNode responseNode = response.asJson();
                        logger.info("支付成功发送模板消息反馈：" + responseNode.toString());
                    });
                }
            }
        }
    }

    private ObjectNode prepareOrderPaidParam(String openId, Order order) {
        ObjectNode param = Json.newObject();
        param.put("touser", openId);
        param.put("template_id", TEMPLATE_MSG_ID_PAY_SUCCEED);
        param.put("page", "/pages/user/order-detail/index?id=" + order.id);
        ObjectNode data = Json.newObject();

        List<OrderDetail> orderDetails = OrderDetail.find.query().where().eq("orderId", order.id)
                .orderBy().asc("id")
                .findList();
        StringBuilder merchantName = new StringBuilder();
        orderDetails.forEach((orderDetail) -> {
            merchantName.append(orderDetail.productName + " ");
        });
        String resultMerchantName = merchantName.toString();

        ObjectNode name4 = Json.newObject();
        name4.put("value", limit20(resultMerchantName));
        data.set("name4", name4);

        ObjectNode amount2 = Json.newObject();
        amount2.put("value", DF_TWO_DIGIT.format(order.realPay / 100.00) + "元");
        data.set("amount2", amount2);

        ObjectNode date3 = Json.newObject();
        date3.put("value", dateUtils.formatToYMDHMSBySecond(order.payTime));
        data.set("date3", date3);

        ObjectNode character_string1 = Json.newObject();
        character_string1.put("value", order.orderNo);
        data.set("character_string1", character_string1);

        ObjectNode thing5 = Json.newObject();
        thing5.put("value", "服务商家:" + order.shopName);
        data.set("thing5", thing5);

        param.set("data", data);
        return param;
    }

    public String limit20(String value) {
        if (ValidationUtil.isEmpty(value)) return "";
        if (value.length() > 20) return value.substring(0, 17) + "...";
        return value;
    }

    public String limit10(String value) {
        if (ValidationUtil.isEmpty(value)) return "";
        if (value.length() > 10) return value.substring(0, 7) + "...";
        return value;
    }

    public void updateOrderDetailStatus(Order order) {
        List<OrderDetail> detailList = OrderDetail.find.query().where().eq("orderId", order.id)
                .findList();
        if (detailList.size() > 0) {
            detailList.parallelStream().forEach((each) -> {
                each.setStatus(order.status);
            });
            Ebean.saveAll(detailList);
        }
    }

    public void updateOrderDetailPostServiceStatus(int returnStatus, long orderDetailId) {
        OrderDetail orderDetail = OrderDetail.find.byId(orderDetailId);
        if (null != orderDetail) {
            orderDetail.setStatus(returnStatus);
            orderDetail.save();
            List<OrderDetail> detailList = OrderDetail.find.query().where().eq("orderId", orderDetail.orderId)
                    .findList();
            boolean allToReturn = true;
            for (OrderDetail each : detailList) {
                if (each.status < OrderDetail.STATUS_AGREE_RETURN) {
                    allToReturn = false;
                    break;
                }
            }
            if (allToReturn) {
                Order order = find.byId(orderDetail.orderId);
                if (null != order) {
                    order.setPostServiceStatus(Order.POST_SERVICE_STATUS_HANDLED);
                    order.setStatus(Order.ORDER_STATUS_RETURNED);
                    order.save();
                }
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
    }

    public CompletionStage<ObjectNode> sendSMS(String phoneNumber, String vcode, String content) {
        return CompletableFuture.supplyAsync(() -> {
            ObjectNode node = Json.newObject();
            node.put("code", 200);
            String appKey = getConfigValue(PARAM_KEY_SMS_BUSINESS_APP_KEY);
            String apiSecret = getConfigValue(PARAM_KEY_SMS_BUSINESS_API_SECRET);
            String requestUrl = getConfigValue(PARAM_KEY_SMS_REQUEST_URL);
            if (ValidationUtil.isEmpty(requestUrl)) {
                node.put("code", 500);
                node.put("reason", "短信请求地址为空");
                logger.info(node.findPath("reason").asText());
                return node;
            }
            if (ValidationUtil.isEmpty(appKey)) {
                node.put("code", 500);
                node.put("reason", "appKey为空");
                logger.info(node.findPath("reason").asText());
                return node;
            }
            if (ValidationUtil.isEmpty(apiSecret)) {
                node.put("code", 500);
                node.put("reason", "apiSecret为空");
                logger.info(node.findPath("reason").asText());
                return node;
            }
            String param = "appkey=" + appKey + "&appsecret=" + apiSecret + "&mobile=" + phoneNumber +
                    "&content=" + content;
            SmsLog smsLog = new SmsLog();
            smsLog.setPhoneNumber(phoneNumber);
            smsLog.setContent(requestUrl + "" + param);
            long unixStamp = System.currentTimeMillis();
            smsLog.setExtno(unixStamp + "");
            smsLog.setReqStatus("");
            smsLog.setRespStatus("");
            smsLog.setReqTime(unixStamp / 1000);
            smsLog.save();
            return wsClient.url(requestUrl).setContentType("application/x-www-form-urlencoded")
                    .post(param).thenApplyAsync((response) -> {
                        ObjectNode returnNode = Json.newObject();
                        ObjectNode result = (ObjectNode) Json.parse(response.getBody());
                        if (!ValidationUtil.isEmpty(vcode)) {
                            String key = cacheUtils.getSMSLastVerifyCodeKey(phoneNumber);
                            redis.set(key, vcode, 10 * 60);
                            if (null != result) {
                                String resultCode = result.findPath("code").asText();
                                if (resultCode.equalsIgnoreCase("0")) {
                                    returnNode.put("code", 200);
                                    //设置缓存，用于判断一分钟内请求短信多少
                                    String existRequestKey = EXIST_REQUEST_SMS + phoneNumber;
                                    redis.set(existRequestKey, existRequestKey, 60);
                                } else {
                                    logger.info(response.getBody());
                                }
                                String msg = result.findPath("msg").asText();
                                smsLog.setMsgId(result.findPath("smsid").asText());
                                smsLog.setReqStatus(resultCode + "   " + msg);
                                if (result.has("data")) {
                                    ArrayNode data = (ArrayNode) result.findPath("data");
                                    if (null != data && data.size() > 0) {
                                        JsonNode nodeData = data.get(0);
                                        if (null != nodeData) {
                                            smsLog.setMsgId(nodeData.findPath("smsid").asText());
                                            String msg2 = result.findPath("msg").asText();
                                            String code2 = result.findPath("code").asText();
                                            smsLog.setRespStatus(code2 + "   " + msg2);
                                        }
                                    }
                                }
                            }
                        }
                        smsLog.setRespTime(System.currentTimeMillis() / 1000);
                        smsLog.save();
                        if (null != result) returnNode.set("result", result);
                        return returnNode;
                    }).toCompletableFuture().join();
        });
    }

    public void geneOrderBarCode(Order order) {
        CompletableFuture.runAsync(() -> {
            String accessToken = "";
            Optional<Object> optional = redis.sync().get(MINI_APP_ACCESS_TOKEN);
            if (optional.isPresent()) {
                accessToken = (String) optional.get();
            }

            String barcodeImgUrl = "";
            if (ValidationUtil.isEmpty(accessToken)) {
                return;
            }
            String url = WechatConfig.WX_BAR_CODE_API_URL.replaceAll("ACCESS_TOKEN", accessToken);
            ObjectNode param = Json.newObject();
            ObjectNode paramNode = Json.newObject();
            paramNode.put("orderId", order.id);

            param.put("scene", convertScene(paramNode));
            param.put("page", "pages/center/index");
            param.put("auto_color", false);
            ObjectNode colorParam = Json.newObject();
            colorParam.put("r", 0);
            colorParam.put("g", 0);
            colorParam.put("b", 0);
            param.set("line_color", colorParam);

            try {
                WSResponse response = wsClient.url(url).post(param).toCompletableFuture().get(20, TimeUnit.SECONDS);
                if (null != response && response.getBody().length() > 500) {
                    ByteString fileStream = response.getBodyAsBytes();
                    ByteArrayInputStream byteArrayInputStream = null;
                    byte[] bytes = fileStream.toArray();
                    ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                    BufferedImage originalImage = ImageIO.read(bis);
                    BufferedImage bgImage = desaturate(originalImage);

                    int w = bgImage.getWidth(null);
                    int h = bgImage.getHeight(null);
                    final BufferedImage finalImage = new BufferedImage(w, h,
                            BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = finalImage.createGraphics();
                    g.drawImage(bgImage, 0, 0, null);
                    String logoCenterFile = play.rootPath().getAbsolutePath() + File.separator + "conf" + File.separator + "logo-center.png";
                    File logo = new File(logoCenterFile);
                    final Image fgImage = ImageIO.read(logo);
                    g.drawImage(fgImage, w / 2 - 95, h / 2 - 95, null);
                    g.dispose();
                    ByteArrayOutputStream bgArraysOs = new ByteArrayOutputStream();
                    ImageIO.write(finalImage, "jpg", bgArraysOs);
                    byteArrayInputStream = new ByteArrayInputStream(bgArraysOs.toByteArray());
                    String key = "order_" + order.orderNo + ".jpg";

                    barcodeImgUrl = aliyunUploadController.uploadToOss(byteArrayInputStream, key, getAlinYunAccessId(), getAliYunSecretKey());
                    if (!ValidationUtil.isEmpty(barcodeImgUrl)) {
                        order.setBarcode(barcodeImgUrl);
                        order.save();
                    }
                } else {
                    logger.info(response.getBody());
                }
            } catch (Exception e) {
                logger.error("geneInviteCode:" + e.getMessage());
            }
        });
    }

    public static BufferedImage desaturate(BufferedImage source) {
        ColorConvertOp colorConvert =
                new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        colorConvert.filter(source, source);
        return source;
    }


    public String convertScene(ObjectNode node) {
        String temp = Json.stringify(node);
        return temp.replaceAll("\\{", "(")
                .replaceAll("\\}", ")")
                .replaceAll("\"", "'");
    }


    public void setSkuStockCache(long skuId, long stock) {
        String key = cacheUtils.getSkuStockCache(skuId);
        redis.set(key, stock, 24 * 3600);
    }


    public Product getProduct(long productId) {
        Product product = null;
        String key = cacheUtils.getMerchantCacheKey(productId);
        try {
            Optional<Product> optional = redis.sync().get(key);
            if (optional.isPresent()) {
                product = optional.get();
            }
        } catch (Exception e) {
            logger.error("getProduct:" + e.getMessage());
        }

        if (null == product) {
            redis.remove(key);
            product = Product.find.byId(productId);
            if (null != product) {
                ProductFavorProducts favorProducts = ProductFavorProducts.find.query().where()
                        .eq("productId", product.id)
                        .setMaxRows(1).findOne();
                product.favorProducts = favorProducts;
                redis.set(key, product, 60);
            }
        }
        return product;
    }

    public long calcMailFee(long shopId, long totalMoney, String provinceCode, double totalWeight, int amount) {
        if (!ValidationUtil.isEmpty(provinceCode) && provinceCode.length() == 6) {
            provinceCode = provinceCode.substring(0, 2) + "0000";
            MailFee mailFee = MailFee.find.query().where()
                    .eq("method", MailFee.METHOD_MAIL)
                    .eq("shopId", shopId)
                    .icontains("regionCode", provinceCode + ",")
                    .setMaxRows(1).findOne();
            if (null == mailFee) {
                mailFee = MailFee.find.query().where()
                        .eq("regionCode", "0")
                        .eq("shopId", shopId)
                        .eq("method", MailFee.METHOD_MAIL)
                        .setMaxRows(1).findOne();
            }
            if (null != mailFee) return calcMailFeeByTotalWeight(mailFee, totalWeight, totalMoney, amount);
        }
        return DEFAULT_MAIL_FEE;
    }

    public long calcMailFeeByDistance(long totalMoney, double lat, double lng, long shopId) {
        long fee = DEFAULT_MAIL_FEE;
//        String latLng = getConfigValue(PARAM_KEY_SHOP_LAT_LNG);
        Shop shop = Shop.find.byId(shopId);
        if (null != shop && shop.lat > 0 && shop.lon > 0) {
            double distance = getDistance(shop.lat, shop.lon, lat, lng);
            fee = getMailFeeByDistance(distance);
            MailFee mailFee = MailFee.find.query().where().eq("regionCode", "0")
                    .eq("method", MailFee.METHOD_INSTANT_SEND)
                    .setMaxRows(1).findOne();
            if (null != mailFee) {
                if (totalMoney >= mailFee.uptoMoneyFree) {
                    fee = fee - mailFee.firstWeightFee;
                    if (fee < 0) fee = 0;
                }
            }
        }
        return fee;
    }

    private long getMailFeeByDistance(double distance) {
        if (distance <= 1) return 450;
        else if (distance <= 3) {
            return 450 + Math.round(Math.ceil(distance - 1) * 100);
        } else {
            return 650 + Math.round(Math.ceil(distance - 3) * 200);
        }
    }

    private long calcMailFeeByTotalWeight(MailFee mailFee, double totalWeight, double totalMoney, int amount) {
        if (mailFee.uptoAmountFree > 0) {
            if (amount >= mailFee.uptoAmountFree) return 0;
        }
        if (mailFee.uptoMoneyFree > 0) {
            if (totalMoney >= mailFee.uptoMoneyFree) return 0;
        }
        if (mailFee.uptoMoneyFree == 0 && mailFee.uptoAmountFree == 0) {
            return 0;
        }
        long mailMoney = 0;
        if (totalWeight > 0) {
            if (totalWeight <= 1) mailMoney = mailMoney + mailFee.firstWeightFee;
            else mailMoney = mailMoney + mailFee.firstWeightFee + (long) ((totalWeight - 1) * mailFee.nextWeightFee);
        }
        if (amount > 0) {
            if (amount <= 1) mailMoney = mailMoney + mailFee.firstAmountFee;
            else mailMoney = mailMoney + mailFee.firstAmountFee + (amount - 1) * mailFee.nextAmountFee;
        }
        return mailMoney;
    }

    public void increaseProductViews(long productId, int views) {
        String key = cacheUtils.getProductViewKey(productId);
        CompletableFuture.runAsync(() -> {
            ProductViews productViews = ProductViews.find.query().where().eq("productId", productId)
                    .orderBy().asc("id")
                    .setMaxRows(1)
                    .findOne();
            if (null == productViews) {
                productViews = new ProductViews();
                productViews.setProductId(productId);
            }
            long updateViews = productViews.views + views;
            productViews.setViews(updateViews);
            productViews.save();
            redis.set(key, updateViews, 10 * 24 * 3600);
        });
    }


    public long getTotalMoney(ProductSku productSku, long amount, OrderDetail orderDetail) {
        orderDetail.setProductPrice(productSku.price);
        return productSku.price * amount;
    }

    public boolean isFlashSale(ProductSku sku) {
        if (sku.beginTime > 0 && sku.endTime > 0 && sku.flashPrice > 0 && sku.flashLeftAmount > 0) return true;
        return false;
    }

    public double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = lat1 * Math.PI / 180.0;
        double radLat2 = lat2 * Math.PI / 180.0;
        double a = radLat1 - radLat2;
        double b = lng1 * Math.PI / 180.0 - lng2 * Math.PI / 180.0;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * 6378.137; // EARTH_RADIUS;
        return s;
    }

    public String getMemberName(Member member) {
        String name = "";
        if (null != member) {
            name = member.realName;
            if (ValidationUtil.isEmpty(name)) name = member.nickName;
        }
        return name;
    }

    public String getDomain() {
        return getConfigValue(PARAM_KEY_DEFAULT_HOME_PAGE_URL);
    }

    public String getWechatMpAppId() {
        return getEncryptConfigValue(PARAM_KEY_WECHAT_MP_APP_ID);
    }

    public String getWechatMpSecretCode() {
        return getEncryptConfigValue(PARAM_KEY_WECHAT_MP_SECRET_CODE);
    }

    public String getWechatAppId() {
        return getEncryptConfigValue(PARAM_KEY_WECHAT_APP_ID);
    }

    public String getWechatMiniAppId() {
        return getEncryptConfigValue(PARAM_KEY_WECHAT_MINI_APP_ID);
    }

    public String getWechatMiniAppSecretCode() {
        return getEncryptConfigValue(PARAM_KEY_WECHAT_MINI_APP_SECRET_CODE);
    }

    public String getWechatMchId() {
        return getEncryptConfigValue(PARAM_KEY_WECHAT_MCH_ID);
    }

    public String getWechatMchAppSecretCode() {
        return getEncryptConfigValue(PARAM_KEY_WECHAT_MCH_API_SECURITY_CODE);
    }

    public String getEncryptConfigValue(String key) {
        Optional<Object> accountOptional = redis.sync().get(key);
        if (accountOptional.isPresent()) {
            String value = (String) accountOptional.get();
            if (!ValidationUtil.isEmpty(value)) return value;
        }

        ParamConfig config = ParamConfig.find.query().where()
                .eq("key", key)
                .orderBy().asc("id")
                .setMaxRows(1).findOne();
        if (null != config && !ValidationUtil.isEmpty(config.value)) {
            String decryptValue = encodeUtils.decrypt(config.value);
            redis.set(key, decryptValue);
        }
        return "";
    }

    //拼团成功通知
    public void sendGrouponSucceedMsg(GrouponOrder grouponOrder, Groupon existGroupon) {
        if (null != grouponOrder) {
            Order order = Order.find.byId(grouponOrder.orderId);
            if (null != order) {
                ObjectNode resultNode = prepareSuccessfulGrouponParam(order, grouponOrder, existGroupon);
                logger.info("sendGrouponSucceedMsg:" + resultNode.toString());
                sendWechatSubscribeMsg(order, resultNode);
            }
        }
    }

    private ObjectNode prepareSuccessfulGrouponParam(Order order, GrouponOrder grouponOrder, Groupon existGroupon) {
        ObjectNode param = Json.newObject();
        param.put("template_id", TEMPLATE_MSG_ID_GROUPON_SUCCEED);
        param.put("page", "/pages/user/order-detail/index?id=" + order.id);
        ObjectNode data = Json.newObject();
        List<OrderDetail> orderDetails = OrderDetail.find.query().where().eq("orderId", order.id)
                .orderBy().asc("id")
                .findList();
        StringBuilder merchantName = new StringBuilder();
        orderDetails.forEach((orderDetail) -> merchantName.append(orderDetail.productName + " "));
        String resultMerchantName = merchantName.toString();
        if (ValidationUtil.isEmpty(resultMerchantName)) resultMerchantName = "已成功购买商品";
        ObjectNode thing7 = Json.newObject();
        thing7.put("value", limit20(resultMerchantName));
        data.set("thing7", thing7);

        ObjectNode amount3 = Json.newObject();
        amount3.put("value", DF_TWO_DIGIT.format(grouponOrder.grouponPrice / 100.00) + "元");
        data.set("amount3", amount3);

        ObjectNode time6 = Json.newObject();
        time6.put("value", dateUtils.getNowYMDHMS());
        data.set("time6", time6);

        ObjectNode thing8 = Json.newObject();
        thing8.put("value", limit20(existGroupon.users));
        data.set("thing8", thing8);

        ObjectNode character_string1 = Json.newObject();
        character_string1.put("value", "尊敬的用户，恭喜您参团成功啦");
        data.set("character_string1", character_string1);
        param.set("data", data);
        return param;
    }


    private void sendWechatSubscribeMsg(Order order, ObjectNode paramNode) {
        if (null != order) {
            String resultUrl = SEND_TEMPLATE_MESSAGE_URL.replace("ACCESS_TOKEN", getAccessToken());
            Member memberProfile = Member.find.byId(order.uid);
            if (null != memberProfile) {
                String openId = memberProfile.openId;
                if (!ValidationUtil.isEmpty(openId)) {
                    paramNode.put("touser", openId);
                    wsClient.url(resultUrl).post(paramNode).thenAccept((response) -> {
                        JsonNode responseNode = response.asJson();
                        logger.info("sendWechatSubscribeMsg 成功发送模板消息反馈：" + responseNode.toString());
                    });
                }
            }
        }
    }

    public String getAlinYunAccessId() {
        return getEncryptConfigValue(PARAM_KEY_ALI_YUN_ACCESS_ID);
    }

    public String getAliYunSecretKey() {
        return getEncryptConfigValue(PARAM_KEY_ALI_YUN_SECRET_KEY);
    }

    /**
     * typeRole={
     * type: 'amountReduce',
     * upto: 10000,
     * discount: 1000
     * }
     * <p>
     * amountReduce  量减
     * priceReduce 价减
     * amountDiscount 量折
     * priceDiscount 价折
     * <p>
     * useRole = {
     * type: 'product',
     * list: [{
     * productId: 11999,
     * name:'美容院工作服足浴技师服'
     * },
     * {
     * productId: 11999,
     * name: '美容院工作服足浴技师服'
     * }]
     * }
     * <p>
     * category 分类
     */
    public ObjectNode checkCouponUse(CouponConfig config, double realPayMoney, List<OrderDetail> detailList, long shopId) {
        ObjectNode node = Json.newObject();
        node.put(CODE, CODE200);
        node.put("free", 0);
        if (ValidationUtil.isEmpty(config.typeRules) || ValidationUtil.isEmpty(config.useRules)) {
            return node;
        }
        if (shopId != 0 && shopId != config.shopId) {
            return node;
        }
        JsonNode typeRule = Json.parse(config.typeRules);
        String type = typeRule.findPath("type").asText();
        long upto = typeRule.findPath("upto").asLong();
        long discount = typeRule.findPath("discount").asLong();

        JsonNode useRule = Json.parse(config.useRules);
        long uptoAmount = 0;
        String useRuleType = useRule.findPath("type").asText();
        ArrayNode idList = (ArrayNode) useRule.findPath("list");
        Set<Long> set = new HashSet<>();
        if (!useRuleType.equalsIgnoreCase("all")) {
            idList.forEach((each) -> {
                if (useRuleType.equalsIgnoreCase("product")) {
                    long productId = each.findPath("productId").asLong();
                    set.add(productId);
                } else if (useRuleType.equalsIgnoreCase("category")) {
                    long id = each.asLong();
                    set.add(id);
                }
            });
        }
        for (OrderDetail orderDetail : detailList) {
            boolean isAvailable = false;
            if (useRuleType.equalsIgnoreCase("all")) {
                isAvailable = true;
            } else if (useRuleType.equalsIgnoreCase("product")) {
                if (set.contains(orderDetail.productId)) isAvailable = true;
            } else {
                if (shopId == 0) {
                    if (set.contains(orderDetail.categoryId)) isAvailable = true;
                } else {
                    if (set.contains(orderDetail.shopCategoryId)) isAvailable = true;
                }
            }
            if (isAvailable) {
                if (type.equalsIgnoreCase("amountReduce") || type.equalsIgnoreCase("amountDiscount")) {
                    uptoAmount = uptoAmount + orderDetail.number;
                } else {
                    uptoAmount = uptoAmount + orderDetail.subTotal;
                }
            }
        }
        if (uptoAmount >= upto) {
            if (type.equalsIgnoreCase("amountReduce") || type.equalsIgnoreCase("priceReduce")) {
                node.put("free", discount);
                return node;
            } else {
                double favor = (100 - discount) * realPayMoney / 100;
                node.put("free", favor);
                return node;
            }
        }
        return node;
    }


    public String getAlipayAppId() {
        return getConfigValue(PARAM_KEY_ALIPAY_APPID);
    }

    public String getAlipayAppPrivateKey() {
        return getConfigValue(PARAM_KEY_ALIPAY_APP_PRIVATE_KEY);
    }

    //    public String getAlipayPublicKeyRSA2() {
//        return getConfigValue(PARAM_KEY_ALIPAY_PUBLIC_KEY_RSA2);
//    }
    public String getAlipayAliPublicKey() {
        return getConfigValue(PARAM_KEY_ALIPAY_ALI_PUBLIC_KEY);
    }

    public String getAlipayWapPayNotifyUrl() {
        return getConfigValue(PARAM_KEY_ALIPAY_WAP_PAY_NOTIFY_URL);
    }

    public String getAlipayDirectPayNotifyUrl() {
        return getConfigValue(PARAM_KEY_ALIPAY_DIRECT_PAY_NOTIFY_URL);
    }

    public String getAlipayReturnUrl() {
        return getConfigValue(PARAM_KEY_ALIPAY_RETURN_URL);
    }

    public String getAlipayPartnerNo() {
        return getConfigValue(PARAM_KEY_ALIPAY_PARTNER_NO);
    }

    public String getAlipaySellAccountName() {
        return getConfigValue(PARAM_KEY_ALIPAY_SELL_ACCOUNT_NAME);
    }

    public String getKDHostUrl() {
        return getConfigValue(PARAM_KEY_KD_HOST_URL);
    }

    public String getKDAppCode() {
        return getConfigValue(PARAM_KEY_KD_APP_CODE);
    }

    public long calcCouponFree(long memberCouponId, long thisItemTotalMoney, List<OrderDetail> detailList, long shopId) {
        //calc coupon
        long currentTime = dateUtils.getCurrentTimeBySecond();
        MemberCoupon coupon = null;
        long couponFree = 0;
        if (memberCouponId > 0) {
            coupon = MemberCoupon.find.byId(memberCouponId);
            if (null != coupon && coupon.status == STATUS_NOT_USE) {
                if (currentTime > coupon.endTime) {
                    coupon.setStatus(MemberCoupon.STATUS_EXPIRED);
                    coupon.save();
                } else {
                    if (currentTime >= coupon.beginTime) {
                        CouponConfig config = getCouponConfig(coupon.couponId);
                        if (null != config) {
                            ObjectNode couponNode = checkCouponUse(config, thisItemTotalMoney, detailList, shopId);
                            int result = couponNode.findPath(CODE).asInt();
                            if (result == 200) {
                                long free = couponNode.findPath("free").asLong();
                                couponFree = free;
                            }
                        }
                    }
                }
            }
        }
        return couponFree;
    }

    public String getWepaySpAppId() {
        return getConfigValue(PARAM_KEY_WE_PAY_SP_APP_ID);
    }

    public String getWepaySpMchId() {
        return getConfigValue(PARAM_KEY_WE_PAY_SP_MCH_ID);
    }

    public String getWepaySubMchId() {
        return getConfigValue(PARAM_KEY_WE_PAY_SUB_MCH_ID);
    }

    public String getWepaySubKeySerialNo() {
        return getConfigValue(PARAM_KEY_WE_PAY_KEY_SERIAL_NO);
    }

    public String getWepayAPIV3Key() {
        return getConfigValue(PARAM_KEY_WE_PAY_API_V3_KEY);
    }

    public String getWepayPrivateKey() {
        return getConfigValue(PARAM_KEY_WE_PAY_PRIVATE_KEY);
    }
}
