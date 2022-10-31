package constants;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * 业务用的常量
 * Created by win7 on 2016/6/11.
 */
public class BusinessConstant {

//    public static final String DEFAULT_HOME_PAGE_URL = "https://qd.starnew.cn";

    public static final int HOUR24_TO_SECONDS = 86400;

    public static final String KEY_SESSION_WECHAT_OPEN_ID = "xx_play";


    public static final int DELIVERY_METHOD_SELF_PICKUP = 1;

    public static final int TYPE_PLACE_ORDER = 1;
    public static final int TYPE_CANCEL_ORDER = 2;

    public static final int DEVICE_TYPE_WECHAT = 5;

    public static final int MAX_TIME_TO_WAIT_PAY = 60 * 60;//下单后1小时内没支付自动关闭订单

    public static final int KEY_EXPIRE_TIME_2M = 120;
    public static final String KEY_AUTH_TOKEN_UID = "X-AUTH-TOKEN-UID";

    /**
     * 支付方式,在线支付(网银)
     */
    public static final int PAYMENT_BANK = 1;

    /**
     * 支付方式,支付宝
     */
    public static final int PAYMENT_ALIPAY = 2;
    /**
     * 支付方式,微信支付
     */
    public static final int PAYMENT_WEPAY = 3;

    /**
     * 支付方式,余额支付
     */
    public static final int PAYMENT_BALANCE_PAY = 4;

    public static final int PAYMENT_ALIPAY_PC = 11;

    /**
     * 支付方式,微信支付，H5调用微信APP支付
     */
    public static final int PAYMENT_WEPAY_H5 = 6;

    /**
     * 支付方式,支付宝H5支付
     */
    public static final int PAYMENT_ALIPAY_WAP = 7;


    /**
     * 支付方式,微信支付，小程序调用微信支付
     */
    public static final int PAYMENT_WEPAY_MINIAPP = 8;

    public static final int PAYMENT_WEPAY_NATIVE = 9;

    public static final int PAYMENT_BEAUTY_CARD = 10;


    public static final BigDecimal BG100 = new BigDecimal("100.00");
    public static final BigDecimal BG1000 = new BigDecimal("1000.00");
    public static final BigDecimal BG10000 = new BigDecimal("10000.00");
    public static final BigDecimal TORLANCE = new BigDecimal("0.0001");
    public static final DecimalFormat DF_TWO_DIGIT = new DecimalFormat("######0.00");
    public static final String OPERATION_CONFIRMATION = "confirmation";
    public static final String OPERATION_CANCEL = "cancel";
    public static final String OPERATION_UPDATE = "update";
    public static final String X_REAL_IP_HEADER = "X-real-ip";

    public static final int MAX_SMS_ACCESS_PER_IP = 10;
    public static final int MAX_SECURE_CODE_ACCESS_PER_IP = 10;
    public static final int MAX_REG_ACCESS_PER_IP = 2;

    public static final int TYPE_LOGIN = 1;//正常登录
    public static final int TYPE_UNUSUAL_LOGIN = 2;//异地登录
    public static final int TYPE_PRMOTION = 3;//营销短信
    public static final int TYPE_CHARGE_RESULT = 4;//充值结果
    public static final int TYPE_CONSUME_NOTIFY = 5;//消费提醒
    public static final int TYPE_CHARGE_PROMPT = 6;//充值提醒
    public static final String SMS_REQUEST_URL = "http://pin.shujudu.cn/api/sms/send/single/?key=%s&secret=%s&mobile=%s&msgid=%s&content=%s";
    public static final String APP_KEY = "7ec9eecab6";
    public static final String APP_SECRET = "5954a8be024640e7c48322b4d417758f";


    /**
     * 管理员手动充值
     */
    public static final String OPERATION_ADMIN_MANNUALLY_DEPOSIT = "OPERATION_ADMIN_MANNUALLY_DEPOSIT";


