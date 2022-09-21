package models.order;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;

/**
 * 自取信息表
 */
@Entity
@Table(name = "v1_myself_take_detail")
public class MySelfTakeDetail extends Model {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "uid")
    public long uid;

    @Column(name = "self_take_id")
    public long selfTakeId;

    @Column(name = "name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String name;

    @Column(name = "contact_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String contactName;

    @Column(name = "details")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String details;

    @Column(name = "phone_number")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String telephone;

    @Column(name = "latitude")
    public double latitude;

    @Column(name = "longitude")
    public double longitude;

    @Column(name = "is_default")
    public boolean isDefault;

    public static Finder<Long, MySelfTakeDetail> find = new Finder<>(MySelfTakeDetail.class);


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

    public long getSelfTakeId() {
        return selfTakeId;
    }

    public void setSelfTakeId(long selfTakeId) {
        this.selfTakeId = selfTakeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    @Override
    public String toString() {
        return "MySelfTakeDetail{" +
                "id=" + id +
                ", uid=" + uid +
                ", selfTakeId=" + selfTakeId +
                ", name='" + name + '\'' +
                ", contactName='" + contactName + '\'' +
                ", details='" + details + '\'' +
                ", telephone='" + telephone + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", isDefault=" + isDefault +
                '}';
    }
}
