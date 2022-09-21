package models.log;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;

/**
 * 登录日志表,以用户id来分表
 */
@Entity
@Table(name = "v1_login_log")
public class LoginLog extends Model {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "uid")
    public long uid;

    @Column(name = "ip")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String ip;//登录ip

    @Column(name = "place")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String place;//登录地点

    @Column(name = "create_time")
    public long createTime;//登录时间

    public static Finder<Long, LoginLog> find = new Finder<>(LoginLog.class);

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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}
