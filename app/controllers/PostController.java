package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import constants.BusinessConstant;
import io.ebean.Expr;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import models.post.Post;
import models.post.PostCategory;
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


public class PostController extends BaseController {

    Logger.ALogger logger = Logger.of(PostController.class);
    @Inject
    MessagesApi messagesApi;

    /**
     * @api {GET} /v1/user/post_categories/?filter&cateType= 01帖子分类列表
     * @apiName listPostCategories
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
    public CompletionStage<Result> listPostCategories(final String filter) {
        return CompletableFuture.supplyAsync(() -> {
            String key = cacheUtils.getPostCategoryJsonCache();
            //第一页从缓存读取
            if (ValidationUtil.isEmpty(filter)) {
                Optional<String> cacheOptional = redis.sync().get(key);
                if (cacheOptional.isPresent()) {
                    String node = cacheOptional.get();
                    if (ValidationUtil.isEmpty(node)) return ok(Json.parse(node));
                }
            }
            ExpressionList<PostCategory> expressionList = PostCategory.find.query().where()
                    .eq("show", PostCategory.SHOW_CATEGORY);
            if (!ValidationUtil.isEmpty(filter)) expressionList.icontains("name", filter);
            List<PostCategory> list = expressionList.orderBy()
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
     * @api {GET} /v1/user/categories_posts/ 02帖子分类列表带贴子
     * @apiName listPostCategoriesWithPosts
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
    public CompletionStage<Result> listPostCategoriesWithPosts() {
        return CompletableFuture.supplyAsync(() -> {
            String key = cacheUtils.getCategoryWithPostsJsonCache();
            Optional<String> cacheOptional = redis.sync().get(key);
            if (cacheOptional.isPresent()) {
                String node = cacheOptional.get();
                if (ValidationUtil.isEmpty(node)) return ok(Json.parse(node));
            }
            ExpressionList<PostCategory> expressionList = PostCategory.find.query().where()
                    .eq("show", PostCategory.SHOW_CATEGORY);
            List<PostCategory> list = expressionList.orderBy()
                    .asc("path")
                    .orderBy().desc("sort")
                    .orderBy().asc("id")
                    .findList();
            list.parallelStream().forEach((postCategory) -> {
                List<Post> postList = Post.find.query().where()
                        .eq("categoryId", postCategory.id)
                        .eq("status", Post.STATUS_NORMAL)
                        .orderBy().desc("placeTop")
                        .orderBy().desc("id")
                        .setMaxRows(10)
                        .findList();
                postCategory.postList.addAll(postList);
                //postList
            });
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            result.set("list", Json.toJson(list));
            redis.set(key, Json.stringify(result), 1);
            return ok(result);
        });
    }

    /**
     * @api {POST} /v1/user/post_list/ 02贴子列表
     * @apiName listPosts
     * @apiGroup Post
     * @apiParam {String} filter filter
     * @apiParam {long} categoryId categoryId
     * @apiParam {int} orderType orderType 1最新发表 2最新回复 3最多回复
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
    public CompletionStage<Result> listPosts(Http.Request request) {
        return CompletableFuture.supplyAsync(() -> {
            //缓存2分钟
//            String key = POST_CATEGORIES_LIST_BY_CATEGORY_ID + categoryId;
//            Optional<String> jsonCache = redis.sync().get(key);
//            if (jsonCache.isPresent()) {
//                String result = jsonCache.get();
//                if (!ValidationUtil.isEmpty(result)) return ok(result);
//            }
            JsonNode jsonNode = request.body().asJson();
            Messages messages = this.messagesApi.preferred(request);
            String baseArgumentError = messages.at("base.argument.error");
            if (null == jsonNode) return okCustomJson(CODE40001, baseArgumentError);
            String filter = jsonNode.findPath("filter").asText();
            long categoryId = jsonNode.findPath("categoryId").asLong();
            int orderType = jsonNode.findPath("orderType").asInt();
            int page = jsonNode.findPath("page").asInt();
            if (page < 1) page = 1;
            ExpressionList<Post> expressionList = Post.find.query().where()
                    .eq("status", Post.STATUS_NORMAL);
            if (categoryId > 0) expressionList.eq("categoryId", categoryId);
            if (!ValidationUtil.isEmpty(filter)) {
                expressionList.or(Expr.icontains("title", filter), Expr.icontains("content", filter));
            }
            if (orderType == 1) {
                expressionList.orderBy().desc("id");
            } else if (orderType == 2) {
                expressionList.orderBy().desc("updateTime");
            } else if (orderType == 3) {
                expressionList.orderBy().desc("replies");
            } else {
                expressionList
                        .orderBy().desc("id");
            }
            PagedList<Post> pagedList = expressionList
                    .setFirstRow((page - 1) * PAGE_SIZE_10)
                    .setMaxRows(PAGE_SIZE_10)
                    .findPagedList();
            ObjectNode node = Json.newObject();
            node.put(CODE, CODE200);
            node.put("hasNext", pagedList.hasNext());
            node.put("pages", pagedList.getTotalPageCount());
            node.set("list", Json.toJson(pagedList.getList()));
//            redis.set(key, Json.stringify(node), 2 * 60);
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
            post.setViews(post.views + 1);
            post.save();
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
            boolean isAdmin = isPostAdmin(request, post);
            node.put("isAdmin", isAdmin);
            return ok(node);
        });
    }

    private boolean isPostAdmin(Http.Request request, Post post) {
        boolean isAdmin = false;
        long uid = businessUtils.getUserIdByAuthToken2(request);
        if (uid > 0) {
            PostCategory postCategory = PostCategory.find.byId(post.categoryId);
            if (null != postCategory) {
                String adminList = postCategory.getAdminList();
                if (adminList.contains("/" + uid + "/")) {
                    isAdmin = true;
                }
            }
        }
        return isAdmin;
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
            //user.error.user.locked="该帐号已被锁定"
            String userLocked = messages.at("post.error.user.locked");
            if (member.status == Member.MEMBER_STATUS_LOCK) return okCustomJson(CODE40001, userLocked);
            Post post = Json.fromJson(jsonNode, Post.class);
            //post.info.please.headline="请输入标题"
            String inHeadline = messages.at("post.info.please.headline");
            if (ValidationUtil.isEmpty(post.title)) return okCustomJson(CODE40001, inHeadline);
            //post.info.please.content="请输入内容"
            String inContent = messages.at("post.info.please.content");
            if (ValidationUtil.isEmpty(post.content)) return okCustomJson(CODE40001, inContent);
            // post.selected.please.category="请选择分类"
            String selCategory = messages.at("post.selected.please.category");
            if (post.categoryId < 1) return okCustomJson(CODE40001, selCategory);
            PostCategory category = PostCategory.find.byId(post.categoryId);
            if (null == category) return okCustomJson(CODE40001, selCategory);
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
     * @api {POST} /v1/user/post/:id/ 04修改帖子
     * @apiName updatePost
     * @apiGroup Post
     * @apiParam {String} title 标题
     * @apiParam {String} content 内容
     * @apiParam {long} categoryId 分类ID
     */
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> updatePost(Http.Request request, long id) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            JsonNode jsonNode = request.body().asJson();
            Messages messages = this.messagesApi.preferred(request);
            String baseArgumentError = messages.at("base.argument.error");
            if (null == jsonNode) return okCustomJson(CODE40001, baseArgumentError);
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            //post.error.user.locked="该帐号已被锁定"
            String userLocked = messages.at("post.error.user.locked");
            if (member.status == Member.MEMBER_STATUS_LOCK) return okCustomJson(CODE40001, userLocked);
            Post post = Post.find.byId(id);
            //post.error.post.exist="该帖子不存在"
            String postNot = messages.at("post.error.post.exist");
            if (null == post || uid != post.uid) return okCustomJson(CODE40001, postNot);

