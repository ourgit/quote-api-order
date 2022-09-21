package models.dealer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.DateSerializer;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;

/**
 * 经销商
 */
@Entity
@Table(name = "v1_dealer")
public class Dealer extends Model {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "type")
    public int type;//1为自营2为加盟3为其他

    @Column(name = "dealer_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String dealerName;//经销商名字

    @Column(name = "dealer_contact_detail")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String dealerContactDetail;//详细的联系方式

    @Column(name = "join_time")
    @JsonSerialize(using = DateSerializer.class)
    public long joinTime;

    @Column(name = "description")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String description;

    @Column(name = "likes")
    public long likes;

    @Column(name = "update_time")
    @JsonSerialize(using = DateSerializer.class)
    public long updateTime;

    @Column(name = "create_time")
    public long createTime;

    public static Finder<Long, Dealer> find = new Finder<>(Dealer.class);

    public void setId(long id) {
        this.id = id;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setDealerName(String dealerName) {
        this.dealerName = dealerName;
    }

    public void setDealerContactDetail(String dealerContactDetail) {
        this.dealerContactDetail = dealerContactDetail;
    }

    public void setJoinTime(long joinTime) {
        this.joinTime = joinTime;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLikes(long likes) {
        this.likes = likes;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Dealer{" +
                "id=" + id +
                ", type=" + type +
                ", dealerName='" + dealerName + '\'' +
                ", dealerContactDetail='" + dealerContactDetail + '\'' +
                ", joinTime=" + joinTime +
                ", description='" + description + '\'' +
                ", likes=" + likes +
                ", updateTime=" + updateTime +
                ", createTime=" + createTime +
                '}';
    }
}
