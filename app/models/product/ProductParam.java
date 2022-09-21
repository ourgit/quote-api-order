package models.product;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品参数
 */
@Entity
@Table(name = "v1_product_param")
public class ProductParam extends Model {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "product_id")
    public long productId;//商品编码

    @Column(name = "name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String name;

    @Column(name = "value")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String value;

    @Transient
    public String method;

    @Transient
    public List<String> list = new ArrayList();

    public static Finder<Long, ProductParam> find = new Finder<>(ProductParam.class);

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
