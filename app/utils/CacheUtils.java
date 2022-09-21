package utils;

import constants.BusinessConstant;
import constants.RedisKeyConstant;
import play.cache.SyncCacheApi;

import javax.inject.Inject;
import javax.inject.Singleton;

import static constants.RedisKeyConstant.*;

/**
 * cache utils
 */
@Singleton
public class CacheUtils {

    @Inject
    SyncCacheApi cache;

    /**
     * 获取用户表的cache key
     *
     * @param id
     * @return
     */
    public String getMemberKey(long id) {
        return RedisKeyConstant.KEY_USER_ID + id;
    }

    public String getMemberKey(String id) {
        return RedisKeyConstant.KEY_USER_ID + id;
    }

    public String getMemberKey(int deviceType, String token) {
        return RedisKeyConstant.KEY_USER_ID + deviceType + ":" + token;
    }

    /**
     * 获取手机号短信限制缓存key
     *
     * @param phoneNumber
     * @return
     */
    public String getSmsPhoneNumberLimitKey(String phoneNumber) {
        return SMS_PHONENUMBER_LIMIT_PREFIX_KEY + phoneNumber;
    }


    public String getSMSLastVerifyCodeKey(String phoneNumber) {
        return BusinessConstant.KEY_LAST_VERIFICATION_CODE_PREFIX_KEY + phoneNumber;
    }

    public String getAllLevelKeySet() {
        return ALL_LEVELS_KEY_SET;
    }

    public String getEachLevelKey(int level) {
        return LEVEL_KEY_PREFIX + level;
    }

    public String getAllScoreConfigKeySet() {
        return ALL_SCORE_CONFIGS_KEY_SET;
    }

    public String getEachScoreConfigKey(int type) {
        return SCORE_CONFIG_KEY_PREFIX + type;
    }

    public String getSoldAmountCacheKey(long merchantId) {
        return RedisKeyConstant.MERCHANTS_SOLD_AMOUNT_CACHE_KEY + merchantId;
    }

    /**
     * 商品分类列表集合
     *
     * @return
     */
    public String getCategoryPrefix(long categoryId) {
        return RedisKeyConstant.MERCHANTS_CATEGORIES_EACH_PREFIX + categoryId;
    }

    /**
     * 兑换分类缓存列表
     *
     * @return
     */
    public String getCategoryEachListKey(String filter) {
        return RedisKeyConstant.MERCHANTS_CATEGORIES_LIST_CACHE_KEY_PREFIX + filter;
    }

    /**
     * 品牌集合
     *
     * @return
     */
    public String getBrandKeySet() {
        return RedisKeyConstant.BRAND_KEY_SET;
    }

    /**
     * 单个品牌的KEY
     *
     * @param brandId
     * @return
     */
    public String getBrandKey(long brandId) {
        return RedisKeyConstant.BRAND_EACH_KEY + brandId;
    }

    /**
     * 首页商品图缓存
     *
     * @return
     */
    public String homepageBrandJsonCache() {
        return HOMEPAGE_BRAND_JSON_CACHE;
    }

    public String homepageOnArrivalJsonCache() {
        return ON_NEW_ARRIVAL_JSON_CACHE;
    }

    public String homepageOnPromotionJsonCache() {
        return ON_PROMOTION_JSON_CACHE;
    }

    public String getMailFeeCacheKeyList() {
        return RedisKeyConstant.MAIL_FEE_CACHE_KEYSET;
    }

    public String getMailFeeKey(String regionCode) {
        return RedisKeyConstant.MAIL_FEE_CACHE_KEY_PREFIX + regionCode;
    }


    public String getCouponConfigCacheKeyset() {
        return RedisKeyConstant.COUPON_CONFIG_KEYSET;
    }

    public String getCouponConfigCacheKey(long id) {
        return RedisKeyConstant.COUPON_CONFIG_KEY_PREFIX + id;
    }

    public String getFlashsaleCacheByProductId(long productId) {
        return FLASH_SALE_CACHE_BY_PRODUCT_ID + productId;
    }

    public String getParamConfigCacheKeyset() {
        return RedisKeyConstant.PARAM_CONFIG_KEYSET;
    }

    public String getParamConfigCacheKey() {
        return RedisKeyConstant.PARAM_CONFIG_KEY_PREFIX;
    }

    public String getMerchantCacheKey(long id) {
        return RedisKeyConstant.MERCHANT_CACHE_KEY_PREFIX + id;
    }

    public String getMerchantJsonCacheKey(long id) {
        return RedisKeyConstant.MERCHANT_JSON_CACHE_KEY_PREFIX + id;
    }

    public String getMerchantCacheKeySet() {
        return RedisKeyConstant.MERCHANT_CACHE_KEYSET;
    }


    public void updateMerchantJsonCache(long merchantId) {
        String key = getMerchantJsonCacheKey(merchantId);
        cache.remove(key);
    }

    public String getMemberTokenKey(int deviceType, long uid) {
        return RedisKeyConstant.KEY_USER_TOKEN_KEY + deviceType + ":" + uid;
    }

    public String getMixOptionCacheSet() {
        return RedisKeyConstant.MIX_OPTION_KEY_SET;
    }


    public String getMemberPlatformTokenKey(long uid) {
        return RedisKeyConstant.KEY_USER_PLATFORM_TOKEN_KEY + uid;
    }


    public String getMembershipJsonCache() {
        return RedisKeyConstant.KEY_MEMBER_SHIP_JSON_CACHE_KEY;
    }

    public String getGrouponRequireOrders() {
        return RedisKeyConstant.KEY_MEMBER_SHIP_JSON_CACHE_KEY;
    }

    public String getNotifySmsCacheKey(long orderId) {
        return SMS_TO_TAKE_NOTIFY_CACHE_KEY + orderId;
    }

    public String getSkuStockCache(long skuId) {
        return RedisKeyConstant.SKU_STOCK_CACHE + skuId;
    }

    public String getProductViewKey(long productId) {
        return PRODUCT_VIEW_KEY + productId;
    }

    public String getGroupProductJsonCacheKey(long id) {
        return RedisKeyConstant.GROUP_PRODUCT_JSON_CACHE_KEY_PREFIX + id;
    }

    public String getGroupProductStatJsonCacheKey(long id) {
        return RedisKeyConstant.GROUP_PRODUCT_STAT_JSON_CACHE_KEY_PREFIX + id;
    }
}
