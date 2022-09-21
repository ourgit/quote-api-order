package models.order;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;

/**
 * 用户持有的优惠券
 */
@Entity
@Table(name = "v1_order_coupon")
public class OrderCoupon extends Model {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "uid")
    public long uid;

    @Column(name = "order_id")
    public long orderId;

    @Column(name = "member_coupon_id")
    public long memberCouponId;

    @Column(name = "coupon_name")
    public String couponName = "";

    @Column(name = "coupon_free")
    public long couponFree;

    @Column(name = "status")
    public int status;

    @Column(name = "create_time")
    public long createTime;

    public static Finder<Long, OrderCoupon> find = new Finder<>(OrderCoupon.class);

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

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public long getMemberCouponId() {
        return memberCouponId;
    }

    public void setMemberCouponId(long memberCouponId) {
        this.memberCouponId = memberCouponId;
    }

    public long getCouponFree() {
        return couponFree;
    }

    public void setCouponFree(long couponFree) {
        this.couponFree = couponFree;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getCouponName() {
        return couponName;
    }

    public void setCouponName(String couponName) {
        this.couponName = couponName;
    }
}

