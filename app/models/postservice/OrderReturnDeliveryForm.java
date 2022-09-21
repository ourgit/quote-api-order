package models.postservice;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;

/**
 * 退货订单
 */
@Entity
@Table(name = "v1_order_return_delivery_form")
public class OrderReturnDeliveryForm extends Model {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "delivery_no")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String deliveryNo;

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

    @Column(name = "express_no")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String expressNo;

    @Column(name = "express_company")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String expressCompany;

    @Column(name = "contact_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String contactName;

    @Column(name = "contact_address")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String contactAddress;

    @Column(name = "contact_phone_number")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String contactPhoneNumber;

    @Column(name = "update_time")
    public long updateTime;//'物流更新时间'

    @Column(name = "create_time")
    public long createTime;//'发货时间'

    public static Finder<Long, OrderReturnDeliveryForm> find = new Finder<>(OrderReturnDeliveryForm.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDeliveryNo() {
        return deliveryNo;
    }

    public void setDeliveryNo(String deliveryNo) {
        this.deliveryNo = deliveryNo;
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

    public String getExpressNo() {
        return expressNo;
    }

    public void setExpressNo(String expressNo) {
        this.expressNo = expressNo;
    }

    public String getExpressCompany() {
        return expressCompany;
    }

    public void setExpressCompany(String expressCompany) {
        this.expressCompany = expressCompany;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactAddress() {
        return contactAddress;
    }

    public void setContactAddress(String contactAddress) {
        this.contactAddress = contactAddress;
    }

    public String getContactPhoneNumber() {
        return contactPhoneNumber;
    }

    public void setContactPhoneNumber(String contactPhoneNumber) {
        this.contactPhoneNumber = contactPhoneNumber;
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
