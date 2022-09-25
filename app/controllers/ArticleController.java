package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import constants.BusinessConstant;
import io.ebean.Ebean;
import io.ebean.PagedList;
import io.ebean.SqlUpdate;
import models.article.Article;
import models.article.ArticleCategory;
import models.article.ArticleFav;
import models.system.ParamConfig;
import models.user.Member;
import play.Logger;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import utils.EncodeUtils;
import utils.ValidationUtil;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static constants.RedisKeyConstant.*;
import static models.system.ParamConfig.SOURCE_FRONTEND;

/**
 * 用户控制类
 */
public class ArticleController extends BaseController {

    Logger.ALogger logger = Logger.of(ArticleController.class);
    @Inject
    EncodeUtils encodeUtils;


    /**
     * @api {GET} /v1/p/discovery_list/ 01发现列表
     * @apiName listDiscovery
     * @apiGroup Article
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Success 200) {JsonObject} list 列表
     * @apiSuccess (Success 200){long} articleId 文章id
     * @apiSuccess (Success 200){String} title 文章标题
     * @apiSuccess (Success 200){String} author 文章作者
     * @apiSuccess (Success 200){String} source 文章来源
     * @apiSuccess (Success 200){String} publishTime 文章发布时间
     * @apiSuccess (Success 200){String} categoryName 所属分类
     * @apiSuccess (Success 200){String} publishStatusName 发布状态
     */
    public CompletionStage<Result> listDiscovery(Http.Request request) {
        return CompletableFuture.supplyAsync(() -> {
            //缓存2分钟
            String key = ARTICLE_DISCOVERY_CACHE;
            Optional<String> jsonCache = redis.sync().get(key);
            if (jsonCache.isPresent()) {
                String result = jsonCache.get();
                if (!ValidationUtil.isEmpty(result)) return ok(result);
            }

            List<ArticleCategory> categoryList = ArticleCategory.find.query().where()
                    .eq("status", ArticleCategory.SHOW)
                    .ge("categoryType", ArticleCategory.TYPE_DISCOVER)
                    .orderBy().desc("sort")
                    .orderBy().desc("id")
                    .findList();
            ArrayNode nodes = Json.newArray();
            categoryList.forEach((each) -> {
                ObjectNode node = Json.newObject();
                node.set("category", Json.toJson(each));
                List<Article> list = Article.find.query()
                        .select("title,digest,headPic,views,favs,comments,shares,createdTime")
                        .where()
                        .eq("categoryId", each.getId())
                        .eq("status", Article.ARTICLE_STATUS_NORMAL)
                        .orderBy().desc("id")
                        .setMaxRows(5)
                        .findList();
                node.set("articles", Json.toJson(list));
                nodes.add(node);
            });

            ObjectNode node = Json.newObject();
            node.put(CODE, CODE200);
            node.set("list", nodes);
            redis.set(key, Json.stringify(node), 2 * 60);
            return ok(node);
        });
    }


    /**
     * @api {GET} /v1/p/articles_by_category_id/?page=&categoryId= 02根据分类id获取文章列表
     * @apiName listArticles
     * @apiGroup Article
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Success 200) {JsonObject} list 列表
     * @apiSuccess (Success 200){long} articleId 文章id
     * @apiSuccess (Success 200){String} title 文章标题
     * @apiSuccess (Success 200){String} author 文章作者
     * @apiSuccess (Success 200){String} source 文章来源
     * @apiSuccess (Success 200){String} publishTime 文章发布时间
     * @apiSuccess (Success 200){String} categoryName 所属分类
     * @apiSuccess (Success 200){String} publishStatusName 发布状态
     */
    public CompletionStage<Result> listArticlesByCategoryId(long categoryId, int page) {
        return CompletableFuture.supplyAsync(() -> {
            //缓存2分钟
            String key = ARTICLE_LIST_JSON_CACHE + categoryId;
            Optional<String> jsonCache = redis.sync().get(key);
            if (jsonCache.isPresent()) {
                String result = jsonCache.get();
                if (!ValidationUtil.isEmpty(result)) return ok(Json.parse(result));
            }
            List<Article> list;
            ObjectNode node = Json.newObject();
            node.put(CODE, CODE200);
            if (page > 0) {
                PagedList<Article> pagedList = Article.find.query()
                        .select("title,digest,headPic,views,favs,comments,shares,createdTime")
                        .where()
                        .eq("categoryId", categoryId)
                        .eq("status", Article.ARTICLE_STATUS_NORMAL)
                        .orderBy().desc("sort")
                        .orderBy().desc("id")
                        .setFirstRow((page - 1) * BusinessConstant.PAGE_SIZE_10)
                        .setMaxRows(BusinessConstant.PAGE_SIZE_10)
                        .findPagedList();
                list = pagedList.getList();
                node.put("hasNext", pagedList.hasNext());
            } else {
                list = Article.find.query()
                        .select("title,digest,headPic,views,favs,comments,shares,createdTime")
                        .where()
                        .eq("categoryId", categoryId)
                        .eq("status", Article.ARTICLE_STATUS_NORMAL)
                        .orderBy().desc("sort")
                        .orderBy().desc("id")
                        .findList();
            }

            node.set("list", Json.toJson(list));
            redis.set(key, Json.stringify(node), 2 * 60);
            return ok(node);
        });
    }


