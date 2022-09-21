package models.store;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;

/**
 * 搜索
 */
@Entity
@Table(name = "v1_store_product_search")
public class StoreProductSearch extends Model {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "keyword")
    public String keyword;

    @Column(name = "views")
    public long views;

    public static Finder<Long, StoreProductSearch> find = new Finder<>(StoreProductSearch.class);

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

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }
}
