package models.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;
import play.data.validation.Constraints;

import javax.persistence.*;

/**
 * 用户类
 * Created by win7 on 2016/6/7.
 */
@Entity
@Table(name = "v1_member")
public class Member extends Model {
    public static final int MEMBER_STATUS_NOT_ACTIVATE = -1;
    public static final int MEMBER_STATUS_NORMAL = 10;
    public static final int MEMBER_STATUS_LOCK = -10;

    public static final int USER_TYPE_CUSTOMER = 1;
    public static final int USER_TYPE_STAFF = 10;
    public static final int USER_TYPE_MANAGER = 20;

    public static final int AUTH_STATUS_NOT_AUTH = 0;
    public static final int AUTH_STATUS_PROCESSING = 1;
    public static final int AUTH_STATUS_PRE_AUTH = 2;
    public static final int AUTH_STATUS_AUTHED = 3;

    public static final int ACCOUNT_TYPE_PHONE_NUMBER = 1;
    public static final int ACCOUNT_TYPE_EMAIL = 2;

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Constraints.Required
    @Constraints.MinLength(6)
    @Constraints.MaxLength(30)
    @com.fasterxml.jackson.annotation.JsonIgnore
    @Column(name = "login_password")
    public String loginPassword;//登录密码

    @Column(name = "pay_password")
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String payPassword;//支付密码

    @Column(name = "status")
    public int status;//用户状态 1正常2锁定

    @Column(name = "real_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String realName;//真实姓名

    @Column(name = "nick_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String nickName;//昵称

    @Column(name = "account_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String accountName;

    @Column(name = "contact_number")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String contactNumber;

    @Column(name = "create_time")
    public long createdTime;//创建时间

    @Column(name = "avatar")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String avatar;//头像

    @Column(name = "shop_id")
    public long shopId;

    @Column(name = "shop_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String shopName;//头像

    @Column(name = "auth_status")
    public int authStatus;

    @Column(name = "user_type")
    public int userType;

    @Column(name = "expire_time")
    public long expireTime;

    @Column(name = "update_time")
    public long updateTime;

    public static Finder<Long, Member> find = new Finder<>(Member.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public String getPayPassword() {
        return payPassword;
    }

    public void setPayPassword(String payPassword) {
        this.payPassword = payPassword;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
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

    public int getAuthStatus() {
        return authStatus;
    }

    public void setAuthStatus(int authStatus) {
        this.authStatus = authStatus;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }
}
