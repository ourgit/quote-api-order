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
@Table(name = "v1_order_count_month")
public class OrderCountMonth extends Model {


    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "uid")
    public long uid;

    @Column(name = "orders")
    public long orders;

    @Column(name = "month")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String month;

    @Column(name = "real_pay")
    public long realPay;//实付

    @Column(name = "user_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String userName;

    @Column(name = "create_time")
    public long createTime;

    public static Finder<Long, OrderCountMonth> find = new Finder<>(OrderCountMonth.class);

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

    public long getOrders() {
        return orders;
    }

    public void setOrders(long orders) {
        this.orders = orders;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getRealPay() {
        return realPay;
    }

    public void setRealPay(long realPay) {
        this.realPay = realPay;
    }

    @Override
    public String toString() {
        return "OrderCountMonth{" +
                "id=" + id +
                ", uid=" + uid +
                ", orders=" + orders +
                ", month='" + month + '\'' +
                ", userName='" + userName + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
