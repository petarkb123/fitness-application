package project.fitnessapplication.notification.util;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class VapidKeyGenerator {

    @Value("${vapid.public-key:}")
    private String vapidPublicKey;

    @Value("${vapid.private-key:}")
    private String vapidPrivateKey;

    // Generate keys once at startup only if both are missing
    @EventListener(ApplicationReadyEvent.class)
    public void maybeGenerateKeys() {
        if (isBlank(vapidPublicKey) && isBlank(vapidPrivateKey)) {
            try {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
                kpg.initialize(new ECGenParameterSpec("secp256r1")); // P-256
                KeyPair kp = kpg.generateKeyPair();

                String publicKeyB64Url = encodePublicKeyUncompressed((ECPublicKey) kp.getPublic());
                String privateKeyB64Url = encodePrivateKeyScalar((ECPrivateKey) kp.getPrivate());

                log.warn("\n=== VAPID KEYS GENERATED (copy into application.properties) ===\n" +
                        "vapid.public-key={}\n" +
                        "vapid.private-key={}\n" +
                        "===========================================================\n",
                        publicKeyB64Url, privateKeyB64Url);
                log.warn("VAPID keys were generated because none were configured. Copy them into your config and restart. Do NOT regenerate on every startup.");
            } catch (Exception e) {
                log.error("Failed to generate VAPID keys", e);
            }
        }
    }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }

    // Per RFC 8291, the VAPID public key is the uncompressed EC point (0x04 || X || Y), base64url without padding
    private static String encodePublicKeyUncompressed(ECPublicKey pub) {
        byte[] x = unsignedFixed(pub.getW().getAffineX());
        byte[] y = unsignedFixed(pub.getW().getAffineY());
        byte[] uncompressed = new byte[1 + x.length + y.length];
        uncompressed[0] = 0x04;
        System.arraycopy(x, 0, uncompressed, 1, x.length);
        System.arraycopy(y, 0, uncompressed, 1 + x.length, y.length);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(uncompressed);
    }

    // Private key is the 32-byte scalar, base64url without padding
    private static String encodePrivateKeyScalar(ECPrivateKey priv) {
        byte[] d = unsignedFixed(priv.getS());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(d);
    }

    // Ensure 32-byte unsigned big-endian for P-256
    private static byte[] unsignedFixed(BigInteger v) {
        byte[] raw = v.toByteArray(); // may contain sign byte
        // Strip leading zero sign byte if present
        if (raw.length > 32) {
            // If 33 bytes with leading 0x00, drop it
            int start = raw.length - 32;
            byte[] out = new byte[32];
            System.arraycopy(raw, start, out, 0, 32);
            return out;
        }
        if (raw.length == 32) return raw;
        byte[] out = new byte[32];
        System.arraycopy(raw, 0, out, 32 - raw.length, raw.length);
        return out;
    }
}