    /**
     * 下单
     */
    public static final String OPERATION_PLACE_ORDER = "OPERATION_PLACE_ORDER";
    public static final String OPERATION_STORE_PLACE_ORDER = "OPERATION_STORE_PLACE_ORDER";
    public static final String OPERATION_PLACE_VIP_ORDER = "OPERATION_PLACE_VIP_ORDER";
    public static final String OPERATION_PLACE_DISCOUNT_ORDER = "OPERATION_PLACE_DISCOUNT_ORDER";
    /**
     * 下单
     */
    public static final String OPERATION_APPLY_ORDER_RETURN = "OPERATION_APPLY_ORDER_RETURN";
    /**
     * 支付订单
     */
    public static final String OPERATION_PAY_ORDER = "OPERATION_PAY_ORDER";
    public static final String OPERATION_PAY_STORE_ORDER = "OPERATION_PAY_STORE_ORDER";
    public static final String OPERATION_PAY_BARGAIN = "OPERATION_PAY_BARGAIN";
    public static final String OPERATION_ENROLL = "OPERATION_ENROLL";
    /**
     * 抢拍
     */
    public static final String OPERATION_PLACE_AUCTION = "OPERATION_PLACE_AUCTION";

    /**
     * 领取优惠券
     */
    public static final String OPERATION_CLAIM_COUPON = "OPERATION_CLAIM_COUPON";
    /**
     * 购买优惠券
     */
    public static final String OPERATION_BUY_COUPON = "OPERATION_BUY_COUPON";

    /**
     * 取消下单
     */
    public static final String OPERATION_CANCEL_ORDER = "OPERATION_CANCEL_ORDER";
    /**
     * 退款
     */
    public static final String OPERATION_REFUND_ORDER = "OPERATION_REFUND_ORDER";
    /**
     * 提现
     */
    public static final String OPERATION_ADMIN_HANDLE_WITHDRAW = "OPERATION_ADMIN_HANDLE_WITHDRAW";
    public static final String OPERATION_SUBMIT_ENROLL = "OPERATION_SUBMIT_ENROLL";

    public static final String OPERATION_PLACE_BID = "OPERATION_PLACE_BID";

    /**
     * 短信限制时效，目前为一天
     */
    public static final int KEY_EXPIRE_TIME_24H = 86400;
    /**
     * 2小时后失效
     */
    public static final int KEY_EXPIRE_TIME_2H = 7200;

    /**
     * 60秒失效时间
     */
    public static final int KEY_EXPIRE_TIME_60S = 60;

    /**
     * 5分失效时间
     */
    public static final int KEY_EXPIRE_TIME_5_MIN = 300;
    /**
     * 5分失效时间
     */
    public static final int KEY_EXPIRE_TIME_30_MIN = 1800;

    /**
     * 首页文章标题最大长度
     */
    public static final int HOMEPAGE_ARTICLE_MAX_LEN = 25;

    public static final int PAGE_SIZE_30 = 30;
    public static final int PAGE_SIZE_20 = 20;
    public static final int PAGE_SIZE_10 = 10;

    /**
     * 认证头
     */
    public static final String KEY_AUTH_TOKEN = "X-AUTH-TOKEN";
    public static final String KEY_AUTH_TOKEN_BY_UID = "X-AUTH-TOKEN-BY-UID:";
    public static final String KEY_AUTH_TOKEN_COOKIE = "X-AUTH-TOKEN-COOKIE";

    /**
     * 最后一次发送的短信验证码
     */
    public static final String KEY_LAST_VERIFICATION_CODE_PREFIX_KEY = "KEY_LAST_VERIFICATION_CODE_PREFIX_KEY:";


    /**
     * 交易类型,取消订单扣除赠送的积分
     */
    public static final int TRANSACTION_TYPE_SUBTRACT_SCORE_GAVE_FOR_CANCEL_ORDER = 11;

    //提现
    public static final int TRANSACTION_TYPE_WITHDRAW = 16;


    /**
     * 交易类型,交易扣除
     */


    public static final int MAX_COUNT_INVOKE_IS_VALID_ACCOUNT_PER_DAY = 50;


    public static final String PARAM_KEY_WECHAT_MP_APP_ID = "PARAM_KEY_WECHAT_MP_APP_ID";
    public static final String PARAM_KEY_WECHAT_MP_SECRET_CODE = "PARAM_KEY_WECHAT_MP_SECRET_CODE";
    public static final String PARAM_KEY_WECHAT_MINI_APP_ID = "PARAM_KEY_WECHAT_MINI_APP_ID";
    public static final String PARAM_KEY_WECHAT_APP_ID = "PARAM_KEY_WECHAT_APP_ID";
    public static final String PARAM_KEY_WECHAT_MINI_APP_SECRET_CODE = "PARAM_KEY_WECHAT_MINI_APP_SECRET_CODE";
    public static final String PARAM_KEY_WECHAT_MCH_ID = "PARAM_KEY_WECHAT_MCH_ID";
    public static final String PARAM_KEY_WECHAT_MCH_API_SECURITY_CODE = "PARAM_KEY_WECHAT_MCH_API_SECURITY_CODE";
    public static final String PARAM_KEY_DEFAULT_HOME_PAGE_URL = "PARAM_KEY_DEFAULT_HOME_PAGE_URL";

