package models.dealer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;

/**
 * 月奖励总结
 */
@Entity
@Table(name = "v1_award_stat")
public class AwardStat extends Model {
    public static final int STATUS_CANCELED = -1;
    public static final int STATUS_NOT_SELLTE = 1;
    public static final int STATUS_SELLTED = 2;
    public static final int STATUS_TRANSFERED = 3;

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "uid")
    public long uid;//用户ID

    @Column(name = "user_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String userName;

    @Column(name = "date")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String date;

    @Column(name = "order_real_pay")
    public double orderRealPay;

    @Column(name = "order_award")
    public double orderAward;

    @Column(name = "status")
    public int status;

    @Column(name = "create_time")
    public long createTime;

    public static Finder<Long, AwardStat> find = new Finder<>(AwardStat.class);

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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "AwardStat{" +
                "id=" + id +
                ", uid=" + uid +
                ", userName='" + userName + '\'' +
                ", date='" + date + '\'' +
                ", orderRealPay=" + orderRealPay +
                ", orderAward=" + orderAward +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }
}
