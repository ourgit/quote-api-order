package constants;

/**
 * Redis key常量
 * Created by win7 on 2016/6/11.
 */
public class RedisKeyConstant {
    /**
     * 短信TOKEN
     */
    public static final String SMS_TOKEN_KEY = "SMS_TOKEN_KEY";

    /**
     * 系统身份认证token
     */
    public static final String AUTH_TOKEN = "authToken";

    /**
     * 放在缓存中的用户id 目前使用双向缓存，通过token能找到用户id，通过id也能找到对应的缓存
     */
    public static final String AUTH_TOKEN_UID = "AUTH_TOKEN_UID";

    /**
     * 放在缓存中的管理员token id 目前使用双向缓存，通过token能找到用户id，通过id也能找到对应的缓存
     */
    public static final String AUTH_ADMIN_TOKEN_UID = "AUTH_ADMIN_TOKEN_UID";

    /**
     * 所有币种集自己的集合
     */
    public static final String KEY_ALL_CURRENCIES_KEYSET = "KEY_ALL_CURRENCIES_KEYSET";

    /**
     * 所有币种基本不会变的信息缓存,ZADD类型，缓存类型为有序集合
     */
    public static final String KEY_CURRENCY_DETAIL_ALL_LIST_KEYSET = "KEY_CURRENCY_DETAIL_ALL_LIST_KEYSET";

    /**
     * 币种列表，返回的数据进行缓存
     */
    public static final String KEY_CACHE_CURRENCY_SIMPLE_LIST_DATA = "KEY_CACHE_CURRENCY_LIST_DATA";


    /**
     * 交易临时并发锁集合
     */
    public static final String TRADE_LOCK_KEY_SET = "TRADE_LOCK_KEY_SET:";
    /**
     * 币种hkey的前缀
     */
    public static final String CURRENCY_DETAIL_KEY_PREFIX = "currency:detail:";

    /**
     * 统计数据中的key前缀
     */
    public static final String TRADE_KEY_STATISTICS_PREFIX = "trade:statistics:";

    /**
     * 统计数据中的key集合关键字
     */
    public static final String TRADE_STATISTICS_KEY_SET = "TRADE_STATISTICS_KEY_SET";

    /**
     * 24小时成交量
     */
    public static final String TRADE_STATISTICS_KEY_24H_DEAL_AMOUNT_PREFIX = "TRADE_STATISTICS_KEY_24H_DEAL_AMOUNT:";
    /**
     * 24小时成交金额
     */
    public static final String TRADE_STATISTICS_KEY_24H_DEAL_MONEY_PREFIX = "TRADE_STATISTICS_KEY_24H_DEAL_MONEY_PREFIX:";

    /**
     * 我的推广明细总页数缓存
     */
    public static final String KEY_MY_PROMOTION_PAGES = "KEY_MY_PROMOTION_PAGES";

    /**
     * 首页信息栏跟链接栏缓存
     */
    public static final String KEY_HOME_PAGE_INFO_LINKS = "KEY_HOME_PAGE_INFO_LINKS";
    /**
     * 首页信息栏跟链接栏缓存
     */
    public static final String KEY_ARTICLE_FOR_NEWBIE = "KEY_ARTICLE_FOR_NEWBIE";

    /**
     * 底部信息栏缓存
     */
    public static final String KEY_HOME_PAGE_BOTTOM_INFO_LINKS = "KEY_HOME_PAGE_BOTTOM_INFO_LINKS";

    /**
     * 首页信息栏跟链接栏缓存
     */
    public static final String KEY_CAROUSEL_PREFIX = "KEY_CAROUSEL_PREFIX:";

    /**
     * 配置缓存key,以string方式
     */
    public static final String KEY_WEBSITE_CONFIG = "KEY_WEBSITE_CONFIG";

