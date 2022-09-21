package models.order;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;


@Entity
@Table(name = "v1_order_gift")
public class OrderGift extends Model {


    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "upto")
    public long upto;

    @Column(name = "product_id")
    public long productId;

    @Column(name = "sku_id")
    public long skuId;

    @Column(name = "amount")
    public int amount;

    @Column(name = "gave_amount")
    public long gaveAmount;

    @Column(name = "total_amount")
    public long totalAmount;

    @Column(name = "require_first_order")
    public boolean requireFirstOrder;

    @Column(name = "enable")
    public boolean enable;

    @Column(name = "title")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String title;

    @Column(name = "begin_time")
    public long beginTime;

    @Column(name = "end_time")
    public long endTime;

    public static Finder<Long, OrderGift> find = new Finder<>(OrderGift.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUpto() {
        return upto;
    }

    public void setUpto(long upto) {
        this.upto = upto;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public boolean isRequireFirstOrder() {
        return requireFirstOrder;
    }

    public void setRequireFirstOrder(boolean requireFirstOrder) {
        this.requireFirstOrder = requireFirstOrder;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public long getSkuId() {
        return skuId;
    }

    public void setSkuId(long skuId) {
        this.skuId = skuId;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public long getGaveAmount() {
        return gaveAmount;
    }

    public void setGaveAmount(long gaveAmount) {
        this.gaveAmount = gaveAmount;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "OrderGift{" +
                "id=" + id +
                ", upto=" + upto +
                ", productId=" + productId +
                ", amount=" + amount +
                ", requireFirstOrder=" + requireFirstOrder +
                ", enable=" + enable +
                ", title='" + title + '\'' +
                '}';
    }
}
