package models.dsp;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;
import java.util.List;

/**
 * 区域经销商
 * Created by win7 on 2016/6/7.
 */
@Entity
@Table(name = "v1_area_dealer")
public class AreaDealer extends Model {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "parent_id")
    public long parentId;//父类目ID=0时，代表的是一级的类目

    @Column(name = "merchant_id")
    public long merchantId;//商品ID

    @Column(name = "is_parent")
    public boolean isParent;//该类目是否为父类目，1为true，0为false

    @Column(name = "title")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String title;

    @Column(name = "path")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String path;

    @Column(name = "ratio")
    public double ratio;//抽成比例

    @Column(name = "uid")
    public long uid;

    @Column(name = "region_code")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String regionCode;

    @Column(name = "user_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String userName;

    @Column(name = "update_time")
    public long updateTime;

    @Column(name = "create_time")
    public long createTime;

    @Transient
    public List<AreaDealer> children;

    @Transient
    public String ratioStr;

    @Transient
    public String salesAmount;

    public static Finder<Long, AreaDealer> find = new Finder<>(AreaDealer.class);

    public void setId(long id) {
        this.id = id;
    }

    public void setParentId(long parentId) {
        this.parentId = parentId;
    }

    public void setParent(boolean parent) {
        isParent = parent;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public void setChildren(List<AreaDealer> children) {
        this.children = children;
    }

    public void setRatioStr(String ratioStr) {
        this.ratioStr = ratioStr;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public void setSalesAmount(String salesAmount) {
        this.salesAmount = salesAmount;
    }

    public void setMerchantId(long merchantId) {
        this.merchantId = merchantId;
    }

    @Override
    public String toString() {
        return "AreaDealer{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", productId=" + merchantId +
                ", isParent=" + isParent +
                ", title='" + title + '\'' +
                ", path='" + path + '\'' +
                ", ratio=" + ratio +
                ", uid=" + uid +
                ", regionCode='" + regionCode + '\'' +
                ", userName='" + userName + '\'' +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                ", children=" + children +
                ", ratioStr='" + ratioStr + '\'' +
                ", salesAmount='" + salesAmount + '\'' +
                '}';
    }
}