            Post param = Json.fromJson(jsonNode, Post.class);
            if (!ValidationUtil.isEmpty(param.title)) {
                post.setTitle(param.title);
            }
            if (!ValidationUtil.isEmpty(param.content)) {
                post.setTitle(param.content);
            }
            if (param.categoryId > 0) {
                PostCategory category = PostCategory.find.byId(post.categoryId);
                //请选择分类
                String categoryed = messages.at("post.selected.please.category");
                if (null == category) return okCustomJson(CODE40001, categoryed);
                post.setCategoryId(param.categoryId);
                post.setCategoryName(category.name);
            }

            post.setUid(uid);
            post.setUserName(member.nickName);
            post.setAvatar(member.avatar);
            long currentTime = dateUtils.getCurrentTimeBySecond();

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
            //post.error.user.locked="该帐号已被锁定"
            String userLocked = messages.at("post.error.user.locked");
            if (member.status == Member.MEMBER_STATUS_LOCK) return okCustomJson(CODE40001, userLocked);
            long id = jsonNode.findPath("id").asLong();
            boolean like = jsonNode.findPath("like").asBoolean();
            Post post = Post.find.byId(id);
            //post.error.post.exist="该帖子不存在"
            String postNot = messages.at("post.error.post.exist");
            if (null == post) return okCustomJson(CODE40001, postNot);
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
            //post.error.user.locked="该帐号已被锁定"
            String userLocked = messages.at("post.error.user.locked");
            if (member.status == Member.MEMBER_STATUS_LOCK) return okCustomJson(CODE40001, userLocked);
            long id = jsonNode.findPath("id").asLong();
            long quoteId = jsonNode.findPath("quoteId").asLong();
            String content = jsonNode.findPath("content").asText();
            Post post = Post.find.byId(id);
            //post.error.post.exist="该帖子不存在"
            String postNot = messages.at("post.error.post.exist");
            if (null == post) return okCustomJson(CODE40001, postNot);
           // post.error.quote.exist="该引用不存在"
            String quoteNo = messages.at("post.error.quote.exist");
            if (quoteId > 0) {
                Reply reply = Reply.find.byId(quoteId);
                if (null == reply) return okCustomJson(CODE40001, quoteNo);
                reply.setReplies(reply.replies + 1);
                post.setReplies(post.replies + 1);
            } else {
                post.setCommentNumber(post.commentNumber + 1);
            }
            Reply reply = new Reply();
            reply.setUid(uid);
            reply.setPostTitle(post.title);
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
            post.setLastReplyTime(currentTime);
            post.save();
            return okJSON200();
        });
    }


    /**
     * @api {POST} /v1/user/post/ 06删除贴子/回复
     * @apiName deletePost
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
            //post.error.user.locked="该帐号已被锁定"
            String userLocked = messages.at("post.error.user.locked");
            if (member.status == Member.MEMBER_STATUS_LOCK) return okCustomJson(CODE40001, userLocked);
            long id = jsonNode.findPath("id").asLong();
            int postType = jsonNode.findPath("postType").asInt();
            //post.error.post.exist="该帖子不存在"
            String postNot = messages.at("post.error.post.exist");
            //post.error.reply.exist="该回复不存在"
            String replyNot = messages.at("post.error.reply.exist");
            if (postType == 1) {
                Post post = Post.find.byId(id);
                if (null == post) return okCustomJson(CODE40001, postNot);
                if (uid != post.uid && !isPostAdmin(request, post)) return okCustomJson(CODE40001, postNot);
                post.setStatus(Post.STATUS_DELETE);
                post.setUpdateTime(dateUtils.getCurrentTimeBySecond());
                post.save();
            } else {
                Reply reply = Reply.find.byId(id);
                if (null == reply) return okCustomJson(CODE40001, replyNot);
                Post post = Post.find.byId(reply.postId);
                if (null == post) return okCustomJson(CODE40001, postNot);
                if (uid != reply.uid && !isPostAdmin(request, post)) return okCustomJson(CODE40001, postNot);
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
            node.put("pages", pagedList.getTotalPageCount());
            node.set("list", Json.toJson(pagedList.getList()));
            return ok(node);
        });
    }


    /**
     * @api {GET} /v1/user/my_reply/ 08我的回复
     * @apiName myReplyList
     * @apiGroup Post
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Success 200) {int} pages 页数
     * @apiSuccess (Success 200) {JsonObject} list 列表
     * @apiSuccess (Success 200){String} postTitle 贴子标题
     * @apiSuccess (Success 200){String} userName 作者
     * @apiSuccess (Success 200){String} avatar 头像
     * @apiSuccess (Success 200){String} content 内容
     * @apiSuccess (Success 200){long} replies 回复数
     * @apiSuccess (Success 200){long} likes 点赞数
     * @apiSuccess (Success 200){String} updateTime 更新时间
     * @apiSuccess (Success 200){String} createTime 创建时间
     */
    public CompletionStage<Result> myReplyList(Http.Request request, int page) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            PagedList<Reply> pagedList = Reply.find.query().where()
                    .lt("status", Post.STATUS_DELETE)
                    .eq("uid", uid)
                    .orderBy().desc("id")
                    .setFirstRow((page - 1) * PAGE_SIZE_10)
                    .setMaxRows(PAGE_SIZE_10)
                    .findPagedList();
            ObjectNode node = Json.newObject();
            node.put(CODE, CODE200);
            node.put("hasNext", pagedList.hasNext());
            node.put("pages", pagedList.getTotalPageCount());
            node.set("list", Json.toJson(pagedList.getList()));
            return ok(node);
        });
    }


    /**
     * @api {POST} /v1/user/post_top/  09置顶/取消置顶贴子
     * @apiName placePostTop
     * @apiGroup Admin-Post
     * @apiParam {long} id id
     * @apiParam {int} placeTop true置顶 false取消置顶
     */
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> placePostTop(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            JsonNode jsonNode = request.body().asJson();
            Messages messages = this.messagesApi.preferred(request);
            String baseArgumentError = messages.at("base.argument.error");
            if (null == jsonNode) return okCustomJson(CODE40001, baseArgumentError);
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            //post.error.user.locked="该帐号已被锁定"
            String userLocked = messages.at("post.error.user.locked");
            if (member.status == Member.MEMBER_STATUS_LOCK) return okCustomJson(CODE40001, userLocked);

            long id = jsonNode.findPath("id").asLong();
            boolean placeTop = jsonNode.findPath("placeTop").asBoolean();
            Post post = Post.find.byId(id);
            //post.error.post.exist="该帖子不存在"
            String postNot = messages.at("post.error.post.exist");
            if (null == post) return okCustomJson(CODE40001, postNot);
            //post.error.operate.Admin="非管理员不能操作"
            String notAdmin = messages.at("post.error.operate.Admin");
            if (!isPostAdmin(request, post)) return okCustomJson(CODE40001, notAdmin);
            post.setPlaceTop(placeTop);
            post.setUpdateTime(dateUtils.getCurrentTimeBySecond());
            post.save();
            return okJSON200();
        });
    }


    /**
     * @api {GET} /v1/user/reply_list/?id=&page= 10评论列表
     * @apiName replyList
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
    public CompletionStage<Result> replyList(Http.Request request, long id, int page) {
        return CompletableFuture.supplyAsync(() -> {
            Messages messages = messagesApi.preferred(request);
            //post.error.postId="id错误"
            String idFalse = messages.at("post.error.postId");
            if (id < 1) return okCustomJson(CODE40001, idFalse);
            ExpressionList<Reply> expressionList = Reply.find.query().where()
                    .eq("postId", id);
            PagedList<Reply> pagedList = expressionList.orderBy().asc("id")
                    .setFirstRow((page - 1) * PAGE_SIZE_10)
                    .setMaxRows(PAGE_SIZE_10)
                    .findPagedList();
            ObjectNode node = Json.newObject();
            node.put(CODE, CODE200);
            List<Reply> replyList = pagedList.getList();
            replyList.parallelStream().forEach((reply) -> {
                if (reply.status == Post.STATUS_DISABLE) {
                    reply.content = "";
                }
            });
            node.set("replyList", Json.toJson(replyList));
            node.put("hasNext", pagedList.hasNext());
            node.put("pages", pagedList.getTotalPageCount());
            return ok(node);
        });
    }

}
