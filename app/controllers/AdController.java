package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import constants.BusinessConstant;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import models.ad.Ad;
import models.ad.AdOwner;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import utils.ValidationUtil;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class AdController extends BaseController {

    /**
     * @api {GET} /v1/user/ad_list/?pageType=&sourceType=  01广告列表
     * @apiName listAd
     * @apiGroup AD
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Success 200) {JsonArray} list 菜单列表
     * @apiSuccess (Success 200) {long} id id
     * @apiSuccess (Success 200)　{String} position 位置
     * @apiSuccess (Success 200)　{String} dimension　尺寸
     * @apiSuccess (Success 200)　{long} price　价格
     * @apiSuccess (Success 200)　{int} days　计价天数
     * @apiSuccess (Success 200)　{String} display 展示商家数
     * @apiSuccess (Success 200)　{long} updateTime 更新时间
     * @apiSuccess (Success 200)　{long} createTime 创建时间
     * @apiSuccess (Success 200)　{Array} adOwnerList 广告主列表
     */
    public CompletionStage<Result> listAd(Http.Request request, int sourceType, int pageType) {
        return CompletableFuture.supplyAsync(() -> {
            ObjectNode result = Json.newObject();
            result.put(CODE, CODE200);
            ExpressionList<Ad> expressionList = Ad.find.query().where().eq("status", Ad.STATUS_NORMAL);
            if (sourceType > 0) expressionList.eq("sourceType", sourceType);
            if (pageType > 0) expressionList.eq("pageType", pageType);
            List<Ad> list = expressionList
                    .orderBy().desc("sort")
                    .findList();
            list.parallelStream().forEach((ad) -> {
                List<AdOwner> adOwners = AdOwner.find.query().where()
                        .eq("adId", ad.id)
                        .orderBy().desc("sort")
                        .findList();
                ad.adOwnerList.addAll(adOwners);
            });
            result.set("list", Json.toJson(list));
            return ok(result);
        });
    }

}
