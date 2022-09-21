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
public class AliyunUploadController {
    Logger.ALogger logger = Logger.of(AliyunUploadController.class);
    private static final String END_POINT = "https://oss-cn-hangzhou.aliyuncs.com";
    private static final String END_POINT2 = "https://oss-accelerate.aliyuncs.com";
    private static String bucketName = "qpub";
    private static String bucketName2 = "qp2";
    public static String IMG_URL_PREFIX = "https://" + bucketName + ".oss-cn-hangzhou.aliyuncs.com/";
    public static String IMG_URL_PREFIX2 = "https://" + bucketName2 + ".oss-cn-hangzhou.aliyuncs.com/";

    public String uploadToOss(ByteArrayInputStream buffer, String key, String alinYunAccessId, String aliyunSecretKey) {
        OSS client = new OSSClientBuilder().build(END_POINT2, alinYunAccessId, aliyunSecretKey);
        try {
            client.putObject(new PutObjectRequest(bucketName, key, buffer));
            client.setObjectAcl(bucketName, key, CannedAccessControlList.PublicRead);
            String url = IMG_URL_PREFIX + key;
            return url;
        } catch (OSSException oe) {
            logger.error("uploadToOss:" + oe.getMessage());
        } finally {
            client.shutdown();
        }
        return "";
    }
}
