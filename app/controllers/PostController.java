package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import models.post.Category;
import models.post.Post;
import models.post.PostLike;
import models.post.Reply;
import models.user.Member;
import play.Logger;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import utils.ValidationUtil;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static constants.BusinessConstant.PAGE_SIZE_10;
import static constants.RedisKeyConstant.POST_CATEGORIES_LIST_BY_CATEGORY_ID;


public class PostController extends BaseController {

    Logger.ALogger logger = Logger.of(PostController.class);
    @Inject
    MessagesApi messagesApi;

    /**
     * @api {GET} /v1/user/categories/?filter&cateType= 01帖子分类列表
     * @apiName listCategories
     * @apiGroup Post
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Success 200) {int} pages 页数
     * @apiSuccess (Success 200) {JsonArray} list 列表
     * @apiSuccess (Success 200){long} id 分类id
     * @apiSuccess (Success 200){String} name 名称
     * @apiSuccess (Success 200){String} imgUrl 图片
     * @apiSuccess (Success 200){String} poster 海报图片
     * @apiSuccess (Success 200){long} soldAmount 已售数量
     * @apiSuccess (Success 200){int} show 1显示2不显示
     * @apiSuccess (Success 200){int} sort 排序顺序
     * @apiSuccess (Success 200){JsonArray} children 子分类列表
     * @apiSuccess (Success 200){string} updateTime 更新时间
     */
    public CompletionStage<Result> listCategories(final String filter, int cateType) {
        return CompletableFuture.supplyAsync(() -> {
            String key = cacheUtils.getCategoryJsonCache(cateType);
            //第一页从缓存读取
            if (ValidationUtil.isEmpty(filter)) {
                Optional<String> cacheOptional = redis.sync().get(key);
                if (cacheOptional.isPresent()) {
                    String node = cacheOptional.get();
                    if (ValidationUtil.isEmpty(node)) return ok(Json.parse(node));
                }
            }
            ExpressionList<Category> expressionList = Category.find.query().where()
                    .eq("show", Category.SHOW_CATEGORY)
                    .eq("cateType", cateType);
            if (!ValidationUtil.isEmpty(filter)) expressionList.icontains("name", filter);
            List<Category> list = expressionList.orderBy()
                    .asc("path")
                    .orderBy().desc("sort")
                    .orderBy().asc("id")
                    .findList();
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            result.set("list", Json.toJson(list));
            if (ValidationUtil.isEmpty(filter)) redis.set(key, Json.stringify(result), 30 * 60);
            return ok(result);
        });
    }

    /**
     * @api {GET} /v1/user/post_list/?categoryId= 02贴子列表
     * @apiName listPosts
     * @apiGroup Post
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Success 200) {JsonObject} list 列表
     * @apiSuccess (Success 200){String} title 标题
     * @apiSuccess (Success 200){String} userName 作者
     * @apiSuccess (Success 200){String} avatar 头像
     * @apiSuccess (Success 200){String} content 内容
     * @apiSuccess (Success 200){long} categoryId 分类ID
     * @apiSuccess (Success 200){String} categoryName 分类名字
     * @apiSuccess (Success 200){long} commentNumber 跟贴数
     * @apiSuccess (Success 200){long} replies 回复数
     * @apiSuccess (Success 200){long} participants 参与人数
     * @apiSuccess (Success 200){long} likes 点赞数
     * @apiSuccess (Success 200){String} updateTime 更新时间
     * @apiSuccess (Success 200){String} createTime 创建时间
     */
    public CompletionStage<Result> listPosts(Http.Request request, int categoryId, int page) {
        return CompletableFuture.supplyAsync(() -> {
            //缓存2分钟
            String key = POST_CATEGORIES_LIST_BY_CATEGORY_ID + categoryId;
            Optional<String> jsonCache = redis.sync().get(key);
            if (jsonCache.isPresent()) {
                String result = jsonCache.get();
                if (!ValidationUtil.isEmpty(result)) return ok(result);
            }
            ExpressionList<Post> expressionList = Post.find.query().where()
                    .eq("status", Post.STATUS_NORMAL);
            if (categoryId > 0) expressionList.eq("categoryId", categoryId);
            PagedList<Post> pagedList = expressionList
                    .orderBy().desc("id")
                    .setFirstRow((page - 1) * PAGE_SIZE_10)
                    .setMaxRows(PAGE_SIZE_10)
                    .findPagedList();
            ObjectNode node = Json.newObject();
            node.put(CODE, CODE200);
            node.put("hasNext", pagedList.hasNext());
            node.set("list", Json.toJson(pagedList.getList()));
            redis.set(key, Json.stringify(node), 2 * 60);
            return ok(node);
        });
    }


