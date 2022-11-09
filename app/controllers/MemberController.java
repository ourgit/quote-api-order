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
import models.user.Image;
import models.user.Member;
import models.user.MemberBalance;
import models.user.Membership;
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
import static models.user.Member.ACCOUNT_TYPE_EMAIL;

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
    public static final String SMS_TEMPLATE = "【Renoseeker】verification code: **code**, valid within 10 min.";

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
            //参数错误
            Messages paramMessage = this.messagesApi.preferred(request);
            String paramErr = paramMessage.at("base.argument.error");
            if (null == json) return okCustomJson(40003, paramErr);

            String accountName = json.findPath("accountName").asText();
            String nickName = json.findPath("nickName").asText();
            String vcode = json.findPath("vcode").asText();
            int accountType = json.findPath("accountType").asInt();
            //无效的邮箱
            Messages emailMessage = this.messagesApi.preferred(request);
            String emailErr = emailMessage.at("base.mail.error");
            if (accountType == ACCOUNT_TYPE_EMAIL) {
                if (!ValidationUtil.isValidEmailAddress(accountName))
                    return okCustomJson(40006, emailErr);
            }

            String loginPassword = json.findPath("loginPassword").asText();
            //密码无效
            Messages passwordMassage = this.messagesApi.preferred(request);
            String passwordErr = passwordMassage.at("base.password.error");
            if (!ValidationUtil.isValidPassword(loginPassword)) return okCustomJson(40004, passwordErr);
            //请输入验证码
            Messages identifyingCodeMassage = this.messagesApi.preferred(request);
            String identifyingCodeErr = identifyingCodeMassage.at("user.info.please.input.identifyingCode");
            if (ValidationUtil.isEmpty(vcode)) return okCustomJson(40004, identifyingCodeErr);

            String key = cacheUtils.getLastVerifyCodeKey(accountName, BIZ_TYPE_SIGNUP);
            Optional<String> optional = redis.sync().get(key);
            //请先请求验证码
            Messages VCodeMassage = this.messagesApi.preferred(request);
            String VCodeErr = VCodeMassage.at("base.please.vcode");
            if (optional.isEmpty()) return okCustomJson(40004,VCodeErr);

            String vcodeSend = optional.get();
            //短信验证码有误
            Messages codeMassage = this.messagesApi.preferred(request);
            String codeErr = codeMassage.at("base.vcode.error");
            if (!vcodeSend.equalsIgnoreCase(vcode)) return okCustomJson(40004, codeErr);

            Member foundMember = Member.find.query().where()
                    .eq("accountName", accountName.trim())
                    .setMaxRows(1).findOne();
            //账号已经被注册
            Messages accountMassage = this.messagesApi.preferred(request);
            String accountErr = accountMassage.at("reg.error.account.used");
            if (null != foundMember) return okCustomJson(40001, accountErr);

            //注册发生异常，请联系客服
            Member resultMember = saveMemberForNormalSignup(accountName, loginPassword, nickName, "");
            Messages occurMassage = this.messagesApi.preferred(request);
            String occurErr = occurMassage.at("reg.error.occur.exceptions");
            if (null == resultMember) return okCustomJson(CODE500, occurErr);

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
            //请输入帐号
            Messages accountNumberMassage = this.messagesApi.preferred(request);
            String accountNumber = accountNumberMassage.at("user.info.please.input.accountNumber");
            if (ValidationUtil.isEmpty(accountName)) return okCustomJson(CODE40001, accountNumber);

            int accountType = jsonNode.findPath("accountType").asInt();
            if (accountType == ACCOUNT_TYPE_EMAIL) {
                mailerService.sendVcode(accountName, BIZ_TYPE_SIGNUP);
            } else if (accountType == Member.ACCOUNT_TYPE_PHONE_NUMBER) {
                final String vcode = businessUtils.generateVerificationCode();
                String content = SMS_TEMPLATE.replace("**code**", vcode);
                businessUtils.sendSMS(accountName.replaceAll("-", "").trim(), vcode, content, BIZ_TYPE_SIGNUP);
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
            Messages paramMessage = this.messagesApi.preferred(request);
            String paramErr = paramMessage.at("base.argument.error");
            if (ValidationUtil.isEmpty(accountName) || accountName.length() > 50)
                return okCustomJson(CODE40001, paramErr);

            String password = jsonNode.findPath("password").asText();
            if (ValidationUtil.isEmpty(password)) password = "";
            if (ValidationUtil.isEmpty(password)) return okCustomJson(CODE40001, paramErr);
            Member member = businessUtils.findByAccountNameAndPassword(accountName, password);
            //用户名或密码错误
            Messages passwordMessage = this.messagesApi.preferred(request);
            String passwordErr = passwordMessage.at("signin.error.username.or.password");
            if (null == member) return okCustomJson(CODE40003, passwordErr);

            if (null == member) {
                member = saveMemberForNormalSignup(accountName, password, "", "");
            }
           //用户被锁定
            Messages userMessage = this.messagesApi.preferred(request);
            String userErr = userMessage.at("signin.error.user.locked");
            if (member.status == Member.MEMBER_STATUS_LOCK) {
                return okCustomJson(CODE40005, userErr);
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

            //密码不合法
            Messages codeMessage = this.messagesApi.preferred(request);
            String codeErr = codeMessage.at("user.error.password.not.correct");
            if (!checkPassword(oldPassword) || !checkPassword(newPassword))
                return okCustomJson(CODE40001, codeErr);
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
        //密码错误
        Messages passwordMessage = this.messagesApi.preferred(request);
        String passwordErr = passwordMessage.at("user.error.password.error");

        //用户不存在
        Messages userMessage = this.messagesApi.preferred(request);
        String userNot = userMessage.at("user.error.user.not.exist");


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
            } else return okCustomJson(CODE40002, passwordErr);
        } else return okCustomJson(CODE40004, userNot);
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
            //用户不存在
            Messages userMessage = this.messagesApi.preferred(request);
            String userNot = userMessage.at("user.error.user.not.exist");
            if (null == member) return okCustomJson(CODE403, userNot);

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
    public CompletionStage<Result> isPhoneNumberUsed(Http.Request request,String phoneNumber) {
        return CompletableFuture.supplyAsync(() -> {
            //无效的手机号码 base.phoneNumber.error
            Messages phoneNumberMessage = this.messagesApi.preferred(request);
            String phoneNumberNot = phoneNumberMessage.at("base.phoneNumber.error");

            if (!ValidationUtil.isPhoneNumber(phoneNumber)) {
                return okCustomJson(40001, phoneNumberNot);
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

            //base.argument.error=参数错误
            Messages argumentMessage = messagesApi.preferred(request);
            String argumentErr = argumentMessage.at("base.argument.error");
            if (jsonNode == null) return okCustomJson(40001, argumentErr);

            String realName = jsonNode.findPath("realName").asText();
            String nickName = jsonNode.findPath("nickName").asText();
            String avatar = jsonNode.findPath("avatar").asText();
            Member member = Member.find.byId(uid);

            //user.error.user.not.exist=该用户不存在
            Messages userMessage = messagesApi.preferred(request);
            String userNotErr = userMessage.at("user.error.user.not.exist");
            if (null == member) return okCustomJson(CODE40001, userNotErr);

            //user.error.name.not.exceed.six="姓名不能超过六位"
            Messages nameMessage = messagesApi.preferred(request);
            String nameNotExceed = nameMessage.at("user.error.name.not.exceed.six");
            if (!ValidationUtil.isEmpty(realName)) {
                if (realName.length() > 6) return okCustomJson(40001, nameNotExceed);
                member.setRealName(realName);
            }
            //user.error.please.fillIn.nickname.to.long"昵称太长，请重新填写"
            Messages nickNameLMessage = messagesApi.preferred(request);
            String nickNameL = nickNameLMessage.at("user.error.please.fillIn.nickname.to.long");

            if (!ValidationUtil.isEmpty(nickName)) {
                if (nickName.length() > 30) return okCustomJson(40001, nickNameL);
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

            //user.error.upto.max.api.used=超过当日最大调用次数
            Messages uptApiMessage = messagesApi.preferred(request);
            String uptoAPI = uptApiMessage.at("user.error.upto.max.api.used");
            if (isUptoLimit) return okCustomJson(CODE40002, uptoAPI);

            //user.error.phonenumber.not.reg=该手机号未注册
            Messages phoneNumberMessage = messagesApi.preferred(request);
            String phoneNumberReg= phoneNumberMessage.at("user.error.phonenumber.not.reg");

            if (!ValidationUtil.isPhoneNumber(accountName)) return okCustomJson(CODE40001, phoneNumberReg);
            List<Member> members = Member.find.query().where().eq("phoneNumber", accountName).findList();
            if (members.size() < 1) return okCustomJson(CODE40001, phoneNumberReg);
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

            //signin.error.phonenumber.or.vcode=无效手机号码/短信验证码
            Messages NoPhoneNumberMessage = messagesApi.preferred(request);
            String NoPhoneNumber = NoPhoneNumberMessage.at("signin.error.phonenumber.or.vcode");
            if (!businessUtils.checkVcode(accountName, vcode, BIZ_TYPE_BIND_VERIFY))
                return okCustomJson(CODE40002, NoPhoneNumber);

            String newPassword = node.findPath("newPassword").asText();
            //base.password.error=无效的密码
            Messages NoPasswordMessage = messagesApi.preferred(request);
            String NoPassword = NoPasswordMessage.at("base.password.error");
            if (!checkPassword(newPassword)) return okCustomJson(CODE40003, NoPassword);

            Member member = Member.find.query().where().eq("phoneNumber", accountName).setMaxRows(1).findOne();

            //user.error.account.not.exist=该帐号不存在
            Messages accouentMessages = messagesApi.preferred(request);
            String accouentNotExist = accouentMessages.at("user.error.account.not.exist");
            if (null == member) return okCustomJson(CODE40005, accouentNotExist);
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
            //没有权限使用功能base.not.auth.to.use=没有权限使用该功能
            Messages NotuseMessage = messagesApi.preferred(request);
            String NotUse = NotuseMessage.at("base.not.auth.to.use");
            if (uid != memberId) return okCustomJson(CODE403, NotUse);

            //base.password.error=无效的密码
            Messages passWordErrMessage = messagesApi.preferred(request);
            String NotPassWord = passWordErrMessage.at("base.password.error");
            if (!checkPassword(newPassword)) return okCustomJson(CODE40003, NotPassWord);
            Member member = Member.find.byId(uid);
            //user.error.account.not.exist=该帐号不存在
            Messages accountMessage = messagesApi.preferred(request);
            String accountNotExist = accountMessage.at("user.error.account.not.exist");
            if (null == member) return okCustomJson(CODE40004, accountNotExist);
            //todo need to change mail code
            //user.error.old.password="原密码有误"
            Messages oldPasswordMessage = messagesApi.preferred(request);
            String oldPasswordErr = oldPasswordMessage.at("user.error.old.password");

            if (!ValidationUtil.isEmpty(member.loginPassword)) {
                if (!member.loginPassword.equals(encodeUtils.getMd5WithSalt(oldPassword))) {
                    return okCustomJson(CODE40001, oldPasswordErr);
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
            //base.not.auth.to.use=没有权限使用该功能
            Messages notAuthUseMessage = messagesApi.preferred(request);
            String notAuthUser = notAuthUseMessage.at("base.not.auth.to.use");
            if (uid < 1) return okCustomJson(403, notAuthUser);

            //user.error.not.this.leftbalance.available=没有该类型的余额
            Messages balanceMessage = messagesApi.preferred(request);
            String Notbalance = balanceMessage.at("user.error.not.this.leftbalance.available");
            if (balanceType > 0) {
                MemberBalance balance = MemberBalance.find.query().where().eq("uid", uid)
                        .eq("itemId", balanceType).setMaxRows(1).findOne();
                if (null == balance) return okCustomJson(CODE40002, Notbalance);
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

            //user.error.new.phonenumber.used="新手机号码已被使用"
            Messages PhoneNumberMessage = messagesApi.preferred(request);
            String NewPhoneNumberUse = PhoneNumberMessage.at("user.error.new.phonenumber.used");
            if (null != exist) return okCustomJson(CODE40001, NewPhoneNumberUse);

            //phonenumber.info.please.input.old.vcode=请输入原手机号短信验证码
            Messages PhoneNumberCodeMessage = messagesApi.preferred(request);
            String PhoneNumberCode = PhoneNumberCodeMessage.at("phonenumber.info.please.input.old.vcode");
            if (ValidationUtil.isEmpty(oldPhoneNumberVcode)) return okCustomJson(CODE40001, PhoneNumberCode);

            //phonenumber.info.please.input.new.vcode=请输入新手机号短信验证码
            Messages NewNumberCodeMessage = messagesApi.preferred(request);
            String NewNumberCode = NewNumberCodeMessage.at("phonenumber.info.please.input.new.vcode");
            if (ValidationUtil.isEmpty(newPhoneNumberVcode)) return okCustomJson(CODE40001, NewNumberCode);

            //phonenumber.error.old.vcode=原手机号短信验证码有误
            Messages phonenumberCodeMessage = messagesApi.preferred(request);
            String oldNumberCodeErr = phonenumberCodeMessage.at("phonenumber.error.old.vcode");
            if (!ValidationUtil.isVcodeCorrect(oldPhoneNumberVcode))
                return okCustomJson(CODE40001, oldNumberCodeErr);

            //phonenumber.error.new.vcode=新手机号短信验证码有误
            Messages NewphonenumberCodeMessage = messagesApi.preferred(request);
            String NewNumberCodeErr = NewphonenumberCodeMessage.at("phonenumber.error.new.vcode");
            if (!ValidationUtil.isVcodeCorrect(newPhoneNumberVcode))
                return okCustomJson(CODE40001, NewNumberCodeErr);

           // phonenumber.error.new.phonenumber=新手机号码有误
            Messages NewphonenumberMessage = messagesApi.preferred(request);
            String NewNumberErr = NewphonenumberMessage.at(" phonenumber.error.new.phonenumber");
            if (!ValidationUtil.isPhoneNumber(newPhoneNumber)) return okCustomJson(CODE40001, NewNumberErr);

           //phonenumber.error.please.input.password=请输入密码
            Messages  InputPasswordMessage = messagesApi.preferred(request);
            String InputPasswordErr = InputPasswordMessage.at(" phonenumber.error.please.input.password");
            if (!ValidationUtil.isEmpty(member.loginPassword)) {
                if (ValidationUtil.isEmpty(password)) return okCustomJson(CODE40001, InputPasswordErr);
                String targetPassword = encodeUtils.getMd5WithSalt(password);

                //user.error.password.error=密码错误
                Messages  PasswordMessage = messagesApi.preferred(request);
                String PasswordErr = PasswordMessage.at(" user.error.password.error");
                if (!targetPassword.equals(member.loginPassword)) return okCustomJson(CODE40001, PasswordErr);
            }
            //phonenumber.error.old.vcode=原手机号短信验证码有误
            Messages oldphonenumberMessage = messagesApi.preferred(request);
            String oldNumberErr = oldphonenumberMessage.at("phonenumber.error.old.vcode");
            if (!businessUtils.checkVcode(member.contactNumber, oldPhoneNumberVcode, BIZ_TYPE_BIND_VERIFY))
                return okCustomJson(CODE40002, oldNumberErr);
            //phonenumber.error.new.vcode=新手机号短信验证码有误
            Messages NewphoneMessage = messagesApi.preferred(request);
            String NewPhoneCodeErr = NewphoneMessage.at("phonenumber.error.new.vcode");
            if (!businessUtils.checkVcode(newPhoneNumber, newPhoneNumberVcode, BIZ_TYPE_BIND_VERIFY))
                return okCustomJson(CODE40002, NewPhoneCodeErr);
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
            //base.argument.error=参数错误
            Messages messages = this.messagesApi.preferred(request);
            String baseArgumentError = messages.at("base.argument.error");
            if (null == jsonNode) return okCustomJson(CODE40001, baseArgumentError);
            if (uid < 1) return unauth403();
            //base.list.argument.error="list参数错误"
            Messages listMessages = this.messagesApi.preferred(request);
            String listArgumentError = listMessages.at("base.list.argument.error");
            if (!jsonNode.has("list")) return okCustomJson(CODE40001, listArgumentError);
            ArrayNode nodes = (ArrayNode) jsonNode.findPath("list");
            if (null == nodes || nodes.size() < 1) return okCustomJson(CODE40001, listArgumentError);
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
            //base.argument.error=参数错误
            Messages messages = this.messagesApi.preferred(request);
            String baseArgumentError = messages.at("base.argument.error");
            if (jsonNode == null) return okCustomJson(40001, baseArgumentError);
            Member member = Member.find.byId(uid);
            //user.error.user.not.exist=该用户不存在
            Messages userMessage = messagesApi.preferred(request);
            String userNotErr = userMessage.at("user.error.user.not.exist");
            if (null == member) return okCustomJson(CODE40001, userNotErr);
            //user.info.you.authentication="您已认证"
            Messages authenticationMessage = messagesApi.preferred(request);
            String authentication = authenticationMessage.at("user.info.you.authentication");
            if (member.authStatus == Member.AUTH_STATUS_AUTHED) return okCustomJson(CODE40001, authentication);
            long currentTime = dateUtils.getCurrentTimeBySecond();
            String shopName = jsonNode.findPath("shopName").asText();
            //user.info.please.input.shopName="请输入店铺名字"
            Messages shopMessage = messagesApi.preferred(request);
            String shoppingName = shopMessage.at("user.info.please.input.shopName");
            if (ValidationUtil.isEmpty(shopName)) return okCustomJson(CODE40001, shoppingName);

            String contactPhoneNumber = jsonNode.findPath("contactNumber").asText();
            //user.info.please.input.phonenumber=请输入电话号码
            Messages PhoneNumMessage = messagesApi.preferred(request);
            String PhoneNumber = PhoneNumMessage.at("user.info.please.input.phonenumber");
            if (ValidationUtil.isEmpty(contactPhoneNumber)) return okCustomJson(CODE40001, PhoneNumber);

            String contactAddress = jsonNode.findPath("contactAddress").asText();
            //user.info.please.input.contactAddress="请输入联系地址"
            Messages address = messagesApi.preferred(request);
            String ContactAddress = address.at("user.info.please.input.contactAddress");
            if (ValidationUtil.isEmpty(contactAddress)) return okCustomJson(CODE40001, ContactAddress);

            String contactName = jsonNode.findPath("contactName").asText();
            //user.info.please.input.contact.name=请输入联系人称呼
            Messages message= messagesApi.preferred(request);
            String ContactName = message.at("user.info.please.input.contact.name");
            if (ValidationUtil.isEmpty(contactName)) return okCustomJson(CODE40001, ContactName);

            String digest = jsonNode.findPath("digest").asText();
            //user.info.please.input.abstracts="请输入摘要"
            Messages abstractMessage= messagesApi.preferred(request);
            String abstracts = abstractMessage.at("user.info.please.input.abstracts");
            if (ValidationUtil.isEmpty(digest)) return okCustomJson(CODE40001, abstracts);

            String businessItems = jsonNode.findPath("businessItems").asText();
            //user.info.please.input.operatingItems="请输入经营项目"
            Messages ItemsMessage= messagesApi.preferred(request);
            String operatingItems = ItemsMessage.at("user.info.please.input.operatingItems");
            if (ValidationUtil.isEmpty(businessItems)) return okCustomJson(CODE40001, operatingItems);

            String rectLogo = jsonNode.findPath("rectLogo").asText();
            String images = jsonNode.findPath("images").asText();

            ShopApplyLog storeApplyLog = ShopApplyLog.find.query().where()
                    .eq("uid", uid)
                    .orderBy().desc("id")
                    .setMaxRows(1)
                    .findOne();
            //请等待审核user.error.please.waitAudit="请先等待"
            Messages Message= messagesApi.preferred(request);
            String waitAudit = Message.at("user.error.please.waitAudit");
            if (null != storeApplyLog && storeApplyLog.status > ShopApplyLog.STATUS_TO_AUDIT) {
                return okCustomJson(CODE40001, waitAudit);
            }
            if (ValidationUtil.isEmpty(contactAddress)) return okCustomJson(CODE40001, ContactAddress);
            if (ValidationUtil.isEmpty(contactPhoneNumber)) return okCustomJson(CODE40001, PhoneNumber);
           // base.name.error=名字有误
            Messages NameMessage= messagesApi.preferred(request);
            String nameErr = NameMessage.at("base.name.error");
            if (ValidationUtil.isEmpty(contactName)) return okCustomJson(CODE40001, nameErr);
            if (ValidationUtil.isEmpty(shopName)) return okCustomJson(CODE40001, shoppingName);


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
            log.setImages(images);
            if (jsonNode.hasNonNull("categoryList")) {
                log.setCategoryList(Json.stringify(jsonNode.findPath("categoryList")));
            }
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

    /**
     * @api {GET} /v1/user/membership_list/ 21会员价格列表
     * @apiName listMembership
     * @apiGroup User
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Success 200) {int} pages 页数
     * @apiSuccess (Success 200) {JsonArray} list 列表
     * @apiSuccess (Success 200){long} id 分类id
     * @apiSuccess (Success 200){long} duration 时长，以天为单位
     * @apiSuccess (Success 200){long} oldPrice 原价
     * @apiSuccess (Success 200){long} price 现价
     * @apiSuccess (Success 200){int} sort 排序顺序
     * @apiSuccess (Success 200){string} updateTime 更新时间
     */
    public CompletionStage<Result> listMembership() {
        return CompletableFuture.supplyAsync(() -> {
            ExpressionList<Membership> expressionList = Membership.find.query().where();
            List<Membership> list = expressionList.orderBy().desc("sort").findList();
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            result.set("list", Json.toJson(list));
            return ok(result);
        });
    }


    /**
     * @api {POST} /v1/user/change_email/ 22换绑邮箱
     * @apiName changeEmail
     * @apiGroup User
     * @apiParam {string} [password] 密码
     * @apiParam {string} newMail  新手机号
     * @apiParam {string} oldMailVcode 原邮箱验证码
     * @apiParam {string} newMailVcode 新邮箱验证码
     * @apiSuccess (Success 200){int}code 200
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured.class)
    public CompletionStage<Result> changeEmail(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            JsonNode node = request.body().asJson();
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (uid < 1) return unauth403();
            //signin.error.user.locked=用户被锁定
            Messages userMessage= messagesApi.preferred(request);
            String userLock = userMessage.at("signin.error.user.locked");
            if (member.status != Member.MEMBER_STATUS_NORMAL)
                return okCustomJson(CODE40001, userLock);
            String password = node.findPath("password").asText();
            String newMail = node.findPath("newMail").asText();
            String oldMailVcode = node.findPath("oldMailVcode").asText();
            String newMailVcode = node.findPath("newMailVcode").asText();
            Member exist = Member.find.query().where()
                    .eq("accountName", newMail)
                    .setMaxRows(1)
                    .findOne();
            //user.error.mailbox.used="邮箱已被使用"
            Messages mailMessage= messagesApi.preferred(request);
            String mailUsed = mailMessage.at("user.error.mailbox.used");
            if (null != exist) return okCustomJson(CODE40001, mailUsed);

            //mailbox.info.please.input.old.vcode="请输入原邮箱验证码"
            Messages OldmailboxMessage= messagesApi.preferred(request);
            String oldmailbox = OldmailboxMessage.at("mailbox.info.please.input.old.vcode");
            if (ValidationUtil.isEmpty(oldMailVcode)) return okCustomJson(CODE40001, oldmailbox);
            //mailbox.info.please.input.new.vcode="请输入新邮箱验证码"
            Messages NewmailboxMessage= messagesApi.preferred(request);
            String newmailbox = NewmailboxMessage.at("mailbox.info.please.input.new.vcode");
            if (ValidationUtil.isEmpty(newMailVcode)) return okCustomJson(CODE40001, newmailbox);

            //mailbox.error.old.vcode="原邮箱验证码有误"
            Messages oldmailCodeMessage= messagesApi.preferred(request);
            String oldMailCode = oldmailCodeMessage.at("mailbox.error.old.vcode");
            if (!ValidationUtil.isVcodeCorrect(oldMailVcode))
                return okCustomJson(CODE40001, oldMailCode);
            // mailbox.error.new.vcode="新邮箱验证码有误"
            Messages newMailCodeMessage= messagesApi.preferred(request);
            String newMailCode = newMailCodeMessage.at("mailbox.error.new.vcode");
            if (!ValidationUtil.isVcodeCorrect(newMailVcode))
                return okCustomJson(CODE40001, newMailCode);
            //mailbox.error.new.mailBox="新邮箱有误"
            Messages newMailMessage = messagesApi.preferred(request);
            String newMailBox = newMailMessage.at("mailbox.error.new.mailBox");
            if (!ValidationUtil.isValidEmailAddress(newMail)) return okCustomJson(CODE40001, newMailBox);
            if (!ValidationUtil.isEmpty(member.loginPassword)) {
                //phonenumber.error.please.input.password=请输入密码
                Messages  InputPasswordMessage = messagesApi.preferred(request);
                String InputPasswordErr = InputPasswordMessage.at(" phonenumber.error.please.input.password");
                if (ValidationUtil.isEmpty(password)) return okCustomJson(CODE40001, InputPasswordErr);
                String targetPassword = encodeUtils.getMd5WithSalt(password);
                //密码错误
                Messages  PasswordMessage = messagesApi.preferred(request);
                String PasswordErr = PasswordMessage.at(" user.error.password.error");
                if (!targetPassword.equals(member.loginPassword)) return okCustomJson(CODE40001, PasswordErr);
            }
            if (!businessUtils.checkVcode(member.accountName, oldMailVcode, BIZ_TYPE_BIND_VERIFY))
                return okCustomJson(CODE40002, oldMailCode);
            if (!businessUtils.checkVcode(newMail, newMailVcode, BIZ_TYPE_BIND_VERIFY))
                return okCustomJson(CODE40002, newMailCode);
            member.setAccountName(newMail);
            //TODO 需要短信提醒
            member.save();

            return okJSON200();
        });
    }


    /**
     * @api {GET}  /v1/user/images/  75图片列表
     * @apiName listImages
     * @apiGroup Product
     * @apiSuccess (Success 200) {String} images 图像地址
     * @apiSuccess (Success 200) {int} code 200 请求成功
     */
    public CompletionStage<Result> listImages(Http.Request request) {
        return CompletableFuture.supplyAsync(() -> {
            List<Image> list = Image.find.query().where().orderBy().desc("id")
                    .findList();
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            result.set("list", Json.toJson(list));
            return ok(result);
        });
    }


    /**
     * @api {POST} /v1/user/mail_vcode/ 76请求当前邮箱验证码
     * @apiName requestMailVcode
     * @apiGroup User
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured.class)
    public CompletionStage<Result> requestMailVcode(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            JsonNode node = request.body().asJson();
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (uid < 1) return unauth403();
            //signin.error.user.locked=用户被锁定
            Messages userMessage= messagesApi.preferred(request);
            String userLock = userMessage.at("signin.error.user.locked");
            if (member.status != Member.MEMBER_STATUS_NORMAL)
                return okCustomJson(CODE40001, userLock);
            if (ValidationUtil.isValidEmailAddress(member.accountName))
                mailerService.sendVcode(member.accountName.replaceAll("-", "").trim(), BIZ_TYPE_BIND_VERIFY);
            return okJSON200();
        });
    }


    /**
     * @api {POST} /v1/user/bind_phone_number/ 77绑定手机
     * @apiName bindPhoneNumber
     * @apiGroup User
     * @apiParam {string} phoneNumber  手机号
     * @apiParam {string} vcode 验证码
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured.class)
    public CompletionStage<Result> bindPhoneNumber(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            JsonNode node = request.body().asJson();
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (uid < 1) return unauth403();
            String phoneNumber = node.findPath("phoneNumber").asText();
            String vcode = node.findPath("vcode").asText();
            //短信验证码有误
            Messages codeMassage = this.messagesApi.preferred(request);
            String codeErr = codeMassage.at("base.vcode.error");
            if (ValidationUtil.isEmpty(vcode)) return okCustomJson(40004, codeErr);
            //user.info.please.input.phonenumber=请输入电话号码
            Messages PhoneNumMessage = messagesApi.preferred(request);
            String PhoneNumber = PhoneNumMessage.at("user.info.please.input.phonenumber");
            if (ValidationUtil.isEmpty(phoneNumber)) return okCustomJson(CODE40001, PhoneNumber);
            Messages phoneNumberMessage = this.messagesApi.preferred(request);
            String phoneNumberNot = phoneNumberMessage.at("base.phoneNumber.error");
            if (phoneNumber.length() > 30) return okCustomJson(CODE40001, phoneNumberNot);

            //signin.error.user.locked=用户被锁定
            Messages userMessage= messagesApi.preferred(request);
            String userLock = userMessage.at("signin.error.user.locked");
            if (member.status != Member.MEMBER_STATUS_NORMAL)
                return okCustomJson(CODE40001, userLock);
            //reg.error.phoneNumber.bound="已绑定手机号码，不可再绑定"
            Messages PhoneNumberMessage= messagesApi.preferred(request);
            String PhoneNumErr = PhoneNumberMessage.at("reg.error.phoneNumber.bound");
            if (!ValidationUtil.isEmpty(member.contactNumber)) {
                return okCustomJson(CODE40003, PhoneNumErr);
            }
            String key = cacheUtils.getLastVerifyCodeKey(phoneNumber, BIZ_TYPE_BIND_VERIFY);
            Optional<String> optional = redis.sync().get(key);
            //请先请求验证码
            Messages VCodeMassage = this.messagesApi.preferred(request);
            String VCodeErr = VCodeMassage.at("base.please.vcode");
            if (optional.isEmpty()) return okCustomJson(40004, VCodeErr);
            String vcodeSend = optional.get();

            if (!vcodeSend.equalsIgnoreCase(vcode)) return okCustomJson(40004, codeErr);

            member.setContactNumber(phoneNumber);
            member.setUpdateTime(dateUtils.getCurrentTimeBySecond());
            member.save();
            return okJSON200();
        });
    }

    /**
     * @api {POST} /v1/user/bind_mail/ 78绑定邮箱
     * @apiName bindMail
     * @apiGroup User
     * @apiParam {string} email  邮箱
     * @apiParam {string} vcode 验证码
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Security.Authenticated(Secured.class)
    public CompletionStage<Result> bindMail(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            JsonNode node = request.body().asJson();
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (uid < 1) return unauth403();
            String email = node.findPath("email").asText();
            //无效的邮箱
            Messages emailMessage = this.messagesApi.preferred(request);
            String emailErr = emailMessage.at("base.mail.error");
            if (!ValidationUtil.isValidEmailAddress(email)) return okCustomJson(CODE40001, emailErr);
            String vcode = node.findPath("vcode").asText();
            //base.Mailvcode.error="邮箱验证码有误"
            Messages emaiCodelMessage = this.messagesApi.preferred(request);
            String emailCodeErr = emaiCodelMessage.at("base.Mailvcode.error");
            if (ValidationUtil.isEmpty(vcode)) return okCustomJson(40004, emailCodeErr);
            //signin.error.user.locked=用户被锁定
            Messages userMessage= messagesApi.preferred(request);
            String userLock = userMessage.at("signin.error.user.locked");
            if (member.status != Member.MEMBER_STATUS_NORMAL)
                return okCustomJson(CODE40001, userLock);
            //reg.error.mailAccounts.bound="已绑定邮箱账号号，不可再绑定"
            Messages mailAccountsMessage= messagesApi.preferred(request);
            String mailAccounts = mailAccountsMessage.at("reg.error.mailAccounts.bound");
            if (!ValidationUtil.isEmpty(member.accountName)) {
                return okCustomJson(CODE40003, mailAccounts);
            }

            String key = cacheUtils.getLastVerifyCodeKey(email, BIZ_TYPE_BIND_VERIFY);
            Optional<String> optional = redis.sync().get(key);
            //请先请求验证码
            Messages VCodeMassage = this.messagesApi.preferred(request);
            String VCodeErr = VCodeMassage.at("base.please.vcode");
            if (optional.isEmpty()) return okCustomJson(40004, VCodeErr);
            String vcodeSend = optional.get();
            //短信验证码有误
            Messages codeMassage = this.messagesApi.preferred(request);
            String codeErr = codeMassage.at("base.vcode.error");
            if (!vcodeSend.equalsIgnoreCase(vcode)) return okCustomJson(40004, codeErr);
            member.setAccountName(email);
            member.setUpdateTime(dateUtils.getCurrentTimeBySecond());
            member.save();
            return okJSON200();
        });
    }
}
