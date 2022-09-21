package models.logistics;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.DateSerializer;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;

/**
 * 物流
 */
@Entity
@Table(name = "v1_logistics")
public class Logistics extends Model {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "order_id")
    public long orderId;

    @Column(name = "express_no")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String expressNo;

    @Column(name = "consignee_realname")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String consigneeRealname;//收货人姓名

    @Column(name = "consignee_phone_number")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String consigneePhoneNumber;//收货人电话

    @Column(name = "consignee_phone_number2")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String consigneePhoneNumber2;//收货人电话2

    @Column(name = "consignee_address")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String consigneeAddress;//收货地址

    @Column(name = "consignee_postcode")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String consigneePostcode;

    @Column(name = "logistics_type")
    public int logisticsType;//'物流方式'

    @Column(name = "logistics_fee")
    public long logisticsFee;//''物流发货运费''

    @Column(name = "logistics_status")
    public int logisticsStatus;//'物流状态'

    @Column(name = "logistics_settlement_status")
    public int logisticsSettlementStatus;//'物流结算状态'

    @Column(name = "logistics_last_desc")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String logisticsLastDesc;//'物流最后状态描述'

    @Column(name = "logistics_desc")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String logisticsDesc;//'物流描述'

    @Column(name = "settlement_time")
    @JsonSerialize(using = DateSerializer.class)
    public long settlementTime;//'物流结算时间'

    @Column(name = "update_time")
    @JsonSerialize(using = DateSerializer.class)
    public long updateTime;//'物流更新时间'

    @Column(name = "create_time")
    @JsonSerialize(using = DateSerializer.class)
    public long createTime;//'发货时间'

    public static Finder<Long, Logistics> find = new Finder<>(Logistics.class);

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

    public String getExpressNo() {
        return expressNo;
    }

    public void setExpressNo(String expressNo) {
        this.expressNo = expressNo;
    }

    public String getConsigneeRealname() {
        return consigneeRealname;
    }

    public void setConsigneeRealname(String consigneeRealname) {
        this.consigneeRealname = consigneeRealname;
    }

    public String getConsigneePhoneNumber() {
        return consigneePhoneNumber;
    }

    public void setConsigneePhoneNumber(String consigneePhoneNumber) {
        this.consigneePhoneNumber = consigneePhoneNumber;
    }

    public String getConsigneePhoneNumber2() {
        return consigneePhoneNumber2;
    }

    public void setConsigneePhoneNumber2(String consigneePhoneNumber2) {
        this.consigneePhoneNumber2 = consigneePhoneNumber2;
    }

    public String getConsigneeAddress() {
        return consigneeAddress;
    }

    public void setConsigneeAddress(String consigneeAddress) {
        this.consigneeAddress = consigneeAddress;
    }

    public String getConsigneePostcode() {
        return consigneePostcode;
    }

    public void setConsigneePostcode(String consigneePostcode) {
        this.consigneePostcode = consigneePostcode;
    }

    public int getLogisticsType() {
        return logisticsType;
    }

    public void setLogisticsType(int logisticsType) {
        this.logisticsType = logisticsType;
    }

    public long getLogisticsFee() {
        return logisticsFee;
    }

    public void setLogisticsFee(long logisticsFee) {
        this.logisticsFee = logisticsFee;
    }

    public int getLogisticsStatus() {
        return logisticsStatus;
    }

    public void setLogisticsStatus(int logisticsStatus) {
        this.logisticsStatus = logisticsStatus;
    }

    public int getLogisticsSettlementStatus() {
        return logisticsSettlementStatus;
    }

    public void setLogisticsSettlementStatus(int logisticsSettlementStatus) {
        this.logisticsSettlementStatus = logisticsSettlementStatus;
    }

    public String getLogisticsLastDesc() {
        return logisticsLastDesc;
    }

    public void setLogisticsLastDesc(String logisticsLastDesc) {
        this.logisticsLastDesc = logisticsLastDesc;
    }

    public String getLogisticsDesc() {
        return logisticsDesc;
    }

    public void setLogisticsDesc(String logisticsDesc) {
        this.logisticsDesc = logisticsDesc;
    }

    public long getSettlementTime() {
        return settlementTime;
    }

    public void setSettlementTime(long settlementTime) {
        this.settlementTime = settlementTime;
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

    @Override
    public String toString() {
        return "Logistics{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", expressNo='" + expressNo + '\'' +
                ", consigneeRealname='" + consigneeRealname + '\'' +
                ", consigneePhoneNumber='" + consigneePhoneNumber + '\'' +
                ", consigneePhoneNumber2='" + consigneePhoneNumber2 + '\'' +
                ", consigneeAddress='" + consigneeAddress + '\'' +
                ", consigneePostcode='" + consigneePostcode + '\'' +
                ", logisticsType=" + logisticsType +
                ", logisticsFee=" + logisticsFee +
                ", logisticsStatus=" + logisticsStatus +
                ", logisticsSettlementStatus=" + logisticsSettlementStatus +
                ", logisticsLastDesc='" + logisticsLastDesc + '\'' +
                ", logisticsDesc='" + logisticsDesc + '\'' +
                ", settlementTime=" + settlementTime +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                '}';
    }
}