    public static final String PARAM_KEY_ALI_YUN_ACCESS_ID = "PARAM_KEY_ALI_YUN_ACCESS_ID";
    public static final String PARAM_KEY_ALI_YUN_SECRET_KEY = "PARAM_KEY_ALI_YUN_SECRET_KEY";

    public static final String PARAM_KEY_ALIPAY_APPID = "PARAM_KEY_ALIPAY_APPID";
    public static final String PARAM_KEY_ALIPAY_APP_PRIVATE_KEY = "PARAM_KEY_ALIPAY_APP_PRIVATE_KEY";
    public static final String PARAM_KEY_ALIPAY_PUBLIC_KEY_RSA2 = "PARAM_KEY_ALIPAY_PUBLIC_KEY_RSA2";
    public static final String PARAM_KEY_ALIPAY_ALI_PUBLIC_KEY = "PARAM_KEY_ALIPAY_ALI_PUBLIC_KEY";
    public static final String PARAM_KEY_ALIPAY_WAP_PAY_NOTIFY_URL = "PARAM_KEY_ALIPAY_WAP_PAY_NOTIFY_URL";
    public static final String PARAM_KEY_ALIPAY_DIRECT_PAY_NOTIFY_URL = "PARAM_KEY_ALIPAY_DIRECT_PAY_NOTIFY_URL";
    public static final String PARAM_KEY_ALIPAY_RETURN_URL = "PARAM_KEY_ALIPAY_RETURN_URL";
    public static final String PARAM_KEY_ALIPAY_PARTNER_NO = "PARAM_KEY_ALIPAY_PARTNER_NO";
    public static final String PARAM_KEY_ALIPAY_SELL_ACCOUNT_NAME = "PARAM_KEY_ALIPAY_SELL_ACCOUNT_NAME";
    public static final String PARAM_KEY_KD_HOST_URL = "PARAM_KEY_KD_HOST_URL";
    public static final String PARAM_KEY_KD_APP_CODE = "PARAM_KEY_KD_APP_CODE";


    public static final String PARAM_KEY_WE_PAY_SP_APP_ID = "PARAM_KEY_WE_PAY_SP_APP_ID";//服务商应用ID
    public static final String PARAM_KEY_WE_PAY_SP_MCH_ID = "PARAM_KEY_WE_PAY_SP_MCH_ID";//服务商户号
    //    public static final String PARAM_KEY_WE_PAY_SUB_APP_ID = "PARAM_KEY_WE_PAY_SUB_APP_ID";//子商户应用ID
    public static final String PARAM_KEY_WE_PAY_SUB_MCH_ID = "PARAM_KEY_WE_PAY_SUB_MCH_ID";//子商户号
    public static final String PARAM_KEY_WE_PAY_KEY_SERIAL_NO = "PARAM_KEY_WE_PAY_KEY_SERIAL_NO";//序列号
    public static final String PARAM_KEY_WE_PAY_API_V3_KEY = "PARAM_KEY_WE_PAY_API_V3_KEY";
    public static final String PARAM_KEY_WE_PAY_PRIVATE_KEY = "PARAM_KEY_WE_PAY_PRIVATE_KEY";
    public static final String PARAM_KEY_SMS_USER_NAME = "PARAM_KEY_SMS_USER_NAME";
    public static final String PARAM_KEY_SMS_PASSWORD = "PARAM_KEY_SMS_PASSWORD";
    public static final String PARAM_KEY_SMS_URL = "PARAM_KEY_SMS_URL";

    public static final String PARAM_KEY_DEFAULT_AVATAR = "PARAM_CONFIG_KEY_PREFIX:DEFAULT_AVATAR";

    public static final int BIZ_TYPE_SIGNUP = 1;
    public static final int BIZ_TYPE_BIND_VERIFY = 2;
}
