package models.store;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;

import javax.persistence.*;

/**
 * 动态图片
 */
@Entity
@Table(name = "v1_store_order_returns_img")
public class StoreOrderReturnsImage extends Model {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "order_id")
    private long orderId;

    @Column(name = "order_detail_id")
    private long orderDetailId;

    @Column(name = "return_apply_id")
    private long returnApplyId;

    @Column(name = "uid")
    private long uid;

    @Column(name = "img_url")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    private String imgUrl;

    @Column(name = "create_time")
    private long createTime;

    public static Finder<Long, StoreOrderReturnsImage> find = new Finder<>(StoreOrderReturnsImage.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public long getOrderDetailId() {
        return orderDetailId;
    }

    public void setOrderDetailId(long orderDetailId) {
        this.orderDetailId = orderDetailId;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getReturnApplyId() {
        return returnApplyId;
    }

    public void setReturnApplyId(long returnApplyId) {
        this.returnApplyId = returnApplyId;
    }
}
