package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import constants.BusinessConstant;
import constants.RedisKeyConstant;
import io.ebean.DB;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import io.ebean.annotation.Transactional;
import models.log.BalanceLog;
import models.log.LoginLog;
import models.msg.Msg;
import models.shop.ShopApplyLog;
import models.user.Member;
import models.user.MemberBalance;
import play.Logger;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import security.Secured;
import utils.*;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static constants.BusinessConstant.*;
import static models.user.Member.ACCOUNT_TYPE_PHONE_EMAIL;

/**
 * 用户控制类
 */
public class MemberController extends BaseController {

    Logger.ALogger logger = Logger.of(MemberController.class);

    @Inject
    EncodeUtils encodeUtils;

    @Inject
    HttpRequestDeviceUtils httpRequestDeviceUtils;
    @Inject
    IPUtil ipUtil;
    @Inject
    MessagesApi messagesApi;

    @Inject
    MailerService mailerService;
    private static final int CHANGE_LOGIN_PASSWORD = 1;
    private static final int CHANGE_PAY_PASSWORD = 2;

    /**
     * @api {post} /v1/user/new/ 01用户注册
     * @apiName signUp
     * @apiGroup User
     * @apiParam {string} accountName 帐号 可手机号也可邮箱
     * @apiParam {string} [vCode] 短信验证码，预留
     * @apiParam {String} loginPassword 登录密码 6-20位，不允许包含非法字符
     * @apiSuccess (Success 200){int} code 200成功创建
     * @apiSuccess (Error 40003){int} code 40001 参数错误
     * @apiSuccess (Error 40001){int} code 40002 帐号已被注册
     * @apiSuccess (Error 40002){int} code 40003 无效的短信验证码
     * @apiSuccess (Error 40004){int} code 40004 登录密码无效
     * @apiSuccess (Error 40006){int} code 40006 无效的手机号码
     * @apiSuccess (Error 40007){int} code 40007 达到注册的限制次数
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public CompletionStage<Result> signUp(Http.Request request) {
        JsonNode json = request.body().asJson();
        return CompletableFuture.supplyAsync(() -> {
            if (null == json) return okCustomJson(40003, "参数错误");
            String accountName = json.findPath("accountName").asText();
            String nickName = json.findPath("nickName").asText();
            String vcode = json.findPath("vcode").asText();
            int accountType = json.findPath("accountType").asInt();
            if (accountType == ACCOUNT_TYPE_PHONE_EMAIL) {
                if (!ValidationUtil.isValidEmailAddress(accountName))
                    return okCustomJson(40006, "无效的邮箱");
            }
            String loginPassword = json.findPath("loginPassword").asText();
            if (!ValidationUtil.isValidPassword(loginPassword)) return okCustomJson(40004, "无效的密码");
            if (ValidationUtil.isEmpty(vcode)) return okCustomJson(40004, "请输入验证码");
            String key = cacheUtils.getSMSLastVerifyCodeKey(accountName);
            Optional<String> optional = redis.sync().get(key);
            if (optional.isEmpty()) return okCustomJson(40004, "请先请求验证码");
            String vcodeSend = optional.get();
            if (!vcodeSend.equalsIgnoreCase(vcode)) return okCustomJson(40004, "验证码有误");

            Member foundMember = Member.find.query().where()
                    .eq("accountName", accountName.trim())
                    .setMaxRows(1).findOne();
            if (null != foundMember) return okCustomJson(40001, "帐号已被注册");
            Member resultMember = saveMemberForNormalSignup(accountName, loginPassword, nickName, "");
            if (null == resultMember) return okCustomJson(CODE500, "注册发生异常，请联系客服");
            ObjectNode authTokenJson = Json.newObject();
            int deviceType = httpRequestDeviceUtils.getMobileDeviceType(request);
            String authToken = businessUtils.generateAuthToken();
            authTokenJson.put(RedisKeyConstant.AUTH_TOKEN, authToken);
            String idToken = businessUtils.generateAuthToken();
            authTokenJson.put("uidToken", idToken);
            authTokenJson.put(CODE, CODE200);
            ObjectNode memberJson = (ObjectNode) Json.toJson(resultMember);
            memberJson.put("code", 200);
            businessUtils.handleCacheToken(resultMember, authToken, deviceType, idToken);
            handleInterceptionAfterSignup(json, resultMember);
            Http.Session session = generateAuthSessionCookie(authToken);
            return ok(authTokenJson).withSession(session);
        });
    }

    public Member saveMemberForNormalSignup(String accountName, String loginPassword, String nickName, String avatarUrl) {
        Member member = new Member();
        long currentTime = businessUtils.getCurrentTimeBySecond();
        member.setNickName(nickName);
        member.setAccountName(accountName);
        member.setStatus(Member.MEMBER_STATUS_NORMAL);
        if (!ValidationUtil.isEmpty(loginPassword)) member.setLoginPassword(encodeUtils.getMd5WithSalt(loginPassword));
        else member.setLoginPassword("");
        member.setPayPassword("");
        if (ValidationUtil.isEmpty(avatarUrl)) avatarUrl = businessUtils.getConfigValue(PARAM_KEY_DEFAULT_AVATAR);
        member.setAvatar(avatarUrl);
        member.setCreatedTime(currentTime);
        if (ValidationUtil.isEmpty(member.avatar)) member.setAvatar("");
        member.save();
        //保存用户余额记录
        prepareBalance(member);
        return member;
    }


    private void handleInterceptionAfterSignup(JsonNode jsonNode, Member member) {
        CompletableFuture.runAsync(() -> {

        });
    }


    private void prepareBalance(Member member) {
        businessUtils.createBalance(member, BusinessItem.CASH);
        businessUtils.createBalance(member, BusinessItem.SCORE);
        businessUtils.createBalance(member, BusinessItem.AWARD);
    }

    /**
     * @api {POST} /v1/user/signup_vcode/ 02请求注册激活码
     * @apiName requestSignupVcode
     * @apiGroup User
     * @apiParam {string} accountName 帐号 可手机号也可邮箱
     * @apiParam {String} accountType 1手机 2邮箱
     */
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> requestSignupVcode(Http.Request request) {
        return CompletableFuture.supplyAsync(() -> {
            JsonNode jsonNode = request.body().asJson();
            String accountName = jsonNode.findPath("accountName").asText();
            int accountType = jsonNode.findPath("accountType").asInt();
            if (accountType == ACCOUNT_TYPE_PHONE_EMAIL) {
                mailerService.sendVcode(accountName);
            } else if (accountType == Member.ACCOUNT_TYPE_PHONE_NUMBER) {

            }
            return okJSON200();
        });
    }


