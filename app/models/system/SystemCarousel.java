package models.system;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 轮播表
 */
@Entity
@Table(name = "v1_system_carousel")
public class SystemCarousel extends Model implements Serializable {
    private static final long serialVersionUID = 4087383400213282737L;
    public static final int TYPE_PC = 1;
    public static final int TYPE_MOBILE = 2;

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    private String name;//轮播链接名称

    @Column(name = "img_url")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    private String imgUrl;//图片链接地址

    @Column(name = "link_url")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    private String linkUrl;//轮播链接地址

    @Column(name = "mobile_img_url")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    private String mobileImgUrl;//图片链接地址

    @Column(name = "mobile_link_url")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    private String mobileLinkUrl;//轮播链接地址

    @Column(name = "type")
    private int type;//类型

    @Column(name = "sort")
    private int displayOrder;//排序，倒序

    @Column(name = "need_show")
    private boolean needShow;

    @Column(name = "title1")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    private String title1;

    @Column(name = "title2")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    private String title2;

    @Column(name = "note")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    private String description;//备注

    @Column(name = "region_code")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String regionCode;

    @Column(name = "region_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String regionName;

    @Column(name = "update_time")
    private long updateTime;

    @Column(name = "create_time")
    private long createdTime;


    public static Finder<Integer, SystemCarousel> find = new Finder<>(SystemCarousel.class);

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getMobileImgUrl() {
        return mobileImgUrl;
    }

    public void setMobileImgUrl(String mobileImgUrl) {
        this.mobileImgUrl = mobileImgUrl;
    }

    public String getMobileLinkUrl() {
        return mobileLinkUrl;
    }

    public void setMobileLinkUrl(String mobileLinkUrl) {
        this.mobileLinkUrl = mobileLinkUrl;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isNeedShow() {
        return needShow;
    }

    public void setNeedShow(boolean needShow) {
        this.needShow = needShow;
    }

    public String getTitle1() {
        return title1;
    }

    public void setTitle1(String title1) {
        this.title1 = title1;
    }

    public String getTitle2() {
        return title2;
    }

    public void setTitle2(String title2) {
        this.title2 = title2;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    @Override
    public String toString() {
        return "SystemCarousel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", linkUrl='" + linkUrl + '\'' +
                ", mobileImgUrl='" + mobileImgUrl + '\'' +
                ", mobileLinkUrl='" + mobileLinkUrl + '\'' +
                ", type=" + type +
                ", displayOrder=" + displayOrder +
                ", needShow=" + needShow +
                ", title1='" + title1 + '\'' +
                ", title2='" + title2 + '\'' +
                ", description='" + description + '\'' +
                ", regionCode='" + regionCode + '\'' +
                ", regionName='" + regionName + '\'' +
                ", updateTime=" + updateTime +
                ", createdTime=" + createdTime +
                '}';
    }
}
