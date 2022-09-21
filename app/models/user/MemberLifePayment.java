package models.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;


@Entity
@Table(name = "v1_member_life_payment")
public class MemberLifePayment extends Model {

    public static final int LIFE_TYPE_WATER = 1;
    public static final int LIFE_TYPE_ELECTRIC = 2;
    public static final int LIFE_TYPE_GAS = 3;

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "uid")
    public long uid;

    @Column(name = "life_type")
    public long lifeType;

    @Column(name = "create_time")
    public long createTime;

    @Column(name = "account_no")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String accountNo;

    @Column(name = "account_company")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String accountCompany;

    @Column(name = "account_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String accountName;

    public static Finder<Long, MemberLifePayment> find = new Finder<>(MemberLifePayment.class);

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

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
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

    @Override
    public String toString() {
        return "MemberLifePayment{" +
                "id=" + id +
                ", uid=" + uid +
                ", lifeType=" + lifeType +
                ", createTime=" + createTime +
                ", accountNo='" + accountNo + '\'' +
                ", accountCompany='" + accountCompany + '\'' +
                ", accountName='" + accountName + '\'' +
                '}';
    }
}