    /**
     * @api {POST} /v1/user/login/ 02登录
     * @apiName login
     * @apiGroup User
     * @apiParam {string} accountName 用户名
     * @apiParam {string} password 密码
     * @apiParam {string} [vCode] 短信验证码，短信登录的时候使用
     * @apiSuccess (Success 200){long} id 用户id
     * @apiSuccess (Success 200){string} accountName 帐号名
     * @apiSuccess (Success 200){int} status 用户状态 1正常2锁定
     * @apiSuccess (Success 200){string} realName 真实姓名
     * @apiSuccess (Success 200){string} phoneNumber 手机号
     * @apiSuccess (Error 40001) {int} code 40001 传进来的参数有误
     * @apiSuccess (Error 40003) {int} code 40003 用户名或密码错误
     * @apiSuccess (Error 40005) {int}  code 40005 用户被锁定
     */
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> login(Http.Request request) {
        JsonNode jsonNode = request.body().asJson();
        String accountName = jsonNode.findPath("accountName").asText();
        String vCode = jsonNode.findPath("vCode").asText();
        String ip = businessUtils.getRequestIP(request);
        Http.Session session = request.session();
        return CompletableFuture.supplyAsync(() -> {
            if (ValidationUtil.isEmpty(accountName) || accountName.length() > 50)
                return okCustomJson(CODE40001, "参数错误");
            String password = jsonNode.findPath("password").asText();
            if (ValidationUtil.isEmpty(password)) password = "";
            if (ValidationUtil.isEmpty(password)) return okCustomJson(CODE40001, "参数错误");
            Member member = businessUtils.findByAccountNameAndPassword(accountName, password);
            if (null == member) return okCustomJson(CODE40003, "用户名或密码错误");
            if (null == member) {
                member = saveMemberForNormalSignup(accountName, password, "", "");
            }
            if (member.status == Member.MEMBER_STATUS_LOCK) {
                return okCustomJson(CODE40005, "用户被锁定");
            }

            ObjectNode memberJson = (ObjectNode) Json.toJson(member);
            memberJson.put("code", 200);

            String authToken = businessUtils.generateAuthToken();
            String idToken = businessUtils.generateAuthToken();
            memberJson.put(RedisKeyConstant.AUTH_TOKEN, authToken);//返回token
            memberJson.put("uidToken", idToken);
            int deviceType = httpRequestDeviceUtils.getMobileDeviceType(request);
            businessUtils.handleCacheToken(member, authToken, deviceType, idToken);
            Http.Session cookie = generateAuthSessionCookie(authToken);
            //写到登录日志表中
            saveToLog(member, ip);
            return ok(memberJson).withSession(cookie);
        });
    }

    private Http.Session generateAuthSessionCookie(String authToken) {
        Map<String, String> map = new HashMap<>();
        map.put(KEY_AUTH_TOKEN, authToken);
        Http.Session session = new Http.Session(map);
        return session;
    }

    /**
     * 写到登录日志中
     *
     * @param member
     */
    private void saveToLog(Member member, String ip) {
        CompletableFuture.runAsync(() -> {
            LoginLog log = new LoginLog();
            long currentTime = System.currentTimeMillis() / 1000;
            String city = ipUtil.getCityByIp(ip);
            log.setCreateTime(currentTime);
            log.setPlace(city);
            log.setIp(ip);
            log.setUid(member.id);
            log.save();
        });
    }


    /**
     * @api {POST} /v1/user/logout/ 03注销
     * @apiName logout
     * @apiGroup User
     * @apiParam {long} id 用户id
     * @apiSuccess (Success 200){int} code 200 注销成功.
     * @apiSuccess (Error 40001) {int} code 40001 参数错误,未提供token
     */
    public Result logout(Http.Request request) {
        String idToken = businessUtils.getUIDFromRequest(request);
        if (ValidationUtil.isEmpty(idToken)) idToken = "";
        Optional<String> tokenOption = redis.sync().get(idToken);
        if (tokenOption.isPresent()) {
            String authToken = tokenOption.get();//uid token对应的是用户uid
            if (!ValidationUtil.isEmpty(authToken)) {
                Optional<String> platformKeyOptional = redis.sync().get(authToken);
                if (platformKeyOptional.isPresent()) {
                    String platformKey = platformKeyOptional.get();
                    redis.remove(platformKey);
                }
            }
            redis.remove(authToken);
        }
        redis.remove(idToken);
        request.session().data().clear();
        return okJSON200().discardingCookie(idToken);
    }


