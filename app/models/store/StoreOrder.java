package models.store;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import models.user.MemberCardCoupon;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单
 */
@Entity
@Table(name = "v1_store_order")
public class StoreOrder extends Model {
    //订单状态 1待付款  3待发货 5待收货 7已签收  9已评价-1退货申请,-2退货中,-3已退货,-4取消交易 -5撤销申请 -6系统取消 -7退款 -8支付超时关闭 -100删除
    public static int ORDER_STATUS_UNPAY = 1;
    public static int ORDER_STATUS_PAID = 2;
    public static int ORDER_STATUS_TO_DELIEVERY = 3;
    public static int ORDER_STATUS_DELIEVERED = 4;
    public static int ORDER_STATUS_TO_TAKE = 5;
    public static int ORDER_STATUS_ARRIVE_SELF_TAKEN_PLACE = 6;
    public static int ORDER_STATUS_TAKEN = 7;
    public static int ORDER_STATUS_COMMENTED = 9;
    public static int ORDER_STATUS_APPLY_RETURN = -1;
    public static int ORDER_STATUS_HANDLING_RETURN = -2;
    public static int ORDER_STATUS_RETURNED = -3;
    public static int ORDER_STATUS_CANCELED = -4;
    public static int ORDER_STATUS_CANCEL_RETURN = -5;
    public static int ORDER_STATUS_SYSTEM_CANCELED = -6;
    public static int ORDER_STATUS_SYSTEM_REFUNDED = -7;
    public static int ORDER_STATUS_UNPAY_CLOSE = -8;
    public static int ORDER_STATUS_DELETE = -100;

    //售后状态 0 未发起售后 1 申请售后 -1 售后已取消 2 处理中 3已寄回 4已换货 5已退款 200 处理完毕
    public static int POST_SERVICE_STATUS_NO = 0;
    public static int POST_SERVICE_STATUS_APPLYING = 1;
    public static int POST_SERVICE_STATUS_CANCEL_APPLY = -1;
    public static int POST_SERVICE_STATUS_HANDLING = 2;
    public static int POST_SERVICE_STATUS_SEND_BACK = 3;
    public static int POST_SERVICE_STATUS_CHANGED = 4;
    public static int POST_SERVICE_STATUS_REFUND = 5;
    public static int POST_SERVICE_STATUS_HANDLED = 200;


