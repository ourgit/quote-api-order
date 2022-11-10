package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import constants.BusinessConstant;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import models.post.PostCategory;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ShowCaseController extends BaseController {

  @Inject
    MessagesApi messagesApi;
    /**
     * @api {GET} /v1/user/show_case_list/?shopId=&page=&title= 01案例图片
     * @apiName listShowCase
     * @apiGroup SHOW-CASE
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Success 200){Array} list
     * @apiSuccess (Success 200){String} title 标题
     * @apiSuccess (Success 200){String} tags 标签
     * @apiSuccess (Success 200){String} images 图片
     * @apiSuccess (Success 200){String} shopName 店铺名字
     * @apiSuccess (Success 200){long} shopId 店铺ID
     * @apiSuccess (Success 200){long} imageCount 图片数
     * @apiSuccess (Error 40001){int} code 40001 参数错误
     */
    public CompletionStage<Result> listShowCase(Http.Request request, long shopId, int page, String title) {
        return CompletableFuture.supplyAsync(() -> {
            ExpressionList<Showcase> expressionList = Showcase.find.query().where();
            if (shopId > 0) expressionList.eq("shopId", shopId);
            if (!ValidationUtil.isEmpty(title)) expressionList.icontains("title", title);
            PagedList<Showcase> pagedList = expressionList.orderBy().desc("id")
                    .setFirstRow((page - 1) * BusinessConstant.PAGE_SIZE_10)
                    .setMaxRows(BusinessConstant.PAGE_SIZE_10)
                    .findPagedList();
            ObjectNode result = Json.newObject();
            List<Showcase> list = pagedList.getList();
            list.parallelStream().forEach((showcase) -> showcase.categoryName = getCategoryName(showcase.categoryId));
            result.set("list", Json.toJson(list));
            result.put("pages", pagedList.getTotalPageCount());
            result.put(CODE, CODE200);
            return ok(result);
        });
    }

    /**
     * @api {GET} /v1/user/show_case_list/:id/ 02案例图片详情
     * @apiName getShowCase
     * @apiGroup SHOW-CASE
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Success 200){String} title 标题
     * @apiSuccess (Success 200){String} tags 标签
     * @apiSuccess (Success 200){String} images 图片
     * @apiSuccess (Success 200){String} shopName 店铺名字
     * @apiSuccess (Success 200){long} shopId 店铺ID
     * @apiSuccess (Success 200){long} imageCount 图片数
     * @apiSuccess (Error 40001){int} code 40001 参数错误
     */
    public CompletionStage<Result> getShowCase(Http.Request request, long id) {
        return CompletableFuture.supplyAsync(() -> {
            Messages messages = messagesApi.preferred(request);
            //shop.error.showcase.not.exist="该案例不存在"
            String NotShowcase = messages.at("shop.error.showcase.not.exist");
            Showcase showcase = Showcase.find.byId(id);
            if (null == showcase) return okCustomJson(CODE40001, NotShowcase);
            showcase.categoryName = getCategoryName(showcase.categoryId);
            ObjectNode result = (ObjectNode) Json.toJson(showcase);
            result.put(CODE, CODE200);
            return ok(result);
        });
    }

    public String getCategoryName(long categoryId) {
        PostCategory postCategory = PostCategory.find.byId(categoryId);
        if (null != postCategory) {
            if (postCategory.path.equals("/")) return postCategory.name;
            String categoryName = postCategory.pathName;
            categoryName = categoryName.replaceAll("/", ">");
            return categoryName;
        }
        return "";
    }

    /**
     * @api {POST} /v1/user/show_case/new/ 03添加案例图片
     * @apiName addShowCase
     * @apiGroup SHOW-CASE
     * @apiParam {String} title 标题
     * @apiParam {String} tags 标签
     * @apiParam {String} images 图片
     * @apiParam {String} content 内容
     * @apiParam {long} [categoryId] 分类ID
     * @apiParam {long} shopId 店铺ID
     * @apiParam {long} imageCount 图片数
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Error 40001){int} code 40001 参数错误
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public CompletionStage<Result> addShowCase(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            Messages messages = messagesApi.preferred(request);
            JsonNode jsonNode = request.body().asJson();
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            // base.argument.error=参数错误
            String paramErr = messages.at("base.argument.error");
            if (null == jsonNode) return okCustomJson(CODE40001, paramErr);
            //service.please.apply.enter ="请先申请入驻"
            String applyEnter = messages.at("service.please.apply.enter");
            if (member.shopId < 1) return okCustomJson(CODE40001, applyEnter);
            //service.error.store.not.member="非店铺管理人员不可操作"
            String NotMember = messages.at("service.error.store.not.member");
            if (member.userType != Member.USER_TYPE_MANAGER) {
                return okCustomJson(CODE40001, NotMember);
            }
            Showcase showcase = Json.fromJson(jsonNode, Showcase.class);
            //shop.info.please.input.title="请输入标题"
            String inTitle = messages.at("shop.info.please.input.title");
            if (ValidationUtil.isEmpty(showcase.title)) return okCustomJson(CODE40001, inTitle);
            // shop.upload.please.picture="请上传图片"
            String upPicture = messages.at("shop.upload.please.picture");
            if (ValidationUtil.isEmpty(showcase.images)) return okCustomJson(CODE40001, upPicture);
            Shop shop = Shop.find.byId(member.shopId);
            if (null == shop) return okCustomJson(CODE40001, applyEnter);
            showcase.setId(0);
            showcase.setShopName(shop.name);
            showcase.setStatus(Showcase.STATUS_AUDIT);
            showcase.setShopId(member.shopId);
            showcase.setShopName(shop.name);
            showcase.setShopLogo(shop.rectLogo);
            showcase.setCreateTime(dateUtils.getCurrentTimeBySecond());
            showcase.save();
            return okJSON200();
        });
    }


    /**
     * @api {POST} /v1/user/show_case/:id/ 04修改案例
     * @apiName updateShowCase
     * @apiGroup SHOW-CASE
     * @apiParam {String} title 标题
     * @apiParam {String} tags 标签
     * @apiParam {String} images 图片
     * @apiParam {long} imageCount 图片数
     * @apiParam {String} content 内容
     * @apiParam {long} [categoryId] 分类ID
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Error 40001){int} code 40001 参数错误
     * @apiSuccess (Error 40002){int} code 40002 案例图片不存在
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public CompletionStage<Result> updateShowCase(Http.Request request, long id) {
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
            String NotMember = messages.at("service.error.store.not.member");
            if (member.userType != Member.USER_TYPE_MANAGER) {
                return okCustomJson(CODE40001, NotMember);
            }
            // base.argument.error=参数错误
            String paramErr = messages.at("base.argument.error");
            if (null == jsonNode || id < 1) return okCustomJson(CODE40001, paramErr);
            Showcase showcase = Showcase.find.byId(id);
            //shop.error.showcase.not.exist="该案例不存在"
            String NotShowcase = messages.at("shop.error.showcase.not.exist");
            if (null == showcase || showcase.shopId != member.shopId) return okCustomJson(CODE40002, NotShowcase);

            String title = jsonNode.findPath("title").asText();
            String tags = jsonNode.findPath("tags").asText();
            String images = jsonNode.findPath("images").asText();
            String content = jsonNode.findPath("content").asText();
            String cover = jsonNode.findPath("cover").asText();
            long imageCount = jsonNode.findPath("imageCount").asLong();
            long categoryId = jsonNode.findPath("categoryId").asLong();
            if (jsonNode.has("title")) showcase.setTitle(title);
            if (jsonNode.has("tags")) showcase.setTags(tags);
            if (jsonNode.has("images")) showcase.setImages(images);
            if (jsonNode.has("imageCount")) showcase.setImageCount(imageCount);
            if (jsonNode.has("content")) showcase.setContent(content);
            if (jsonNode.has("cover")) showcase.setCover(cover);
            if (categoryId > 0) showcase.setCategoryId(categoryId);
            showcase.save();
            return okJSON200();
        });
    }

    /**
     * @api {POST} /v1/user/show_case/ 05删除案例图片
     * @apiName deleteShowcase
     * @apiGroup SHOW-CASE
     * @apiParam {int} id 案例id
     * @apiParam {String} operation 操作,"del"为删除
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Error 40001) {int} code 40001 参数错误
     * @apiSuccess (Error 40002) {int} code 40002 商品属性不存在
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public CompletionStage<Result> deleteShowcase(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            Messages messages = messagesApi.preferred(request);
            JsonNode jsonNode = request.body().asJson();
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            String applyEnter = messages.at("service.please.apply.enter");
            if (member.shopId < 1) return okCustomJson(CODE40001, applyEnter);
            //service.error.store.not.member="非店铺管理人员不可操作"
            String NotMember = messages.at("service.error.store.not.member");
            if (member.userType != Member.USER_TYPE_MANAGER) {
                return okCustomJson(CODE40001, NotMember);
            }
            String operation = jsonNode.findPath("operation").asText();
            String paramErr = messages.at("base.argument.error");
            if (ValidationUtil.isEmpty(operation) || !operation.equals("del"))
                return okCustomJson(CODE40001, paramErr);
            long id = jsonNode.findPath("id").asInt();
            if (id < 1) return okCustomJson(CODE40001, paramErr);
            Showcase showcase = Showcase.find.byId(id);
            //shop.error.showcase.not.exist="该案例不存在"
            String NotShowcase = messages.at("shop.error.showcase.not.exist");
            if (null == showcase || showcase.shopId != member.shopId) return okCustomJson(CODE40002, NotShowcase);

            showcase.delete();
            return okJSON200();
        });

    }
}
