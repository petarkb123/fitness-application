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

    @GetMapping({"", "/"})
    public String settings(@AuthenticationPrincipal UserDetails me, Model model) {
        var u = settings.requireByUsername(me.getUsername());
        model.addAttribute("username", u.getUsername());
        model.addAttribute("firstName", u.getFirstName());
        model.addAttribute("lastName", u.getLastName());
        model.addAttribute("avatarPath", u.getProfilePicture());
        model.addAttribute("navAvatar", u.getProfilePicture());
        model.addAttribute("heightCm", u.getHeightCm());
        model.addAttribute("weightKg", u.getWeightKg());
        model.addAttribute("goal", u.getGoal());
        model.addAttribute("desiredWeightKg", u.getDesiredWeightKg());
        model.addAttribute("weightChangeSpeedKg", u.getWeightChangeSpeedKg());
        model.addAttribute("workoutFrequency", u.getWorkoutFrequency());
        model.addAttribute("region", u.getRegion());
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
                                  @RequestParam("region") String region,
                                  RedirectAttributes ra) {
        try {
            UUID id = users.findByUsernameOrThrow(me.getUsername()).getId();
            settings.updateTimezone(id, region);
            ra.addFlashAttribute("successMessage", "Timezone updated.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/settings";
    }
}
