package utils;

import com.google.inject.Inject;
import org.apache.commons.mail.EmailAttachment;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;

import java.io.File;

public class MailerService {
    @Inject
    MailerClient mailerClient;

    public void sendEmail() {
        String cid = "1234";
        Email email = new Email()
                .setSubject("Simple email")
                .setFrom("Mister FROM <from@email.com>")
                .addTo("Miss TO <to@email.com>")
                // adds attachment
                .addAttachment("attachment.pdf", new File("/some/path/attachment.pdf"))
                // adds inline attachment from byte array
                .addAttachment("data.txt", "data".getBytes(), "text/plain", "Simple data", EmailAttachment.INLINE)
                // adds cid attachment
                .addAttachment("image.jpg", new File("/some/path/image.jpg"), cid)
                // sends text, HTML or both...
                .setBodyText("A text message")
                .setBodyHtml("<html><body><p>An <b>html</b> message with cid <img src=\"cid:" + cid + "\"></p></body></html>");
        mailerClient.send(email);
    }
}
