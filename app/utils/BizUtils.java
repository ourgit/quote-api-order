package utils;

import actor.ActorProtocol;
import akka.actor.ActorRef;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import constants.BusinessConstant;
import io.ebean.DB;
import io.ebean.Ebean;
import models.system.ParamConfig;
import models.user.Member;
import models.user.MemberBalance;
import play.Logger;
import play.cache.NamedCache;
import play.libs.Json;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static constants.BusinessConstant.*;

@Singleton
public class BizUtils {

    Logger.ALogger logger = Logger.of(BizUtils.class);
    public static final int TOKEN_EXPIRE_TIME = 2592000;

    @Inject
    CacheUtils cacheUtils;

    @Inject
    Config config;


    @Inject
    EncodeUtils encodeUtils;

    @Inject
    @NamedCache("redis")
    protected play.cache.redis.AsyncCacheApi redis;

    public static DecimalFormat DF = new DecimalFormat("0.0");

    public CompletionStage<Long> getUserIdByAuthToken(Http.Request request) {
        return CompletableFuture.supplyAsync(() -> getUserIdByAuthToken2(request));
    }


    public String getUIDFromRequest(Http.Request request) {
        Optional<String> authTokenHeaderValues = request.getHeaders().get(KEY_AUTH_TOKEN_UID);
        if (authTokenHeaderValues.isPresent()) {
            String authToken = authTokenHeaderValues.get();
            return authToken;
        }
        return "";
    }

    public long getUserIdByAuthToken2(Http.Request request) {
        String uidToken = getUIDFromRequest(request);
        if (ValidationUtil.isEmpty(uidToken)) return 0L;
        Optional<String> tokenOptional = redis.sync().get(uidToken);
        if (!tokenOptional.isPresent()) return 0L;

        String authToken = tokenOptional.get();//uid token对应的是用户uid
        if (ValidationUtil.isEmpty(authToken)) return 0L;
        Optional<String> platformKeyOptional = redis.sync().get(authToken);
        if (!platformKeyOptional.isPresent()) return 0L;
        String platformKey = platformKeyOptional.get();
        if (ValidationUtil.isEmpty(platformKey)) return 0L;
        Optional<Long> optional = redis.sync().get(platformKey);
        if (!optional.isPresent()) return 0L;
        Long uid = optional.get();
        return uid;
    }

    public long getCurrentTimeBySecond() {
        return System.currentTimeMillis() / 1000;
    }

    public int getTokenExpireTime() {
        int tokenExpireTime = config.getInt("token_expire_time");
        if (tokenExpireTime < 1) tokenExpireTime = TOKEN_EXPIRE_TIME;
        return tokenExpireTime;
    }

