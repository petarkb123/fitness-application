package project.fitnessapplication.user.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@fitpower.com}")
    private String fromEmail;

    @Value("${app.url:http://localhost:3000}")
    private String appUrl;

    public void sendPasswordResetEmail(String to, String token) {
        String resetUrl = appUrl + "/reset-password?token=" + token;
        
        // For localhost development: Log to console
        log.info("=".repeat(80));
        log.info("📧 PASSWORD RESET EMAIL (Development Mode - Printed to Console)");
        log.info("=".repeat(80));
        log.info("To: {}", to);
        log.info("From: {}", fromEmail);
        log.info("Subject: Reset Your FitPower Password");
        log.info("=".repeat(80));
        log.info("🔗 RESET LINK (COPY THIS): {}", resetUrl);
        log.info("=".repeat(80));
        
        // Try to send actual email if mailSender is available
        try {
            if (mailSender != null) {
                String htmlBody = buildHtmlEmailBody(resetUrl);
                String plainBody = buildPlainTextEmailBody(resetUrl);
                
                log.debug("HTML Email Body Length: {}", htmlBody.length());
                log.debug("Plain Text Email Body Length: {}", plainBody.length());
                
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
                
                helper.setFrom(fromEmail);
                helper.setTo(to);
                helper.setSubject("Reset Your FitPower Password");
                helper.setText(plainBody, htmlBody);
                
                mailSender.send(message);
                log.info("✅ Email sent successfully via SMTP");
            }
        } catch (MessagingException e) {
            log.error("⚠️  Failed to send email via SMTP: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send email. Please try again later.", e);
        } catch (Exception e) {
            log.warn("⚠️  Could not send email via SMTP (this is OK for localhost testing): {}", e.getMessage());
            // Don't throw exception - console logging is sufficient for development
        }
    }

    private String buildPlainTextEmailBody(String resetUrl) {
        return String.format(
            "Hello,\n\n" +
            "You requested to reset your password for FitPower.\n\n" +
            "Click the link below to reset your password:\n" +
            "%s\n\n" +
            "This link will expire in 10 minutes.\n\n" +
            "If you didn't request this, please ignore this email.\n\n" +
            "Best regards,\n" +
            "The FitPower Team",
            resetUrl
        );
    }

    private String buildHtmlEmailBody(String resetUrl) {
        return String.format(
            "<!DOCTYPE html>" +
            "<html lang=\"en\">" +
            "<head>" +
            "    <meta charset=\"UTF-8\">" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
            "    <style type=\"text/css\">" +
            "        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif; background-color: #f5f5f5; line-height: 1.6; color: #333; margin: 0; padding: 0; }" +
            "        .email-container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }" +
            "        .email-header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px 20px; text-align: center; }" +
            "        .email-header h1 { color: #ffffff; font-size: 28px; font-weight: 700; margin: 0; }" +
            "        .email-body { padding: 30px 25px; }" +
            "        .email-body p { margin-bottom: 15px; font-size: 16px; color: #555; }" +
            "        .button-container { text-align: center; margin: 25px 0; }" +
            "        .reset-button { display: inline-block; padding: 14px 35px; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: #ffffff !important; text-decoration: none; border-radius: 8px; font-weight: 600; font-size: 16px; box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4); }" +
            "        .app-features { margin-top: 20px; padding: 20px; background: #f8f9ff; border-radius: 8px; border-left: 4px solid #667eea; }" +
            "        .app-features h3 { color: #667eea; font-size: 16px; margin-bottom: 12px; margin-top: 0; }" +
            "        .app-features ul { list-style: none; padding: 0; margin: 0; }" +
            "        .app-features li { margin: 8px 0; color: #555; font-size: 14px; padding-left: 20px; position: relative; }" +
            "        .app-features li:before { content: '✓'; position: absolute; left: 0; color: #667eea; font-weight: bold; }" +
            "        .app-link { text-align: center; margin-top: 15px; }" +
            "        .app-link a { color: #667eea; text-decoration: none; font-weight: 600; }" +
            "        .email-footer { padding: 20px; background-color: #f8f9fa; text-align: center; border-top: 1px solid #e0e0e0; }" +
            "        .email-footer p { margin: 3px 0; font-size: 13px; color: #888; }" +
            "        .expiry-note { margin-top: 20px; color: #999; font-size: 13px; }" +
            "    </style>" +
            "</head>" +
            "<body>" +
            "    <div class=\"email-container\">" +
            "        <div class=\"email-header\">" +
            "            <h1>FitPower</h1>" +
            "        </div>" +
            "        <div class=\"email-body\">" +
            "            <p>Hello,</p>" +
            "            <p>You requested to reset your password for FitPower. Click the button below to reset it:</p>" +
            "            <div class=\"button-container\">" +
            "                <a href=\"%s\" class=\"reset-button\" target=\"_self\">Reset Password</a>" +
            "            </div>" +
            "            <div class=\"app-features\">" +
            "                <h3>Get Back to Your Fitness Journey</h3>" +
            "                <ul>" +
            "                    <li>Track your workouts and progress</li>" +
            "                    <li>Create custom exercise templates</li>" +
            "                    <li>Monitor your strength gains over time</li>" +
            "                    <li>Access advanced statistics and analytics</li>" +
            "                </ul>" +
            "            </div>" +
            "            <div class=\"app-link\">" +
            "                <a href=\"%s\" target=\"_self\">Visit FitPower →</a>" +
            "            </div>" +
            "            <p class=\"expiry-note\">⏰ This link will expire in 10 minutes. If you didn't request this, please ignore this email.</p>" +
            "        </div>" +
            "        <div class=\"email-footer\">" +
            "            <p><strong>Best regards,</strong></p>" +
            "            <p>The FitPower Team</p>" +
            "        </div>" +
            "    </div>" +
            "</body>" +
            "</html>",
            resetUrl, appUrl
        );
    }
}

