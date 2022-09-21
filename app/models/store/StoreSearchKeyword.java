package models.store;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;

/**
 * 搜索
 */
@Entity
@Table(name = "v1_search_keyword")
public class StoreSearchKeyword extends Model {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "source")
    public int from;

    @Column(name = "enable")
    public boolean enable;

    @Column(name = "keyword")
    public String keyword;

    @Column(name = "sort")
    public long sort;

    public static Finder<Long, StoreSearchKeyword> find = new Finder<>(StoreSearchKeyword.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public long getSort() {
        return sort;
    }

    public void setSort(long sort) {
        this.sort = sort;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
