package models.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;


@Entity
@Table(name = "v1_member_lift_payment_log")
public class MemberLifePaymentLog extends Model {
    public static final int STATUS_UNPAY = 1;
    public static final int STATUS_PAID = 2;
    public static final int STATUS_CHARGED = 3;
    public static final int STATUS_CHARGE_FAIL = 4;
    public static final int STATUS_CHARGE_FAIL_REFUND = 5;

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "uid")
    public long uid;

    @Column(name = "life_type")
    public long lifeType;

    @Column(name = "amount")
    public long amount;

    @Column(name = "payment_method")
    public long paymentMethod;

    @Column(name = "order_id")
    public long orderId;

    @Column(name = "tx_no")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String txNo;

    @Column(name = "third_tx_no")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String thirdTxNo;

    @Column(name = "create_time")
    public long createTime;

    @Column(name = "pay_time")
    public long payTime;

    @Column(name = "status")
    public int status;

    @Column(name = "account_no")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String accountNo;

    @Column(name = "account_company")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String accountCompany;

    @Column(name = "account_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String accountName;

    public static Finder<Long, MemberLifePaymentLog> find = new Finder<>(MemberLifePaymentLog.class);

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

    public long getLifeType() {
        return lifeType;
    }

    public void setLifeType(long lifeType) {
        this.lifeType = lifeType;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(long paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTxNo() {
        return txNo;
    }

    public void setTxNo(String txNo) {
        this.txNo = txNo;
    }

    public String getThirdTxNo() {
        return thirdTxNo;
    }

    public void setThirdTxNo(String thirdTxNo) {
        this.thirdTxNo = thirdTxNo;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getPayTime() {
        return payTime;
    }

    public void setPayTime(long payTime) {
        this.payTime = payTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getAccountCompany() {
        return accountCompany;
    }

    public void setAccountCompany(String accountCompany) {
        this.accountCompany = accountCompany;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }
}
