package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.inject.Inject;
import play.libs.Json;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * BaseController 所有的业务逻辑必须都继续自该controller以便方便管理
 *
 * @link BaseSecurityController
 */
public class TestController extends Controller {
    @Inject
    MailerClient mailerClient;

    public Result index() {
        ObjectNode node = Json.newObject();
        node.put("code", 200);
        node.put("result", "success");
        return ok(node);
    }

    public CompletionStage<Result> sendMail() {
        return CompletableFuture.supplyAsync(() -> {
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
            try {
                mailerClient.send(email);
            } catch (Exception e) {
                System.out.println(e.fillInStackTrace());
            }

            ObjectNode node = Json.newObject();
            node.put("code", 200);
            node.put("result", "success");
            return ok(node);
        });
    }
}
