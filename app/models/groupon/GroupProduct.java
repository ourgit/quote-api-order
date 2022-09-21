package models.groupon;

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
@Table(name = "v1_group_product")
public class GroupProduct extends Model {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String name;

    @Column(name = "product_type")
    public int productType;

    @Column(name = "sketch")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String sketch = "";//简述

    @Column(name = "details")
    @JsonDeserialize(using = EscapeHtmlSerializerForKeepSomeHtml.class)
    public String details = "";//商品描述

    @Column(name = "old_price")
    public long oldPrice;

    @Column(name = "price")
    public long price;

    @Column(name = "group_price")
    public long groupPrice;

    @Column(name = "require_users")
    public long requireUsers;

    @Column(name = "require_time")
    public long requireTime;

    @Column(name = "stock")
    public long stock;

    @Column(name = "sold_amount")
    public long soldAmount;//已售数量

    @Column(name = "virtual_amount")
    @JsonIgnore
    public long virtualAmount;

    @Column(name = "poster")
    @JsonDeserialize(using = EscapeHtmlSerializerForKeepSomeHtml.class)
    public String poster = "";

    @Column(name = "carousel")
    @JsonDeserialize(using = EscapeHtmlSerializerForKeepSomeHtml.class)
    public String carousel = "";

    @Column(name = "status")
    public int status;// '状态 -1=>下架,1=>上架,2=>预售,0=>未上架',

    @Column(name = "sort")
    public int sort;


    @Column(name = "update_time")
    public long updateTime;

    @Column(name = "create_time")
    public long createTime;

    @Column(name = "commission")
    public long commission;


    public static Finder<Long, GroupProduct> find = new Finder<>(GroupProduct.class);

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

    public int getProductType() {
        return productType;
    }

    public void setProductType(int productType) {
        this.productType = productType;
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

    public long getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(long oldPrice) {
        this.oldPrice = oldPrice;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
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

    public long getStock() {
        return stock;
    }

    public void setStock(long stock) {
        this.stock = stock;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getCarousel() {
        return carousel;
    }

    public void setCarousel(String carousel) {
        this.carousel = carousel;
    }

    public long getGroupPrice() {
        return groupPrice;
    }

    public void setGroupPrice(long groupPrice) {
        this.groupPrice = groupPrice;
    }

    public long getRequireUsers() {
        return requireUsers;
    }

    public void setRequireUsers(long requireUsers) {
        this.requireUsers = requireUsers;
    }

    public long getRequireTime() {
        return requireTime;
    }

    public void setRequireTime(long requireTime) {
        this.requireTime = requireTime;
    }

    public long getCommission() {
        return commission;
    }

    public void setCommission(long commission) {
        this.commission = commission;
    }
}
