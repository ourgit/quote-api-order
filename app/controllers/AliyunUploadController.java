package controllers;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.PutObjectRequest;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FilenameUtils;
import play.Logger;
import play.libs.Files;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * 文件上传
 */
public class AliyunUploadController extends BaseSecurityController {
    Logger.ALogger logger = Logger.of(AliyunUploadController.class);
    public static final String END_POINT = "https://oss-cn-hangzhou.aliyuncs.com";
    public static String bucketName = "renoseeker";
    public static String IMG_URL_PREFIX = "https://renoseeker.oss-cn-hangzhou.aliyuncs.com/";

    /**
     * @api {POST} /v1/user/upload/ 上传
     * @apiName upload
     * @apiGroup System
     * @apiSuccess (Success 200){int}code 200
     * @apiSuccess (Success 200){string} url 保存的地址
     * @apiSuccess (Error 40003) {int}code 40003 上传失败
     */
    public CompletionStage<Result> upload(Http.Request request) {
        Http.MultipartFormData<Files.TemporaryFile> body = request.body().asMultipartFormData();
        Http.MultipartFormData.FilePart<Files.TemporaryFile> uploadFile = body.getFile("file");
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (null == uploadFile) return okCustomJson(CODE500, "上传文件失败，请重试");
                String fileName = uploadFile.getFilename();
                long fileSize = uploadFile.getFileSize();
                if (fileSize > 6 * 1024 * 1024) return okCustomJson(CODE40005, "系统限制上传文件的大小最多6M");
                Files.TemporaryFile file = uploadFile.getRef();
                String key = UUID.randomUUID() + "." + FilenameUtils.getExtension(fileName);
                String destPath = "/tmp/upload/" + key;
                file.copyTo(Paths.get(destPath), true);
                File destFile = new File(destPath);
                return uploadToOss(destFile, key);
            } catch (Exception e) {
                return okCustomJson(40003, "reason:" + e.getMessage());
            }
        });

    }

    public Result uploadToOss(File file, String key) {
        OSS client = new OSSClientBuilder().build(END_POINT, businessUtils.getAlinYunAccessId(), businessUtils.getAliYunSecretKey());
        try {
            client.putObject(new PutObjectRequest(bucketName, key, file));
            client.setObjectAcl(bucketName, key, CannedAccessControlList.PublicRead);
            ObjectNode node = Json.newObject();
            node.put("code", 200);
            node.put("url", IMG_URL_PREFIX + key);
            return ok(node);
        } catch (OSSException oe) {
            logger.error("uploadToOss:" + oe.getMessage());
            return okCustomJson(CODE500, oe.getMessage());
        } finally {
            client.shutdown();
        }
    }



}
