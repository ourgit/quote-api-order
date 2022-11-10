package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import constants.BusinessConstant;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import models.shop.Service;
import models.shop.Shop;
import models.shop.Showcase;
import models.user.Member;
import play.db.ebean.Transactional;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import utils.ValidationUtil;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ServiceController extends BaseController {

    @Inject
    MessagesApi messagesApi;

    /**
     * @api {POST} /v1/user/service_list/ 01商家服务列表
     * @apiName listService
     * @apiGroup SERVICE
     * @apiParam {long} shopId shopId
     * @apiParam {long} categoryId categoryId
     * @apiParam {long} page page
     * @apiParam {String} serviceName serviceName
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Success 200){Array} list
     * @apiSuccess (Success 200){String} serviceName 服务名字
     * @apiSuccess (Success 200){String} serviceIcon 服务图标
     * @apiSuccess (Success 200){String} serviceDigest 服务摘要
     * @apiSuccess (Success 200){String} serviceContent 服务内容
     * @apiSuccess (Success 200){long} shopId 店铺ID
     * @apiSuccess (Success 200){long} categoryId 分类ID
     * @apiSuccess (Error 40001){int} code 40001 参数错误
     */
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> listService(Http.Request request) {
        return CompletableFuture.supplyAsync(() -> {
            //参数错误
            Messages messages = messagesApi.preferred(request);
            String paramErr = messages.at("base.argument.error");
            JsonNode jsonNode = request.body().asJson();
            if (null == jsonNode) return okCustomJson(CODE40001, paramErr);
            long shopId = jsonNode.findPath("shopId").asLong();
            long categoryId = jsonNode.findPath("categoryId").asLong();
            int page = jsonNode.findPath("page").asInt();
            String serviceName = jsonNode.findPath("serviceName").asText();
            ExpressionList<Service> expressionList = Service.find.query().where();
            if (shopId > 0) expressionList.eq("shopId", shopId);
            if (categoryId > 0) expressionList.eq("categoryId", categoryId);
            if (!ValidationUtil.isEmpty(serviceName)) expressionList.icontains("serviceName", serviceName);
            PagedList<Service> pagedList = expressionList.orderBy().desc("sort")
                    .setFirstRow((page - 1) * BusinessConstant.PAGE_SIZE_10)
                    .setMaxRows(BusinessConstant.PAGE_SIZE_10)
                    .findPagedList();
            ObjectNode result = Json.newObject();
            result.set("list", Json.toJson(pagedList.getList()));
            result.put("pages", pagedList.getTotalPageCount());
            result.put(CODE, CODE200);
            return ok(result);
        });
    }

    /**
     * @api {GET} /v1/user/service_list/:id/ 02商家服务详情
     * @apiName getService
     * @apiGroup SERVICE
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Success 200){String} serviceName 服务名字
     * @apiSuccess (Success 200){String} serviceIcon 服务图标
     * @apiSuccess (Success 200){String} serviceDigest 服务摘要
     * @apiSuccess (Success 200){String} serviceContent 服务内容
     * @apiSuccess (Success 200){long} shopId 店铺ID
     * @apiSuccess (Success 200){long} categoryId 分类ID
     * @apiSuccess (Error 40001){int} code 40001 参数错误
     */
    public CompletionStage<Result> getService(Http.Request request, long id) {
        return CompletableFuture.supplyAsync(() -> {
            //service.error.business.exist=""该商家不存在
            Messages messages = messagesApi.preferred(request);
            String busServiceNot = messages.at("service.error.businessService.exist");
            Service service = Service.find.byId(id);
            if (null == service) return okCustomJson(CODE40001, busServiceNot);
            ObjectNode result = (ObjectNode) Json.toJson(service);
            result.put(CODE, CODE200);
            return ok(result);
        });
    }

    /**
     * @api {POST} /v1/user/service/new/ 03添加商家服务
     * @apiName addShowCase
     * @apiGroup SERVICE
     * @apiParam {String} serviceName 服务名字
     * @apiParam {String} serviceIcon 服务图标
     * @apiParam {String} serviceDigest 服务摘要
     * @apiParam {String} serviceContent 服务内容
     * @apiParam {long} categoryId 分类ID
     * @apiSuccess (Error 40001){int} code 40001 参数错误
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public CompletionStage<Result> addService(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            Messages messages = messagesApi.preferred(request);
            JsonNode jsonNode = request.body().asJson();
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            //base.argument.error=参数错误
            String paramErr = messages.at("base.argument.error");
            if (null == jsonNode) return okCustomJson(CODE40001, paramErr);
            //service.please.apply.admission ="请先申请认证"
            String admission = messages.at("service.please.apply.admission");
            if (member.shopId < 1) return okCustomJson(CODE40001, admission);
            //service.error.store.not.member="非店铺管理人员不可操作"
            String noMember = messages.at("service.error.store.not.member");
            if (member.userType != Member.USER_TYPE_MANAGER) {
                return okCustomJson(CODE40001, noMember);
            }
            Service service = Json.fromJson(jsonNode, Service.class);
            //service.info.please.serviceName="请输入服务名字"
            String serviceName = messages.at("service.info.please.serviceName");
            if (ValidationUtil.isEmpty(service.serviceName)) return okCustomJson(CODE40001, serviceName);
            //service.info.please.digest="请输入服务摘要"
            String serDigest = messages.at("service.info.please.digest");
            if (ValidationUtil.isEmpty(service.serviceDigest)) return okCustomJson(CODE40001, serDigest);
            //service.upload.please.serviceContent="请上传服务内容"
            String serContent = messages.at("service.upload.please.serviceContent");
            if (ValidationUtil.isEmpty(service.serviceContent)) return okCustomJson(CODE40001, serContent);
            service.setId(0);
            service.setShopId(member.shopId);
            service.setCreateTime(dateUtils.getCurrentTimeBySecond());
            service.save();
            return okJSON200();
        });
    }


    /**
     * @api {POST} /v1/user/service/:id/ 04修改服务
     * @apiName updateService
     * @apiGroup SERVICE
     * @apiParam {String} serviceName 服务名字
     * @apiParam {String} serviceIcon 服务图标
     * @apiParam {String} serviceDigest 服务摘要
     * @apiParam {String} serviceContent 服务内容
     * @apiParam {long} categoryId 分类ID
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Error 40001){int} code 40001 参数错误
     * @apiSuccess (Error 40002){int} code 40002 服务不存在
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public CompletionStage<Result> updateService(Http.Request request, long id) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            Messages messages = messagesApi.preferred(request);
            JsonNode jsonNode = request.body().asJson();
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            //service.please.apply.enter ="请先申请入驻"
            String applyEnter = messages.at("service.please.apply.enter");
            if (member.shopId < 1) return okCustomJson(CODE40001, applyEnter);
            //service.error.store.not.member="非店铺管理人员不可操作"
            String storeMeb = messages.at("service.error.store.not.member");
            if (member.userType != Member.USER_TYPE_MANAGER) {
                return okCustomJson(CODE40001, storeMeb);
            }
            //base.argument.error=参数错误
            String paramErr = messages.at("base.argument.error");
            if (null == jsonNode || id < 1) return okCustomJson(CODE40001, paramErr);
            Service service = Service.find.byId(id);
            //service.error.not.exist="服务不存在"
            String NotExist = messages.at("service.error.not.exist");
            if (null == service || service.shopId != member.shopId) return okCustomJson(CODE40002, NotExist);

            String serviceName = jsonNode.findPath("serviceName").asText();
            String serviceIcon = jsonNode.findPath("serviceIcon").asText();
            String serviceDigest = jsonNode.findPath("serviceDigest").asText();
            String serviceContent = jsonNode.findPath("serviceContent").asText();
            long categoryId = jsonNode.findPath("categoryId").asLong();
            if (!ValidationUtil.isEmpty(serviceName)) service.setServiceName(serviceName);
            if (jsonNode.has("serviceIcon")) service.setServiceIcon(serviceIcon);
            if (jsonNode.has("serviceDigest")) service.setServiceDigest(serviceDigest);
            if (jsonNode.has("serviceContent")) service.setServiceContent(serviceContent);
            if (categoryId > 0) service.setCategoryId(categoryId);
            service.setUpdateTime(dateUtils.getCurrentTimeBySecond());
            service.save();
            return okJSON200();
        });
    }

    /**
     * @api {POST} /v1/user/service/ 05删除服务
     * @apiName deleteService
     * @apiGroup SERVICE
     * @apiParam {int} id id
     * @apiParam {String} operation 操作,"del"为删除
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Error 40001) {int} code 40001 参数错误
     * @apiSuccess (Error 40002) {int} code 40002 服务不存在
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public CompletionStage<Result> deleteService(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            Messages messages = messagesApi.preferred(request);
            JsonNode jsonNode = request.body().asJson();
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            //service.please.apply.enter ="请先申请入驻"
            String applyEnter = messages.at("service.please.apply.enter");
            if (member.shopId < 1) return okCustomJson(CODE40001, applyEnter);
            //service.error.store.not.member="非店铺管理人员不可操作"
            String storeMeb = messages.at("service.error.store.not.member");
            if (member.userType != Member.USER_TYPE_MANAGER) {
                return okCustomJson(CODE40001, storeMeb);
            }
            String operation = jsonNode.findPath("operation").asText();
            //base.argument.error=参数错误
            String paramErr = messages.at("base.argument.error");
            if (ValidationUtil.isEmpty(operation) || !operation.equals("del"))
                return okCustomJson(CODE40001, paramErr);
            long id = jsonNode.findPath("id").asInt();
            if (id < 1) return okCustomJson(CODE40001, paramErr);
            Service service = Service.find.byId(id);
            //service.error.not.exist="服务不存在"
            String NotExist = messages.at("service.error.not.exist");
            if (null == service || service.shopId != member.shopId) return okCustomJson(CODE40002, NotExist);
            service.delete();
            return okJSON200();
        });

    }
}