    /**
     * 用户表缓存
     */
    public static final String KEY_MEMBER_ACCOUNT_NAME = "KEY_MEMBER_ACCOUNT_NAME";
    public static final String KEY_USER_ID = "KEY_USER_ID";
    public static final String KEY_USER_TOKEN_KEY = "KEY_USER_TOKEN_KEY:";
    public static final String KEY_USER_PLATFORM_TOKEN_KEY = "KEY_USER_PLATFORM_TOKEN_KEY:";

    /**
     * 限制访问是否是有效用户的请求接口，目前限制次数为10
     */
    public static final String KEY_INVOKE_IS_VALID_ACCOUNT_LIMIT = "KEY_INVOKE_IS_VALID_ACCOUNT_LIMIT:";

    /**
     * 邮箱登录密码重置码
     */
    public static final String KEY_MAIL_RESET_LOGIN_PASSWORD_VCODE = "KEY_MAIL_RESET_LOGIN_PASSWORD_VCODE:";
    /**
     * 邮箱支付密码重置码
     */
    public static final String KEY_MAIL_RESET_PAY_PASSWORD_VCODE = "KEY_MAIL_RESET_PAY_PASSWORD_VCODE:";

    /**
     * 绑定邮箱激活码
     */
    public static final String KEY_BIND_MAIL_VCODE = "KEY_BIND_MAIL_VCODE:";

    /**
     * 返回前端url
     */
    public static final String KEY_RETURN_URL_FOR_WEB = "KEY_RETURN_URL_FOR_WEB:";

    /**
     * 是否在维护当中
     */
    public static final String KEY_SYSTEM_UNDER_MAINTAINCE = "KEY_SYSTEM_UNDER_MAINTAINCE";

    /**
     * 活动redis key，用于记录剩余数量
     */
    public static final String ACTIVITY_AWARD_LEFT_AMOUNT_PREFIX_KEY = "ACTIVITY_AWARD_LEFT_AMOUNT_PREFIX_KEY:";

    /**
     * 短信限制前缀
     */
    public static final String SMS_PHONENUMBER_LIMIT_PREFIX_KEY = "SMS_PHONENUMBER_LIMIT_PREFIX_KEY:";

    public static final String ALL_LEVELS_KEY_SET = "ALL_LEVELS_KEY_SET";

    /**
     * 每个等级的key
     */
    public static final String LEVEL_KEY_PREFIX = "LEVEL_KEY_PREFIX:";

    /**
     * 所有积分配置的集合
     */
    public static final String ALL_SCORE_CONFIGS_KEY_SET = "ALL_SCORE_CONFIGS_KEY_SET";

    /**
     * 每个积分配置的key
     */
    public static final String SCORE_CONFIG_KEY_PREFIX = "SCORE_CONFIG_KEY_PREFIX:";

    public static final String MERCHANTS_CATEGORIES_EACH_PREFIX = "MERCHANTS_CATEGORIES_EACH_PREFIX:";
    /**
     * 分类每一页单独缓存
     */

    public static final String MERCHANTS_SOLD_AMOUNT_CACHE_KEY = "MERCHANTS_SOLD_AMOUNT_CACHE_KEY:";


    /**
     * 品牌列表KEY
     */
    public static final String BRAND_KEY_SET = "BRAND_KEY_SET";

    /**
     * 每个品牌的前缀key
     */
    public static final String BRAND_EACH_KEY = "BRAND_EACH_KEY:";

    public static final String HOMEPAGE_BRAND_JSON_CACHE = "HOMEPAGE_BRAND_JSON_CACHE";
    public static final String ON_NEW_ARRIVAL_JSON_CACHE = "ON_NEW_ARRIVAL_JSON_CACHE";
    public static final String ON_PROMOTION_JSON_CACHE = "ON_PROMOTION_JSON_CACHE";

    public static final String MAIL_FEE_CACHE_KEYSET = "MAIL_FEE_CACHE_KEYSET";
    public static final String MAIL_FEE_CACHE_KEY_PREFIX = "MAIL_FEE_CACHE_KEY_PREFIX:";

