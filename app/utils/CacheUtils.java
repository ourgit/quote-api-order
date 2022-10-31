package utils;

import constants.BusinessConstant;
import constants.RedisKeyConstant;

import javax.inject.Singleton;

/**
 * cache utils
 */
@Singleton
public class CacheUtils {

    public String getLastVerifyCodeKey(String phoneNumber, int bizType) {
        return BusinessConstant.KEY_LAST_VERIFICATION_CODE_PREFIX_KEY + ":" + bizType + ":" + phoneNumber;
    }


    public String getParamConfigCacheKey() {
        return RedisKeyConstant.PARAM_CONFIG_KEY_PREFIX;
    }

    public String getMemberTokenKey(int deviceType, long uid) {
        return RedisKeyConstant.KEY_USER_TOKEN_KEY + deviceType + ":" + uid;
    }


    public String getMemberPlatformTokenKey(long uid) {
        return RedisKeyConstant.KEY_USER_PLATFORM_TOKEN_KEY + uid;
    }


    public String getSkuStockCache(long skuId) {
        return RedisKeyConstant.SKU_STOCK_CACHE + skuId;
    }


    public String getArticleCategoryKey(String cateName) {
        return RedisKeyConstant.ARTICLE_CATEGORY_KEY_PREFIX + cateName;
    }

    public String getArticleCategoryKey(int categoryId) {
        return RedisKeyConstant.ARTICLE_CATEGORY_KEY_BY_ID_PREFIX + categoryId;
    }

    public String getCarouselJsonCache(int bizType, int clientType) {
        return RedisKeyConstant.CAROUSEL_JSON_CACHE + ":" + bizType + ":" + clientType;
    }

    public String getCategoryJsonCache() {
        return RedisKeyConstant.CATEGORIES_LIST_CACHE_KEY_PREFIX;
    }

    public String getPostCategoryJsonCache() {
        return RedisKeyConstant.POST_CATEGORIES_LIST_CACHE_KEY_PREFIX;
    }

    public String getCategoryWithPostsJsonCache() {
        return RedisKeyConstant.CATEGORIES_POSTS_LIST_CACHE_KEY_PREFIX;
    }
}
