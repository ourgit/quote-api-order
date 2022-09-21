package models.log;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "v1_service_balance_log")
public class ServiceBalanceLog extends Model implements Serializable {
    private static final long serialVersionUID = -1885841224604019263L;

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "uid")
    public long uid;//用户id

    @Column(name = "service_id")
    public long serviceId;

    @Column(name = "shop_id")
    public long shopId;

    @Column(name = "shop_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String shopName;

    @Column(name = "staff_id")
    public long staffId;

    @Column(name = "staff_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String staffName;

    @Column(name = "moment_left_balance")
    public int momentLeftBalance;

    @Column(name = "moment_used_balance")
    public int momentUsedBalance;

    @Column(name = "moment_total_balance")
    public int momentTotalBalance;

    @Column(name = "change_amount")
    public int changeAmount;

    @Column(name = "biz_type")
    public int bizType;

    @Column(name = "note")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String note;

    @Column(name = "create_time")
    public long createTime;//创建时间

    public static Finder<Long, ServiceBalanceLog> find = new Finder<>(ServiceBalanceLog.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getServiceId() {
        return serviceId;
    }

    public void setServiceId(long serviceId) {
        this.serviceId = serviceId;
    }

    public int getMomentLeftBalance() {
        return momentLeftBalance;
    }

    public void setMomentLeftBalance(int momentLeftBalance) {
        this.momentLeftBalance = momentLeftBalance;
    }

    public int getMomentUsedBalance() {
        return momentUsedBalance;
    }

    public void setMomentUsedBalance(int momentUsedBalance) {
        this.momentUsedBalance = momentUsedBalance;
    }

    public int getMomentTotalBalance() {
        return momentTotalBalance;
    }

    public void setMomentTotalBalance(int momentTotalBalance) {
        this.momentTotalBalance = momentTotalBalance;
    }

    public int getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(int changeAmount) {
        this.changeAmount = changeAmount;
    }

    public int getBizType() {
        return bizType;
    }

    public void setBizType(int bizType) {
        this.bizType = bizType;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getShopId() {
        return shopId;
    }

    public void setShopId(long shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public long getStaffId() {
        return staffId;
    }

    public void setStaffId(long staffId) {
        this.staffId = staffId;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    @Override
    public String toString() {
        return "ServiceBalanceLog{" +
                "id=" + id +
                ", uid=" + uid +
                ", serviceId=" + serviceId +
                ", shopId=" + shopId +
                ", shopName='" + shopName + '\'' +
                ", staffId=" + staffId +
                ", staffName='" + staffName + '\'' +
                ", momentLeftBalance=" + momentLeftBalance +
                ", momentUsedBalance=" + momentUsedBalance +
                ", momentTotalBalance=" + momentTotalBalance +
                ", changeAmount=" + changeAmount +
                ", bizType=" + bizType +
                ", note='" + note + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
