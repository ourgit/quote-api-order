package models.shop;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;
import myannotation.EscapeHtmlSerializerForKeepSomeHtml;

import javax.persistence.*;

@Entity
@Table(name = "v1_service")
public class Service extends Model {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "service_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String serviceName;


    @Column(name = "service_icon")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String serviceIcon;

    @Column(name = "service_digest")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String serviceDigest;

    @Column(name = "service_content")
    @JsonDeserialize(using = EscapeHtmlSerializerForKeepSomeHtml.class)
    public String serviceContent;

    @Column(name = "shop_id")
    public long shopId;//联系电话

    @Column(name = "category_id")
    public long categoryId;

    @Column(name = "sort")
    public int sort;

    @Column(name = "update_time")
    public long updateTime;

    @Column(name = "create_time")
    public long createTime;

    public static Finder<Long, Service> find = new Finder<>(Service.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceIcon() {
        return serviceIcon;
    }

    public void setServiceIcon(String serviceIcon) {
        this.serviceIcon = serviceIcon;
    }

    public String getServiceDigest() {
        return serviceDigest;
    }

    public void setServiceDigest(String serviceDigest) {
        this.serviceDigest = serviceDigest;
    }

    public String getServiceContent() {
        return serviceContent;
    }

    public void setServiceContent(String serviceContent) {
        this.serviceContent = serviceContent;
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

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}
