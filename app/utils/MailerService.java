package utils;

import com.google.inject.Inject;
import play.cache.NamedCache;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;


public class MailerService {
    @Inject
    MailerClient mailerClient;

    @Inject
    BizUtils bizUtils;

    @Inject
    CacheUtils cacheUtils;

    @javax.inject.Inject
    @NamedCache("redis")
    protected play.cache.redis.AsyncCacheApi redis;


    public void sendVcode(String accountName) {
        final String generatedVerificationCode = bizUtils.generateVerificationCode();
        String key = cacheUtils.getSMSLastVerifyCodeKey(accountName);
        redis.set(key, generatedVerificationCode, 10 * 60);
        Email email = new Email()
                .setSubject("Simple email")
                .setFrom("Mister FROM <ray.Renoseeker@gmail.com>")
                .addTo("Miss TO <157579114@qq.com>")
                .setBodyText("Renoseeker 注册验证码")
                .setBodyHtml("<html><body><p>An <b>html</b> message with cid <img src=\"cid:" + generatedVerificationCode + "\"></p></body></html>");
        mailerClient.send(email);
    }

    public void sendEmail() {
        String cid = "1234";
        Email email = new Email()
                .setSubject("Simple email")
                .setFrom("Mister FROM <ray.Renoseeker@gmail.com>")
                .addTo("Miss TO <157579114@qq.com>")
                // adds attachment
//                .addAttachment("attachment.pdf", new File("/some/path/attachment.pdf"))
//                // adds inline attachment from byte array
//                .addAttachment("data.txt", "data".getBytes(), "text/plain", "Simple data", EmailAttachment.INLINE)
//                // adds cid attachment
//                .addAttachment("image.jpg", new File("/some/path/image.jpg"), cid)
                // sends text, HTML or both...
                .setBodyText("A text message")
                .setBodyHtml("<html><body><p>An <b>html</b> message with cid <img src=\"cid:" + cid + "\"></p></body></html>");
        mailerClient.send(email);
    }
}
