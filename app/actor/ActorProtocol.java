package actor;

import com.fasterxml.jackson.databind.JsonNode;
import models.groupon.Groupon;
import models.order.OrderDetail;
import models.product.Product;
import models.store.StoreOrderDetail;
import models.store.StoreProduct;
import models.user.Member;
import models.user.Membership;
import utils.OrderParam;

import java.util.List;
import java.util.Map;

/**
 * Actor对象
 */
public class ActorProtocol {

    public static class PLACE_ORDER {
        public OrderParam orderParam;
        public Member member;
        public String orderShops;
        public Membership membership;
        public MemberCoupon memberCoupon;
        public long memberCardCouponId;
        public long favorMoney;
        public List<OrderDetail> detailList;
        public JsonNode paramNode;
        public Map<Long, Product> productMap;

        public PLACE_ORDER() {
        }

        public PLACE_ORDER(JsonNode paramNode, Member member, Map<Long, Product> map) {
            this.paramNode = paramNode;
            this.member = member;
            this.productMap = map;
        }

        public PLACE_ORDER(OrderParam orderParam, Member member, String orderShops,
                           Membership membership, MemberCoupon memberCoupon,
                           long memberCardCouponId,
                           List<OrderDetail> detailList, long favorMoney) {
            this.orderParam = orderParam;
            this.member = member;
            this.orderShops = orderShops;
            this.membership = membership;
            this.memberCoupon = memberCoupon;
            this.detailList = detailList;
            this.memberCardCouponId = memberCardCouponId;
            this.favorMoney = favorMoney;
        }
    }

    public static class PLACE_GROUPON_ORDER {
        public OrderParam orderParam;
        public Member member;
        public OrderDetail orderDetail;
        public long staffId;
        public ContactDetail contactDetail;
        public Groupon groupon;

        public PLACE_GROUPON_ORDER() {
        }

        public PLACE_GROUPON_ORDER(OrderParam orderParam, Member member, long staffId, OrderDetail orderDetail,
                                   ContactDetail contactDetail, Groupon groupon) {
            this.orderParam = orderParam;
            this.member = member;
            this.orderDetail = orderDetail;
            this.staffId = staffId;
            this.contactDetail = contactDetail;
            this.groupon = groupon;
        }
    }

    public static class HANDLE_ORDER_PAID {
        public int payType;
        public String orderNo;
        public String payTxId;
        public String outTradeNo;

        public HANDLE_ORDER_PAID(String orderNo, String payTxId, int payType, String outTradeNo) {
            this.orderNo = orderNo;
            this.payTxId = payTxId;
            this.payType = payType;
            this.outTradeNo = outTradeNo;
        }

        @Override
        public String toString() {
            return "HANDLE_ORDER_PAID{" +
                    "payType=" + payType +
                    ", orderNo='" + orderNo + '\'' +
                    ", payTxId='" + payTxId + '\'' +
                    ", outTradeNo='" + outTradeNo + '\'' +
                    '}';
        }
    }

    public static class HANDLE_ENROLL {
        public OrderParam orderParam;
        public long productId;
        public long skuId;
        public String phoneNumber;

        public HANDLE_ENROLL(OrderParam orderParam, long productId, long skuId, String phoneNumber) {
            this.orderParam = orderParam;
            this.productId = productId;
            this.skuId = skuId;
            this.phoneNumber = phoneNumber;
        }
    }


    public static class EXCHANGE {
        public OrderParam orderParam;
        public Member member;
        public String orderShops;
        public int productType;
        public long mailFee;
        public Membership membership;
        public MemberCoupon memberCoupon;
        public OrderDetail orderDetail;

        public EXCHANGE() {
        }

        public EXCHANGE(OrderParam orderParam, Member member, OrderDetail orderDetail) {
            this.orderParam = orderParam;
            this.member = member;
            this.orderDetail = orderDetail;
        }
    }

    public static class CHECK_PAY_RESULT {
        public CHECK_PAY_RESULT() {
        }
    }

    public static class QUERY_REFUND_RESULT {
        public QUERY_REFUND_RESULT() {
        }
    }

    public static class PLACE_ORDER_GROUP_BY_PAID {
        public String txId;
        public String orderNo;

        public PLACE_ORDER_GROUP_BY_PAID() {
        }

        public PLACE_ORDER_GROUP_BY_PAID(String orderNo, String txId) {
            this.txId = txId;
            this.orderNo = orderNo;
        }
    }


    public static class PLACE_STORE_ORDER {
        public OrderParam orderParam;
        public Member member;
        public MemberCoupon memberCoupon;
        public long memberCardCouponId;
        public long favorMoney;
        public List<StoreOrderDetail> detailList;
        public JsonNode paramNode;
        public Map<Long, StoreProduct> productMap;

        public PLACE_STORE_ORDER() {
        }

        public PLACE_STORE_ORDER(OrderParam orderParam, Member member, List<StoreOrderDetail> detailList) {
            this.orderParam = orderParam;
            this.member = member;
            this.detailList = detailList;
        }

        public PLACE_STORE_ORDER(OrderParam orderParam, Member member, MemberCoupon memberCoupon,
                                 long memberCardCouponId,
                                 List<StoreOrderDetail> detailList, long favorMoney) {
            this.orderParam = orderParam;
            this.member = member;
            this.memberCoupon = memberCoupon;
            this.detailList = detailList;
            this.memberCardCouponId = memberCardCouponId;
            this.favorMoney = favorMoney;
        }
    }

}
