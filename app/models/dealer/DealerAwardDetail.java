package models.dealer;

import io.ebean.Finder;
import io.ebean.Model;
import models.order.OrderDetail;

import javax.persistence.*;

/**
 * 业务员返点明细
 */
@Entity
@Table(name = "v1_dealer_award_detail")
public class DealerAwardDetail extends Model {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "award_id")
    public long awardId;

    @Column(name = "order_detail_id")
    public long orderDetailId;

    @Column(name = "award_money")
    public long awardMoney;

    @Column(name = "item_award")
    public long itemAward;

    @Column(name = "amount")
    public long amount;

    @Transient
    public OrderDetail orderDetail;

    public static Finder<Long, DealerAwardDetail> find = new Finder<>(DealerAwardDetail.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAwardId() {
        return awardId;
    }

    public void setAwardId(long awardId) {
        this.awardId = awardId;
    }

    public long getOrderDetailId() {
        return orderDetailId;
    }

    public void setOrderDetailId(long orderDetailId) {
        this.orderDetailId = orderDetailId;
    }

    public double getAwardMoney() {
        return awardMoney;
    }

    public void setAwardMoney(long awardMoney) {
        this.awardMoney = awardMoney;
    }

    public double getItemAward() {
        return itemAward;
    }

    public void setItemAward(long itemAward) {
        this.itemAward = itemAward;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "DealerAwardDetail{" +
                "id=" + id +
                ", awardId=" + awardId +
                ", orderDetailId=" + orderDetailId +
                ", awardMoney=" + awardMoney +
                ", itemAward=" + itemAward +
                ", amount=" + amount +
                ", orderDetail=" + orderDetail +
                '}';
    }
}
