package project.fitnessapplication.notification.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import project.fitnessapplication.notification.model.PushSubscription;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Utils;
import org.jose4j.lang.JoseException;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebPushNotificationService {

    @Value("${vapid.public-key:}")
    private String vapidPublicKey;

    @Value("${vapid.private-key:}")
    private String vapidPrivateKey;

    @Value("${vapid.subject:mailto:office@franjohnsonhouse.com}")
    private String subject;

    public boolean isConfigured() {
        return vapidPublicKey != null && !vapidPublicKey.isBlank()
                && vapidPrivateKey != null && !vapidPrivateKey.isBlank();
    }

    public void sendToSubscriptions(List<PushSubscription> subs, String title, String body, String url) {
        if (!isConfigured()) {
            log.warn("WebPush VAPID not configured; skipping push. Title: {}", title);
            return;
        }
        try {
            PushService pushService = new PushService();
            pushService.setPublicKey(Utils.loadPublicKey(vapidPublicKey));
            pushService.setPrivateKey(Utils.loadPrivateKey(vapidPrivateKey));
            pushService.setSubject(subject);

            String payloadJson = String.format("{\"title\":%s,\"body\":%s,\"url\":%s}",
                    json(title), json(body), json(url));

            for (PushSubscription s : subs) {
                if (!s.isActive()) continue;
                Notification notification = new Notification(
                        s.getEndpoint(), s.getP256dh(), s.getAuth(), payloadJson.getBytes()
                );
                try {
                    pushService.send(notification);
                    log.info("[PUSH SENT] {}", s.getEndpoint());
                } catch (IOException | JoseException e) {
                    log.warn("[PUSH FAIL] {} - {}", s.getEndpoint(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("WebPush init failed: {}", e.getMessage(), e);
        }
    }

    private static String json(String s) {
        if (s == null) return "null";
        return '"' + s.replace("\\", "\\\\").replace("\"", "\\\"") + '"';
    }
}


