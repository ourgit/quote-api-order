package models.postservice;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;

/**
 * 退货订单
 */
@Entity
@Table(name = "v1_order_returns_detail")
public class OrderReturnsDetail extends Model {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "return_id")
    public long returnId;

    @Column(name = "order_detail_id")
    public long orderDetailId;

    @Column(name = "old_price")
    public long oldPrice;

    @Column(name = "product_price")
    public long productPrice;

    @Column(name = "return_amount")
    public long returnAmount;

    @Column(name = "product_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String productName;

    @Column(name = "sku_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String skuName;

    @Column(name = "product_img_url")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String productImgUrl;

    @Column(name = "create_time")
    public long createTime;

    public static Finder<Long, OrderReturnsDetail> find = new Finder<>(OrderReturnsDetail.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getReturnId() {
        return returnId;
    }

    public void setReturnId(long returnId) {
        this.returnId = returnId;
    }

    public long getOrderDetailId() {
        return orderDetailId;
    }

    public void setOrderDetailId(long orderDetailId) {
        this.orderDetailId = orderDetailId;
    }

    public long getReturnAmount() {
        return returnAmount;
    }

    public void setReturnAmount(long returnAmount) {
        this.returnAmount = returnAmount;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getProductImgUrl() {
        return productImgUrl;
    }

    public void setProductImgUrl(String productImgUrl) {
        this.productImgUrl = productImgUrl;
    }

    public long getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(long oldPrice) {
        this.oldPrice = oldPrice;
    }

    public long getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(long productPrice) {
        this.productPrice = productPrice;
    }
}