    /**
     * @api {GET} /v1/user/posts/:id/ 03帖子详情
     * @apiName getPost
     * @apiGroup Post
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Success 200) {Array} replyList 跟贴内容
     * @apiSuccess (Success 200){long} id id
     * @apiSuccess (Success 200){String} title 标题
     * @apiSuccess (Success 200){String} userName 作者
     * @apiSuccess (Success 200){String} avatar 头像
     * @apiSuccess (Success 200){String} content 内容
     * @apiSuccess (Success 200){long} categoryId 分类ID
     * @apiSuccess (Success 200){String} categoryName 分类名字
     * @apiSuccess (Success 200){long} commentNumber 跟贴数
     * @apiSuccess (Success 200){long} replies 回复数
     * @apiSuccess (Success 200){int} status 1正常 -1隐藏
     * @apiSuccess (Success 200){long} participants 参与人数
     * @apiSuccess (Success 200){long} likes 点赞数
     * @apiSuccess (Success 200){String} updateTime 更新时间
     * @apiSuccess (Success 200){String} createTime 创建时间
     */
    public CompletionStage<Result> getPost(Http.Request request, long id) {
        return CompletableFuture.supplyAsync(() -> {
            Post post = Post.find.byId(id);
            if (post.status == Post.STATUS_DISABLE) {
                return okCustomJson(CODE40001, "该贴子被隐藏");
            }
            List<Reply> replyList = Reply.find.query().where().eq("postId", id)
                    .orderBy().asc("id")
                    .findList();
            ObjectNode node = (ObjectNode) Json.toJson(post);
            node.put(CODE, CODE200);
            replyList.parallelStream().forEach((reply) -> {
                if (reply.status == Post.STATUS_DISABLE) {
                    reply.content = "";
                }
            });
            node.set("replyList", Json.toJson(replyList));
            return ok(node);
        });
    }


