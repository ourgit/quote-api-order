package models.order;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;

/**
 * 订单
 */
@Entity
@Table(name = "v1_order_count")
public class OrderCount extends Model {


    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "order_id")
    public long orderId;

    @Column(name = "order_no")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String orderNo;

    @Column(name = "real_pay")
    public long realPay;//实付

    @Column(name = "benefit_award_uid")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String benefitAwardUid;//地址

    @Column(name = "create_time")
    public long createTime;

    public static Finder<Long, OrderCount> find = new Finder<>(OrderCount.class);

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

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public long getRealPay() {
        return realPay;
    }

    public void setRealPay(long realPay) {
        this.realPay = realPay;
    }

    public String getBenefitAwardUid() {
        return benefitAwardUid;
    }

    public void setBenefitAwardUid(String benefitAwardUid) {
        this.benefitAwardUid = benefitAwardUid;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "OrderCount{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", orderNo='" + orderNo + '\'' +
                ", realPay=" + realPay +
                ", benefitAwardUid='" + benefitAwardUid + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
