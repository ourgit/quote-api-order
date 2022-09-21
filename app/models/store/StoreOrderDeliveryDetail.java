package models.store;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;

@Entity
@Table(name = "v1_store_order_delivery_detail")
public class StoreOrderDeliveryDetail extends Model {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "order_delivery_id")
    public long orderDeliveryId;

    @Column(name = "order_id")
    public long orderId;

    @Column(name = "order_detail_id")
    public long orderDetailId;

    @Column(name = "product_name")
    public String productName;

    @Column(name = "sku_name")
    public String skuName;

    @Column(name = "product_img_url")
    public String productImgUrl;

    @Column(name = "product_price")
    public long productPrice;

    @Column(name = "create_time")
    public long createTime;

    public static Finder<Long, StoreOrderDeliveryDetail> find = new Finder<>(StoreOrderDeliveryDetail.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOrderDeliveryId() {
        return orderDeliveryId;
    }

    public void setOrderDeliveryId(long orderDeliveryId) {
        this.orderDeliveryId = orderDeliveryId;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public long getOrderDetailId() {
        return orderDetailId;
    }

    public void setOrderDetailId(long orderDetailId) {
        this.orderDetailId = orderDetailId;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
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

    public String getProductImgUrl() {
        return productImgUrl;
    }

    public void setProductImgUrl(String productImgUrl) {
        this.productImgUrl = productImgUrl;
    }

    public long getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(long productPrice) {
        this.productPrice = productPrice;
    }
}
