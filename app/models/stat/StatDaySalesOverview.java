package models.stat;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;

/**
 * 平台日业绩
 */
@Entity
@Table(name = "v1_stat_day_sales")
public class StatDaySalesOverview extends Model {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "day")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String day;

    @Column(name = "commission")
    public long commission;//抽成奖励

    @Column(name = "total_money")
    public long totalMoney;//订单总额

    @Column(name = "total_commission")
    public long totalCommission;//总抽成

    @Column(name = "total_cost")
    public long totalCost;//成本

    @Transient
    public String platformCommissonStr;
    @Transient
    public String totalMoneyStr;
    @Transient
    public String totalCommissionStr;

    @Column(name = "create_time")
    public long createdTime;

    public static Finder<Long, StatDaySalesOverview> find = new Finder<>(StatDaySalesOverview.class);

    public void setId(long id) {
        this.id = id;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void setCommission(long commission) {
        this.commission = commission;
    }

    public void setTotalMoney(long totalMoney) {
        this.totalMoney = totalMoney;
    }

    public void setTotalCommission(long totalCommission) {
        this.totalCommission = totalCommission;
    }

    public void setTotalCost(long totalCost) {
        this.totalCost = totalCost;
    }

    public void setPlatformCommissonStr(String platformCommissonStr) {
        this.platformCommissonStr = platformCommissonStr;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public void setTotalMoneyStr(String totalMoneyStr) {
        this.totalMoneyStr = totalMoneyStr;
    }

    public void setTotalCommissionStr(String totalCommissionStr) {
        this.totalCommissionStr = totalCommissionStr;
    }

    @Override
    public String toString() {
        return "StatDaySalesOverview{" +
                "id=" + id +
                ", day='" + day + '\'' +
                ", commission=" + commission +
                ", totalMoney=" + totalMoney +
                ", totalCommission=" + totalCommission +
                ", totalCost=" + totalCost +
                ", platformCommissonStr='" + platformCommissonStr + '\'' +
                ", totalMoneyStr='" + totalMoneyStr + '\'' +
                ", totalCommissionStr='" + totalCommissionStr + '\'' +
                ", createdTime=" + createdTime +
                '}';
    }
}
