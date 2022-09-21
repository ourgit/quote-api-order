package models.order;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import models.groupon.Groupon;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;

/**
 * 订单
 */
@Entity
@Table(name = "v1_group_order")
public class GroupOrder extends Model {
    //订单状态 1待付款  2已付款  7已完成  -4取消交易   -6系统取消 -7退款 -8支付超时关闭
    public static int ORDER_STATUS_UNPAY = 1;
    public static int ORDER_STATUS_PAID = 2;
    public static int ORDER_STATUS_FINISH = 7;
    public static int ORDER_STATUS_CANCELED = -4;
    public static int ORDER_STATUS_SYSTEM_CANCELED = -6;
    public static int ORDER_STATUS_SYSTEM_REFUNDED = -7;
    public static int ORDER_STATUS_SYSTEM_NEED_REFUND = -8;
    public static int ORDER_STATUS_UNPAY_CLOSE = -8;
    public static int ORDER_TYPE_SINGLE_BUY = 1;
    public static int ORDER_TYPE_GROUP_BUY_LAUNCH = 2;
    public static int ORDER_TYPE_GROUP_BUY_JOIN = 3;

    public static final int ACCOUNT_SETTLE_NOT_NEED = 0;
    public static final int ACCOUNT_SETTLE_NEED = 1;
    public static final int ACCOUNT_SETTLE_FINISHED = 2;

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

    @Column(name = "phone_number")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String phoneNumber;

    @Column(name = "user_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String userName;

    @Column(name = "order_no")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String orderNo;

    @Column(name = "status")
    public int status;//订单状态 0未付款,1已付款,2已发货,3已签收,-1退货申请,-2退货中,-3已退货,-4取消交易 -5撤销申请

    @Column(name = "product_amount")
    public int productCount;//商品数量

    @Column(name = "total_money")
    public long totalMoney;//总价

    @Column(name = "real_pay")
    public long realPay;//实付

    @Column(name = "address")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String address;//地址

    @Column(name = "product_poster")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String productPoster;//地址

    @Column(name = "out_trade_no")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String outTradeNo;//订单支付单号

    @Column(name = "pay_tx_no")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String payTxNo;//第三方支付流水号

    @Column(name = "refund_tx_no")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String refundTxNo;

    @Column(name = "refund_time")
    public long refundTime;

    @Column(name = "avatar")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String avatar;//第三方支付流水号

    @Column(name = "pay_time")
    public long payTime;//支付时间

    @Column(name = "order_settlement_time")
    public long orderSettlementTime;//订单结算时间

    @Column(name = "commission_handled")
    public boolean commissionHandled;//返佣是否已处理


    @Column(name = "description")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String description;

    @Column(name = "product_id")
    public long productId;

    @Column(name = "product_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String productName;

    @Column(name = "groupon_id")
    public long grouponId;

    @Column(name = "groupon_status")
    public int grouponStatus;

    @Column(name = "order_type")
    public int orderType;

    @Column(name = "account_settle")
    public int accountSettle;

    @Column(name = "update_time")
    public long updateTime;


    @Column(name = "create_time")
    public long createTime;

    @Column(name = "staff_id")
    public long staffId;

    @Column(name = "staff_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String staffName;

    @Column(name = "commission")
    public long commission;

    @Transient
    public Groupon groupon;

    public static Finder<Long, GroupOrder> find = new Finder<>(GroupOrder.class);

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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public long getRealPay() {
        return realPay;
    }

    public void setRealPay(long realPay) {
        this.realPay = realPay;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public long getOrderSettlementTime() {
        return orderSettlementTime;
    }

    public void setOrderSettlementTime(long orderSettlementTime) {
        this.orderSettlementTime = orderSettlementTime;
    }

    public boolean isCommissionHandled() {
        return commissionHandled;
    }

    public void setCommissionHandled(boolean commissionHandled) {
        this.commissionHandled = commissionHandled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Groupon getGroupon() {
        return groupon;
    }

    public void setGroupon(Groupon groupon) {
        this.groupon = groupon;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public long getGrouponId() {
        return grouponId;
    }

    public void setGrouponId(long grouponId) {
        this.grouponId = grouponId;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRefundTxNo() {
        return refundTxNo;
    }

    public void setRefundTxNo(String refundTxNo) {
        this.refundTxNo = refundTxNo;
    }

    public long getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(long refundTime) {
        this.refundTime = refundTime;
    }

    public String getProductPoster() {
        return productPoster;
    }

    public void setProductPoster(String productPoster) {
        this.productPoster = productPoster;
    }

    public int getGrouponStatus() {
        return grouponStatus;
    }

    public void setGrouponStatus(int grouponStatus) {
        this.grouponStatus = grouponStatus;
    }

    public int getOrderType() {
        return orderType;
    }

    public void setOrderType(int orderType) {
        this.orderType = orderType;
    }

    public long getStaffId() {
        return staffId;
    }

    public void setStaffId(long staffId) {
        this.staffId = staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public int getAccountSettle() {
        return accountSettle;
    }

    public void setAccountSettle(int accountSettle) {
        this.accountSettle = accountSettle;
    }

    public long getCommission() {
        return commission;
    }

    public void setCommission(long commission) {
        this.commission = commission;
    }
}
