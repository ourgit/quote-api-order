package models.team;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 团里团购
 */
@Entity
@Table(name = "v1_team_groupon")
public class TeamGroupon extends Model {

    //1进行中 2已结束  3可团优惠 4不可团优惠 5已结算
    public static final int STATUS_NORMAL = 1;
    public static final int STATUS_END = 2;
    public static final int STATUS_QUALIFIED = 3;
    public static final int STATUS_NOT_QUALIFIED = 4;
    public static final int STATUS_REFUNDED = 5;
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "begin_time")
    public long beginTime;

    @Column(name = "end_time")
    public long endTime;

    @Column(name = "team_id")
    public long teamId;

    @Column(name = "team_name")
    public String teamName;

    @Column(name = "launcher_uid")
    public long launcherUid;

    @Column(name = "status")
    public int status;

    @Column(name = "join_members")
    public int joinMembers;

    @Column(name = "total_realpay")
    public double totalRealpay;

    @Column(name = "create_time")
    public long createTime;

    @Transient
    public String launcherName;

    @Transient
    public String launcherAvatar;

    @Transient
    public int launcherLevel;

    @Transient
    public List<TeamGrouponMembers> grouponMembers = new ArrayList();

    public static Finder<Long, TeamGroupon> find = new Finder<>(TeamGroupon.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public long getLauncherUid() {
        return launcherUid;
    }

    public void setLauncherUid(long launcherUid) {
        this.launcherUid = launcherUid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getJoinMembers() {
        return joinMembers;
    }

    public void setJoinMembers(int joinMembers) {
        this.joinMembers = joinMembers;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public double getTotalRealpay() {
        return totalRealpay;
    }

    public void setTotalRealpay(double totalRealpay) {
        this.totalRealpay = totalRealpay;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    @Override
    public String toString() {
        return "TeamGroupon{" +
                "id=" + id +
                ", beginTime=" + beginTime +
                ", endTime=" + endTime +
                ", teamId=" + teamId +
                ", teamName='" + teamName + '\'' +
                ", launcherUid=" + launcherUid +
                ", status=" + status +
                ", joinMembers=" + joinMembers +
                ", totalRealpay=" + totalRealpay +
                ", createTime=" + createTime +
                ", grouponMembers=" + grouponMembers +
                '}';
    }
}
