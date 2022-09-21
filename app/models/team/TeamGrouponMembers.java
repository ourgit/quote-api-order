package models.team;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;

/**
 * 团购成员
 */
@Entity
@Table(name = "v1_team_groupon_members")
public class TeamGrouponMembers extends Model {

    public static final int STATUS_NOT_REFUND = 1;
    public static final int STATUS_NEED_REFUND = 2;
    public static final int STATUS_REFUND = 3;

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    @Column(name = "groupon_id")
    public long grouponId;

    @Column(name = "uid")
    public long uid;

    @Column(name = "order_id")
    public long orderId;

    @Column(name = "order_no")
    public String orderNo;

    @Column(name = "real_pay")
    public double realPay;

    @Column(name = "refund_money")
    public double refundMoney;

    @Column(name = "order_status")
    public int orderStatus;

    @Column(name = "refund_status")
    public int refundStatus;

    @Column(name = "create_time")
    public long createTime;

    @Transient
    public String userName;

    @Transient
    public String userAvatar;

    @Transient
    public int userLevel;

    public static Finder<Integer, TeamGrouponMembers> find = new Finder<>(TeamGrouponMembers.class);

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getGrouponId() {
        return grouponId;
    }

    public void setGrouponId(long grouponId) {
        this.grouponId = grouponId;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public double getRealPay() {
        return realPay;
    }

    public void setRealPay(double realPay) {
        this.realPay = realPay;
    }

    public int getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(int orderStatus) {
        this.orderStatus = orderStatus;
    }

    public int getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(int refundStatus) {
        this.refundStatus = refundStatus;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public double getRefundMoney() {
        return refundMoney;
    }

    public void setRefundMoney(double refundMoney) {
        this.refundMoney = refundMoney;
    }

    @Override
    public String toString() {
        return "TeamGrouponMembers{" +
                "id=" + id +
                ", grouponId=" + grouponId +
                ", uid=" + uid +
                ", orderId=" + orderId +
                ", orderNo='" + orderNo + '\'' +
                ", realPay=" + realPay +
                ", refundMoney=" + refundMoney +
                ", orderStatus=" + orderStatus +
                ", refundStatus=" + refundStatus +
                ", createTime=" + createTime +
                ", userName='" + userName + '\'' +
                ", userAvatar='" + userAvatar + '\'' +
                ", userLevel=" + userLevel +
                '}';
    }
}