    //优惠券
    public static final String COUPON_CONFIG_KEYSET = "COUPON_CONFIG_KEYSET";
    public static final String COUPON_CONFIG_KEY_PREFIX = "COUPON_CONFIG_KEY_PREFIX:";

    //系统参数配置
    public static final String PARAM_CONFIG_KEYSET = "PARAM_CONFIG_KEYSET";
    public static final String PARAM_CONFIG_KEY_PREFIX = "PARAM_CONFIG_KEY_PREFIX:";
    public static final String PARAM_CONFIG_KEY_ID = "COUPON_CONFIG_KEY_ID";
    public static final String PARAM_CONFIG_KEY_KEY = "PARAM_CONFIG_KEY_KEY";
    public static final String PARAM_CONFIG_KEY_VALUE = "PARAM_CONFIG_KEY_VALUE";

    //商品缓存
    public static final String MERCHANT_JSON_CACHE_KEY_PREFIX = "MERCHANT_JSON_CACHE_KEY_PREFIX:";
    //商品缓存
    public static final String MERCHANT_CACHE_KEY_PREFIX = "MERCHANT_CACHE_KEY_PREFIX:";
    public static final String MERCHANT_CACHE_KEYSET = "MERCHANT_CACHE_KEYSET";

    //关键字列表
    public static final String SEARCH_KEY_WORD_LIST_CACHE = "SEARCH_KEY_WORD_LIST_CACHE";
    public static final String MIX_OPTION_KEY_SET = "MIX_OPTION_KEY_SET";

    public static final String KEY_MEMBER_SHIP_JSON_CACHE_KEY = "KEY_MEMBER_SHIP_JSON_CACHE_KEY:";
    public static final String FLASH_SALE_CACHE_BY_PRODUCT_ID = "FLASH_SALE_CACHE_BY_PRODUCT_ID:";
    public static final String SMS_TO_TAKE_NOTIFY_CACHE_KEY = "SMS_TO_TAKE_NOTIFY_CACHE_KEY:";
    public static final String SKU_STOCK_CACHE = "SKU_STOCK_CACHE:";
    public static final String PRODUCT_VIEW_KEY = "PRODUCT_VIEW_KEY:";
    public static final String EXIST_REQUEST_SMS = "EXIST_REQUEST_SMS:";
    public static final String GROUP_PRODUCT_JSON_CACHE_KEY_PREFIX = "GROUP_PRODUCT_JSON_CACHE_KEY_PREFIX:";
    public static final String GROUP_PRODUCT_STAT_JSON_CACHE_KEY_PREFIX = "GROUP_PRODUCT_STAT_JSON_CACHE_KEY_PREFIX:";


    public static final String CAROUSEL_JSON_CACHE = "CAROUSEL_JSON_CACHE:";

    public static final String ARTICLE_LIST_JSON_CACHE = "ARTICLE_LIST_JSON_CACHE:";
    public static final String ARTICLE_RECOMMEND_JSON_CACHE = "ARTICLE_RECOMMEND_JSON_CACHE:";
    public static final String ARTICLE_DISCOVERY_CACHE = "ARTICLE_DISCOVERY_CACHE";
    public static final String ARTICLE_HOME_PAGE = "ARTICLE_HOME_PAGE";
    public static final String ARTICLE_JSON_CACHE = "ARTICLE_JSON_CACHE:";
    public static final String COMMENT_JSON_CACHE = "COMMENT_JSON_CACHE:";
    public static final String ARTICLE_CATEGORY_KEY_PREFIX = "ARTICLE_CATEGORY_KEY_PREFIX:";
    public static final String ARTICLE_CATEGORY_KEY_BY_ID_PREFIX = "ARTICLE_CATEGORY_KEY_BY_ID_PREFIX:";

    public static final String POST_CATEGORIES_LIST_CACHE_KEY_PREFIX = "POST_CATEGORIES_LIST_CACHE_KEY_PREFIX:";
    public static final String POST_CATEGORIES_LIST_BY_CATEGORY_ID = "POST_CATEGORIES_LIST_BY_CATEGORY_ID:";
}
