package models.team;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;

/**
 * 开团成员
 */
@Entity
@Table(name = "v1_team_consume")
public class TeamConsume extends Model {
    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    @Column(name = "team_id")
    public long teamId;

    @Column(name = "team_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String teamName;

    @Column(name = "team_cover_img_url")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String teamCoverImgUrl;

    @Column(name = "month")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String month;

    @Column(name = "consume_money")
    public double consumeMoney;

    @Column(name = "create_time")
    public long createTime;

    @Transient
    public String consumeMoneyStr;

    public static Finder<Integer, TeamConsume> find = new Finder<>(TeamConsume.class);

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTeamId() {
        return teamId;
    }

    public void setTeamId(long teamId) {
        this.teamId = teamId;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public double getConsumeMoney() {
        return consumeMoney;
    }

    public void setConsumeMoney(double consumeMoney) {
        this.consumeMoney = consumeMoney;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamCoverImgUrl() {
        return teamCoverImgUrl;
    }

    public void setTeamCoverImgUrl(String teamCoverImgUrl) {
        this.teamCoverImgUrl = teamCoverImgUrl;
    }

    public String getConsumeMoneyStr() {
        return consumeMoneyStr;
    }

    public void setConsumeMoneyStr(String consumeMoneyStr) {
        this.consumeMoneyStr = consumeMoneyStr;
    }

    @Override
    public String toString() {
        return "TeamConsume{" +
                "id=" + id +
                ", teamId=" + teamId +
                ", teamName='" + teamName + '\'' +
                ", teamCoverImgUrl='" + teamCoverImgUrl + '\'' +
                ", month='" + month + '\'' +
                ", consumeMoney=" + consumeMoney +
                ", createTime=" + createTime +
                ", consumeMoneyStr='" + consumeMoneyStr + '\'' +
                '}';
    }
}
