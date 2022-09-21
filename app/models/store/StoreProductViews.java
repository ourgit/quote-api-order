package models.store;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;

/**
 * 商品参数
 */
@Entity
@Table(name = "v1_store_product_views")
public class StoreProductViews extends Model {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "product_id")
    public long productId;//商品编码

    @Column(name = "views")
    public long views;

    public static Finder<Long, StoreProductViews> find = new Finder<>(StoreProductViews.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }

    @Override
    public String toString() {
        return "ProductViews{" +
                "id=" + id +
                ", productId=" + productId +
                ", views=" + views +
                '}';
    }
}
