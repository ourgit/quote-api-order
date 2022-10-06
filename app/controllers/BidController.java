package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import constants.BusinessConstant;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import models.bid.Bid;
import models.bid.BidDetail;
import play.Logger;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import security.Secured;

import javax.inject.Inject;
import java.util.List;
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
     * @api {POST} /v1/user/bid_list/ 01报价列表
     * @apiName listBid
     * @apiGroup BID
     * @apiSuccess (Success 200) {int} code 200
     * @apiSuccess (Success 200) {JsonArray} list 列表
     * @apiSuccess (Success 200) {int} status  1未读 2已读
     */
    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> listBid(Http.Request request) {
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
     * @api {GET} /v1/user/bid_list/:id/ 02报价单详情
     * @apiName getBid
     * @apiGroup BID
     * @apiSuccess (Success 200) {int} code 200
     * @apiSuccess (Success 200) {JsonArray} list 列表
     * @apiSuccess (Success 200) {int} status  1未读 2已读
     */
    @Security.Authenticated(Secured.class)
    public CompletionStage<Result> getBid(Http.Request request, long id) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            Bid bid = Bid.find.byId(id);
            if (null == bid) return okCustomJson(CODE40001, "该报价不存在");
            ObjectNode result = (ObjectNode) Json.toJson(bid);
            result.put(CODE, CODE200);
            return ok(result);
        });
    }


    /**
     * @api {POST} /v1/user/ask_bid/ 03请求报价
     * @apiName askBid
     * @apiGroup BID
     * @apiParam {long} price 价格
     * @apiParam {string} [note] 备注
     * @apiSuccess (Success 200) {int} code 200
     */
    @Security.Authenticated(Secured.class)
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> askBid(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            JsonNode jsonNode = request.body().asJson();
            Messages messages = this.messagesApi.preferred(request);
            String baseArgumentError = messages.at("base.argument.error");
            if (null == jsonNode) return okCustomJson(CODE40001, baseArgumentError);

            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            return ok(result);
        });
    }

    /**
     * @api {POST} /v1/user/place_bid/ 04报价
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
