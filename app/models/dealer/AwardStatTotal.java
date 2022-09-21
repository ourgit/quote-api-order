package models.dealer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;

/**
 * 奖励总结
 */
@Entity
@Table(name = "v1_award_stat_total")
public class AwardStatTotal extends Model {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "uid")
    public long uid;//用户ID

    @Column(name = "user_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String userName;

    @Column(name = "order_real_pay")
    public double orderRealPay;

    @Column(name = "order_award")
    public double orderAward;

    @Column(name = "create_time")
    public long createTime;

    public static Finder<Long, AwardStatTotal> find = new Finder<>(AwardStatTotal.class);

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public double getOrderRealPay() {
        return orderRealPay;
    }

    public void setOrderRealPay(double orderRealPay) {
        this.orderRealPay = orderRealPay;
    }

    public double getOrderAward() {
        return orderAward;
    }

    public void setOrderAward(double orderAward) {
        this.orderAward = orderAward;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "AwardStat{" +
                "id=" + id +
                ", uid=" + uid +
                ", userName='" + userName + '\'' +
                ", orderRealPay=" + orderRealPay +
                ", orderAward=" + orderAward +
                ", createTime=" + createTime +
                '}';
    }
}
