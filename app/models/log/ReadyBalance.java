package models.log;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 余额预操作
 * Created by win7 on 2016/6/14.
 */
@Entity
@Table(name = "v1_ready_balance")
public class ReadyBalance extends Model implements Serializable {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "uid")
    public long uid;//用户id

    @Column(name = "order_id")
    public long orderId;

    @Column(name = "handled")
    public boolean handled;

    @Column(name = "change_amount")
    public double changeAmount;

    @Column(name = "item_id")
    public int itemId;

    @Column(name = "note")
    public String note;

    @Column(name = "create_time")
    public long createTime;

    public static Finder<Long, ReadyBalance> find = new Finder<>(ReadyBalance.class);

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

    public boolean isHandled() {
        return handled;
    }

    public void setHandled(boolean handled) {
        this.handled = handled;
    }

    public double getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(double changeAmount) {
        this.changeAmount = changeAmount;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "ReadyBalance{" +
                "id=" + id +
                ", uid=" + uid +
                ", orderId=" + orderId +
                ", handled=" + handled +
                ", changeAmount=" + changeAmount +
                ", itemId=" + itemId +
                ", note='" + note + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
