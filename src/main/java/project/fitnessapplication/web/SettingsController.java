package project.fitnessapplication.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import project.fitnessapplication.user.service.UserService;
import project.fitnessapplication.user.service.UserSettingsService;
import java.util.UUID;

@Controller
@RequestMapping("/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final UserSettingsService settings;
    private final UserService users;
    private final project.fitnessapplication.user.service.AccountMaintenanceService accountMaintenance;

    @GetMapping({"", "/"})
    public String settings(@AuthenticationPrincipal UserDetails me, Model model) {
        var u = settings.requireByUsername(me.getUsername());
        model.addAttribute("username", u.getUsername());
        model.addAttribute("email", u.getEmail());
        model.addAttribute("firstName", u.getFirstName());
        model.addAttribute("lastName", u.getLastName());
        model.addAttribute("avatarPath", u.getProfilePicture());
        model.addAttribute("navAvatar", u.getProfilePicture());
        model.addAttribute("heightCm", u.getHeightCm());
        model.addAttribute("weightKg", u.getWeightKg());
        model.addAttribute("unitSystem", u.getUnitSystem());
        model.addAttribute("heightFeet", u.getHeightFeet());
        model.addAttribute("heightInches", u.getHeightInches());
        model.addAttribute("weightLbs", u.getWeightLbs());
        model.addAttribute("goal", u.getGoal());
        model.addAttribute("desiredWeightKg", u.getDesiredWeightKg());
        model.addAttribute("weightChangeSpeedKg", u.getWeightChangeSpeedKg());
        // Compute imperial values for display if unit system is imperial
        if ("imperial".equals(u.getUnitSystem())) {
            Integer desiredWeightLbs = u.getDesiredWeightKg() != null 
                ? (int) Math.round(u.getDesiredWeightKg() * 2.20462) : null;
            Double weightChangeSpeedLbs = u.getWeightChangeSpeedKg() != null 
                ? Math.round(u.getWeightChangeSpeedKg() * 2.20462 * 10.0) / 10.0 : null;
            model.addAttribute("desiredWeightLbs", desiredWeightLbs);
            model.addAttribute("weightChangeSpeedLbs", weightChangeSpeedLbs);
        } else {
            model.addAttribute("desiredWeightLbs", null);
            model.addAttribute("weightChangeSpeedLbs", null);
        }
        model.addAttribute("workoutFrequency", u.getWorkoutFrequency());
        model.addAttribute("timezone", u.getTimezone() != null ? u.getTimezone() : "UTC");
        return "settings";
    }

    @PostMapping("/avatar-url")
    public String setAvatarUrl(@AuthenticationPrincipal UserDetails me,
                               @RequestParam("avatarUrl") String avatarUrl,
                               RedirectAttributes ra) {
        try {
            UUID id = users.findByUsernameOrThrow(me.getUsername()).getId();
            settings.setAvatarUrl(id, avatarUrl);
            ra.addFlashAttribute("successMessage", "Profile picture updated.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/avatar/delete")
    public String deleteAvatar(@AuthenticationPrincipal UserDetails me,
                               RedirectAttributes ra) {
        UUID id = users.findByUsernameOrThrow(me.getUsername()).getId();
        settings.removeAvatar(id);
        ra.addFlashAttribute("successMessage", "Profile picture removed.");
        return "redirect:/settings";
    }


    @PostMapping("/profile")
    public String updateUsername(@AuthenticationPrincipal UserDetails me,
                                 @RequestParam("username") String newUsername,
                                 RedirectAttributes ra) {
        try {
            UUID id = users.findByUsernameOrThrow(me.getUsername()).getId();
            users.changeUsername(id, newUsername);

            var u = users.findByIdOrThrow(id);
            var principal = org.springframework.security.core.userdetails.User
                    .withUsername(u.getUsername())
                    .password(u.getPasswordHash())
                    .roles(u.getRole().name())
                    .disabled(!u.isActive())
                    .build();

            var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    principal, principal.getPassword(), principal.getAuthorities());
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

            ra.addFlashAttribute("successMessage", "Profile saved.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/settings";
    }



    @PostMapping("/password")
    public String changePassword(@AuthenticationPrincipal UserDetails me,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes ra) {
        try {
            UUID id = users.findByUsernameOrThrow(me.getUsername()).getId();
            settings.changePassword(id, currentPassword, newPassword, confirmPassword);
            ra.addFlashAttribute("successMessage", "Password changed.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/update-first-name")
    public String updateFirstName(@AuthenticationPrincipal UserDetails me,
                                   @RequestParam("firstName") String firstName,
                                   RedirectAttributes ra) {
        try {
            UUID id = users.findByUsernameOrThrow(me.getUsername()).getId();
            settings.updateFirstName(id, firstName);
            ra.addFlashAttribute("successMessage", "First name updated.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/update-last-name")
    public String updateLastName(@AuthenticationPrincipal UserDetails me,
                                 @RequestParam("lastName") String lastName,
                                 RedirectAttributes ra) {
        try {
            UUID id = users.findByUsernameOrThrow(me.getUsername()).getId();
            settings.updateLastName(id, lastName);
            ra.addFlashAttribute("successMessage", "Last name updated.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/update-email")
    public String updateEmail(@AuthenticationPrincipal UserDetails me,
                              @RequestParam("email") String email,
                              RedirectAttributes ra) {
        try {
            UUID id = users.findByUsernameOrThrow(me.getUsername()).getId();
            settings.updateEmail(id, email);
            ra.addFlashAttribute("successMessage", "Email updated.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/update-height")
    public String updateHeight(@AuthenticationPrincipal UserDetails me,
                               @RequestParam("heightCm") Integer heightCm,
                               RedirectAttributes ra) {
        try {
            UUID id = users.findByUsernameOrThrow(me.getUsername()).getId();
            settings.updateHeight(id, heightCm);
            ra.addFlashAttribute("successMessage", "Height updated.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/update-height-imperial")
    public String updateHeightImperial(@AuthenticationPrincipal UserDetails me,
                                       @RequestParam("heightFeet") Integer feet,
                                       @RequestParam("heightInches") Integer inches,
                                       RedirectAttributes ra) {
        try {
            UUID id = users.findByUsernameOrThrow(me.getUsername()).getId();
            settings.updateHeightImperial(id, feet, inches);
            ra.addFlashAttribute("successMessage", "Height updated.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/update-weight")
    public String updateWeight(@AuthenticationPrincipal UserDetails me,
                               @RequestParam("weightKg") Integer weightKg,
                               RedirectAttributes ra) {
        try {
            UUID id = users.findByUsernameOrThrow(me.getUsername()).getId();
            settings.updateWeight(id, weightKg);
            ra.addFlashAttribute("successMessage", "Weight updated.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/update-weight-lbs")
    public String updateWeightLbs(@AuthenticationPrincipal UserDetails me,
                                  @RequestParam("weightLbs") Integer weightLbs,
                                  RedirectAttributes ra) {
        try {
            UUID id = users.findByUsernameOrThrow(me.getUsername()).getId();
            settings.updateWeightLbs(id, weightLbs);
            ra.addFlashAttribute("successMessage", "Weight updated.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/update-unit-system")
    public String updateUnitSystem(@AuthenticationPrincipal UserDetails me,
                                   @RequestParam("unitSystem") String unitSystem,
                                   RedirectAttributes ra) {
        try {
            UUID id = users.findByUsernameOrThrow(me.getUsername()).getId();
            settings.updateUnitSystem(id, unitSystem);
            ra.addFlashAttribute("successMessage", "Units updated.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/update-goal")
    public String updateGoal(@AuthenticationPrincipal UserDetails me,
                             @RequestParam("goal") String goal,
                             RedirectAttributes ra) {
        try {
            UUID id = users.findByUsernameOrThrow(me.getUsername()).getId();
            settings.updateGoal(id, goal);
            ra.addFlashAttribute("successMessage", "Goal updated.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/update-desired-weight")
    public String updateDesiredWeight(@AuthenticationPrincipal UserDetails me,
                                      @RequestParam("desiredWeightKg") Integer desiredWeightKg,
                                      RedirectAttributes ra) {
        try {
            UUID id = users.findByUsernameOrThrow(me.getUsername()).getId();
            settings.updateDesiredWeight(id, desiredWeightKg);
            ra.addFlashAttribute("successMessage", "Desired weight updated.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/update-weight-speed")
    public String updateWeightSpeed(@AuthenticationPrincipal UserDetails me,
                                    @RequestParam("weightChangeSpeedKg") Double weightChangeSpeedKg,
                                    RedirectAttributes ra) {
        try {
            UUID id = users.findByUsernameOrThrow(me.getUsername()).getId();
            settings.updateWeightChangeSpeed(id, weightChangeSpeedKg);
            ra.addFlashAttribute("successMessage", "Weight change speed updated.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/update-desired-weight-lbs")
    public String updateDesiredWeightLbs(@AuthenticationPrincipal UserDetails me,
                                        @RequestParam("desiredWeightLbs") Integer desiredWeightLbs,
                                        RedirectAttributes ra) {
        try {
            UUID id = users.findByUsernameOrThrow(me.getUsername()).getId();
            // Convert lbs to kg for storage
            Integer desiredWeightKg = (int) Math.round(desiredWeightLbs / 2.20462);
            settings.updateDesiredWeight(id, desiredWeightKg);
            ra.addFlashAttribute("successMessage", "Desired weight updated.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/update-weight-speed-lbs")
    public String updateWeightSpeedLbs(@AuthenticationPrincipal UserDetails me,
                                       @RequestParam("weightChangeSpeedLbs") Double weightChangeSpeedLbs,
                                       RedirectAttributes ra) {
        try {
            UUID id = users.findByUsernameOrThrow(me.getUsername()).getId();
            // Convert lbs/week to kg/week for storage
            Double weightChangeSpeedKg = Math.round(weightChangeSpeedLbs / 2.20462 * 10.0) / 10.0;
            settings.updateWeightChangeSpeed(id, weightChangeSpeedKg);
            ra.addFlashAttribute("successMessage", "Weight change speed updated.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/update-workout-frequency")
    public String updateWorkoutFrequency(@AuthenticationPrincipal UserDetails me,
                                         @RequestParam("workoutFrequency") String workoutFrequency,
                                         RedirectAttributes ra) {
        try {
            UUID id = users.findByUsernameOrThrow(me.getUsername()).getId();
            settings.updateWorkoutFrequency(id, workoutFrequency);
            ra.addFlashAttribute("successMessage", "Workout frequency updated.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/update-timezone")
    public String updateTimezone(@AuthenticationPrincipal UserDetails me,
                                  @RequestParam("timezone") String timezone,
                                  RedirectAttributes ra) {
        try {
            UUID id = users.findByUsernameOrThrow(me.getUsername()).getId();
            settings.updateTimezone(id, timezone);
            ra.addFlashAttribute("successMessage", "Timezone updated.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/update-user-timezone")
    public String updateUserTimezone(@AuthenticationPrincipal UserDetails me,
                                     @RequestParam("timezone") String timezone,
                                     RedirectAttributes ra) {
        try {
            UUID id = users.findByUsernameOrThrow(me.getUsername()).getId();
            settings.updateUserTimezone(id, timezone);
            ra.addFlashAttribute("successMessage", "Timezone updated.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/settings";
    }

    @PostMapping("/reset-account")
    public String resetAccount(@AuthenticationPrincipal UserDetails me,
                               @RequestParam("confirmText") String confirmText,
                               RedirectAttributes ra) {
        if (!"reset account".equalsIgnoreCase(confirmText == null ? "" : confirmText.trim())) {
            ra.addFlashAttribute("errorMessage", "You must type 'reset account' to confirm.");
            return "redirect:/settings";
        }
        UUID id = users.findByUsernameOrThrow(me.getUsername()).getId();
        accountMaintenance.resetAccount(id);
        ra.addFlashAttribute("successMessage", "Your account data has been reset.");
        return "redirect:/settings";
    }

    @PostMapping("/delete-account")
    public String deleteAccount(@AuthenticationPrincipal UserDetails me,
                                @RequestParam("confirmText") String confirmText,
                                RedirectAttributes ra) {
        if (!"delete account".equalsIgnoreCase(confirmText == null ? "" : confirmText.trim())) {
            ra.addFlashAttribute("errorMessage", "You must type 'delete account' to confirm.");
            return "redirect:/settings";
        }
        UUID id = users.findByUsernameOrThrow(me.getUsername()).getId();
        accountMaintenance.deleteAccount(id);
        ra.addFlashAttribute("successMessage", "Your account has been deleted.");
        return "redirect:/login";
    }
}
