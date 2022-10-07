package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import constants.BusinessConstant;
import io.ebean.ExpressionList;
import io.ebean.PagedList;
import models.shop.Shop;
import models.shop.Showcase;
import models.user.Member;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Http;
import play.mvc.Result;
import utils.ValidationUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ShopController extends BaseSecurityController {

    /**
     * @api {GET} /v1/user/shop_list/?page=&name= 01店铺列表
     * @apiName listShop
     * @apiGroup Shop
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Success 200){Array} list
     * @apiSuccess (Success 200){long} id id
     * @apiSuccess (Success 200){String} name 名称
     * @apiSuccess (Success 200){String} shopLevel 店铺级别
     * @apiSuccess (Success 200){String} digest 简要
     * @apiSuccess (Success 200){Array} showcaseList 案例列表
     * @apiSuccess (Error 40001){int} code 40001 参数错误
     */
    public CompletionStage<Result> listShop(Http.Request request, int page, String name) {
        return CompletableFuture.supplyAsync(() -> {
            ExpressionList<Shop> expressionList = Shop.find.query().where().eq("status", Shop.STATUS_NORMAL);
            if (!ValidationUtil.isEmpty(name)) expressionList.icontains("name", name);
            PagedList<Shop> pagedList = expressionList.orderBy().desc("id")
                    .setFirstRow((page - 1) * BusinessConstant.PAGE_SIZE_10)
                    .setMaxRows(BusinessConstant.PAGE_SIZE_10)
                    .findPagedList();
            ObjectNode result = Json.newObject();
            List<Shop> list = pagedList.getList();
            Set<Long> set = new HashSet<>();
            list.parallelStream().forEach((shop) -> set.add(shop.id));
            List<Showcase> showcaseList = Showcase.find.query().where()
                    .eq("status", Showcase.STATUS_AUDIT)
                    .idIn(set)
                    .orderBy().desc("id")
                    .findList();
            Map<Long, List<Showcase>> map = new HashMap<>();
            showcaseList.stream().forEach((showcase) -> {
                List<Showcase> showcases = map.get(showcase.shopId);
                if (null == showcases) {
                    showcases = new ArrayList<>();
                    map.put(showcase.shopId, showcases);
                }
                showcases.add(showcase);
            });
            ArrayNode nodes = Json.newArray();
            list.forEach((shop) -> {
                ObjectNode node = Json.newObject();
                node.put("id", shop.id);
                node.put("name", shop.name);
                node.put("shopLevel", shop.shopLevel);
                node.put("digest", shop.digest);
                List<Showcase> showcases = map.get(shop.id);
                if (null != showcases) {
                    node.set("showcaseList", Json.toJson(showcaseList));
                }
                nodes.add(node);
            });
            result.put("pages", pagedList.getTotalPageCount());
            result.put(CODE, CODE200);
            result.set("list", nodes);
            return ok(result);
        });
    }

    /**
     * @api {GET} /v1/user/shop_list/:id/ 02店铺详情
     * @apiName getShop
     * @apiGroup Shop
     * @apiSuccess (Success 200){int} code 200
     * @apiSuccess (Success 200) {long} id id
     * @apiSuccess (Success 200){String} name 名称
     * @apiSuccess (Success 200){String} contactNumber 联系电话
     * @apiSuccess (Success 200){String} contactName 联系人
     * @apiSuccess (Success 200){String} contactAddress 联系地址
     * @apiSuccess (Success 200){String} licenseNumber 营业执照号
     * @apiSuccess (Success 200){String} licenseImg 营业执照图片
     * @apiSuccess (Success 200){String} businessTime 营业时间描述
     * @apiSuccess (Success 200){String} avatar 店铺头像
     * @apiSuccess (Success 200){String} images 店铺图片相册
     * @apiSuccess (Success 200){String} description 备注
     * @apiSuccess (Success 200){double} lat 纬度
     * @apiSuccess (Success 200){double} lon 经度
     * @apiSuccess (Success 200){long} updateTime 更新时间
     */
    public CompletionStage<Result> getShop(Http.Request request, long id) {
        return CompletableFuture.supplyAsync(() -> {
            Shop shop = Shop.find.byId(id);
            if (null == shop) return okCustomJson(CODE40001, "该商家不存在");
            ObjectNode result = (ObjectNode) Json.toJson(shop);
            result.put(CODE, CODE200);
            return ok(result);
        });
    }

    /**
     * @api {POST} /v1/user/shop/:id/ 03修改门店
     * @apiName updateShop
     * @apiGroup ADMIN-SHOP
     * @apiParam {String} name 名称
     * @apiParam {String} contactNumber 联系电话
     * @apiParam {String} contactName 联系人
     * @apiParam {String} contactAddress 联系地址
     * @apiParam {String} licenseNumber 营业执照号
     * @apiParam {String} licenseImg 营业执照图片
     * @apiParam {String} description 备注
     * @apiParam {double} lat 纬度
     * @apiParam {double} lon 经度
     * @apiSuccess (Success 200) {int} code 200 请求成功
     * @apiSuccess (Error 40001) {int} code 40001 参数错误
     */
    @BodyParser.Of(BodyParser.Json.class)
    @Transactional
    public CompletionStage<Result> updateShop(Http.Request request, long id) {
        JsonNode requestNode = request.body().asJson();
        return businessUtils.getUserIdByAuthToken(request).thenApplyAsync((uid) -> {
            if (id < 1) return okCustomJson(CODE40001, "参数错误");
            Shop shop = Shop.find.byId(id);
            if (null == shop) return okCustomJson(CODE40002, "该店铺不存在");
            if (uid < 1) return unauth403();
            if (shop.creatorId != uid) return okCustomJson(CODE40002, "该店铺不存在");
            Shop param = Json.fromJson(requestNode, Shop.class);
            if (null == param) return okCustomJson(CODE40001, "参数错误");
            if (!ValidationUtil.isEmpty(param.name)) {
                Shop nameShop = Shop.find.query().where()
                        .eq("name", param.name)
                        .ne("id", shop.id)
                        .setMaxRows(1).findOne();
                if (null != nameShop) return okCustomJson(CODE40001, "该店铺已存在");
                shop.setName(param.name);
            }
            if (param.status > 0) shop.setStatus(param.status);
            if (!ValidationUtil.isEmpty(param.name)) {
                shop.setName(param.name);
            }
            if (!ValidationUtil.isEmpty(param.rectLogo)) shop.setRectLogo(param.rectLogo);
            if (!ValidationUtil.isEmpty(param.contactNumber)) shop.setContactNumber(param.contactNumber);
            if (!ValidationUtil.isEmpty(param.contactName)) shop.setContactName(param.contactName);
            if (!ValidationUtil.isEmpty(param.contactAddress)) shop.setContactAddress(param.contactAddress);
            if (!ValidationUtil.isEmpty(param.licenseNumber)) shop.setLicenseNumber(param.licenseNumber);
            if (!ValidationUtil.isEmpty(param.licenseImg)) shop.setLicenseImg(param.licenseImg);
            if (!ValidationUtil.isEmpty(param.description)) shop.setDescription(param.description);
            if (!ValidationUtil.isEmpty(param.approveNote)) shop.setApproveNote(param.approveNote);
            if (requestNode.has("digest")) shop.setDigest(param.digest);
            if (requestNode.has("sort")) shop.setSort(param.sort);
            if (requestNode.has("envImages")) shop.setEnvImages(param.envImages);
            if (requestNode.has("bulletin")) shop.setBulletin(param.bulletin);

            if (requestNode.has("businessTime")) {
                shop.setBusinessTime(param.businessTime);
            }
            if (requestNode.has("avatar")) {
                shop.setAvatar(param.avatar);
            }
            if (requestNode.has("images")) {
                shop.setImages(param.images);
            }

            if (requestNode.has("averageConsumption")) {
                shop.setAverageConsumption(param.averageConsumption);
            }
            if (requestNode.has("orderCount")) {
                shop.setOrderCount(param.orderCount);
            }
            if (!ValidationUtil.isEmpty(param.log)) shop.setLog(param.log);
            if (requestNode.has("lat")) {
                double lat = param.lat;
                shop.setLat(lat);
            }
            if (requestNode.has("lon")) {
                double lon = param.lon;
                shop.setLon(lon);
            }
            if (requestNode.has("openTime")) {
                shop.setOpenTime(requestNode.findPath("openTime").asInt());
            }
            if (requestNode.has("closeTime")) {
                shop.setCloseTime(requestNode.findPath("closeTime").asInt());
            }
            if (requestNode.has("productCounts")) shop.setProductCounts(param.productCounts);
            if (requestNode.has("views")) shop.setViews(param.views);

            shop.setUpdateTime(dateUtils.getCurrentTimeBySecond());
            shop.setFilter("");
            shop.setFilter(Json.stringify(Json.toJson(shop)));
            shop.save();
            return okJSON200();
        });
    }

}