    /**
     * @api {GET} /v1/p/articles/?page=&size= 03根据分类名字获取文章列表
     * @apiName listArticles
     * @apiGroup Article
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Success 200) {JsonObject} list 列表
     * @apiSuccess (Success 200){long} articleId 文章id
     * @apiSuccess (Success 200){String} title 文章标题
     * @apiSuccess (Success 200){String} author 文章作者
     * @apiSuccess (Success 200){String} source 文章来源
     * @apiSuccess (Success 200){String} publishTime 文章发布时间
     * @apiSuccess (Success 200){String} categoryName 所属分类
     * @apiSuccess (Success 200){String} publishStatusName 发布状态
     */
    public CompletionStage<Result> listArticles(int page, String cateName, int size) {
        return CompletableFuture.supplyAsync(() -> {
            if (ValidationUtil.isEmpty(cateName)) return okCustomJson(CODE40001, "请输入分类名");
            ArticleCategory articleCategory = getArticleCategory(cateName);
            if (null == articleCategory) return okCustomJson(CODE40001, "该分类不存在");
            //缓存2分钟
            String key = ARTICLE_LIST_JSON_CACHE + cateName;
            Optional<String> jsonCache = redis.sync().get(key);
            if (jsonCache.isPresent()) {
                String result = jsonCache.get();
                if (!ValidationUtil.isEmpty(result)) return ok(Json.parse(result));
            }
            PagedList<Article> pagedList = Article.find.query()
                    .select("title,digest,headPic,views,favs,comments,shares,createdTime")
                    .where()
                    .eq("categoryId", articleCategory.getId())
                    .eq("status", Article.ARTICLE_STATUS_NORMAL)
                    .orderBy().desc("sort")
                    .orderBy().desc("id")
                    .setFirstRow((page - 1) * size)
                    .setMaxRows(size)
                    .findPagedList();
            List<Article> list = pagedList.getList();
            ObjectNode node = Json.newObject();
            node.put(CODE, CODE200);
            node.set("list", Json.toJson(list));
            redis.set(key, node.toString(), 60);
            return ok(node);
        });
    }

