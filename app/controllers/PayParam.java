package controllers;

/**
 * 支付参数辅助类
 */
public class PayParam {

    public String tradeNo;
    public String subject;
    public String productionCode;
    public long totalAmount;
    public long realPayMoney;
    public long uid;
    public String openId;
    public int payMethod;
    public int useScore;
    public long returnMoney;

    public PayParam(String tradeNo, String subject, String productionCode, long totalAmount,
                    long uid, String openId, long realPayMoney, int payMethod, int useScore, long returnMoney) {
        this.tradeNo = tradeNo;
        this.subject = subject;
        this.productionCode = productionCode;
        this.totalAmount = totalAmount;
        this.uid = uid;
        this.openId = openId;
        this.realPayMoney = realPayMoney;
        this.payMethod = payMethod;
        this.useScore = useScore;
        this.returnMoney = returnMoney;
    }

    @Override
    public String toString() {
        return "PayParam{" +
                "tradeNo='" + tradeNo + '\'' +
                ", subject='" + subject + '\'' +
                ", productionCode='" + productionCode + '\'' +
                ", totalAmount=" + totalAmount +
                ", realPayMoney=" + realPayMoney +
                ", uid=" + uid +
                ", openId='" + openId + '\'' +
                ", payMethod=" + payMethod +
                ", useScore=" + useScore +
                ", returnMoney=" + returnMoney +
                '}';
    }

    public static class Builder {
        private String innerTradeNo;
        private String innerSubject;
        private String innerProductionCode;
        private long innerTotalAmount;
        private long innerUid;
        private String innerOpenId;
        private long innerRealPayMoney;
        private long innerGroupBuyLauncherId;
        private int innerPayMethod;
        private int innerUseScore;
        private long innerReturnMoney;

        public Builder() {
        }

        public Builder(String innerTradeNo, String innerSubject, String innerProductionCode,
                       long innerTotalAmount, long innerUid, String innerOpenId,
                       long innerRealPayMoney, int innerPayMethod, int innerUseScore, long innerReturnMoney) {
            this.innerTradeNo = innerTradeNo;
            this.innerSubject = innerSubject;
            this.innerProductionCode = innerProductionCode;
            this.innerTotalAmount = innerTotalAmount;
            this.innerUid = innerUid;
            this.innerOpenId = innerOpenId;
            this.innerRealPayMoney = innerRealPayMoney;
            this.innerPayMethod = innerPayMethod;
            this.innerUseScore = innerUseScore;
            this.innerReturnMoney = innerReturnMoney;
        }

        public PayParam.Builder tradeNo(String tradeNo) {
            this.innerTradeNo = tradeNo;
            return this;
        }

        public PayParam.Builder subject(String subject) {
            this.innerSubject = subject;
            return this;
        }

        public PayParam.Builder productionCode(String productionCode) {
            this.innerProductionCode = productionCode;
            return this;
        }

        public PayParam.Builder totalAmount(long totalAmount) {
            this.innerTotalAmount = totalAmount;
            return this;
        }

        public PayParam.Builder uid(long uid) {
            this.innerUid = uid;
            return this;
        }

        public PayParam.Builder openId(String openId) {
            this.innerOpenId = openId;
            return this;
        }

        public PayParam.Builder payMethod(int payMethod) {
            this.innerPayMethod = payMethod;
            return this;
        }

        public PayParam.Builder realPayMoney(long realPayMoney) {
            this.innerRealPayMoney = realPayMoney;
            return this;
        }

        public PayParam.Builder useScore(int useScore) {
            this.innerUseScore = useScore;
            return this;
        }

        public PayParam.Builder returnMoney(long innerReturnMoney) {
            this.innerReturnMoney = innerReturnMoney;
            return this;
        }

        public PayParam build() {
            return new PayParam(innerTradeNo, innerSubject, innerProductionCode, innerTotalAmount, innerUid,
                    innerOpenId, innerRealPayMoney, innerPayMethod, innerUseScore, innerReturnMoney);
        }
    }

}
