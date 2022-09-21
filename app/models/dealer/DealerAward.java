package models.dealer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 业务员返点
 */
@Entity
@Table(name = "v1_dealer_award")
public class DealerAward extends Model {

    public static final int STATUS_CANCELED = -1;
    public static final int STATUS_NOT_SELLTE = 1;
    public static final int STATUS_SELLTED = 2;
    public static final int STATUS_TRANSFERED = 3;

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "order_id")
    public long orderId;//订单ID

    @Column(name = "order_no")
    public String orderNo;//订单号

    @Column(name = "uid")
    public long uid;//用户ID

    @Column(name = "user_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String userName;

    @Column(name = "dealer_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String dealerName;

    @Column(name = "dealer_id")
    public long dealerId;

    @Column(name = "order_real_pay")
    public long orderRealPay;

    @Column(name = "order_award")
    public long orderAward;

    @Column(name = "status")
    public int status;

    @Column(name = "award_type")
    public int awardType;

    @Column(name = "create_time")
    public long createTime;

    @Transient
    public List<DealerAwardDetail> detailList = new ArrayList();

    public static Finder<Long, DealerAward> find = new Finder<>(DealerAward.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getDealerId() {
        return dealerId;
    }

    public void setDealerId(long dealerId) {
        this.dealerId = dealerId;
    }

    public double getOrderRealPay() {
        return orderRealPay;
    }

    public void setOrderRealPay(long orderRealPay) {
        this.orderRealPay = orderRealPay;
    }

    public double getOrderAward() {
        return orderAward;
    }

    public void setOrderAward(long orderAward) {
        this.orderAward = orderAward;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getDealerName() {
        return dealerName;
    }

    public void setDealerName(String dealerName) {
        this.dealerName = dealerName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public int getAwardType() {
        return awardType;
    }

    public void setAwardType(int awardType) {
        this.awardType = awardType;
    }

    @Override
    public String toString() {
        return "DealerAward{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", orderNo='" + orderNo + '\'' +
                ", uid=" + uid +
                ", userName='" + userName + '\'' +
                ", dealerName='" + dealerName + '\'' +
                ", dealerId=" + dealerId +
                ", orderRealPay=" + orderRealPay +
                ", orderAward=" + orderAward +
                ", status=" + status +
                ", awardType=" + awardType +
                ", createTime=" + createTime +
                ", detailList=" + detailList +
                '}';
    }
}
