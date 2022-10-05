package models.bid;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;

/**
 * 商品分类
 */
@Entity
@Table(name = "v1_bid")
public class Bid extends Model {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "service_region")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String serviceRegion;

    @Column(name = "service_address")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String serviceAddress;

    @Column(name = "category_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String categoryName;

    @Column(name = "preference_service_time")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String preferenceServiceTime;

    @Column(name = "service_content")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String serviceContent;

    @Column(name = "contact_mail")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String contactMail;

    @Column(name = "contact_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String contactName;

    @Column(name = "asker_uid")
    public long askerUid;

    @Column(name = "asker_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String askerName;


    @Column(name = "status")
    public int status;

    @Column(name = "taker_uid")
    public long takerUid;

    @Column(name = "taker_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String takerName;
    @Column(name = "price")
    public long price;

    @Column(name = "update_time")
    public long updateTime;

    @Column(name = "create_time")
    public long createTime;

    public static Finder<Long, Bid> find = new Finder<>(Bid.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getServiceRegion() {
        return serviceRegion;
    }

    public void setServiceRegion(String serviceRegion) {
        this.serviceRegion = serviceRegion;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getPreferenceServiceTime() {
        return preferenceServiceTime;
    }

    public void setPreferenceServiceTime(String preferenceServiceTime) {
        this.preferenceServiceTime = preferenceServiceTime;
    }

    public String getServiceContent() {
        return serviceContent;
    }

    public void setServiceContent(String serviceContent) {
        this.serviceContent = serviceContent;
    }

    public String getContactMail() {
        return contactMail;
    }

    public void setContactMail(String contactMail) {
        this.contactMail = contactMail;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public long getAskerUid() {
        return askerUid;
    }

    public void setAskerUid(long askerUid) {
        this.askerUid = askerUid;
    }

    public String getAskerName() {
        return askerName;
    }

    public void setAskerName(String askerName) {
        this.askerName = askerName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTakerUid() {
        return takerUid;
    }

    public void setTakerUid(long takerUid) {
        this.takerUid = takerUid;
    }

    public String getTakerName() {
        return takerName;
    }

    public void setTakerName(String takerName) {
        this.takerName = takerName;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
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
}
