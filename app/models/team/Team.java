package models.team;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;

/**
 * 开团
 */
@Entity
@Table(name = "v1_team")
public class Team extends Model {

    public static final int LEVEL_1 = 1;//
    public static final int LEVEL_2 = 2;//
    public static final int LEVEL_3 = 3;//
    public static final int LEVEL_4 = 4;//

    public static final int STATUS_NORMAL = 1;
    public static final int STATUS_PAUSE = 2;
    public static final int STATUS_TRANSFERED = -1;
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "cover_img_url")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String coverImgUrl;

    @Column(name = "logo")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String logo;

    @Column(name = "team_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String teamName;

    @Column(name = "digest")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String digest;

    @Column(name = "status")
    public int status;

    @Column(name = "level")
    public int level;

    @Column(name = "province_code")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String provinceCode;

    @Column(name = "region_code")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String regionCode;

    @Column(name = "region_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String regionName;

    @Column(name = "members_number")
    public long membersNumber;

    @Column(name = "leader_uid")
    public long leaderUid;

    @Column(name = "leader_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String leaderName;

    @Column(name = "team_code")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String teamCode;

    @Column(name = "create_time")
    public long createTime;

    public static Finder<Long, Team> find = new Finder<>(Team.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCoverImgUrl() {
        return coverImgUrl;
    }

    public void setCoverImgUrl(String coverImgUrl) {
        this.coverImgUrl = coverImgUrl;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public long getMembersNumber() {
        return membersNumber;
    }

    public void setMembersNumber(long membersNumber) {
        this.membersNumber = membersNumber;
    }

    public long getLeaderUid() {
        return leaderUid;
    }

    public void setLeaderUid(long leaderUid) {
        this.leaderUid = leaderUid;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    @Override
    public String toString() {
        return "Team{" +
                "id=" + id +
                ", coverImgUrl='" + coverImgUrl + '\'' +
                ", teamName='" + teamName + '\'' +
                ", digest='" + digest + '\'' +
                ", status=" + status +
                ", level=" + level +
                ", provinceCode='" + provinceCode + '\'' +
                ", regionCode='" + regionCode + '\'' +
                ", regionName='" + regionName + '\'' +
                ", membersNumber=" + membersNumber +
                ", leaderUid=" + leaderUid +
                ", leaderName='" + leaderName + '\'' +
                ", teamCode='" + teamCode + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