    public static final int DELIVERY_TYPE_PICK_UP = 1;
    public static final int DELIVERY_TYPE_DELIVERY = 2;

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "uid")
    public long uid;

    @Column(name = "dealer_id")
    public long dealerUid;

    @Column(name = "dealer_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String dealerName;

    @Column(name = "user_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String userName;

    @Column(name = "order_no")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String orderNo;

    @Column(name = "store_id")
    public long storeId;

    @Column(name = "store_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String storeName;//商户名称

    @Column(name = "score_gave")
    public long scoreGave;//赠送的积分

    @Column(name = "status")
    public int status;//订单状态 0未付款,1已付款,2已发货,3已签收,-1退货申请,-2退货中,-3已退货,-4取消交易 -5撤销申请

    @Column(name = "post_service_status")
    public int postServiceStatus;//售后状态 0 未发起售后 1 申请售后 -1 售后已取消 2 处理中 200 处理完毕

    @Column(name = "product_count")
    public int productCount;//商品数量

    @Column(name = "total_money")
    public long totalMoney;//总价

    @Column(name = "total_return_money")
    public long totalReturnMoney;

    @Column(name = "total_return_number")
    public long totalReturnNumber;

    @Column(name = "real_pay")
    public long realPay;

    @Column(name = "discount_money")
    public long discountMoney;

    @Column(name = "logistics_fee")
    public long logisticsFee;//运费

    @Column(name = "address")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String address;//地址

    @Column(name = "pay_method")
    public int payMethod;//支付渠道

    @Column(name = "out_trade_no")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String outTradeNo;//订单支付单号

    @Column(name = "pay_tx_no")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String payTxNo;//第三方支付流水号

    @Column(name = "pay_time")
    public long payTime;//支付时间

    @Column(name = "delivery_time")
    public long deliveryTime;//发货时间

    @Column(name = "score_use")
    public long scoreUse;//使用的积分

    @Column(name = "score_to_money")
    public long scoreToMoney;//积分使用对应的金额

    @Column(name = "order_settlement_time")
    public long orderSettlementTime;//订单结算时间

    @Column(name = "coupon_id")
    public long couponId;

    @Column(name = "member_card_coupon_id")
    public long memberCardCouponId;

    @Column(name = "coupon_free")
    public long couponFree;

    @Column(name = "commission_handled")
    public boolean commissionHandled;//返佣是否已处理

    @Column(name = "refund_tx_id")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String refundTxId;//退款流水ID

    @Column(name = "express_no")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String expressNo;

    @Column(name = "description")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String description;

    @Column(name = "express_company")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String expressCompany;

    @Column(name = "barcode")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String barcode;

    @Column(name = "delivery_type")
    public int deliveryType;//送货类型

    @Column(name = "order_type")
    public int orderType;


    @Column(name = "field1")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String field1;

    @Column(name = "update_time")
    public long updateTime;

    @Column(name = "create_time")
    public long createTime;

    @Column(name = "take_time")
    public long takeTime;

    @Column(name = "relationship")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String relationship = "";

    @Column(name = "location")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String location = "";


    @Column(name = "note")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String note = "";


    @Column(name = "filter")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String filter = "";

    @Column(name = "pay_detail")
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String payDetail = "";

    @Column(name = "account_settle")
    public int accountSettle;

    @Transient
    public String totalMoneyStr;

    @Transient
    public String payMethodName;

    @Transient
    public List<StoreOrderDetail> detailList = new ArrayList();

    @Transient
    public List<MemberCardCoupon> cardCouponList = new ArrayList();

    @Transient
    public StoreOrderReturns orderReturns;


    public static Finder<Long, StoreOrder> find = new Finder<>(StoreOrder.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getDealerUid() {
        return dealerUid;
    }

    public void setDealerUid(long dealerUid) {
        this.dealerUid = dealerUid;
    }

    public String getDealerName() {
        return dealerName;
    }

    public void setDealerName(String dealerName) {
        this.dealerName = dealerName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public long getStoreId() {
        return storeId;
    }

    public void setStoreId(long storeId) {
        this.storeId = storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public long getScoreGave() {
        return scoreGave;
    }

    public void setScoreGave(long scoreGave) {
        this.scoreGave = scoreGave;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPostServiceStatus() {
        return postServiceStatus;
    }

    public void setPostServiceStatus(int postServiceStatus) {
        this.postServiceStatus = postServiceStatus;
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }

    public long getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(long totalMoney) {
        this.totalMoney = totalMoney;
    }

    public long getTotalReturnMoney() {
        return totalReturnMoney;
    }

    public void setTotalReturnMoney(long totalReturnMoney) {
        this.totalReturnMoney = totalReturnMoney;
    }

    public long getTotalReturnNumber() {
        return totalReturnNumber;
    }

    public void setTotalReturnNumber(long totalReturnNumber) {
        this.totalReturnNumber = totalReturnNumber;
    }

    public long getRealPay() {
        return realPay;
    }

    public void setRealPay(long realPay) {
        this.realPay = realPay;
    }

    public long getDiscountMoney() {
        return discountMoney;
    }

    public void setDiscountMoney(long discountMoney) {
        this.discountMoney = discountMoney;
    }

    public long getLogisticsFee() {
        return logisticsFee;
    }

    public void setLogisticsFee(long logisticsFee) {
        this.logisticsFee = logisticsFee;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(int payMethod) {
        this.payMethod = payMethod;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getPayTxNo() {
        return payTxNo;
    }

    public void setPayTxNo(String payTxNo) {
        this.payTxNo = payTxNo;
    }

    public long getPayTime() {
        return payTime;
    }

    public void setPayTime(long payTime) {
        this.payTime = payTime;
    }

    public long getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(long deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public long getScoreUse() {
        return scoreUse;
    }

    public void setScoreUse(long scoreUse) {
        this.scoreUse = scoreUse;
    }

    public long getScoreToMoney() {
        return scoreToMoney;
    }

    public void setScoreToMoney(long scoreToMoney) {
        this.scoreToMoney = scoreToMoney;
    }

    public long getOrderSettlementTime() {
        return orderSettlementTime;
    }

    public void setOrderSettlementTime(long orderSettlementTime) {
        this.orderSettlementTime = orderSettlementTime;
    }

    public long getCouponId() {
        return couponId;
    }

    public void setCouponId(long couponId) {
        this.couponId = couponId;
    }

    public long getMemberCardCouponId() {
        return memberCardCouponId;
    }

    public void setMemberCardCouponId(long memberCardCouponId) {
        this.memberCardCouponId = memberCardCouponId;
    }

    public long getCouponFree() {
        return couponFree;
    }

    public void setCouponFree(long couponFree) {
        this.couponFree = couponFree;
    }

    public boolean isCommissionHandled() {
        return commissionHandled;
    }

    public void setCommissionHandled(boolean commissionHandled) {
        this.commissionHandled = commissionHandled;
    }

    public String getRefundTxId() {
        return refundTxId;
    }

    public void setRefundTxId(String refundTxId) {
        this.refundTxId = refundTxId;
    }

    public String getExpressNo() {
        return expressNo;
    }

    public void setExpressNo(String expressNo) {
        this.expressNo = expressNo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExpressCompany() {
        return expressCompany;
    }

    public void setExpressCompany(String expressCompany) {
        this.expressCompany = expressCompany;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public int getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(int deliveryType) {
        this.deliveryType = deliveryType;
    }

    public int getOrderType() {
        return orderType;
    }

    public void setOrderType(int orderType) {
        this.orderType = orderType;
    }

    public String getField1() {
        return field1;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getTakeTime() {
        return takeTime;
    }

    public void setTakeTime(long takeTime) {
        this.takeTime = takeTime;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getPayDetail() {
        return payDetail;
    }

    public void setPayDetail(String payDetail) {
        this.payDetail = payDetail;
    }

    public int getAccountSettle() {
        return accountSettle;
    }

    public void setAccountSettle(int accountSettle) {
        this.accountSettle = accountSettle;
    }

    public String getTotalMoneyStr() {
        return totalMoneyStr;
    }

    public void setTotalMoneyStr(String totalMoneyStr) {
        this.totalMoneyStr = totalMoneyStr;
    }

    public String getPayMethodName() {
        return payMethodName;
    }

    public void setPayMethodName(String payMethodName) {
        this.payMethodName = payMethodName;
    }
}