    /**
     * @api {POST} /v1/user/loginPassword/ 04修改登录密码
     * @apiName loginPassword
     * @apiGroup User
     * @apiParam {string}oldPassword旧密码
     * @apiParam {string}newPassword新密码
     * @apiSuccess (Success 200){int} code 200 成功修改
     * @apiSuccess (Error 40001){int} code 40001 密码不合法6-20位
     * @apiSuccess (Error 40002){int} code 40002 旧密码验证不通过
     * @apiSuccess (Error 40004){int} code 40004 用户不存在
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured.class)
    public CompletionStage<Result> changeLoginPassword(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            JsonNode jsonNode = request.body().asJson();
            String oldPassword = jsonNode.findPath("oldPassword").textValue();
            String newPassword = jsonNode.findPath("newPassword").textValue();
            if (!checkPassword(oldPassword) || !checkPassword(newPassword))
                return okCustomJson(CODE40001, "密码不合法");
            return savePassword(uid, oldPassword, newPassword, CHANGE_LOGIN_PASSWORD, request);
        });
    }

    /**
     * 保存密码
     *
     * @param oldPassword
     * @param newPassword
     * @return
     */
    private Result savePassword(long uid, String oldPassword, String newPassword, int type, Http.Request request) {
        Member member = Member.find.byId(uid);
        if (null != member) {
            String newTargetPassword = encodeUtils.getMd5WithSalt(newPassword);
            String storedPassword = "";
            switch (type) {
                case CHANGE_LOGIN_PASSWORD:
                    storedPassword = member.loginPassword;
                    member.setLoginPassword(newTargetPassword);
                    break;
                case CHANGE_PAY_PASSWORD:
                    storedPassword = member.payPassword;
                    member.setPayPassword(newTargetPassword);
                    break;
            }
            if (null == storedPassword || storedPassword.length() < 1) {
                member.update();
                String authToken = businessUtils.generateAuthToken();
                String idToken = businessUtils.generateAuthToken();
                ObjectNode memberJson = Json.newObject();
                memberJson.put(RedisKeyConstant.AUTH_TOKEN, authToken);//返回token
                memberJson.put("uidToken", idToken);
                Http.Session session = refreshAuthCookie(member, authToken, type, idToken);
                return ok(memberJson).withSession(session);
            } else if (encodeUtils.getMd5WithSalt(oldPassword).equals(storedPassword)) {
                member.update();
                ObjectNode memberJson = Json.newObject();
                memberJson.put(CODE, CODE200);
                String authToken = businessUtils.generateAuthToken();
                String idToken = businessUtils.generateAuthToken();
                memberJson.put(RedisKeyConstant.AUTH_TOKEN, authToken);//返回token
                memberJson.put("uidToken", idToken);
                Http.Session session = refreshAuthCookie(member, authToken, type, idToken);
                return ok(memberJson).withSession(session);
            } else return okCustomJson(CODE40002, "密码错误");
        } else return okCustomJson(CODE40004, "用户不存在");
    }

    public Http.Session refreshAuthCookie(Member member, String idToken, int type, String authToken) {
        String tokenKey = cacheUtils.getMemberTokenKey(type, member.id);
        //把旧的删除
        businessUtils.removeOldToken(tokenKey);
        businessUtils.handleCacheToken(member, authToken, type, idToken);
        Http.Session cookie = generateAuthSessionCookie(authToken);
        return cookie;
    }

    private boolean checkPassword(String password) {
        if (null == password || password.length() < 6 || password.length() > 20) return false;
        else return true;
    }