    public String getRequestIP(Http.Request request) {
        String ip = null;
        try {
            String remoteAddr = request.remoteAddress();
            String forwarded = request.getHeaders().get("X-Forwarded-For").get();
            String realIp = request.getHeaders().get(BusinessConstant.X_REAL_IP_HEADER).get();
            if (forwarded != null) {
                ip = forwarded.split(",")[0];
            }
            if (ValidationUtil.isEmpty(ip)) {
                ip = realIp;
            }
            if (ValidationUtil.isEmpty(ip)) {
                ip = remoteAddr;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return ip == null ? "" : escapeHtml(ip);
    }

    public boolean checkVcode(String accountName, String vcode) {
        if (ValidationUtil.isPhoneNumber(accountName)) {
            String key = cacheUtils.getSMSLastVerifyCodeKey(accountName);
            Optional<String> optional = redis.sync().get(key);
            if (optional.isPresent()) {
                String correctVcode = optional.get();
                if (!ValidationUtil.isEmpty(correctVcode)) {
                    if (ValidationUtil.isVcodeCorrect(vcode) && ValidationUtil.isVcodeCorrect(correctVcode) && vcode.equals(correctVcode))
                        return true;
                }
            }
        } else return false;
        return false;
    }


    /**
     * 转义html脚本
     *
     * @param value
     * @return
     */
    public String escapeHtml(String value) {
        if (ValidationUtil.isEmpty(value)) return "";
        value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        value = value.replaceAll("\\(", "（").replaceAll("\\)", "）");
        value = value.replaceAll("eval\\((.*)\\)", "");
        value = value.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
        value = value.replaceAll("script", "");
        value = value.replaceAll("select", "");
        value = value.replaceAll("insert", "");
        value = value.replaceAll("update", "");
        value = value.replaceAll("delete", "");
        value = value.replaceAll("%", "\\%");
        value = value.replaceAll("union", "");
        value = value.replaceAll("load_file", "");
        value = value.replaceAll("outfile", "");
        return value;
    }

    public boolean setLock(String id, String operationType) {
        String key = operationType + ":" + id;
        try {
            if (redis.exists(key).toCompletableFuture().get(10, TimeUnit.SECONDS)) return false;
            return redis.setIfNotExists(key, 1, 5).toCompletableFuture().get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("getLock:" + e.getMessage());
            redis.remove(key);
        }
        return true;
    }


    /**
     * 解锁
     *
     * @param uid
     * @param operationType
     */
    public void unLock(String uid, String operationType) {
        redis.remove(operationType + ":" + uid);
    }

    public String hidepartialChar(String userName) {
        if (ValidationUtil.isEmpty(userName)) return "";
        if (ValidationUtil.isValidEmailAddress(userName)) {
            int index = userName.indexOf("@");
            if (index < 2) return userName;
            if (index > 5) {
                return "*" + userName.substring(8);
            } else {
                String toReplaced = userName.substring(1, index);
                String result = toReplaced.replaceAll(".", "*");
                return userName.substring(0, 1) + result + userName.substring(index);
            }
        }
        if (ValidationUtil.isPhoneNumber(userName)) {
            return "*" + userName.substring(8);
        }
        String toReplaced = userName.substring(1);
        String result = toReplaced.replaceAll(".", "*");
        return userName.substring(0, 1) + result;
    }

    public String getUserName(Member member) {
        String userName = "";
        if (null != member) {
            userName = member.realName;
            if (ValidationUtil.isEmpty(userName)) userName = member.nickName;
        }
        return userName;
    }

    public String getConfigValue(String key) {
        String value = "";
        Optional<Object> accountOptional = redis.sync().get(key);
        if (accountOptional.isPresent()) {
            value = (String) accountOptional.get();
            if (!ValidationUtil.isEmpty(value)) return value;
        }
        if (ValidationUtil.isEmpty(value)) {
            ParamConfig config = ParamConfig.find.query().where()
                    .eq("key", key)
                    .orderBy().asc("id")
                    .setMaxRows(1).findOne();
            if (null != config) {
                value = config.value;
                redis.set(key, value, 30 * 3600 * 24);
            }
        }
        return value;
    }


    public boolean checkBalanceEnough(long uid, long totalAmount) {
        MemberBalance cashBalance = MemberBalance.find.query().where().eq("uid", uid)
                .eq("itemId", BusinessItem.CASH).setMaxRows(1).findOne();
        if (null != cashBalance) {
            if (cashBalance.leftBalance >= totalAmount) return true;
        }
        return false;
    }


    public String limit20(String value) {
        if (ValidationUtil.isEmpty(value)) return "";
        if (value.length() > 20) return value.substring(0, 17) + "...";
        return value;
    }

    public String limit10(String value) {
        if (ValidationUtil.isEmpty(value)) return "";
        if (value.length() > 10) return value.substring(0, 7) + "...";
        return value;
    }


    public static BufferedImage desaturate(BufferedImage source) {
        ColorConvertOp colorConvert =
                new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        colorConvert.filter(source, source);
        return source;
    }


    public String convertScene(ObjectNode node) {
        String temp = Json.stringify(node);
        return temp.replaceAll("\\{", "(")
                .replaceAll("\\}", ")")
                .replaceAll("\"", "'");
    }


    public void setSkuStockCache(long skuId, long stock) {
        String key = cacheUtils.getSkuStockCache(skuId);
        redis.set(key, stock, 24 * 3600);
    }


    public String getMemberName(Member member) {
        String name = "";
        if (null != member) {
            name = member.realName;
            if (ValidationUtil.isEmpty(name)) name = member.nickName;
        }
        return name;
    }

    public String getDomain() {
        return getConfigValue(PARAM_KEY_DEFAULT_HOME_PAGE_URL);
    }

    public String getWechatMpAppId() {
        return getEncryptConfigValue(PARAM_KEY_WECHAT_MP_APP_ID);
    }

    public String getWechatMpSecretCode() {
        return getEncryptConfigValue(PARAM_KEY_WECHAT_MP_SECRET_CODE);
    }

    public String getWechatAppId() {
        return getEncryptConfigValue(PARAM_KEY_WECHAT_APP_ID);
    }

    public String getWechatMiniAppId() {
        return getEncryptConfigValue(PARAM_KEY_WECHAT_MINI_APP_ID);
    }

    public String getWechatMiniAppSecretCode() {
        return getEncryptConfigValue(PARAM_KEY_WECHAT_MINI_APP_SECRET_CODE);
    }

    public String getWechatMchId() {
        return getEncryptConfigValue(PARAM_KEY_WECHAT_MCH_ID);
    }

    public String getWechatMchAppSecretCode() {
        return getEncryptConfigValue(PARAM_KEY_WECHAT_MCH_API_SECURITY_CODE);
    }

    public String getEncryptConfigValue(String key) {
        Optional<Object> accountOptional = redis.sync().get(key);
        if (accountOptional.isPresent()) {
            String value = (String) accountOptional.get();
            if (!ValidationUtil.isEmpty(value)) return value;
        }

        ParamConfig config = ParamConfig.find.query().where()
                .eq("key", key)
                .orderBy().asc("id")
                .setMaxRows(1).findOne();
        if (null != config && !ValidationUtil.isEmpty(config.value)) {
            String decryptValue = encodeUtils.decrypt(config.value);
            redis.set(key, decryptValue);
        }
        return "";
    }


    public String getAlinYunAccessId() {
        return getEncryptConfigValue(PARAM_KEY_ALI_YUN_ACCESS_ID);
    }

    public String getAliYunSecretKey() {
        return getEncryptConfigValue(PARAM_KEY_ALI_YUN_SECRET_KEY);
    }


    public String getAlipayAppId() {
        return getConfigValue(PARAM_KEY_ALIPAY_APPID);
    }

    public String getAlipayAppPrivateKey() {
        return getConfigValue(PARAM_KEY_ALIPAY_APP_PRIVATE_KEY);
    }

    //    public String getAlipayPublicKeyRSA2() {
//        return getConfigValue(PARAM_KEY_ALIPAY_PUBLIC_KEY_RSA2);
//    }
    public String getAlipayAliPublicKey() {
        return getConfigValue(PARAM_KEY_ALIPAY_ALI_PUBLIC_KEY);
    }

    public String getAlipayWapPayNotifyUrl() {
        return getConfigValue(PARAM_KEY_ALIPAY_WAP_PAY_NOTIFY_URL);
    }

    public String getAlipayDirectPayNotifyUrl() {
        return getConfigValue(PARAM_KEY_ALIPAY_DIRECT_PAY_NOTIFY_URL);
    }

    public String getAlipayReturnUrl() {
        return getConfigValue(PARAM_KEY_ALIPAY_RETURN_URL);
    }

    public String getAlipayPartnerNo() {
        return getConfigValue(PARAM_KEY_ALIPAY_PARTNER_NO);
    }

    public String getAlipaySellAccountName() {
        return getConfigValue(PARAM_KEY_ALIPAY_SELL_ACCOUNT_NAME);
    }

    public String getKDHostUrl() {
        return getConfigValue(PARAM_KEY_KD_HOST_URL);
    }

    public String getKDAppCode() {
        return getConfigValue(PARAM_KEY_KD_APP_CODE);
    }

    public String getWepaySpAppId() {
        return getConfigValue(PARAM_KEY_WE_PAY_SP_APP_ID);
    }

    public String getWepaySpMchId() {
        return getConfigValue(PARAM_KEY_WE_PAY_SP_MCH_ID);
    }

    public String getWepaySubMchId() {
        return getConfigValue(PARAM_KEY_WE_PAY_SUB_MCH_ID);
    }

    public String getWepaySubKeySerialNo() {
        return getConfigValue(PARAM_KEY_WE_PAY_KEY_SERIAL_NO);
    }

    public String getWepayAPIV3Key() {
        return getConfigValue(PARAM_KEY_WE_PAY_API_V3_KEY);
    }

    public String getWepayPrivateKey() {
        return getConfigValue(PARAM_KEY_WE_PAY_PRIVATE_KEY);
    }

    public String generateAuthToken() {
        String authToken = UUID.randomUUID().toString();
        return authToken;
    }

    public void handleCacheToken(Member member, String authToken, int deviceType, String idToken) {
        //authtoken -> platformtoken
        //uid:token:1 -> TOKEN_FOR_PLATFORM:UID
        //platformtoken -> Member

        //idToken => authToken
        // userToken(uid:token:1) => authToken
        //authToken => platformKey
        //platformKey => user
        //缓存新的token数据
        String tokenKey = cacheUtils.getMemberTokenKey(deviceType, member.id);

        //把旧的删除
        removeOldToken(tokenKey);
        int expireTime = getTokenExpireTime();
        String platformTokenKey = cacheUtils.getMemberPlatformTokenKey(member.id);
        redis.set(idToken, authToken, expireTime);
        redis.set(tokenKey, authToken, expireTime);
        redis.set(authToken, platformTokenKey, expireTime);
        redis.set(platformTokenKey, member.id, expireTime);
    }

    public void removeOldToken(String tokenKey) {
        Optional<Object> oldOptional = redis.sync().get(tokenKey);
        if (oldOptional.isPresent()) {
            String oldAuthToken = (String) oldOptional.get();
            if (!ValidationUtil.isEmpty(oldAuthToken)) {
                redis.remove(oldAuthToken);
            }
            redis.remove(tokenKey);
        }
    }


    public void createBalance(Member member, int itemId) {
        MemberBalance balance = MemberBalance.find.query().where().eq("uid", member.id)
                .eq("itemId", BusinessItem.CASH).setMaxRows(1).findOne();
        if (null == balance) {
            balance = new MemberBalance();
            balance.setItemId(itemId);
            balance.setUid(member.id);
            long currentTime = System.currentTimeMillis() / 1000;
            balance.setUpdateTime(currentTime);
            balance.setCreatedTime(currentTime);
            balance.save();
        }
    }

    public Member findByAccountNameAndPassword(String accountName, String password) {
        String targetPassword = encodeUtils.getMd5WithSalt(password);
        return DB.find(Member.class).where().eq("accountName",
                        accountName.toLowerCase()).eq("loginPassword", targetPassword)
                .setMaxRows(1)
                .orderBy().asc("id")
                .findOne();
    }


    public boolean upToIPLimit(Http.Request request, String key, int max) {
        String ip = getRequestIP(request);
        if (!ValidationUtil.isEmpty(ip)) {
            String accessCount = "";
            Optional<Object> optional = redis.sync().get(key + ip);
            if (optional.isPresent()) {
                accessCount = (String) optional.get();
            }
            if (ValidationUtil.isEmpty(accessCount)) {
                redis.set(key + ip, "1", BusinessConstant.KEY_EXPIRE_TIME_2M);
            } else {
                int accessCountInt = Integer.parseInt(accessCount) + 1;
                if (accessCountInt > max) return true;
                redis.set(key + ip, String.valueOf(accessCountInt), BusinessConstant.KEY_EXPIRE_TIME_2M);
            }

        }
        return false;
    }
}
