package project.fitnessapplication.config;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public class TimezoneConfig {
    
    private static final Map<String, String> REGION_TO_TIMEZONE = new HashMap<>();
    
    static {
        // Using most representative regional timezones
        REGION_TO_TIMEZONE.put("Europe", "Europe/Sofia");             // EET/EEST (UTC+2 or +3) - Bulgaria
        REGION_TO_TIMEZONE.put("North America", "America/New_York"); // EST/EDT (UTC-5 or -4)
        REGION_TO_TIMEZONE.put("Asia", "Asia/Shanghai");           // CST (UTC+8) - most populated timezone in Asia
        REGION_TO_TIMEZONE.put("Africa", "Africa/Nairobi");         // EAT (UTC+3) - East Africa
        REGION_TO_TIMEZONE.put("Oceania", "Australia/Sydney");      // AEST/AEDT (UTC+10 or +11)
        REGION_TO_TIMEZONE.put("South America", "America/Sao_Paulo"); // BRT (UTC-3)
    }
    
    public static String getTimezoneForRegion(String region) {
        if (region == null) return "UTC";
        return REGION_TO_TIMEZONE.getOrDefault(region, "UTC");
    }
    
    public static ZoneId getZoneIdForRegion(String region) {
        return ZoneId.of(getTimezoneForRegion(region));
    }
    
    /**
     * Converts a LocalDateTime to the user's regional timezone.
     * The time stored is already in the correct format, we just need to consider the offset.
     */
    public static LocalDateTime convertToRegionalTime(LocalDateTime storedTime, String region) {
        if (storedTime == null) return null;
        
        // The time stored in the database is in the server's timezone
        // We need to convert it to show in the user's regional timezone
        ZoneId serverZone = ZoneId.systemDefault(); // Server's actual timezone
        ZoneId userZone = getZoneIdForRegion(region);
        
        // Convert from server timezone to user regional timezone
        return storedTime
                .atZone(serverZone)
                .withZoneSameInstant(userZone)
                .toLocalDateTime();
    }
    
    /**
     * Converts a LocalDateTime stored in UTC to the user's regional timezone.
     * Use this when the database stores timestamps in UTC.
     */
    public static LocalDateTime convertFromUtcToRegionalTime(LocalDateTime utcTime, String region) {
        if (utcTime == null) return null;
        
        ZoneId utcZone = ZoneId.of("UTC");
        ZoneId userZone = getZoneIdForRegion(region);
        
        // Convert from UTC to user regional timezone
        return utcTime
                .atZone(utcZone)
                .withZoneSameInstant(userZone)
                .toLocalDateTime();
    }
}

