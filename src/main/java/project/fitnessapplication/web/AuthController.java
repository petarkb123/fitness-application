package project.fitnessapplication.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import project.fitnessapplication.user.service.UserService;
import project.fitnessapplication.user.form.RegisterForm;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
@Validated
public class AuthController {

    private final UserService users;
    public AuthController(UserService users){ this.users = users; }

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
                    birthdate, goal, desiredWeightKg, weightChangeSpeedKg
                );
                
                // Clear onboarding session data
                session.removeAttribute("onboarding_gender");
                session.removeAttribute("onboarding_workout_frequency");
                session.removeAttribute("onboarding_height_cm");
                session.removeAttribute("onboarding_weight_kg");
                session.removeAttribute("onboarding_birthdate");
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
}
