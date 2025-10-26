package project.fitnessapplication.config;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public class TimezoneConfig {
    
    private static final Map<String, String> REGION_TO_TIMEZONE = new HashMap<>();
    
    static {
        // Using most representative regional timezones
        REGION_TO_TIMEZONE.put("Europe", "Europe/Paris");            // CET (UTC+1) - Central Europe
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
     * Converts a LocalDateTime (stored in server's timezone, which is UTC) to a LocalDateTime in the user's regional timezone.
     * The returned value represents what the time would be if you displayed it in the user's region.
     */
    public static LocalDateTime convertToRegionalTime(LocalDateTime utcTime, String region) {
        if (utcTime == null) return null;
        
        // The time stored in the database is in the server's timezone (UTC)
        ZoneId serverZone = ZoneId.of("UTC"); // Server is in UTC
        ZoneId userZone = getZoneIdForRegion(region);
        
        // Convert UTC time to user regional time
        return utcTime
                .atZone(serverZone)
                .withZoneSameInstant(userZone)
                .toLocalDateTime();
    }
}

