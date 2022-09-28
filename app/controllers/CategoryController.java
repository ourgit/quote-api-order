package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.ebean.ExpressionList;
import models.Category.Category;
import play.Logger;
import play.i18n.MessagesApi;
import play.libs.Json;
import play.mvc.Result;
import utils.ValidationUtil;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


public class CategoryController extends BaseController {

    Logger.ALogger logger = Logger.of(CategoryController.class);
    @Inject
    MessagesApi messagesApi;

    /**
     * @api {GET} /v1/user/categories/?filter&cateType= 01分类列表
     * @apiName listCategories
     * @apiGroup Category
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
            List<Category> resultList = convertListToTreeNode(list);
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            result.set("list", Json.toJson(resultList));
            if (ValidationUtil.isEmpty(filter)) redis.set(key, Json.stringify(result), 30 * 60);
            return ok(result);
        });
    }

    public List<Category> convertListToTreeNode(List<Category> categoryList) {
        List<Category> nodeList = new ArrayList<>();
        if (null == categoryList) return nodeList;
        for (Category node : categoryList) {
            if (null != node) {
                if (!ValidationUtil.isEmpty(node.path) && node.path.equalsIgnoreCase("/")) {
                    //根目录
                    nodeList.add(node);
                } else {
                    updateChildren(node, categoryList);
                }
            }

        }
        return nodeList;
    }


    private void updateChildren(Category category, List<Category> nodeList) {
        for (Category parentCategory : nodeList) {
            if (null != parentCategory && category.parentId == parentCategory.id) {
                if (parentCategory.children == null) parentCategory.children = new ArrayList<>();
                parentCategory.children.add(category);
                break;
            }
        }
    }
}