    /**
     * @api {POST} /v1/user/post/new/ 03发帖
     * @apiName newPost
     * @apiGroup Post
     * @apiParam {String} title 标题
     * @apiParam {String} content 内容
     * @apiParam {long} categoryId 分类ID
     */
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> newPost(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            JsonNode jsonNode = request.body().asJson();
            Messages messages = this.messagesApi.preferred(request);
            String baseArgumentError = messages.at("base.argument.error");
            if (null == jsonNode) return okCustomJson(CODE40001, baseArgumentError);
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            if (member.status == Member.MEMBER_STATUS_LOCK) return okCustomJson(CODE40001, "该帐号被锁定");
            Post post = Json.fromJson(jsonNode, Post.class);
            if (!ValidationUtil.isEmpty(post.title)) return okCustomJson(CODE40001, "请输入标题");
            if (!ValidationUtil.isEmpty(post.content)) return okCustomJson(CODE40001, "请输入内容");
            if (post.categoryId < 1) return okCustomJson(CODE40001, "请选择分类");
            Category category = Category.find.byId(post.categoryId);
            if (null == category) return okCustomJson(CODE40001, "请选择分类");
            post.setUid(uid);
            post.setUserName(member.nickName);
            post.setAvatar(member.avatar);
            long currentTime = dateUtils.getCurrentTimeBySecond();
            post.setCategoryName(category.name);
            post.setUpdateTime(currentTime);
            post.setCreateTime(currentTime);
            post.setStatus(Post.STATUS_NORMAL);
            post.save();
            return okJSON200();
        });
    }


    /**
     * @api {POST} /v1/user/post/like/ 04点赞
     * @apiName likePost
     * @apiGroup Post
     * @apiParam {long} id id
     * @apiParam {boolean} like true为点赞 false取消点赞
     */
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> likePost(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            JsonNode jsonNode = request.body().asJson();
            Messages messages = this.messagesApi.preferred(request);
            String baseArgumentError = messages.at("base.argument.error");
            if (null == jsonNode) return okCustomJson(CODE40001, baseArgumentError);
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            if (member.status == Member.MEMBER_STATUS_LOCK) return okCustomJson(CODE40001, "该帐号被锁定");
            long id = jsonNode.findPath("id").asLong();
            boolean like = jsonNode.findPath("like").asBoolean();
            Post post = Post.find.byId(id);
            if (null == post) return okCustomJson(CODE40001, "该贴子不存在");
            PostLike postLike = PostLike.find.query().where()
                    .eq("uid", uid)
                    .eq("postId", id)
                    .setMaxRows(1)
                    .findOne();
            if (like) {
                if (null == postLike) {
                    postLike = new PostLike();
                    postLike.setPostId(id);
                    postLike.setUid(uid);
                    postLike.setLike(true);
                    postLike.setUpdateTime(dateUtils.getCurrentTimeBySecond());
                }
                post.setLikes(post.likes + 1);
            } else {
                postLike.setLike(false);
                long likes = post.likes - 1;
                if (likes < 1) likes = 0;
                post.setLikes(likes);
            }
            post.save();
            return okJSON200();
        });
    }


    /**
     * @api {POST} /v1/user/post/reply/ 05回复
     * @apiName replyPost
     * @apiGroup Post
     * @apiParam {long} id id
     * @apiParam {long} [quoteId] 引用某个回复
     * @apiParam {String} content content
     */
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> replyPost(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            JsonNode jsonNode = request.body().asJson();
            Messages messages = this.messagesApi.preferred(request);
            String baseArgumentError = messages.at("base.argument.error");
            if (null == jsonNode) return okCustomJson(CODE40001, baseArgumentError);
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            if (member.status == Member.MEMBER_STATUS_LOCK) return okCustomJson(CODE40001, "该帐号被锁定");
            long id = jsonNode.findPath("id").asLong();
            long quoteId = jsonNode.findPath("quoteId").asLong();
            String content = jsonNode.findPath("content").asText();
            Post post = Post.find.byId(id);
            if (null == post) return okCustomJson(CODE40001, "该贴子不存在");
            if (quoteId > 0) {
                Reply reply = Reply.find.byId(quoteId);
                if (null == reply) return okCustomJson(CODE40001, "该引用不存在");
                reply.setReplies(reply.replies + 1);
                post.setReplies(post.replies + 1);
            } else {
                post.setCommentNumber(post.commentNumber + 1);
            }
            Reply reply = new Reply();
            reply.setUid(uid);
            reply.setUserName(member.nickName);
            reply.setAvatar(member.avatar);
            reply.setContent(content);
            reply.setPostId(id);
            reply.setQuoteId(quoteId);
            reply.setStatus(Reply.STATUS_NORMAL);
            long currentTime = dateUtils.getCurrentTimeBySecond();
            reply.setUpdateTime(currentTime);
            reply.setCreateTime(currentTime);
            reply.save();
            List<Long> replyUidList = Reply.find.query().select("uid").where().eq("postId", id)
                    .findSingleAttributeList();
            long count = replyUidList.stream().distinct().count();
            post.setParticipants(count);
            post.save();
            return okJSON200();
        });
    }


    /**
     * @api {POST} /v1/user/post/ 06删除贴子/回复
     * @apiName replyPost
     * @apiGroup Post
     * @apiParam {long} id id
     * @apiParam {int} postType 1主贴 2跟贴/回复
     */
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> deletePost(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            JsonNode jsonNode = request.body().asJson();
            Messages messages = this.messagesApi.preferred(request);
            String baseArgumentError = messages.at("base.argument.error");
            if (null == jsonNode) return okCustomJson(CODE40001, baseArgumentError);
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            if (member.status == Member.MEMBER_STATUS_LOCK) return okCustomJson(CODE40001, "该帐号被锁定");
            long id = jsonNode.findPath("id").asLong();
            int postType = jsonNode.findPath("postType").asInt();
            if (postType == 1) {
                Post post = Post.find.byId(id);
                if (null == post) return okCustomJson(CODE40001, "该贴子不存在");
                if (uid != post.uid) return okCustomJson(CODE40001, "该贴子不存在");
                post.setStatus(Post.STATUS_DELETE);
                post.setUpdateTime(dateUtils.getCurrentTimeBySecond());
                post.save();
            } else {
                Reply reply = Reply.find.byId(id);
                if (null == reply) return okCustomJson(CODE40001, "该回复不存在");
                if (uid != reply.uid) return okCustomJson(CODE40001, "该贴子不存在");
                reply.setStatus(Reply.STATUS_DELETE);
                reply.setUpdateTime(dateUtils.getCurrentTimeBySecond());
                reply.save();
            }
            return okJSON200();
        });
    }


    /**
     * @api {GET} /v1/user/my_posts/ 07我的帖子
     * @apiName myPosts
     * @apiGroup Post
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Success 200) {int} pages 页数
     * @apiSuccess (Success 200) {JsonArray} list 列表
     * @apiSuccess (Success 200){long} id 分类id
     * @apiSuccess (Success 200){String} name 名称
     * @apiSuccess (Success 200){String} imgUrl 图片
     * @apiSuccess (Success 200){String} poster 海报图片
     * @apiSuccess (Success 200){long} soldAmount 已售数量
     * @apiSuccess (Success 200){int} show 1显示2不显示
     * @apiSuccess (Success 200){int} sort 排序顺序
     * @apiSuccess (Success 200){JsonArray} children 子分类列表
     * @apiSuccess (Success 200){string} updateTime 更新时间
     */
    public CompletionStage<Result> myPosts(Http.Request request, int page) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            PagedList<Post> pagedList = Post.find.query().where()
                    .lt("status", Post.STATUS_DELETE)
                    .eq("uid", uid)
                    .orderBy().desc("id")
                    .setFirstRow((page - 1) * PAGE_SIZE_10)
                    .setMaxRows(PAGE_SIZE_10)
                    .findPagedList();
            ObjectNode node = Json.newObject();
            node.put(CODE, CODE200);
            node.put("hasNext", pagedList.hasNext());
            node.set("list", Json.toJson(pagedList.getList()));
            return ok(node);
        });
    }


}
