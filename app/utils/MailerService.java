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


    public void sendVcode(String accountName, int bizType) {
        final String generatedVerificationCode = bizUtils.generateVerificationCode();
        String key = cacheUtils.getLastVerifyCodeKey(accountName, bizType);
        redis.set(key, generatedVerificationCode, 10 * 60);
        String html = VCODE_TEMPLATE.replace("USER_EMAIL", accountName)
                .replace("VCODE", generatedVerificationCode);
        System.out.println("sendVcode:" + accountName);
        Email email = new Email()
                .setSubject("Renoseeker验证码")
                .setFrom("FROM <157579114@qq.com>")
                .addTo("TO <" + accountName + ">")
                .setBodyText("Renoseeker 注册验证码")
//                .setBodyHtml("<html><body><p>An <b>html</b> 注册验证码: " + generatedVerificationCode + "</p></body></html>");
                .setBodyHtml(html);
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

    String VCODE_TEMPLATE = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>Document</title>\n" +
            "    <style>\n" +
            "        *{\n" +
            "            margin: 0;\n" +
            "            padding: 0;\n" +
            "        }\n" +
            "        .Box1{\n" +
            "            width: 600px;\n" +
            "            height: 500px;\n" +
            "            box-shadow: 1px 2px 5px gray;\n" +
            "            margin-left: 10px;\n" +
            "        }\n" +
            "        .Box2{\n" +
            "            margin-left: 10px;\n" +
            "        }\n" +
            "        .box1{\n" +
            "            background-color: #009999;\n" +
            "            width: 600PX;\n" +
            "            height: 120px;\n" +
            "            position: relative;\n" +
            "        }\n" +
            "        .box2{\n" +
            "            height: 100%;\n" +
            "            margin-left: 20px;\n" +
            "            margin-top: 40px;\n" +
            "        }\n" +
            "        h2{\n" +
            "            color: white;\n" +
            "            font-size: 20px; \n" +
            "            position:absolute; \n" +
            "            left: 30px;\n" +
            "            bottom: 30px;\n" +
            "        }\n" +
            "        .num{\n" +
            "            font-size: 20px;\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "        \n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <img src=\"/pic/1.jpg\">\n" +
            "    <div class=\"Box1\">\n" +
            "        <div class=\"box1\">\n" +
            "            <h2>Renoseeker验证码</h2>\n" +
            "        </div>\n" +
            "        <div class=\"box2\">\n" +
            "            <p>您好，USER_EMAIL</p>\n" +
            "            <br>\n" +
            "            <p>您的Renoseeker验证码为:</p>\n" +
            "            <br>\n" +
            "            <p class=\"num\"><strong>VCODE</strong></p>\n" +
            "            <br>\n" +
            "            <p>如果您并未请求此验证码,则可能是他人正在尝试访问以下Renoseeker帐<br>号：USER_EMAIL\n" +
            "            <strong>请勿将此验证码转发给或提供任何人，并及时检查您的账户安全。</strong></p>\n" +
            "            <br>\n" +
            "            <p>此致</p>\n" +
            "            <br>\n" +
            "            <p>Renoseeker团队敬上</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "    <br>\n" +
            "    <div class=\"Box2\">\n" +
            "        <p>此电子邮件地址无法接收回复。如需了解更多信息，请访问<a href=\"https://www.renoseeker.com\">Renoseeker</a></p>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";
}
