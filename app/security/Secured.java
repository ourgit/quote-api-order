package security;

import com.fasterxml.jackson.databind.node.ObjectNode;
import play.Logger;
import play.cache.NamedCache;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import utils.BizUtils;
import utils.EncodeUtils;
import utils.ValidationUtil;

import javax.inject.Inject;
import java.util.Optional;

public class Secured extends Security.Authenticator {

    Logger.ALogger logger = Logger.of(Secured.class);
    @Inject
    BizUtils businessUtils;

    @Inject
    @NamedCache("redis")
    protected play.cache.AsyncCacheApi redis;

    @Inject
    EncodeUtils encodeUtils;

    /**
     * 认证接口，目前以token作为凭据，如果有在缓存中说明是合法用户，如果不在缓存中说明是非法用户
     *
     * @return
     */
    @Override
    public Optional<String> getUsername(Http.Request request) {
        String uri = request.uri();
        String uidToken = businessUtils.getUIDFromRequest(request);
        if (ValidationUtil.isEmpty(uidToken)) return Optional.empty();
        try {
            String authToken = "";
            Optional<Object> tokenOptional = redis.sync().get(uidToken);
            if (!tokenOptional.isPresent()) return Optional.empty();
            authToken = (String) tokenOptional.get();//uid token对应的是用户uid
            if (ValidationUtil.isEmpty(authToken)) return Optional.empty();
            String timeStamp = "";
            String md5 = "";
            String nonce = "";
            Optional<String> tOptional = request.getHeaders().get("t");
            if (tOptional.isPresent()) {
                timeStamp = tOptional.get();
            }
            Optional<String> sOptional = request.getHeaders().get("s");
            if (sOptional.isPresent()) {
                md5 = sOptional.get();
            }
            Optional<String> nonceOptional = request.getHeaders().get("nonce");
            if (nonceOptional.isPresent()) {
                nonce = nonceOptional.get();
            }
            if (ValidationUtil.isEmpty(nonce) || ValidationUtil.isEmpty(timeStamp) || ValidationUtil.isEmpty(md5))
                return Optional.empty();
            long timeStampLong = Long.parseLong(timeStamp);
            long currentTime = System.currentTimeMillis() / 1000;
            if (currentTime > timeStampLong + 30) {
//                logger.info("uri 11:" + uri);
                return Optional.empty();//请求时间超出8秒置错
            }
            String salt = authToken + EncodeUtils.API_SALT + timeStamp + nonce;
            String md5FirstTime = encodeUtils.getMd5(salt);

            String md5SecondTime = encodeUtils.getMd5(authToken + md5FirstTime);
            if (!md5.equals(md5SecondTime)) {
//                logger.info("uri 22:" + uri);
                return Optional.empty();
            }

            Optional<Object> platformKeyOptional = redis.sync().get(authToken);
            if (!platformKeyOptional.isPresent()) {
//                logger.info("uri 33:" + uri);
                return Optional.empty();
            }
            String platformKey = (String) platformKeyOptional.get();
            if (ValidationUtil.isEmpty(platformKey)) {
//                logger.info("uri 44:" + uri);
                return Optional.empty();
            }
            Optional<Long> optional = redis.sync().get(platformKey);
            if (!optional.isPresent()) {
//                logger.info("uri 55:" + uri);
                return Optional.empty();
            }
            Long uid = optional.get();
            return Optional.of(uid + "");
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Result onUnauthorized(Http.Request req) {
        ObjectNode node = Json.newObject();
        node.put("code", 403);
        node.put("reason", "亲，请先登录...");
        return ok(node);
    }

}
