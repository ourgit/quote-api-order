package models.test;

import io.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class TestUser extends Model {
    @Column(name="USER_ID")
    private long id;
    @Column(name="USER_NAME")
    private String name;
    @Column(name="CREATED_BY")
    private String CreatedBy;
    @Column(name="CREATED_TIME")
    private long CreatedTime;
    @Column(name="UPDATED_BY")
    private String UpdatedBy;
    @Column(name="UPDATED_TIME")
    private long UpdatedTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedBy() {
        return CreatedBy;
    }

    public void setCreatedBy(String createdBy) {
        CreatedBy = createdBy;
    }

    public long getCreatedTime() {
        return CreatedTime;
    }

    public void setCreatedTime(long createdTime) {
        CreatedTime = createdTime;
    }

    public String getUpdatedBy() {
        return UpdatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        UpdatedBy = updatedBy;
    }

    public long getUpdatedTime() {
        return UpdatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        UpdatedTime = updatedTime;
    }
}
