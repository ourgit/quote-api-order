package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import constants.BusinessConstant;
import io.ebean.DB;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import models.bid.Bid;
import models.bid.BidDetail;
import models.bid.BidUser;
import models.user.Member;
import play.Logger;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import security.Secured;
import utils.ValidationUtil;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

import static constants.BusinessConstant.OPERATION_PLACE_BID;

/**
 * 报价控制类
 */
public class BidController extends BaseController {

    Logger.ALogger logger = Logger.of(BidController.class);
    @Inject
    MessagesApi messagesApi;

    /**
     * @api {POST} /v1/user/my_bid_list/ 01我的报价列表
     * @apiName listMyBid
     * @apiGroup BID
     * @apiParam {long} page page
     * @apiParam {long} size size
     * @apiParam {int} status  status10未报价 20报价中 30执行中 40已完成
     * @apiSuccess (Success 200) {int} code 200
     * @apiSuccess (Success 200) {JsonArray} list 列表
     * @apiSuccess (Success 200) {String} serviceRegion 服务区域
     * @apiSuccess (Success 200) {String} serviceAddress 服务地址
     * @apiSuccess (Success 200) {String} categoryName 分类名字
     * @apiSuccess (Success 200) {String} preferenceServiceTime 预约时间
     * @apiSuccess (Success 200) {String} serviceContent 服务内容
     * @apiSuccess (Success 200) {String} contactMail 联系人邮箱
     * @apiSuccess (Success 200) {String} contactName 联系人名字
     * @apiSuccess (Success 200) {int} status  10未报价 20报价中 30执行中 40已完成
     */
    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> listMyBid(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            JsonNode jsonNode = request.body().asJson();
            Messages messages = this.messagesApi.preferred(request);
            String baseArgumentError = messages.at("base.argument.error");
            if (null == jsonNode) return okCustomJson(CODE40001, baseArgumentError);
            int page = jsonNode.findPath("page").asInt();
            int size = jsonNode.findPath("size").asInt();
            int status = jsonNode.findPath("status").asInt();
            if (page < 1) page = 1;
            if (size < 1) size = BusinessConstant.PAGE_SIZE_20;
            ExpressionList<Bid> expressionList = Bid.find.query().where()
                    .eq("uid", uid);
            if (status > 0) expressionList.eq("status", status);
            PagedList<Bid> pagedList = expressionList
                    .orderBy().desc("id")
                    .setFirstRow((page - 1) * size)
                    .setMaxRows(size)
                    .findPagedList();
            List<Bid> list = pagedList.getList();
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            result.put("pages", pagedList.getTotalPageCount());
            result.put("hasNext", pagedList.hasNext());
            result.set("list", Json.toJson(list));
            return ok(result);
        });
    }

    /**
     * @api {POST} /v1/user/my_to_bid_list/ 02获得报价资格的报价列表
     * @apiName myToBidList
     * @apiGroup BID
     * @apiParam {long} page page
     * @apiParam {long} size size
     * @apiParam {int} status  status10未报价 20报价中 30执行中 40已完成
     * @apiSuccess (Success 200) {int} code 200
     * @apiSuccess (Success 200) {JsonArray} list 列表
     * @apiSuccess (Success 200) {String} serviceRegion 服务区域
     * @apiSuccess (Success 200) {String} serviceAddress 服务地址
     * @apiSuccess (Success 200) {String} categoryName 分类名字
     * @apiSuccess (Success 200) {String} preferenceServiceTime 预约时间
     * @apiSuccess (Success 200) {String} serviceContent 服务内容
     * @apiSuccess (Success 200) {String} contactMail 联系人邮箱
     * @apiSuccess (Success 200) {String} contactName 联系人名字
     * @apiSuccess (Success 200) {int} status  10未报价 20报价中 30执行中 40已完成
     */
    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> myToBidList(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            JsonNode jsonNode = request.body().asJson();
            Messages messages = this.messagesApi.preferred(request);
            String baseArgumentError = messages.at("base.argument.error");
            if (null == jsonNode) return okCustomJson(CODE40001, baseArgumentError);
            int page = jsonNode.findPath("page").asInt();
            int size = jsonNode.findPath("size").asInt();
            if (page < 1) page = 1;
            if (size < 1) size = BusinessConstant.PAGE_SIZE_20;
            ExpressionList<Bid> expressionList = Bid.find.query().where()
                    .eq("uid", uid);
            PagedList<Bid> pagedList = expressionList
                    .orderBy().desc("id")
                    .setFirstRow((page - 1) * size)
                    .setMaxRows(size)
                    .findPagedList();
            List<Bid> list = pagedList.getList();
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            result.put("pages", pagedList.getTotalPageCount());
            result.put("hasNext", pagedList.hasNext());
            result.set("list", Json.toJson(list));
            return ok(result);
        });
    }

    /**
     * @api {GET} /v1/user/bid_list/:id/ 03报价单详情
     * @apiName getBid
     * @apiGroup BID
     * @apiSuccess (Success 200) {int} code 200
     * @apiSuccess (Success 200) {JsonArray} list 列表
     * @apiSuccess (Success 200) {String} serviceRegion 服务区域
     * @apiSuccess (Success 200) {String} serviceAddress 服务地址
     * @apiSuccess (Success 200) {String} categoryName 分类名字
     * @apiSuccess (Success 200) {String} preferenceServiceTime 预约时间
     * @apiSuccess (Success 200) {String} serviceContent 服务内容
     * @apiSuccess (Success 200) {String} contactMail 联系人邮箱
     * @apiSuccess (Success 200) {String} contactName 联系人名字
     */
    @Security.Authenticated(Secured.class)
    public CompletionStage<Result> getBid(Http.Request request, long id) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            Bid bid = Bid.find.byId(id);
            if (null == bid) return okCustomJson(CODE40001, "该报价不存在");
            List<BidUser> bidUserList = BidUser.find.query().where()
                    .eq("bidId", id)
                    .eq("uid", uid)
                    .findList();
            Set<Long> set = new HashSet<>();
            set.add(bid.askerUid);
            bidUserList.parallelStream().forEach((each) -> set.add(each.uid));
            if (!set.contains(uid)) return okCustomJson(CODE40001, "该报价不存在");
            ObjectNode result = (ObjectNode) Json.toJson(bid);
            result.put(CODE, CODE200);
            return ok(result);
        });
    }


    /**
     * @api {POST} /v1/user/ask_bid/ 04请求报价
     * @apiName askBid
     * @apiGroup BID
     * @apiParam {String} serviceRegion 服务区域
     * @apiParam {String} serviceAddress 服务地址
     * @apiParam {String} categoryName 分类名字
     * @apiParam {String} preferenceServiceTime 预约时间
     * @apiParam {String} serviceContent 服务内容
     * @apiParam {String} contactMail 联系人邮箱
     * @apiParam {String} contactName 联系人名字
     * @apiSuccess (Success 200) {int} code 200
     */
    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> askBid(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            JsonNode jsonNode = request.body().asJson();
            Messages messages = this.messagesApi.preferred(request);
            String baseArgumentError = messages.at("base.argument.error");
            if (null == jsonNode) return okCustomJson(CODE40001, baseArgumentError);
            String serviceRegion = jsonNode.findPath("serviceRegion").asText();
            if (ValidationUtil.isEmpty(serviceRegion)) return okCustomJson(CODE40001, "请输入服务区域");
            String serviceAddress = jsonNode.findPath("serviceAddress").asText();
            if (ValidationUtil.isEmpty(serviceAddress)) return okCustomJson(CODE40001, "请输入服务地址");
            String categoryName = jsonNode.findPath("categoryName").asText();
            String preferenceServiceTime = jsonNode.findPath("preferenceServiceTime").asText();
            if (ValidationUtil.isEmpty(preferenceServiceTime)) return okCustomJson(CODE40001, "请输入预约服务时间");
            String serviceContent = jsonNode.findPath("serviceContent").asText();
            if (ValidationUtil.isEmpty(serviceContent)) return okCustomJson(CODE40001, "请输入服务内容");
            String contactMail = jsonNode.findPath("contactMail").asText();
            if (ValidationUtil.isEmpty(contactMail)) return okCustomJson(CODE40001, "请输入联系邮箱");
            String contactName = jsonNode.findPath("contactName").asText();
            if (ValidationUtil.isEmpty(contactName)) return okCustomJson(CODE40001, "请输入联系人名字");

            Bid bid = new Bid();
            bid.setServiceRegion(serviceRegion);
            bid.setServiceAddress(serviceAddress);
            bid.setCategoryName(categoryName);
            bid.setPreferenceServiceTime(preferenceServiceTime);
            bid.setServiceContent(serviceContent);
            bid.setContactMail(contactMail);
            bid.setContactName(contactName);
            bid.setAskerUid(uid);
            bid.setAskerName(businessUtils.getMemberName(member));
            long currentTime = dateUtils.getCurrentTimeBySecond();
            bid.setUpdateTime(currentTime);
            bid.setCreateTime(currentTime);
            bid.save();
            List<Member> memberList = Member.find.query().where()
                    .eq("status", Member.MEMBER_STATUS_NORMAL)
                    .orderBy().desc("expireTime")
                    .orderBy().asc("id")
                    .setMaxRows(20)
                    .findList();
            if (memberList.size() > 0) {
                List<BidUser> list = new ArrayList<>();
                memberList.forEach((each) -> {
                    if (each.id != uid) {
                        BidUser bidUser = new BidUser();
                        bidUser.setBidId(bid.id);
                        bidUser.setUid(each.id);
                        bidUser.setUserName(businessUtils.getUserName(each));
                        bidUser.setCreateTime(currentTime);
                        list.add(bidUser);
                    }
                });
                if (list.size() > 0) {
                    DB.saveAll(list);
                }
            }
            return okJSON200();
        });
    }

    /**
     * @api {POST} /v1/user/place_bid/ 05报价/修改报价
     * @apiName placeBid
     * @apiGroup BID
     * @apiParam {long} id id
     * @apiParam {long} price 价格
     * @apiParam {string} [note] 备注
     * @apiSuccess (Success 200) {int} code 200
     */
    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> placeBid(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            JsonNode jsonNode = request.body().asJson();
            Messages messages = this.messagesApi.preferred(request);
            String baseArgumentError = messages.at("base.argument.error");
            if (null == jsonNode) return okCustomJson(CODE40001, baseArgumentError);
            long id = jsonNode.findPath("id").asLong();
            long price = jsonNode.findPath("price").asLong();
            String note = jsonNode.findPath("note").asText();
            Bid bid = Bid.find.byId(id);
            if (null == bid) return okCustomJson(CODE40001, "该报价不存在");
            if (bid.status > Bid.STATUS_BIDDING) {
                return okCustomJson(CODE40004, "当前非报价状态，不可报价");
            }
            BidUser bidUser = BidUser.find.query().where()
                    .eq("bidId", id)
                    .eq("uid", uid)
                    .setMaxRows(1)
                    .findOne();
            if (null == bidUser) return okCustomJson(CODE40004, "非报价侯选人，不可报价");
            if (!businessUtils.setLock(String.valueOf(uid), OPERATION_PLACE_BID))
                return okCustomJson(CODE40004, "正在操作中，请稍等");
            BidDetail bidDetail = BidDetail.find.query().where()
                    .eq("bidId", id)
                    .eq("bidderUid", uid)
                    .setMaxRows(1)
                    .findOne();
            long currentTime = dateUtils.getCurrentTimeBySecond();
            if (price < 1) return okCustomJson(CODE40001, "请设置报价的价格");
            if (null == bidDetail) {
                bidDetail = new BidDetail();
                bidDetail.setCreateTime(currentTime);
            }
            bidDetail.setPrice(price);
            bidDetail.setNote(note);
            bidDetail.setUpdateTime(currentTime);
            bidDetail.save();
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            businessUtils.unLock(String.valueOf(uid), OPERATION_PLACE_BID);
            return ok(result);
        });
    }

}