    private ArticleCategory getArticleCategory(String cateName) {
        try {
            cateName = URLEncoder.encode(cateName, "UTF-8");
            String key = cacheUtils.getArticleCategoryKey(cateName);
            Optional<ArticleCategory> optional = redis.sync().get(key);
            if (optional.isPresent()) {
                ArticleCategory category = optional.get();
                if (null != category) return category;
            }
            List<ArticleCategory> list = ArticleCategory.find.all();
            list.parallelStream().forEach((each) -> {
                try {
                    String name = URLEncoder.encode(each.getName(), "UTF-8");
                    redis.set(cacheUtils.getArticleCategoryKey(name), each);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            });
            optional = redis.sync().get(key);
            if (optional.isPresent()) {
                ArticleCategory category = optional.get();
                if (null != category) return category;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getArticleCategoryName(int categoryId) {
        String key = cacheUtils.getArticleCategoryKey(categoryId);
        Optional<ArticleCategory> optional = redis.sync().get(key);
        if (optional.isPresent()) {
            ArticleCategory category = optional.get();
            if (null != category) return category.getName();
        }
        ArticleCategory category = ArticleCategory.find.byId(categoryId);
        if (null != category) {
            redis.set(key, category, 24 * 3600);
            return category.getName();
        }
        return "";
    }


    /**
     * @api {GET} /v1/p/articles/:articleId/ 04文章详情
     * @apiName getArticle
     * @apiGroup Article
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Success 200){long} id 文章id
     * @apiSuccess (Success 200){String} title 文章标题
     * @apiSuccess (Success 200){String} author 文章作者
     * @apiSuccess (Success 200){String} digest 文章摘要
     * @apiSuccess (Success 200){String} headPic 文图
     * @apiSuccess (Success 200){String} content 内容
     * @apiSuccess (Success 200){long} categoryId 分类ID
     * @apiSuccess (Success 200){String} categoryName 分类名字
     * @apiSuccess (Success 200){long} views 阅读数
     * @apiSuccess (Success 200){long} favs 收藏数
     * @apiSuccess (Success 200){long} comments 评论数
     * @apiSuccess (Success 200){long} shares 分享数
     * @apiSuccess (Success 200){boolean} isFav 是否已收藏 ，true收藏 ，false未收藏
     * @apiSuccess (Success 200){String} createTime 时间
     */
    public CompletionStage<Result> getArticle(Http.Request request, long articleId) {
        long uid = businessUtils.getUserIdByAuthToken2(request);
        return CompletableFuture.supplyAsync(() -> {
            if (articleId < 1) return okCustomJson(CODE40001, "该篇文章不存在");
            String key = ARTICLE_JSON_CACHE + articleId;
            updateArticleViews(articleId);
            if (uid < 1) {
                Optional<String> jsonCache = redis.sync().get(key);
                if (jsonCache.isPresent()) {
                    String result = jsonCache.get();
                    if (!ValidationUtil.isEmpty(result)) {
                        ObjectNode node = (ObjectNode) Json.parse(result);
                        long views = node.findPath("views").asLong();
                        node.put("views", views + 1);
                        redis.set(key, node.toString(), 60);
                        return ok(node);
                    }
                }
            }
            Article article = Article.find.query().where()
                    .eq("id", articleId)
                    .eq("status", Article.ARTICLE_STATUS_NORMAL)
                    .setMaxRows(1).findOne();
            if (null == article) return okCustomJson(CODE40001, "该篇文章不存在");
            boolean isFav = false;
            if (uid > 0) {
                ArticleFav articleFav = ArticleFav.find.query().where().eq("uid", uid)
                        .eq("articleId", articleId).setMaxRows(1).findOne();
                if (null != articleFav) {
                    if (articleFav.isEnable()) isFav = true;
                }
            }
            article.isFav = isFav;
            if (article.getCategoryId() > 0) {
                article.setCategoryName(getArticleCategoryName(article.getCategoryId()));
            }

            ObjectNode node = (ObjectNode) Json.toJson(article);
            node.put(CODE, CODE200);
            //缓存24小时
            if (uid < 1) redis.set(key, node.toString(), 60);
            return ok(node);
        });
    }

    private void updateArticleViews(long articleId) {
        CompletableFuture.runAsync(() -> {
            String sql = "UPDATE  `v1_article` AS `dest`, " +
                    "    ( SELECT * FROM `v1_article` " +
                    "        WHERE id = :articleId  limit 1" +
                    "    ) AS `src`" +
                    "   SET" +
                    "       `dest`.`views` = `src`.`views`+ 1 " +
                    "   WHERE  dest.id = :articleId " +
                    ";";
            SqlUpdate sqlUpdate = Ebean.createSqlUpdate(sql);
            sqlUpdate.setParameter("articleId", articleId);
            sqlUpdate.executeNow();
        });
    }

    /**
     * @api {GET} /v1/p/recommend_articles/ 05精选文章列表
     * @apiName listRecommendArticles
     * @apiGroup Article
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Success 200) {JsonObject} list 列表
     * @apiSuccess (Success 200){long} articleId 文章id
     * @apiSuccess (Success 200){String} title 文章标题
     * @apiSuccess (Success 200){String} author 文章作者
     * @apiSuccess (Success 200){String} source 文章来源
     * @apiSuccess (Success 200){String} publishTime 文章发布时间
     * @apiSuccess (Success 200){String} categoryName 所属分类
     * @apiSuccess (Success 200){String} publishStatusName 发布状态
     */
    public CompletionStage<Result> listRecommendArticles() {
        return CompletableFuture.supplyAsync(() -> {
            //缓存2分钟
            String key = ARTICLE_RECOMMEND_JSON_CACHE;
            Optional<String> jsonCache = redis.sync().get(key);
            if (jsonCache.isPresent()) {
                String result = jsonCache.get();
                if (!ValidationUtil.isEmpty(result)) return ok(Json.parse(result));
            }
            List<Article> list = Article.find.query()
                    .select("title,digest,headPic,views,favs,comments,shares,createdTime")
                    .where()
                    .eq("isRecommend", true)
                    .eq("status", Article.ARTICLE_STATUS_NORMAL)
                    .orderBy().desc("id")
                    .setMaxRows(BusinessConstant.PAGE_SIZE_10)
                    .findList();
            ObjectNode node = Json.newObject();
            node.put(CODE, CODE200);
            node.set("list", Json.toJson(list));
            redis.set(key, Json.stringify(node), 2 * 60);
            return ok(node);
        });
    }

    /**
     * @api {post} /v1/p/articles/fav/ 06收藏
     * @apiName fav
     * @apiGroup Article
     * @apiParam {boolean} enable true收藏 false取消收藏
     * @apiParam {long} articleId  文章ID
     * @apiSuccess (Success 200) {int} code 200成功修改
     */
    @BodyParser.Of(BodyParser.Json.class)
    public CompletionStage<Result> fav(Http.Request request) {
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (uid < 1) return unauth403();
            Member member = Member.find.byId(uid);
            if (null == member) return unauth403();
            JsonNode jsonNode = request.body().asJson();
            long articleId = jsonNode.findPath("articleId").asLong();
            boolean enable = jsonNode.findPath("enable").asBoolean();
            if (articleId < 1) return okCustomJson(CODE40001, "文章不存在");
            Article article = Article.find.byId(articleId);
            if (null == article) return okCustomJson(CODE40001, "文章不存在");
            if (article.getStatus() == Article.ARTICLE_STATUS_DISABLE) return okCustomJson(CODE40001, "文章已下架");
            ArticleFav articleFav = ArticleFav.find.query().where().eq("uid", uid)
                    .eq("articleId", articleId).setMaxRows(1).findOne();
            long currentTime = dateUtils.getCurrentTimeBySecond();
            if (null == articleFav) {
                articleFav = new ArticleFav();
                articleFav.setArticleId(articleId);
                articleFav.setTitle(article.getTitle());
                articleFav.setAuthor(article.getAuthor());
                articleFav.setHeadPic(article.getHeadPic());
                articleFav.setArticleTime(article.getCreatedTime());
                articleFav.setUid(uid);
                articleFav.setEnable(enable);
                articleFav.setCreatedTime(currentTime);
            } else {
                articleFav.setEnable(enable);
            }
            articleFav.save();
            member.save();
            article.save();

            String key = ARTICLE_JSON_CACHE + articleId;
            Optional<String> jsonCache = redis.sync().get(key);
            if (jsonCache.isPresent()) {
                String result = jsonCache.get();
                if (!ValidationUtil.isEmpty(result)) {
                    ObjectNode node = (ObjectNode) Json.parse(result);
                    long favs = node.findPath("favs").asLong();
                    node.put("favs", favs + 1);
                    redis.set(key, node.toString(), 7 * 24 * 3600);
                }
            }
            return okJSON200();
        });
    }

    /**
     * @api {GET} /v1/p/param_config/?key= 07获取参数配置
     * @apiName getParamConfig
     * @apiGroup Article
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Success 200){String} value 值
     * @apiSuccess (Success 200){String} note 中文备注
     */
    public CompletionStage<Result> getParamConfig(String key) {
        return CompletableFuture.supplyAsync(() -> {
            if (ValidationUtil.isEmpty(key)) return okCustomJson(CODE40001, "请输入KEY值");
            String jsonKey = cacheUtils.getParamConfigCacheKey() + key;
            Optional<String> jsonCache = redis.sync().get(jsonKey);
            if (jsonCache.isPresent()) {
                String result = jsonCache.get();
                if (!ValidationUtil.isEmpty(result)) return ok(Json.parse(result));
            }
            ParamConfig config = ParamConfig.find.query().where()
                    .eq("key", key)
                    .eq("source", SOURCE_FRONTEND)
                    .orderBy().asc("id")
                    .setMaxRows(1).findOne();
            if (null == config) return okCustomJson(CODE40001, "该参数不存在");
            String value = "";
            if (!ValidationUtil.isEmpty(config.value)) {
                if (config.isEncrypt) {
                    value = encodeUtils.decrypt(config.value);
                } else {
                    value = config.value;
                }
            }

            ObjectNode resultNode = Json.newObject();
            resultNode.put(CODE, CODE200);
            resultNode.put("value", value);
            redis.set(jsonKey, Json.stringify(resultNode), 30 * 24 * 60 * 60);
            return ok(resultNode);
        });
    }


}
