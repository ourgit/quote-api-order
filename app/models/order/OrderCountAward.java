package models.order;

import io.ebean.Finder;
import io.ebean.Model;

import javax.persistence.*;

@Entity
@Table(name = "v1_order_count_award")
public class OrderCountAward extends Model {

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "order_count")
    public long orderCount;

    @Column(name = "refund_point")
    public double refundPoint;

    @Column(name = "level")
    public int level;

    public static Finder<Long, OrderCountAward> find = new Finder<>(OrderCountAward.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(long orderCount) {
        this.orderCount = orderCount;
    }

    public double getRefundPoint() {
        return refundPoint;
    }

    public void setRefundPoint(double refundPoint) {
        this.refundPoint = refundPoint;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "OrderCountAward{" +
                "id=" + id +
                ", orderCount=" + orderCount +
                ", refundPoint=" + refundPoint +
                ", level=" + level +
                '}';
    }
}
