package models.order;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;

/**
 * 订单详情
 */
@Entity
@Table(name = "v1_supplier_order_stat")
public class SupplierOrderStat extends Model {

    public static final int STATUS_CANCELED = -1;
    public static final int STATUS_NOT_SELLTE = 1;
    public static final int STATUS_SELLTED = 2;
    public static final int STATUS_TRANSFERED = 3;

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "supplier_id")
    public long supplierId;

    @Column(name = "product_id")
    public long productId;

    @Column(name = "order_id")
    public long orderId;

    @Column(name = "product_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String productName;

    @Column(name = "img_url")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String imgUrl;

    @Column(name = "sku_id")
    public long skuId;

    @Column(name = "sku_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String skuName;

    @Column(name = "total_count")
    public long totalCount;

    @Column(name = "post_service_count")
    public long postServiceCount;

    @Column(name = "post_service_orders")
    public long postServiceOrders;

    @Column(name = "post_service_money")
    public long postServiceMoney;

    @Column(name = "total_money")
    public long totalMoney;

    @Column(name = "quality_percentage")
    public double qualityPercentage;

    @Column(name = "status")
    public long status;

    @Column(name = "create_time")
    public long createTime;

    public static Finder<Long, SupplierOrderStat> find = new Finder<>(SupplierOrderStat.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(long supplierId) {
        this.supplierId = supplierId;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public long getSkuId() {
        return skuId;
    }

    public void setSkuId(long skuId) {
        this.skuId = skuId;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public long getPostServiceCount() {
        return postServiceCount;
    }

    public void setPostServiceCount(long postServiceCount) {
        this.postServiceCount = postServiceCount;
    }

    public long getPostServiceOrders() {
        return postServiceOrders;
    }

    public void setPostServiceOrders(long postServiceOrders) {
        this.postServiceOrders = postServiceOrders;
    }

    public long getPostServiceMoney() {
        return postServiceMoney;
    }

    public void setPostServiceMoney(long postServiceMoney) {
        this.postServiceMoney = postServiceMoney;
    }

    public long getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(long totalMoney) {
        this.totalMoney = totalMoney;
    }

    public double getQualityPercentage() {
        return qualityPercentage;
    }

    public void setQualityPercentage(double qualityPercentage) {
        this.qualityPercentage = qualityPercentage;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "SupplierOrderStat{" +
                "id=" + id +
                ", supplierId=" + supplierId +
                ", productId=" + productId +
                ", orderId=" + orderId +
                ", productName='" + productName + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", skuId=" + skuId +
                ", skuName='" + skuName + '\'' +
                ", totalCount=" + totalCount +
                ", postServiceCount=" + postServiceCount +
                ", postServiceOrders=" + postServiceOrders +
                ", postServiceMoney=" + postServiceMoney +
                ", totalMoney=" + totalMoney +
                ", qualityPercentage=" + qualityPercentage +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }
}
