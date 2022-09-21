package models.store;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;

/**
 * 退货订单
 */
@Entity
@Table(name = "v1_store_order_return_refund_form")
public class StoreOrderReturnRefundForm extends Model {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "refund_no")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String refundNo;

    @Column(name = "order_id")
    public long orderId;

    @Column(name = "order_no")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String orderNo;

    @Column(name = "return_apply_id")
    public long returnApplyId;

    @Column(name = "return_no")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String returnNo;

    @Column(name = "uid")
    public long uid;

    @Column(name = "user_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String userName;

    @Column(name = "operator_id")
    public long operatorId;

    @Column(name = "operator_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String operatorName;

    @Column(name = "refund_money")
    public long refundMoney;

    @Column(name = "refund_method")
    public long refundMethod;

    @Column(name = "status")
    public int status;

    @Column(name = "pay_method")
    public int payMethod;

    @Column(name = "tx_data")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String txData;

    @Column(name = "update_time")
    public long updateTime;//'物流更新时间'

    @Column(name = "create_time")
    public long createTime;//'发货时间'

    public static Finder<Long, StoreOrderReturnRefundForm> find = new Finder<>(StoreOrderReturnRefundForm.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRefundNo() {
        return refundNo;
    }

    public void setRefundNo(String refundNo) {
        this.refundNo = refundNo;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public long getRefundMoney() {
        return refundMoney;
    }

    public void setRefundMoney(long refundMoney) {
        this.refundMoney = refundMoney;
    }

    public long getRefundMethod() {
        return refundMethod;
    }

    public void setRefundMethod(long refundMethod) {
        this.refundMethod = refundMethod;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPayMethod() {
        return payMethod;
    }

    public void setPayMethod(int payMethod) {
        this.payMethod = payMethod;
    }

    public String getTxData() {
        return txData;
    }

    public void setTxData(String txData) {
        this.txData = txData;
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

    public long getReturnApplyId() {
        return returnApplyId;
    }

    public void setReturnApplyId(long returnApplyId) {
        this.returnApplyId = returnApplyId;
    }

    public String getReturnNo() {
        return returnNo;
    }

    public void setReturnNo(String returnNo) {
        this.returnNo = returnNo;
    }
}
