package project.fitnessapplication.web;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import project.fitnessapplication.user.service.PasswordResetService;
import project.fitnessapplication.user.service.UserService;
import project.fitnessapplication.user.form.RegisterForm;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@Validated
@RequiredArgsConstructor
public class AuthController {

    private final UserService users;
    private final PasswordResetService passwordResetService;

    @GetMapping("/login")    public String loginPage(){ return "login"; }
    @GetMapping("/register") public String registerForm(){ return "register"; }

    @PostMapping("/register")
    public String register(@ModelAttribute RegisterForm f, Model model, HttpSession session) {
        try {
            // Check if onboarding data exists
            String gender = (String) session.getAttribute("onboarding_gender");
            if (gender != null) {
                // Register with onboarding data
                String workoutFrequency = (String) session.getAttribute("onboarding_workout_frequency");
                Integer heightCm = (Integer) session.getAttribute("onboarding_height_cm");
                Integer weightKg = (Integer) session.getAttribute("onboarding_weight_kg");
                String birthdateStr = (String) session.getAttribute("onboarding_birthdate");
                String region = (String) session.getAttribute("onboarding_region");
                String goal = (String) session.getAttribute("onboarding_goal");
                Integer desiredWeightKg = (Integer) session.getAttribute("onboarding_desired_weight_kg");
                Double weightChangeSpeedKg = (Double) session.getAttribute("onboarding_weight_change_speed_kg");
                
                LocalDate birthdate = null;
                if (birthdateStr != null) {
                    birthdate = LocalDate.parse(birthdateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                }
                
                users.registerWithOnboarding(
                    f.getUsername(), f.getPassword(), f.getEmail(),
                    f.getFirstName(), f.getLastName(),
                    gender, workoutFrequency, heightCm, weightKg,
                    birthdate, region, goal, desiredWeightKg, weightChangeSpeedKg
                );
                
                // Clear onboarding session data
                session.removeAttribute("onboarding_gender");
                session.removeAttribute("onboarding_workout_frequency");
                session.removeAttribute("onboarding_height_cm");
                session.removeAttribute("onboarding_weight_kg");
                session.removeAttribute("onboarding_birthdate");
                session.removeAttribute("onboarding_region");
                session.removeAttribute("onboarding_goal");
                session.removeAttribute("onboarding_desired_weight_kg");
                session.removeAttribute("onboarding_weight_change_speed_kg");
            } else {
                // Regular registration without onboarding
                users.register(f.getUsername(), f.getPassword(), f.getEmail(), f.getFirstName(), f.getLastName());
            }
            
            model.addAttribute("msg", "Registration successful. Please log in.");
            return "login";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            // Return onboarding/register if onboarding data exists, otherwise regular register
            String gender = (String) session.getAttribute("onboarding_gender");
            return (gender != null) ? "onboarding/register" : "register";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String requestPasswordReset(@RequestParam String email, Model model, RedirectAttributes redirectAttributes) {
        try {
            passwordResetService.requestPasswordReset(email);
            // Always show success message (don't reveal if email exists)
            redirectAttributes.addFlashAttribute("success", 
                "If an account with that email exists, a password reset link has been sent.");
            return "redirect:/forgot-password";
        } catch (Exception e) {
            model.addAttribute("error", "An error occurred. Please try again later.");
            return "forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(required = false) String token, Model model) {
        if (token == null) {
            model.addAttribute("error", "No reset token provided. Please request a new password reset.");
            return "reset-password";
        }
        
        boolean isValid = passwordResetService.isTokenValid(token);
        if (!isValid) {
            model.addAttribute("error", "Invalid or expired reset token. Please request a new password reset link.");
            return "reset-password";
        }
        
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam String token,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("token", token);
            return "reset-password";
        }

        if (password.length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters long.");
            model.addAttribute("token", token);
            return "reset-password";
        }

        boolean success = passwordResetService.resetPassword(token, password);
        
        if (success) {
            redirectAttributes.addFlashAttribute("success", 
                "Password reset successfully! You can now log in with your new password.");
            return "redirect:/login";
        } else {
            model.addAttribute("error", "Invalid or expired reset token.");
            return "reset-password";
        }
    }
}
