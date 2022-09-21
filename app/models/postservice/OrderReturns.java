package models.postservice;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.ebean.Finder;
import io.ebean.Model;
import models.order.Order;
import myannotation.DateSerializer;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 退货订单
 */
@Entity
@Table(name = "v1_store_order_returns")
public class OrderReturns extends Model {
    public static final int STATUS_REJECT_REFUND = -3;
    public static final int STATUS_REJECT = -2;//申请退货被驳回
    public static final int STATUS_CANCEL_APPLY = -1;//取消退货
    public static final int STATUS_RETURN_TO_AUDIT = 10;
    public static final int STATUS_RETURN_TO_DELIVERY_BACK = 20;
    public static final int STATUS_RETURN_TO_REFUND = 30;
    public static final int STATUS_AGREE_REFUND = 40;
    public static final int STATUS_REFUND = 50;//已退款
    public static final int STATUS_FINISHED = 200;//处理完毕

    public static final int STATE_REFUND = 1;//仅退款
    public static final int STATE_RETURN_REFUND = 2;//退货退款

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "returns_no")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String returnsNo;

    @Column(name = "return_tx")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String returnTx;

    @Column(name = "shop_id")
    public long shopId;

    @Column(name = "order_id")
    public long orderId;

    @Column(name = "order_no")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String orderNo;

    @Column(name = "order_detail_id")
    public long orderDetailId;//子订单编码

    @Column(name = "express_no")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String expressNo;

    @Column(name = "consignee_realname")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String consigneeRealname;//收货人姓名

    @Column(name = "consignee_phone_number")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String consigneePhoneNumber;//收货人电话

    @Column(name = "consignee_address")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String consigneeAddress;//收货地址

    @Column(name = "consignee_postcode")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String consigneePostcode;

    @Column(name = "logis_name")
    public String logisName;

    @Column(name = "state")
    public int state;//0 仅退款 1退货退款

    @Column(name = "pre_status")
    public int preStatus;

    @Column(name = "status")
    public int status;

    @Column(name = "uid")
    public long uid;

    @Column(name = "operator_id")
    public long operatorId;

    @Column(name = "operator_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String operatorName;

    @Column(name = "logistics_last_desc")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String logisticsLastDesc;//'物流最后状态描述'

    @Column(name = "logistics_desc")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String logistics_desc;//'物流描述'

    @Column(name = "return_type")
    public int returnType;//'0全部退单 1部分退单' DEFAULT '0'

    @Column(name = "handling_way")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String handlingWay;//'PUPAWAY:退货入库;REDELIVERY:重新发货;RECLAIM-REDELIVERY:不要求归还并重新发货; REFUND:退款; COMPENSATION:不退货并赔偿',

    @Column(name = "return_money")
    public long returnMoney;//'退款金额'

    @Column(name = "total_return_number")
    public long totalReturnNumber;

    @Column(name = "return_submit_time")
    public long returnSubmitTime;//'退货申请时间'

    @Column(name = "handling_return_time")
    public long handlingReturnTime;

    @Column(name = "return_submit_end_time")
    public long returnSubmitEndTime;

    @Column(name = "handling_apply_end_time")
    public long handlingApplyEndTime;

    @Column(name = "handling_return_end_time")
    public long handlingReturnEndTime;

    @Column(name = "handling_refund_end_time")
    public long handlingRefundEndTime;

    @Column(name = "handling_refund_time")
    public long handlingRefundTime;

    @Column(name = "reason")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String reason;

    @Column(name = "remark")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String remark;

    @Column(name = "audit_reason")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String auditReason;

    @Column(name = "audit_remark")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String auditRemark;

    @Column(name = "update_time")
    @JsonSerialize(using = DateSerializer.class)
    public long updateTime;//'物流更新时间'

    @Column(name = "create_time")
    @JsonSerialize(using = DateSerializer.class)
    public long createTime;//'发货时间'

    @Transient
    public Order order;

    @Transient
    public List<OrderReturnsDetail> orderReturnsDetailList = new ArrayList<>();

    public static Finder<Long, OrderReturns> find = new Finder<>(OrderReturns.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getReturnsNo() {
        return returnsNo;
    }

    public void setReturnsNo(String returnsNo) {
        this.returnsNo = returnsNo;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public long getOrderDetailId() {
        return orderDetailId;
    }

    public void setOrderDetailId(long orderDetailId) {
        this.orderDetailId = orderDetailId;
    }

    public String getExpressNo() {
        return expressNo;
    }

    public void setExpressNo(String expressNo) {
        this.expressNo = expressNo;
    }

    public String getLogisName() {
        return logisName;
    }

    public void setLogisName(String logisName) {
        this.logisName = logisName;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getPreStatus() {
        return preStatus;
    }

    public void setPreStatus(int preStatus) {
        this.preStatus = preStatus;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getLogisticsLastDesc() {
        return logisticsLastDesc;
    }

    public void setLogisticsLastDesc(String logisticsLastDesc) {
        this.logisticsLastDesc = logisticsLastDesc;
    }

    public String getLogistics_desc() {
        return logistics_desc;
    }

    public void setLogistics_desc(String logistics_desc) {
        this.logistics_desc = logistics_desc;
    }

    public int getReturnType() {
        return returnType;
    }

    public void setReturnType(int returnType) {
        this.returnType = returnType;
    }

    public String getHandlingWay() {
        return handlingWay;
    }

    public void setHandlingWay(String handlingWay) {
        this.handlingWay = handlingWay;
    }

    public double getReturnMoney() {
        return returnMoney;
    }

    public void setReturnMoney(long returnMoney) {
        this.returnMoney = returnMoney;
    }

    public long getReturnSubmitTime() {
        return returnSubmitTime;
    }

    public void setReturnSubmitTime(long returnSubmitTime) {
        this.returnSubmitTime = returnSubmitTime;
    }

    public long getHandlingReturnTime() {
        return handlingReturnTime;
    }

    public void setHandlingReturnTime(long handlingReturnTime) {
        this.handlingReturnTime = handlingReturnTime;
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

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
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

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public long getReturnSubmitEndTime() {
        return returnSubmitEndTime;
    }

    public void setReturnSubmitEndTime(long returnSubmitEndTime) {

        this.returnSubmitEndTime = returnSubmitEndTime;
    }

    public long getHandlingReturnEndTime() {
        return handlingReturnEndTime;
    }

    public void setHandlingReturnEndTime(long handlingReturnEndTime) {
        this.handlingReturnEndTime = handlingReturnEndTime;
    }

    public long getHandlingApplyEndTime() {
        return handlingApplyEndTime;
    }

    public void setHandlingApplyEndTime(long handlingApplyEndTime) {
        this.handlingApplyEndTime = handlingApplyEndTime;
    }

    public long getHandlingRefundEndTime() {
        return handlingRefundEndTime;
    }

    public void setHandlingRefundEndTime(long handlingRefundEndTime) {
        this.handlingRefundEndTime = handlingRefundEndTime;
    }

    public long getHandlingRefundTime() {
        return handlingRefundTime;
    }

    public void setHandlingRefundTime(long handlingRefundTime) {
        this.handlingRefundTime = handlingRefundTime;
    }

    public String getReturnTx() {
        return returnTx;
    }

    public void setReturnTx(String returnTx) {
        this.returnTx = returnTx;
    }

    public String getAuditReason() {
        return auditReason;
    }

    public void setAuditReason(String auditReason) {
        this.auditReason = auditReason;
    }

    public String getAuditRemark() {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark) {
        this.auditRemark = auditRemark;
    }

    public long getShopId() {
        return shopId;
    }

    public void setShopId(long shopId) {
        this.shopId = shopId;
    }

    public long getTotalReturnNumber() {
        return totalReturnNumber;
    }

    public void setTotalReturnNumber(long totalReturnNumber) {
        this.totalReturnNumber = totalReturnNumber;
    }
}
