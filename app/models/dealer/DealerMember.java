package models.dealer;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;

/**
 * 分销客户列表
 */
@Entity
@Table(name = "v1_dealer_member")
public class DealerMember extends Model {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "dealer_id")
    public long dealerId;

    @Column(name = "dealer_name")
    public String dealerName;

    @Column(name = "uid")
    public long uid;//用户id

    @Column(name = "user_name")
    public String userName;

    @Column(name = "shop_name")
    public String shopName;

    @Column(name = "create_time")
    public long createdTime;

    public static Finder<Long, DealerMember> find = new Finder<>(DealerMember.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDealerId() {
        return dealerId;
    }

    public void setDealerId(long dealerId) {
        this.dealerId = dealerId;
    }

    public String getDealerName() {
        return dealerName;
    }

    public void setDealerName(String dealerName) {
        this.dealerName = dealerName;
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

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    @Override
    public String toString() {
        return "DealerMember{" +
                "id=" + id +
                ", dealerId=" + dealerId +
                ", dealerName='" + dealerName + '\'' +
                ", uid=" + uid +
                ", userName='" + userName + '\'' +
                ", shopName='" + shopName + '\'' +
                ", createdTime=" + createdTime +
                '}';
    }
}
