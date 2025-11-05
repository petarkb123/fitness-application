package project.fitnessapplication.notification.web;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import project.fitnessapplication.notification.model.PushSubscription;
import project.fitnessapplication.notification.repository.PushSubscriptionRepository;
import project.fitnessapplication.notification.web.dto.PushSubscriptionDto;
import project.fitnessapplication.user.model.User;
import project.fitnessapplication.user.service.UserService;
import project.fitnessapplication.notification.service.WebPushNotificationService;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationsController {

    private final PushSubscriptionRepository subscriptions;
    private final UserService users;
    @Value("${vapid.public-key:}")
    private String vapidPublicKey;
    private final WebPushNotificationService webPush;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(
            @AuthenticationPrincipal UserDetails me,
            @RequestBody PushSubscriptionDto dto
    ) {
        if (me == null) {
            return ResponseEntity.status(401).build();
        }
        User user = users.findByUsernameOrThrow(me.getUsername());

        PushSubscription sub = subscriptions.findByEndpoint(dto.getEndpoint())
                .orElseGet(PushSubscription::new);

        sub.setUser(user);
        sub.setEndpoint(dto.getEndpoint());
        sub.setP256dh(dto.getP256dh());
        sub.setAuth(dto.getAuth());
        sub.setUserAgent(dto.getUserAgent());
        sub.setTimezone(dto.getTimezone());
        sub.setActive(true);

        subscriptions.save(sub);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/subscribe")
    public ResponseEntity<?> unsubscribe(
            @AuthenticationPrincipal UserDetails me,
            @RequestBody PushSubscriptionDto dto
    ) {
        if (me == null) {
            return ResponseEntity.status(401).build();
        }
        subscriptions.findByEndpoint(dto.getEndpoint()).ifPresent(sub -> {
            sub.setActive(false);
            subscriptions.save(sub);
        });
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vapid-public-key")
    public ResponseEntity<String> getVapidPublicKey() {
        if (vapidPublicKey == null || vapidPublicKey.isBlank()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(vapidPublicKey);
    }

    @PostMapping("/test")
    public ResponseEntity<?> sendTest(@AuthenticationPrincipal UserDetails me) {
        if (me == null) return ResponseEntity.status(401).build();
        User user = users.findByUsernameOrThrow(me.getUsername());
        List<PushSubscription> subs = subscriptions.findAllByUserAndActiveIsTrue(user);
        if (subs.isEmpty()) return ResponseEntity.badRequest().body("No active subscriptions");

        scheduler.schedule(() ->
                webPush.sendToSubscriptions(subs, "Test notification", "This is a test from FitPower", "/dashboard"),
                10, TimeUnit.SECONDS);
        return ResponseEntity.accepted().body("Scheduled");
    }
}


