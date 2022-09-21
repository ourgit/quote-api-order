package models.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;

/**
 * 订单详情
 */
@Entity
@Table(name = "v1_order_detail")
public class OrderDetail extends Model {
    public static final int STATUS_NORMAL = 0;
    public static final int STATUS_APPLY_RETURN = 1;
    public static final int STATUS_AGREE_RETURN = 2;
    public static final int STATUS_REFUND = 3;
    public static final int STATUS_DISAGREE_RETURN = -1;
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "order_id")
    public long orderId;

    @Column(name = "shop_id")
    public long shopId;

    @Column(name = "uid")
    public long uid;

    @Column(name = "product_id")
    public long productId;

    @Column(name = "product_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String productName;

    @Column(name = "old_price")
    public long oldPrice;

    @Column(name = "product_price")
    public long productPrice;

    @Column(name = "sku_id")
    public long productSkuId;

    @Column(name = "sku_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String skuName;

    @Column(name = "unit")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String unit;

    @Column(name = "product_img_url")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String productImgUrl;

    @Column(name = "product_mode_desc")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String productModeDesc;//商品型号信息

    @Column(name = "product_mode_params")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String productModeParams;//商品型号参数

    @Column(name = "discount_rate")
    public int discountRate;//折扣比例,95折，就写95

    @Column(name = "discount_amount")
    public long discountAmount;//折扣金额

    @Column(name = "number")
    public long number;//购买数量

    @Column(name = "flash_sale_price")
    public long flashSalePrice;

    @Column(name = "flash_sale_number")
    public long flashSaleNumber;

    @Column(name = "sub_total")
    public long subTotal;//小计金额

    @Column(name = "is_product_available")
    public boolean isProductAvailable;//商品是否有效，0有效，1失效

    @Column(name = "remark")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String remark;//备注

    @Column(name = "source")
    public int source;

    @Column(name = "delivery_method")
    public int deliveryMethod;

    @Column(name = "return_status")
    public long returnStatus;

    @Column(name = "sub_return")
    public long subReturn;

    @Column(name = "shop_category_id")
    public long shopCategoryId;

    @Column(name = "category_id")
    public long categoryId;

    @Column(name = "return_number")
    public long returnNumber;

    @Column(name = "update_time")
    public long updateTime;

    @Column(name = "take_time")
    public long takeTime;

    @Column(name = "create_time")
    public long createTime;

    @Column(name = "status")
    public long status;

    @Column(name = "is_gift")
    private boolean isGift;

    @Column(name = "order_gift_id")
    private long orderGiftId;

    @Column(name = "supplier_id")
    @JsonIgnore
    public long supplierId;

    @Column(name = "bid_price")
    @JsonIgnore
    public long bidPrice;

    @Transient
    public long brandId;

    @Transient
    public boolean joinFlashSale;

    @Transient
    public int productType;

    public static Finder<Long, OrderDetail> find = new Finder<>(OrderDetail.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
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

    public long getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(long productPrice) {
        this.productPrice = productPrice;
    }

    public long getProductSkuId() {
        return productSkuId;
    }

    public void setProductSkuId(long productSkuId) {
        this.productSkuId = productSkuId;
    }

    public String getSkuName() {
        return skuName;
    }

    public void setSkuName(String skuName) {
        this.skuName = skuName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getProductImgUrl() {
        return productImgUrl;
    }

    public void setProductImgUrl(String productImgUrl) {
        this.productImgUrl = productImgUrl;
    }

    public String getProductModeDesc() {
        return productModeDesc;
    }

    public void setProductModeDesc(String productModeDesc) {
        this.productModeDesc = productModeDesc;
    }

    public String getProductModeParams() {
        return productModeParams;
    }

    public void setProductModeParams(String productModeParams) {
        this.productModeParams = productModeParams;
    }

    public int getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(int discountRate) {
        this.discountRate = discountRate;
    }

    public long getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(long discountAmount) {
        this.discountAmount = discountAmount;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(long number) {
        this.number = number;
    }

    public long getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(long subTotal) {
        this.subTotal = subTotal;
    }

    public boolean isProductAvailable() {
        return isProductAvailable;
    }

    public void setProductAvailable(boolean productAvailable) {
        isProductAvailable = productAvailable;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getBrandId() {
        return brandId;
    }

    public void setBrandId(long brandId) {
        this.brandId = brandId;
    }

    public long getFlashSalePrice() {
        return flashSalePrice;
    }

    public void setFlashSalePrice(long flashSalePrice) {
        this.flashSalePrice = flashSalePrice;
    }

    public long getFlashSaleNumber() {
        return flashSaleNumber;
    }

    public void setFlashSaleNumber(long flashSaleNumber) {
        this.flashSaleNumber = flashSaleNumber;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getReturnStatus() {
        return returnStatus;
    }

    public void setReturnStatus(long returnStatus) {
        this.returnStatus = returnStatus;
    }

    public long getSubReturn() {
        return subReturn;
    }

    public void setSubReturn(long subReturn) {
        this.subReturn = subReturn;
    }

    public long getReturnNumber() {
        return returnNumber;
    }

    public void setReturnNumber(long returnNumber) {
        this.returnNumber = returnNumber;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public long getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(long supplierId) {
        this.supplierId = supplierId;
    }

    public long getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(long bidPrice) {
        this.bidPrice = bidPrice;
    }

    public long getTakeTime() {
        return takeTime;
    }

    public void setTakeTime(long takeTime) {
        this.takeTime = takeTime;
    }

    public long getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(long oldPrice) {
        this.oldPrice = oldPrice;
    }

    public int getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(int deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public boolean isGift() {
        return isGift;
    }

    public void setGift(boolean gift) {
        isGift = gift;
    }

    public boolean isJoinFlashSale() {
        return joinFlashSale;
    }

    public void setJoinFlashSale(boolean joinFlashSale) {
        this.joinFlashSale = joinFlashSale;
    }

    public int getProductType() {
        return productType;
    }

    public void setProductType(int productType) {
        this.productType = productType;
    }

    public long getOrderGiftId() {
        return orderGiftId;
    }

    public void setOrderGiftId(long orderGiftId) {
        this.orderGiftId = orderGiftId;
    }

    public long getShopId() {
        return shopId;
    }

    public void setShopId(long shopId) {
        this.shopId = shopId;
    }

    public long getShopCategoryId() {
        return shopCategoryId;
    }

    public void setShopCategoryId(long shopCategoryId) {
        this.shopCategoryId = shopCategoryId;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }
}
