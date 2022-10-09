package models.shop;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.ebean.Finder;
import io.ebean.Model;
import myannotation.EscapeHtmlSerializer;
import play.data.validation.Constraints;

import javax.persistence.*;

@Entity
@Table(name = "v1_shop")
public class Shop extends Model {

    public static final int SELF_RUN = 1;
    public static final int THIRD_RUN = 2;
    public static final int STATUS_NORMAL = 1;
    public static final int STATUS_LOCK = 2;
    public static final int STATUS_DELETED = 6;
    public static final int STATUS_NOT_SHOW = 7;

    @Column(name = "id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;

    @Column(name = "status")
    public int status;

    @Column(name = "run_type")
    public int runType;

    @Column(name = "shop_level")
    public int shopLevel;

    @Column(name = "name")
    @Constraints.Required
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String name = "";

    @Column(name = "digest")
    @Constraints.Required
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String digest = "";

    @Column(name = "contact_number")
    @Constraints.Required
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String contactNumber = "";//联系电话

    @Column(name = "contact_name")
    @Constraints.Required
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String contactName = "";//联系人

    @Column(name = "contact_address")
    @Constraints.Required
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String contactAddress = "";//联系地址

    @Column(name = "license_number")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String licenseNumber = "";//营业执照号

    @Column(name = "license_img")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String licenseImg = "";//营业执照图片

    @Column(name = "description")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String description = "";//备注

    @Column(name = "approve_note")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String approveNote = "";//审核说明

    @Column(name = "log")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String log = "";//审请记录

    @Column(name = "creator_id")
    public long creatorId;//创建者uid

    @Column(name = "approver_id")
    public long approverId;//审核人员uid

    @Column(name = "lat")
    public double lat;//latitude

    @Column(name = "lon")
    public double lon;//longtitude

    @Column(name = "open_time")
    public int openTime;

    @Column(name = "close_time")
    public int closeTime;

    @Column(name = "update_time")
    public long updateTime;//更新时间

    @Column(name = "create_time")
    public long createTime;//创建时间

    @Column(name = "filter")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String filter = "";//审请记录

    @Column(name = "business_time")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String businessTime = "";

    @Column(name = "avatar")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String avatar = "";

    @Column(name = "rect_logo")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String rectLogo = "";

    @Column(name = "product_counts")
    public long productCounts;

    @Column(name = "views")
    public long views;

    @Column(name = "tags")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String tags = "";

    @Column(name = "images")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String images = "";

    @Column(name = "discount_str")
    public String discountStr = "";

    @Column(name = "branches")
    public String branches = "";

    @Column(name = "discount")
    public int discount;

    @Column(name = "bid_discount")
    public int bidDiscount;

    @Column(name = "average_consumption")
    public int averageConsumption;

    @Column(name = "order_count")
    public long orderCount;

    @Column(name = "sort")
    public int sort;

    @Column(name = "score")
    public int score;

    @Column(name = "place_top")
    public boolean placeTop;

    @Column(name = "bulletin")
    public String bulletin = "";

    @Column(name = "env_images")
    public String envImages = "";

    @Column(name = "new_shop_category_id")
    public String newShopCategoryId = "";

    @Column(name = "product_description_score")
    public int productDescriptionScore;

    @Column(name = "service_score")
    public int serviceScore;

    @Column(name = "mail_score")
    public int mailScore;

    @Column(name = "total_money")
    public long totalMoney;

    @Column(name = "total_comment_count")
    public long totalCommentCount;

    @Column(name = "apply_categories")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String applyCategories = "";

    @Column(name = "apply_categories_name")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String applyCategoriesName = "";

    @Column(name = "qualifications")
    @JsonDeserialize(using = EscapeHtmlSerializer.class)
    public String qualifications = "";

    @Transient
    public boolean isFav;

    @Transient
    public String customerName = "";

    @Transient
    public String openId = "";

    public static Finder<Long, Shop> find = new Finder<>(Shop.class);

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getRunType() {
        return runType;
    }

    public void setRunType(int runType) {
        this.runType = runType;
    }

    public int getShopLevel() {
        return shopLevel;
    }

    public void setShopLevel(int shopLevel) {
        this.shopLevel = shopLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactAddress() {
        return contactAddress;
    }

    public void setContactAddress(String contactAddress) {
        this.contactAddress = contactAddress;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getLicenseImg() {
        return licenseImg;
    }

    public void setLicenseImg(String licenseImg) {
        this.licenseImg = licenseImg;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApproveNote() {
        return approveNote;
    }

    public void setApproveNote(String approveNote) {
        this.approveNote = approveNote;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(long creatorId) {
        this.creatorId = creatorId;
    }

    public long getApproverId() {
        return approverId;
    }

    public void setApproverId(long approverId) {
        this.approverId = approverId;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public int getOpenTime() {
        return openTime;
    }

    public void setOpenTime(int openTime) {
        this.openTime = openTime;
    }

    public int getCloseTime() {
        return closeTime;
    }

    public void setCloseTime(int closeTime) {
        this.closeTime = closeTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getBusinessTime() {
        return businessTime;
    }

    public void setBusinessTime(String businessTime) {
        this.businessTime = businessTime;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRectLogo() {
        return rectLogo;
    }

    public void setRectLogo(String rectLogo) {
        this.rectLogo = rectLogo;
    }

    public long getProductCounts() {
        return productCounts;
    }

    public void setProductCounts(long productCounts) {
        this.productCounts = productCounts;
    }

    public long getViews() {
        return views;
    }

    public void setViews(long views) {
        this.views = views;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getDiscountStr() {
        return discountStr;
    }

    public void setDiscountStr(String discountStr) {
        this.discountStr = discountStr;
    }

    public String getBranches() {
        return branches;
    }

    public void setBranches(String branches) {
        this.branches = branches;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public int getBidDiscount() {
        return bidDiscount;
    }

    public void setBidDiscount(int bidDiscount) {
        this.bidDiscount = bidDiscount;
    }

    public int getAverageConsumption() {
        return averageConsumption;
    }

    public void setAverageConsumption(int averageConsumption) {
        this.averageConsumption = averageConsumption;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(long orderCount) {
        this.orderCount = orderCount;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public boolean isPlaceTop() {
        return placeTop;
    }

    public void setPlaceTop(boolean placeTop) {
        this.placeTop = placeTop;
    }

    public String getBulletin() {
        return bulletin;
    }

    public void setBulletin(String bulletin) {
        this.bulletin = bulletin;
    }

    public String getEnvImages() {
        return envImages;
    }

    public void setEnvImages(String envImages) {
        this.envImages = envImages;
    }

    public String getNewShopCategoryId() {
        return newShopCategoryId;
    }

    public void setNewShopCategoryId(String newShopCategoryId) {
        this.newShopCategoryId = newShopCategoryId;
    }

    public int getProductDescriptionScore() {
        return productDescriptionScore;
    }

    public void setProductDescriptionScore(int productDescriptionScore) {
        this.productDescriptionScore = productDescriptionScore;
    }

    public int getServiceScore() {
        return serviceScore;
    }

    public void setServiceScore(int serviceScore) {
        this.serviceScore = serviceScore;
    }

    public int getMailScore() {
        return mailScore;
    }

    public void setMailScore(int mailScore) {
        this.mailScore = mailScore;
    }

    public long getTotalMoney() {
        return totalMoney;
    }

    public void setTotalMoney(long totalMoney) {
        this.totalMoney = totalMoney;
    }

    public long getTotalCommentCount() {
        return totalCommentCount;
    }

    public void setTotalCommentCount(long totalCommentCount) {
        this.totalCommentCount = totalCommentCount;
    }

    public String getApplyCategories() {
        return applyCategories;
    }

    public void setApplyCategories(String applyCategories) {
        this.applyCategories = applyCategories;
    }

    public String getApplyCategoriesName() {
        return applyCategoriesName;
    }

    public void setApplyCategoriesName(String applyCategoriesName) {
        this.applyCategoriesName = applyCategoriesName;
    }

    public String getQualifications() {
        return qualifications;
    }

    public void setQualifications(String qualifications) {
        this.qualifications = qualifications;
    }

    public boolean isFav() {
        return isFav;
    }

    public void setFav(boolean fav) {
        isFav = fav;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
