package models.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;


@Entity
@Table(name = "v1_membership_right")
public class MembershipRight extends Model {


    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "membership_id")
    public long membershipId;

    @Column(name = "title")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String title;

    @Column(name = "icon")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String icon;

    @Column(name = "detail")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String detail;

    @Column(name = "sort")
    public int sort;

    @Column(name = "coupon_config_id")
    public long couponConfigId;

    @Column(name = "coupon_amount")
    public int couponAmount;

    @Column(name = "card_coupon_id")
    public long cardCouponId;

    @Column(name = "card_coupon_amount")
    public int cardCouponAmount;

    public static Finder<Long, MembershipRight> find = new Finder<>(MembershipRight.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(long membershipId) {
        this.membershipId = membershipId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public long getCouponConfigId() {
        return couponConfigId;
    }

    public void setCouponConfigId(long couponConfigId) {
        this.couponConfigId = couponConfigId;
    }

    public int getCouponAmount() {
        return couponAmount;
    }

    public void setCouponAmount(int couponAmount) {
        this.couponAmount = couponAmount;
    }

    public long getCardCouponId() {
        return cardCouponId;
    }

    public void setCardCouponId(long cardCouponId) {
        this.cardCouponId = cardCouponId;
    }

    public int getCardCouponAmount() {
        return cardCouponAmount;
    }

    public void setCardCouponAmount(int cardCouponAmount) {
        this.cardCouponAmount = cardCouponAmount;
    }

    @Override
    public String toString() {
        return "MembershipRight{" +
                "id=" + id +
                ", membershipId=" + membershipId +
                ", title='" + title + '\'' +
                ", icon='" + icon + '\'' +
                ", detail='" + detail + '\'' +
                ", sort=" + sort +
                ", couponConfigId=" + couponConfigId +
                ", couponAmount=" + couponAmount +
                ", cardCouponId=" + cardCouponId +
                ", cardCouponAmount=" + cardCouponAmount +
                '}';
    }
}
