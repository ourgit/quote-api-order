package models.store;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;
import myannotation.EscapeHtmlSerializerForKeepSomeHtml;

import javax.persistence.*;

/**
 * 商品表
 */
@Entity
@Table(name = "v1_store_product")
public class StoreProduct extends Model {
    //state 审核状态 -1 审核失败 0 未审核 1 审核成功
    //status 商品状态 -1=>下架,1=>上架,2=>预售,0=>草稿',
    public static final int STATUS_OFF_SHELVE = -1;
    public static final int STATUS_ON_SHELVE = 1;
    public static final int STATUS_PRE_SALE = 2;
    public static final int STATUS_DRAFT = 3;


    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String name;

    @Column(name = "shop_id")
    public long shopId;

    @Column(name = "category_id")
    public long categoryId;

    @Column(name = "shop_name")
    public String shopName;

    @Column(name = "sketch")
    public String sketch;

    @Column(name = "details")
    @JsonDeserialize(using = EscapeHtmlSerializerForKeepSomeHtml.class)
    public String details = "";//商品描述

    @Column(name = "keywords")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String keywords = "";

    @Column(name = "tag")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String tag = "";

    @Column(name = "price")
    public long price;

    @Column(name = "old_price")
    public long oldPrice;

    @Column(name = "min_price")
    public long minPrice;

    @Column(name = "max_price")
    public long maxPrice;

    @Column(name = "sold_amount")
    public long soldAmount;//已售数量

    @Column(name = "virtual_amount")
    @JsonIgnore
    public long virtualAmount;

    @Column(name = "cover_img_url")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String coverImgUrl = "";//''封面图''

    @Column(name = "poster")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String poster = "";

    @Column(name = "status")
    public int status;// '状态 -1=>下架,1=>上架,2=>预售,0=>未上架',

    @Column(name = "state")
    public int state;// '审核状态 -1 审核失败 0 未审核 1 审核成功',

    @Column(name = "sort")
    public int sort;

    @Column(name = "stock")
    public long stock;

    @Column(name = "warning_stock")
    public long warningStock;

    @Column(name = "cover_frame_id")
    public long coverFrameId;

    @Column(name = "cover_frame_url")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String coverFrameUrl = "";

    @Column(name = "unit")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String unit = "";

    @Column(name = "delivery_methods")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String deliveryMethods = "";


    @Column(name = "update_time")
    public long updateTime;

    @Column(name = "create_time")
    public long createTime;

    @Column(name = "enable_free_mail")
    public boolean enableFreeMail;

    @Column(name = "sku")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String sku = "";

    @Column(name = "param")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String param = "";

    @Column(name = "attr")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String attr = "";

    @Column(name = "album")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String album = "";

    public static Finder<Long, StoreProduct> find = new Finder<>(StoreProduct.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getShopId() {
        return shopId;
    }

    public void setShopId(long shopId) {
        this.shopId = shopId;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getSketch() {
        return sketch;
    }

    public void setSketch(String sketch) {
        this.sketch = sketch;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public long getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(long oldPrice) {
        this.oldPrice = oldPrice;
    }

    public long getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(long minPrice) {
        this.minPrice = minPrice;
    }

    public long getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(long maxPrice) {
        this.maxPrice = maxPrice;
    }

    public long getSoldAmount() {
        return soldAmount;
    }

    public void setSoldAmount(long soldAmount) {
        this.soldAmount = soldAmount;
    }

    public long getVirtualAmount() {
        return virtualAmount;
    }

    public void setVirtualAmount(long virtualAmount) {
        this.virtualAmount = virtualAmount;
    }

    public String getCoverImgUrl() {
        return coverImgUrl;
    }

    public void setCoverImgUrl(String coverImgUrl) {
        this.coverImgUrl = coverImgUrl;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public long getStock() {
        return stock;
    }

    public void setStock(long stock) {
        this.stock = stock;
    }

    public long getWarningStock() {
        return warningStock;
    }

    public void setWarningStock(long warningStock) {
        this.warningStock = warningStock;
    }

    public long getCoverFrameId() {
        return coverFrameId;
    }

    public void setCoverFrameId(long coverFrameId) {
        this.coverFrameId = coverFrameId;
    }

    public String getCoverFrameUrl() {
        return coverFrameUrl;
    }

    public void setCoverFrameUrl(String coverFrameUrl) {
        this.coverFrameUrl = coverFrameUrl;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDeliveryMethods() {
        return deliveryMethods;
    }

    public void setDeliveryMethods(String deliveryMethods) {
        this.deliveryMethods = deliveryMethods;
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

    public boolean isEnableFreeMail() {
        return enableFreeMail;
    }

    public void setEnableFreeMail(boolean enableFreeMail) {
        this.enableFreeMail = enableFreeMail;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public String getAttr() {
        return attr;
    }

    public void setAttr(String attr) {
        this.attr = attr;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }
}