    /**
     * @api {get} /v1/user/?code= 05获取用户信息
     * @apiName getUser
     * @apiGroup User
     * @apiSuccess (Success 200){string} phoneNumber 手机号
     * @apiSuccess (Success 200){long} score 积分
     * @apiSuccess (Success 200){long} leftMoney 余额
     * @apiSuccess (Success 200){string} authStatus 认证状态  -1已拒绝 0未申请  1进行中 2已通过
     * @apiSuccess (Success 200){string} levelName 等级
     * @apiSuccess (Success 200){string} realName 实名
     * @apiSuccess (Success 200){string} nickName 昵称
     * @apiSuccess (Success 200){string} uid 用户Id
     * @apiSuccess (Success 200){string} birthday 生日
     * @apiSuccess (Success 200){string} idCardNo 身份证号码
     * @apiSuccess (Success 200){string} licenseNo 营业执照
     * @apiSuccess (Success 200){int} gender 0：未知、1：男、2：女
     * @apiSuccess (Success 200){String} city 城市
     * @apiSuccess (Success 200){String} province 省份
     * @apiSuccess (Success 200){String} country 国家
     * @apiSuccess (Success 200){String} shopName 品牌
     * @apiSuccess (Success 200){String} contactPhoneNumber 联系电话
     * @apiSuccess (Success 200){String} contactAddress 联系地址
     * @apiSuccess (Success 200){String} businessItems 经营类目
     * @apiSuccess (Success 200){String} images 图片，多张，以逗号隔开
     * @apiSuccess (Success 200){String} dealerCode 业务员/代理商编号(如果自己是代理商或业务员才会有这个字段)
     * @apiSuccess (Success 200){String} dealerName 绑定的业务员名字
     * @apiSuccess (Success 200){String} dealerPhoneNumber 绑定的业务员联系电话
     * @apiSuccess (Success 200){int} couponCount 可用优惠券数量
     * @apiSuccess (Success 200){Object} profile 用户认证相关对象
     * @apiSuccess (Success 200){string} authStatus 认证状态  -1已拒绝 0未申请  1进行中 2已通过
     * @apiSuccess (Success 200){long} applyTime 申请时间
     * @apiSuccess (Success 200){long} approveTime 认证通过时间
     * @apiSuccess (Error 40001) {int} code 40001 未找到对应的用户
     */
    public CompletionStage<Result> getUser(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return okCustomJson(CODE403, "该用户不存在");
            boolean isLoginPasswordSet = false;
            boolean isPayPasswordSet = false;
            if (!ValidationUtil.isEmpty(member.loginPassword)) isLoginPasswordSet = true;
            if (!ValidationUtil.isEmpty(member.payPassword)) isPayPasswordSet = true;
            member.setPayPassword("");
            member.setLoginPassword("");
            ObjectNode jsonNode = (ObjectNode) Json.toJson(member);
            jsonNode.put(CODE, CODE200);
            jsonNode.put("isPayPasswordSet", isPayPasswordSet);
            jsonNode.put("isLoginPasswordSet", isLoginPasswordSet);

            List<MemberBalance> balances = MemberBalance.find.query().where()
                    .eq("uid", uid)
                    .orderBy().asc("itemId").findList();
            jsonNode.set("balances", Json.toJson(balances));

            return ok(jsonNode);
        });

    }


    /**
     * @api {GET} /v1/user/is_phonenumber_used/:phoneNumber/  06检测手机号码是否已被注册
     * @apiName is_phonenumber_used
     * @apiGroup User
     * @apiParam {string} phoneNumber 手机号
     * @apiSuccess (Success 200){int} code 200 未注册
     * @apiSuccess (Success 200){boolean} used true已注册，false未注册
     * @apiSuccess (Error 40001) {int}code 40001 无效的手机号码
     */
    public CompletionStage<Result> isPhoneNumberUsed(String phoneNumber) {
        return CompletableFuture.supplyAsync(() -> {
            if (!ValidationUtil.isPhoneNumber(phoneNumber)) {
                return okCustomJson(40001, "无效的手机号码");
            } else {
                Member member = Member.find.query().where().eq("phoneNumber", phoneNumber)
                        .setMaxRows(1)
                        .findOne();
                ObjectNode result = Json.newObject();
                result.put(CODE, CODE200);
                if (null == member) {
                    result.put("used", false);
                    return ok(result);
                } else {
                    String realName = member.realName;
                    if (ValidationUtil.isEmpty(realName)) realName = "";
                    result.put("used", true);
                    result.put("realName", realName);
                    return ok(result);
                }
            }
        });
    }

    /**
     * @api {post} /v1/user/ 07修改用户资料
     * @apiName updateUser
     * @apiGroup User
     * @apiParam {string} [realName] 姓名 不超过6位
     * @apiParam {string} [nickName] 昵称 不超过10位
     * @apiParam {string} [birthday] 生日
     * @apiParam {string} [avatar] 头像
     * @apiParam {int} [gender] 性别 0：未知、1：男、2：女
     * @apiParam {string} [city] 城市
     * @apiParam {string} [province] 省份
     * @apiParam {string} [country] 国家
     * @apiSuccess (Success 200) {int} code 200成功修改
     * @apiSuccess (Error 40001)  {int} code 40001 参数错误
     * @apiSuccess (Error 40002)  {int} code 40002 密码无效，密码为6到20位
     * @apiSuccess (Error 403)  {int} code 403 没有权限，请重新登录
     * @apiSuccess (Error 40004)  {int} code 40004 用户不存在
     */
    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> update(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            JsonNode jsonNode = request.body().asJson();
            if (jsonNode == null) return okCustomJson(40001, "参数错误");
            String realName = jsonNode.findPath("realName").asText();
            String nickName = jsonNode.findPath("nickName").asText();
            String avatar = jsonNode.findPath("avatar").asText();
            Member member = Member.find.byId(uid);
            if (null == member) return okCustomJson(CODE40001, "该用户不存在");

            if (!ValidationUtil.isEmpty(realName)) {
                if (realName.length() > 6) return okCustomJson(40001, "姓名 不超过6位");
                member.setRealName(realName);
            }
            if (!ValidationUtil.isEmpty(nickName)) {
                if (nickName.length() > 30) return okCustomJson(40001, "昵称太长，请重新填写");
                member.setNickName(nickName);
            }
            if (!ValidationUtil.isEmpty(avatar)) {
                member.setAvatar(avatar);
            }
            member.save();
            member.setLoginPassword("");
            member.setPayPassword("");
            return okJSON200();
        });
    }

    /**
     * @api {GET} /v1/user/is_valid_account/:accountName/ 08检验帐号是否有效
     * @apiExample 说明:1个ip一天限制10个该请求
     * http://192.168.2.100/v1/is_valid_account/:accountName/
     * @apiName isValidAccount
     * @apiGroup User
     * @apiSuccess (Success 200){int}code 200
     * @apiSuccess (Error 40001) {int}code 40001该手机号未注册
     * @apiSuccess (Error 40002) {int}code 40002超过当日最大调用次数
     */
    public CompletionStage<Result> isValidAccount(Http.Request request, String accountName) {
        boolean isUptoLimit = businessUtils.upToIPLimit(request, RedisKeyConstant.KEY_INVOKE_IS_VALID_ACCOUNT_LIMIT, BusinessConstant.MAX_COUNT_INVOKE_IS_VALID_ACCOUNT_PER_DAY);
        return CompletableFuture.supplyAsync(() -> {
            if (isUptoLimit) return okCustomJson(CODE40002, "超过当日最大调用次数");
            if (!ValidationUtil.isPhoneNumber(accountName)) return okCustomJson(CODE40001, "该手机号未注册");
            List<Member> members = Member.find.query().where().eq("phoneNumber", accountName).findList();
            if (members.size() < 1) return okCustomJson(CODE40001, "该手机号未注册");
            return okJSON200();
        });

    }

    /**
     * @api {POST} /v1/user/reset_login_password/ 09重置登录密码
     * @apiName resetLoginPassword
     * @apiGroup User
     * @apiParam {string} accountName 帐号
     * @apiParam {string} vcode 短信验证码
     * @apiParam {string} newPassword 新密码
     * @apiSuccess (Success 200){int}code 200
     * @apiSuccess (Error 40001) {int}code 40001无效的参数
     * @apiSuccess (Error 40002) {int}code 40002 无效的短信验证码
     * @apiSuccess (Error 40003) {int}code 40003 无效的密码
     * @apiSuccess (Error 40004) {int}code 40004无效的手机号码
     * @apiSuccess (Error 40005) {int}code 40005 该帐号不存在
     */
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> resetLoginPassword(Http.Request request) {
        JsonNode node = request.body().asJson();
        return CompletableFuture.supplyAsync(() -> {
            String accountName = node.findPath("accountName").asText();
            accountName = businessUtils.escapeHtml(accountName);
            String vcode = node.findPath("vcode").asText();
            if (!businessUtils.checkVcode(accountName, vcode))
                return okCustomJson(CODE40002, "无效手机号码/短信验证码");

            String newPassword = node.findPath("newPassword").asText();
            if (!checkPassword(newPassword)) return okCustomJson(CODE40003, "无效的密码");
            Member member = Member.find.query().where().eq("phoneNumber", accountName).setMaxRows(1).findOne();
            if (null == member) return okCustomJson(CODE40005, "该帐号不存在");
            member.setLoginPassword(encodeUtils.getMd5WithSalt(newPassword));
            member.save();
            return okJSON200();
        });
    }

    /**
     * @api {POST} /v1/user/set_login_password/ 10设置/修改登录密码
     * @apiName setLoginPassword
     * @apiGroup User
     * @apiParam {string} [oldPassword] 旧密码
     * @apiParam {string} password 新密码
     * @apiParam {string} [vcode] 短信验证码
     * @apiParam {long} uid 用戶Id
     * @apiSuccess (Success 200){int}code 200
     * @apiSuccess (Error 40001) {int}code 40001 无效的参数
     * @apiSuccess (Error 40004) {int}code 40004 该帐号不存在
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured.class)
    public CompletionStage<Result> setLoginPassword(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            JsonNode node = request.body().asJson();
            String newPassword = node.findPath("password").asText();
            String oldPassword = node.findPath("oldPassword").asText();
            String vcode = node.findPath("vcode").asText();
            long memberId = node.findPath("uid").asLong();
            if (uid != memberId) return okCustomJson(CODE403, "没有权限使用该功能");
            if (!checkPassword(newPassword)) return okCustomJson(CODE40003, "无效的密码");
            Member member = Member.find.byId(uid);
            if (null == member) return okCustomJson(CODE40004, "该帐号不存在");
            //todo need to change mail code
            if (!ValidationUtil.isEmpty(member.loginPassword)) {
                if (!member.loginPassword.equals(encodeUtils.getMd5WithSalt(oldPassword))) {
                    return okCustomJson(CODE40001, "原密码有误");
                }
            }
            member.setLoginPassword(encodeUtils.getMd5WithSalt(newPassword));
            member.save();
            //TODO sms prompt password change
            return okJSON200();
        });
    }

    /**
     * @api {GET} /v1/user/member_balance/?balanceType= 11用户余额信息
     * @apiName getMemberBalance
     * @apiGroup User
     * @apiSuccess (Success 200){int} code 请求返回码
     * @apiSuccess (Success 200){JsonArray} list 所持有币种的余额列表
     * @apiSuccess (Success 200){BigDecimal} leftBalance 可用余额
     * @apiSuccess (Success 200){BigDecimal} freezeBalance 冻结余额
     * @apiSuccess (Success 200){BigDecimal} totalBalance 总额
     * @apiSuccess (Success 200){BigDecimal} expireTime 失效时间
     * @apiSuccess (Error 403) {int} code 403 没有权限使用该功能
     */
    @Security.Authenticated(Secured.class)
    public CompletionStage<Result> getBalance(Http.Request request, int balanceType) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return okCustomJson(403, "没有权限");
            if (balanceType > 0) {
                MemberBalance balance = MemberBalance.find.query().where().eq("uid", uid)
                        .eq("itemId", balanceType).setMaxRows(1).findOne();
                if (null == balance) return okCustomJson(CODE40002, "没有该类型的余额");
                else {
                    ObjectNode resultNode = (ObjectNode) Json.toJson(balance);
                    resultNode.put(CODE, CODE200);
                    return ok(resultNode);
                }
            } else {
                List<MemberBalance> balances = MemberBalance.find.query().where()
                        .eq("uid", uid)
                        .orderBy().asc("itemId").findList();
                ObjectNode result = Json.newObject();
                result.put(CODE, CODE200);
                result.set("list", Json.toJson(balances));
                return ok(result);
            }
        });
    }

    /**
     * @api {GET} /v1/user/my_login_logs/?page= 12我的登录日志
     * @apiName listMyLoginLog
     * @apiGroup User
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Success 200) {JsonArray} list 列表
     * @apiSuccess (Success 200) {long} id
     * @apiSuccess (Success 200) {long} memberId
     * @apiSuccess (Success 200) {String} loginIp 登录ip
     * @apiSuccess (Success 200) {String} loginPlace 登录地点
     * @apiSuccess (Success 200) {String} createdTime 登录时间
     * @apiSuccess (Success 403){int} code 没有权限使用该功能
     * @apiSuccess (Error 505){int} code 未知错误
     */
    @Security.Authenticated(Secured.class)
    public CompletionStage<Result> listMyLoginLog(Http.Request request, int page) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            PagedList<LoginLog> pagedList = LoginLog.find.query().where().eq("uid", uid)
                    .orderBy().desc("id")
                    .setFirstRow((page - 1) * BusinessConstant.PAGE_SIZE_20)
                    .setMaxRows(BusinessConstant.PAGE_SIZE_20)
                    .findPagedList();
            int pages = pagedList.getTotalPageCount();
            List<LoginLog> list = pagedList.getList();
            List<LoginLog> listToSave = new ArrayList<>();
            list.forEach((loginLog) -> {
                if (ValidationUtil.isEmpty(loginLog.getPlace())) {
                    loginLog.setPlace(ipUtil.getCityByIp(loginLog.ip));
                    listToSave.add(loginLog);
                }
            });
            if (listToSave.size() > 0) DB.saveAll(listToSave);
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            result.set("list", Json.toJson(list));
            result.put("pages", pages);
            return ok(result);
        });
    }


    /**
     * @api {GET} /v1/user/is_login/ 13是否已登录
     * @apiName isLogin
     * @apiGroup User
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess {boolean} login true已登录 false未登录
     * @apiSuccess (Error 40001){int} code 40001 参数错误
     */
    public CompletionStage<Result> isLogin(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            result.put("login", (uid < 1) ? false : true);
            return ok(result);
        });
    }

    /**
     * @api {GET} /v1/user/balance_logs/?bizType=&itemId=&page= 14余额/积分明细列表
     * @apiName listBalanceLogs
     * @apiGroup User
     * @apiParam itemId 1现金 2积分
     * @apiParam bizType 99积分奖励 100团购返现
     * @apiSuccess (Success 200) {int} code 200成功修改
     * @apiSuccess (Success 200) {JsonArray} list 列表
     * @apiSuccess (Success 200) {double} leftBalance 可用余额
     * @apiSuccess (Success 200) {double} freezeBalance 冻结余额
     * @apiSuccess (Success 200) {double} totalBalance 总额
     * @apiSuccess (Success 200) {double} changeAmount 当次变动金额
     * @apiSuccess (Success 200) {String} note 备注
     * @apiSuccess (Success 200) {long} createTime 时间
     */
    @Transactional
    @Security.Authenticated(Secured.class)
    public CompletionStage<Result> listBalanceLogs(Http.Request request, int bizType, int itemId, int page) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            ExpressionList<BalanceLog> expressionList = BalanceLog.find.query().where().eq("uid", uid)
                    .gt("bizType", 0);
            if (itemId > 0) expressionList.eq("itemId", itemId);
            if (bizType > 0) expressionList.eq("bizType", bizType);
            PagedList<BalanceLog> pagedList = expressionList
                    .orderBy().desc("id")
                    .setFirstRow((page - 1) * PAGE_SIZE_20)
                    .setMaxRows(PAGE_SIZE_20)
                    .findPagedList();
            List<BalanceLog> list = pagedList.getList();
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            result.set("list", Json.toJson(list));
            result.put("pages", pagedList.getTotalPageCount());
            return ok(result);
        });
    }

    /**
     * @api {POST} /v1/user/change_phone_number/ 15修改手机号码
     * @apiName changePhoneNumber
     * @apiGroup User
     * @apiParam {String} [note]备注，需要分两步，第一步，为输入密码【可选】，以及新手机号与新手机号短信验证码，第二步，请求原手机号的短信验证码，调用这个接口，
     * /v1/user/request_user_vcode/ 03请求给用户号码发短信，最终将所有参数一起送过来
     * @apiParam {string} [password] 密码，如果有设置的话
     * @apiParam {string} newPhoneNumber  新手机号
     * @apiParam {string} oldPhoneNumberVcode 原手机号短信验证码
     * @apiParam {string} newPhoneNumberVcode 新手机号短信验证码
     * @apiSuccess (Success 200){int}code 200
     * @apiSuccess (Error 40001) {int}code 40001无效的参数
     * @apiSuccess (Error 40002) {int}code 40002 无效的短信验证码
     * @apiSuccess (Error 40003) {int}code 40003 密码错误
     * @apiSuccess (Error 40004) {int}code 40004无效的手机号码
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured.class)
    public CompletionStage<Result> changePhoneNumber(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            JsonNode node = request.body().asJson();
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (uid < 1) return unauth403();
            String password = node.findPath("password").asText();
            String newPhoneNumber = node.findPath("newPhoneNumber").asText();
            String oldPhoneNumberVcode = node.findPath("oldPhoneNumberVcode").asText();
            String newPhoneNumberVcode = node.findPath("newPhoneNumberVcode").asText();
            Member exist = Member.find.query().where().eq("phoneNumber", newPhoneNumber).setMaxRows(1).findOne();
            if (null != exist) return okCustomJson(CODE40001, "新手机号码已被使用");
            if (ValidationUtil.isEmpty(oldPhoneNumberVcode)) return okCustomJson(CODE40001, "请输入原手机号短信验证码");
            if (ValidationUtil.isEmpty(newPhoneNumberVcode)) return okCustomJson(CODE40001, "请输入新手机号短信验证码");
            if (!ValidationUtil.isVcodeCorrect(oldPhoneNumberVcode))
                return okCustomJson(CODE40001, "原手机号短信验证码有误");
            if (!ValidationUtil.isVcodeCorrect(newPhoneNumberVcode))
                return okCustomJson(CODE40001, "新手机号短信验证码有误");
            if (!ValidationUtil.isPhoneNumber(newPhoneNumber)) return okCustomJson(CODE40001, "新手机号码有误");
            if (!ValidationUtil.isEmpty(member.loginPassword)) {
                if (ValidationUtil.isEmpty(password)) return okCustomJson(CODE40001, "请输入密码");
                String targetPassword = encodeUtils.getMd5WithSalt(password);
                if (!targetPassword.equals(member.loginPassword)) return okCustomJson(CODE40001, "密码错误");
            }
            if (!businessUtils.checkVcode(member.contactNumber, oldPhoneNumberVcode))
                return okCustomJson(CODE40002, "原手机号短信验证码有误");
            if (!businessUtils.checkVcode(newPhoneNumber, newPhoneNumberVcode))
                return okCustomJson(CODE40002, "新手机号短信验证码有误");
            //TODO 需要短信提醒
            member.save();
            return okJSON200();
        });
    }

    /**
     * @api {GET} /v1/user/msg_list/?page=&msgType= 16我的消息列表
     * @apiName listMsg
     * @apiGroup User
     * @apiSuccess (Success 200) {int} code 200
     * @apiSuccess (Success 200) {JsonArray} list 列表
     * @apiSuccess (Success 200) {int} status  1未读 2已读
     * @apiSuccess (Success 200) {String} title 标题
     * @apiSuccess (Success 200) {String} content 内容
     * @apiSuccess (Success 200) {String} linkUrl  链接地址
     * @apiSuccess (Success 200) {int} msgType 消息类型 1余额变动 2交易提醒 3系统通知
     * @apiSuccess (Success 200) {int} itemId 余额类型，跟余额中的一样
     * @apiSuccess (Success 200) {int} changeAmount 余额变动数量
     * @apiSuccess (Success 200) {long} createTime 时间
     */
    @Security.Authenticated(Secured.class)
    public CompletionStage<Result> listMsg(Http.Request request, int page, int size, int msgType) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            ExpressionList<Msg> expressionList = Msg.find.query().where()
                    .eq("uid", uid);
            if (msgType > 0) expressionList.eq("msgType", msgType);
            PagedList<Msg> pagedList = expressionList
                    .orderBy().desc("id")
                    .setFirstRow((page - 1) * size)
                    .setMaxRows(size)
                    .findPagedList();
            List<Msg> list = pagedList.getList();
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            result.put("pages", pagedList.getTotalPageCount());
            result.put("hasNext", pagedList.hasNext());
            result.set("list", Json.toJson(list));
            if (list.size() > 0) {
                CompletableFuture.runAsync(() -> {
                    list.parallelStream().forEach((each) -> each.setStatus(Msg.STATUS_READ));
                    DB.saveAll(list);
                });
            }
            return ok(result);
        });
    }

    /**
     * @api {POST} /v1/user/clear_all_msg/ 17清除全部消息列表
     * @apiName clearMsg
     * @apiGroup User
     * @apiSuccess (Success 200) {int} code 200
     */
    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> clearMsg(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            List<Msg> list = Msg.find.query().where()
                    .eq("uid", uid)
                    .findList();
            DB.deleteAll(list);
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            return ok(result);
        });
    }

    /**
     * @api {POST} /v1/user/clear_selected_msg/ 18清除选中消息列表
     * @apiName clearSelectedMsg
     * @apiGroup User
     * @apiParam {Array} list
     * @apiSuccess (Success 200) {int} code 200
     */
    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> clearSelectedMsg(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            JsonNode jsonNode = request.body().asJson();
            Messages messages = this.messagesApi.preferred(request);
            String baseArgumentError = messages.at("base.argument.error");
            if (null == jsonNode) return okCustomJson(CODE40001, baseArgumentError);
            if (uid < 1) return unauth403();
            if (!jsonNode.has("list")) return okCustomJson(CODE40001, "list参数错误");
            ArrayNode nodes = (ArrayNode) jsonNode.findPath("list");
            if (null == nodes || nodes.size() < 1) return okCustomJson(CODE40001, "list参数错误");
            List<Msg> updateList = new ArrayList<>();
            nodes.forEach((each) -> {
                long id = each.asLong();
                Msg msg = Msg.find.query().where()
                        .eq("id", id)
                        .eq("uid", uid)
                        .setMaxRows(1)
                        .findOne();
                if (null != msg) {
                    updateList.add(msg);
                }
            });
            if (updateList.size() > 0) DB.deleteAll(updateList);
            return okJSON200();
        });
    }


    /**
     * @api {post} /v1/user/apply_auth/ 19提交认证审核
     * @apiName applyAuth
     * @apiGroup User
     * @apiParam {string} [licenseUrl] 营业执照 图片地址
     * @apiParam {string} [licenseNo] 营业执照，认证后不可修改
     * @apiParam {string} [shopName] 店铺名
     * @apiParam {string} [phoneNumber] 联系电话
     * @apiParam {string} [realName] 联系人名字
     * @apiParam {string} [address] 联系地址
     * @apiParam {string} [businessItems] 经营类目
     * @apiSuccess (Success 200) {int} code 200成功修改
     * @apiSuccess (Error 40001)  {int} code 40001 参数错误
     * @apiSuccess (Error 40002)  {int} code 40002 密码无效，密码为6到20位
     * @apiSuccess (Error 403)  {int} code 403 没有权限，请重新登录
     * @apiSuccess (Error 40004)  {int} code 40004 用户不存在
     */
    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> applyAuth(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            JsonNode jsonNode = request.body().asJson();
            if (jsonNode == null) return okCustomJson(40001, "参数错误");
            Member member = Member.find.byId(uid);
            if (null == member) return okCustomJson(CODE40001, "该用户不存在");
            if (member.authStatus == Member.AUTH_STATUS_AUTHED) return okCustomJson(CODE40001, "您已认证");

            long currentTime = dateUtils.getCurrentTimeBySecond();
            String shopName = jsonNode.findPath("shopName").asText();
            if (ValidationUtil.isEmpty(shopName)) return okCustomJson(CODE40001, "请输入店铺名字");

            String contactPhoneNumber = jsonNode.findPath("contactNumber").asText();
            if (ValidationUtil.isEmpty(contactPhoneNumber)) return okCustomJson(CODE40001, "请输入联系电话");

            String contactAddress = jsonNode.findPath("contactAddress").asText();
            if (ValidationUtil.isEmpty(contactAddress)) return okCustomJson(CODE40001, "请输入联系地址");

            String contactName = jsonNode.findPath("contactName").asText();
            if (ValidationUtil.isEmpty(contactName)) return okCustomJson(CODE40001, "请输入联系人名字");

            String digest = jsonNode.findPath("digest").asText();
            if (ValidationUtil.isEmpty(digest)) return okCustomJson(CODE40001, "请输入摘要");

            String businessItems = jsonNode.findPath("businessItems").asText();
            if (ValidationUtil.isEmpty(businessItems)) return okCustomJson(CODE40001, "请输入经营项目");

            String rectLogo = jsonNode.findPath("rectLogo").asText();

            ShopApplyLog storeApplyLog = ShopApplyLog.find.query().where()
                    .eq("uid", uid)
                    .orderBy().desc("id")
                    .setMaxRows(1)
                    .findOne();
            if (null != storeApplyLog && storeApplyLog.status > ShopApplyLog.STATUS_TO_AUDIT) {
                return okCustomJson(CODE40001, "请等待审核");
            }

            if (ValidationUtil.isEmpty(contactAddress)) return okCustomJson(CODE40001, "请输入联系地址");
            if (ValidationUtil.isEmpty(contactPhoneNumber)) return okCustomJson(CODE40001, "请输入联系");
            if (ValidationUtil.isEmpty(contactName)) return okCustomJson(CODE40001, "名字有误");
            if (ValidationUtil.isEmpty(shopName)) return okCustomJson(CODE40001, "请输入店铺名字");


            ShopApplyLog log = new ShopApplyLog();
            log.setUid(uid);
            log.setLogo(rectLogo);
            log.setPhoneNumber(contactPhoneNumber);
            log.setAddress(contactAddress);
            log.setShopName(shopName);
            log.setUserName(contactName);
            log.setAuditNote("");
            log.setAuditorUid(0);
            log.setAuditorName("");
            log.setCreateTime(currentTime);
            log.setAuditTime(0);
            log.setStatus(ShopApplyLog.STATUS_TO_AUDIT);
            log.setDigest(digest);
            log.setBusinessItems(businessItems);
            log.save();

            member.setAuthStatus(Member.AUTH_STATUS_PROCESSING);
            member.save();
            return okJSON200();
        });
    }


    /**
     * @api {GET} /v1/user/my_apply_log/ 20我的申请单
     * @apiName myApplyLog
     * @apiGroup User
     * @apiSuccess (Success 200){int} code 200
     */
    @Transactional
    public CompletionStage<Result> myApplyLog(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);

            ShopApplyLog storeApplyLog = ShopApplyLog.find.query().where()
                    .eq("uid", uid)
                    .orderBy().desc("id")
                    .setMaxRows(1)
                    .findOne();
            if (null != storeApplyLog) {
                result.set("storeApplyLog", Json.toJson(storeApplyLog));
            }
            return ok(result);
        });
    }

}
