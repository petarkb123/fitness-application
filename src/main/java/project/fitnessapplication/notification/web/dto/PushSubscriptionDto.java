package project.fitnessapplication.notification.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PushSubscriptionDto {
    private String endpoint;
    private String p256dh;
    private String auth;
    private String userAgent;
    private String timezone; // IANA timezone name
}


