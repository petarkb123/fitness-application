package project.fitnessapplication.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/onboarding")
public class OnboardingController {

    @GetMapping("/start")
    public String start(HttpSession session, Model model) {
        // Clear all onboarding data when starting fresh
        clearOnboardingData(session);
        return "onboarding/gender"; // Step 3: Choose gender
    }

    @GetMapping("/clear")
    public String clearOnboarding(HttpSession session) {
        clearOnboardingData(session);
        return "redirect:/";
    }

    private void clearOnboardingData(HttpSession session) {
        session.removeAttribute("onboarding_gender");
        session.removeAttribute("onboarding_workout_frequency");
        session.removeAttribute("onboarding_unit_system");
        session.removeAttribute("onboarding_height_cm");
        session.removeAttribute("onboarding_weight_kg");
        session.removeAttribute("onboarding_height_feet");
        session.removeAttribute("onboarding_height_inches");
        session.removeAttribute("onboarding_weight_lbs");
        session.removeAttribute("onboarding_birthdate");
        session.removeAttribute("onboarding_region");
        session.removeAttribute("onboarding_goal");
        session.removeAttribute("onboarding_desired_weight_kg");
        session.removeAttribute("onboarding_weight_change_speed_kg");
        session.removeAttribute("onboarding_skipped_weight_pages");
    }

    @GetMapping("/workout-frequency")
    public String workoutFrequencyForm(HttpSession session, Model model) {
        // Load saved data if exists
        String gender = (String) session.getAttribute("onboarding_gender");
        String frequency = (String) session.getAttribute("onboarding_workout_frequency");
        
        if (gender != null) model.addAttribute("selectedGender", gender);
        if (frequency != null) model.addAttribute("selectedFrequency", frequency);
        
        return "onboarding/workout-frequency";
    }

    @GetMapping("/height-weight")
    public String heightWeightForm(HttpSession session, Model model) {
        // Load saved data if exists
        String gender = (String) session.getAttribute("onboarding_gender");
        String frequency = (String) session.getAttribute("onboarding_workout_frequency");
        String unitSystem = (String) session.getAttribute("onboarding_unit_system");
        Integer heightCm = (Integer) session.getAttribute("onboarding_height_cm");
        Integer weightKg = (Integer) session.getAttribute("onboarding_weight_kg");
        Integer heightFeet = (Integer) session.getAttribute("onboarding_height_feet");
        Integer heightInches = (Integer) session.getAttribute("onboarding_height_inches");
        Integer weightLbs = (Integer) session.getAttribute("onboarding_weight_lbs");
        
        if (gender != null) model.addAttribute("selectedGender", gender);
        if (frequency != null) model.addAttribute("selectedFrequency", frequency);
        if (unitSystem != null) model.addAttribute("selectedUnitSystem", unitSystem);
        if (heightCm != null) model.addAttribute("heightCm", heightCm);
        if (weightKg != null) model.addAttribute("weightKg", weightKg);
        if (heightFeet != null) model.addAttribute("heightFeet", heightFeet);
        if (heightInches != null) model.addAttribute("heightInches", heightInches);
        if (weightLbs != null) model.addAttribute("weightLbs", weightLbs);
        
        return "onboarding/height-weight";
    }

    @GetMapping("/birthdate")
    public String birthdateForm(HttpSession session, Model model) {
        // Load saved data if exists
        String birthdate = (String) session.getAttribute("onboarding_birthdate");
        String region = (String) session.getAttribute("onboarding_region");
        if (birthdate != null) {
            model.addAttribute("birthdate", birthdate);
        }
        if (region != null) {
            model.addAttribute("region", region);
        }
        return "onboarding/birthdate";
    }

    @GetMapping("/goal")
    public String goalForm(HttpSession session, Model model) {
        // Load saved data if exists
        String goal = (String) session.getAttribute("onboarding_goal");
        if (goal != null) {
            model.addAttribute("selectedGoal", goal);
        }
        return "onboarding/goal";
    }

    @GetMapping("/desired-weight")
    public String desiredWeightForm(HttpSession session, Model model) {
        // Load saved data if exists
        String goal = (String) session.getAttribute("onboarding_goal");
        String unitSystem = (String) session.getAttribute("onboarding_unit_system");
        Integer desiredWeightKg = (Integer) session.getAttribute("onboarding_desired_weight_kg");
        
        model.addAttribute("goal", goal);
        model.addAttribute("unitSystem", unitSystem != null ? unitSystem : "metric");
        if (desiredWeightKg != null) {
            model.addAttribute("desiredWeightKg", desiredWeightKg);
            // Compute imperial value if unit system is imperial
            if ("imperial".equals(unitSystem)) {
                model.addAttribute("desiredWeightLbs", (int) Math.round(desiredWeightKg * 2.20462));
            }
        }
        return "onboarding/desired-weight";
    }

    @GetMapping("/weight-speed")
    public String weightSpeedForm(HttpSession session, Model model) {
        // Load saved data if exists
        String goal = (String) session.getAttribute("onboarding_goal");
        String unitSystem = (String) session.getAttribute("onboarding_unit_system");
        Double weightChangeSpeedKg = (Double) session.getAttribute("onboarding_weight_change_speed_kg");
        
        model.addAttribute("goal", goal);
        model.addAttribute("unitSystem", unitSystem != null ? unitSystem : "metric");
        if (weightChangeSpeedKg != null) {
            model.addAttribute("weightChangeSpeedKg", weightChangeSpeedKg);
            // Compute imperial value if unit system is imperial
            if ("imperial".equals(unitSystem)) {
                model.addAttribute("weightChangeSpeedLbs", Math.round(weightChangeSpeedKg * 2.20462 * 10.0) / 10.0);
            }
        }
        return "onboarding/weight-speed";
    }

    @PostMapping("/gender")
    public String saveGender(@RequestParam String gender, HttpSession session, Model model) {
        session.setAttribute("onboarding_gender", gender);
        model.addAttribute("gender", gender);
        return "onboarding/workout-frequency"; // Step 4
    }

    @PostMapping("/workout-frequency")
    public String saveWorkoutFrequency(@RequestParam String frequency, HttpSession session, Model model) {
        session.setAttribute("onboarding_workout_frequency", frequency);
        model.addAttribute("frequency", frequency);
        return "onboarding/height-weight"; // Step 5
    }

    @PostMapping("/height-weight")
    public String saveHeightWeight(
            @RequestParam(required = false) String unitSystem,
            @RequestParam(required = false) Integer feet,
            @RequestParam(required = false) Integer inches,
            @RequestParam(required = false) Integer cm,
            @RequestParam(required = false) Integer lbs,
            @RequestParam(required = false) Integer kg,
            HttpSession session,
            Model model) {
        
        // Save unit system
        session.setAttribute("onboarding_unit_system", unitSystem);
        
        // Convert to metric if imperial, preserving original values
        Integer heightCm = null;
        Integer weightKg = null;
        
        if ("imperial".equals(unitSystem)) {
            // Imperial: convert feet/inches to cm, lbs to kg
            if (feet != null && inches != null) {
                heightCm = (int) Math.round((feet * 30.48) + (inches * 2.54));
            }
            if (lbs != null) {
                weightKg = (int) Math.round(lbs * 0.453592);
            }
        } else {
            // Metric: use cm and kg directly
            heightCm = cm;
            weightKg = kg;
        }
        
        // Save all values (both converted and original)
        if (heightCm != null) session.setAttribute("onboarding_height_cm", heightCm);
        if (weightKg != null) session.setAttribute("onboarding_weight_kg", weightKg);
        if (feet != null) session.setAttribute("onboarding_height_feet", feet);
        if (inches != null) session.setAttribute("onboarding_height_inches", inches);
        if (lbs != null) session.setAttribute("onboarding_weight_lbs", lbs);
        
        model.addAttribute("heightCm", heightCm);
        model.addAttribute("weightKg", weightKg);
        
        return "onboarding/birthdate"; // Step 6
    }

    @PostMapping("/birthdate")
    public String saveBirthdate(
            @RequestParam String month,
            @RequestParam String day, 
            @RequestParam String year,
            @RequestParam String timezone,
            HttpSession session, 
            Model model) {
        
        // Construct birthdate in ISO format (YYYY-MM-DD)
        String birthdate = year + "-" + month + "-" + day;
        
        session.setAttribute("onboarding_birthdate", birthdate);
        // Validate and save timezone
        String validTimezone = "UTC";
        if (timezone != null && !timezone.trim().isEmpty()) {
            try {
                java.time.ZoneId.of(timezone); // Validate
                validTimezone = timezone.trim();
            } catch (Exception e) {
                // Invalid timezone, default to UTC
                validTimezone = "UTC";
            }
        }
        session.setAttribute("onboarding_timezone", validTimezone);
        model.addAttribute("birthdate", birthdate);
        model.addAttribute("timezone", validTimezone);
        return "onboarding/goal"; // Step 7
    }

    @PostMapping("/goal")
    public String saveGoal(@RequestParam String goal, HttpSession session, Model model) {
        session.setAttribute("onboarding_goal", goal);
        model.addAttribute("goal", goal);
        
        // Step 8: If Maintain Weight, skip to thank you page
        if ("MAINTAIN_WEIGHT".equalsIgnoreCase(goal)) {
            session.setAttribute("onboarding_skipped_weight_pages", true);
            return "onboarding/thank-you"; // Step 11
        } else {
            session.removeAttribute("onboarding_skipped_weight_pages");
        }
        
        return "onboarding/desired-weight"; // Step 9
    }

    @PostMapping("/desired-weight")
    public String saveDesiredWeight(
            @RequestParam(required = false) Integer desiredWeightKg,
            @RequestParam(required = false) Integer desiredWeightLbs,
            HttpSession session, Model model) {
        String unitSystem = (String) session.getAttribute("onboarding_unit_system");
        
        // Convert to kg for storage
        Integer weightKg;
        if ("imperial".equals(unitSystem) && desiredWeightLbs != null) {
            // Imperial: convert lbs to kg
            weightKg = (int) Math.round(desiredWeightLbs / 2.20462);
        } else if (desiredWeightKg != null) {
            // Metric: use kg directly
            weightKg = desiredWeightKg;
        } else {
            weightKg = null;
        }
        
        session.setAttribute("onboarding_desired_weight_kg", weightKg);
        model.addAttribute("desiredWeightKg", weightKg);
        
        String goal = (String) session.getAttribute("onboarding_goal");
        model.addAttribute("goal", goal);
        model.addAttribute("unitSystem", unitSystem != null ? unitSystem : "metric");
        
        return "onboarding/weight-speed"; // Step 10
    }

    @PostMapping("/weight-speed")
    public String saveWeightSpeed(
            @RequestParam(required = false) Double weightChangeSpeedKg,
            @RequestParam(required = false) Double weightChangeSpeedLbs,
            HttpSession session, Model model) {
        String unitSystem = (String) session.getAttribute("onboarding_unit_system");
        
        // Convert to kg/week for storage
        Double speedKg;
        if ("imperial".equals(unitSystem) && weightChangeSpeedLbs != null) {
            // Imperial: convert lb/week to kg/week
            speedKg = Math.round(weightChangeSpeedLbs / 2.20462 * 10.0) / 10.0;
        } else if (weightChangeSpeedKg != null) {
            // Metric: use kg/week directly
            speedKg = weightChangeSpeedKg;
        } else {
            speedKg = null;
        }
        
        session.setAttribute("onboarding_weight_change_speed_kg", speedKg);
        model.addAttribute("weightChangeSpeedKg", speedKg);
        
        String goal = (String) session.getAttribute("onboarding_goal");
        model.addAttribute("goal", goal);
        model.addAttribute("unitSystem", unitSystem != null ? unitSystem : "metric");
        
        return "onboarding/thank-you"; // Step 11
    }

    @GetMapping("/register")
    public String registerForm(HttpSession session, Model model) {
        // Pre-fill form with onboarding data
        model.addAttribute("gender", session.getAttribute("onboarding_gender"));
        model.addAttribute("workoutFrequency", session.getAttribute("onboarding_workout_frequency"));
        model.addAttribute("heightCm", session.getAttribute("onboarding_height_cm"));
        model.addAttribute("weightKg", session.getAttribute("onboarding_weight_kg"));
        model.addAttribute("birthdate", session.getAttribute("onboarding_birthdate"));
        model.addAttribute("goal", session.getAttribute("onboarding_goal"));
        model.addAttribute("desiredWeightKg", session.getAttribute("onboarding_desired_weight_kg"));
        model.addAttribute("weightChangeSpeedKg", session.getAttribute("onboarding_weight_change_speed_kg"));
        
        return "onboarding/register"; // Step 12: Custom register page with onboarding data
    }

    @GetMapping("/thank-you")
    public String thankYou(HttpSession session, Model model) {
        Boolean skippedWeightPages = (Boolean) session.getAttribute("onboarding_skipped_weight_pages");
        if (skippedWeightPages != null) {
            model.addAttribute("skippedWeightPages", skippedWeightPages);
        }
        return "onboarding/thank-you"; // Step 11
    }

    // Direct access methods for back navigation that load saved data
    @GetMapping("/gender")
    public String genderDirect(HttpSession session, Model model) {
        // Load saved data if exists
        String gender = (String) session.getAttribute("onboarding_gender");
        if (gender != null) {
            model.addAttribute("selectedGender", gender);
        }
        return "onboarding/gender";
    }

    @GetMapping("/workout-frequency-direct")
    public String workoutFrequencyDirect(HttpSession session, Model model) {
        // Load saved data if exists
        String gender = (String) session.getAttribute("onboarding_gender");
        String frequency = (String) session.getAttribute("onboarding_workout_frequency");
        
        if (gender != null) model.addAttribute("selectedGender", gender);
        if (frequency != null) model.addAttribute("selectedFrequency", frequency);
        
        return "onboarding/workout-frequency";
    }

    @GetMapping("/height-weight-direct")
    public String heightWeightDirect(HttpSession session, Model model) {
        // Load saved data if exists
        String gender = (String) session.getAttribute("onboarding_gender");
        String frequency = (String) session.getAttribute("onboarding_workout_frequency");
        String unitSystem = (String) session.getAttribute("onboarding_unit_system");
        Integer heightCm = (Integer) session.getAttribute("onboarding_height_cm");
        Integer weightKg = (Integer) session.getAttribute("onboarding_weight_kg");
        Integer heightFeet = (Integer) session.getAttribute("onboarding_height_feet");
        Integer heightInches = (Integer) session.getAttribute("onboarding_height_inches");
        Integer weightLbs = (Integer) session.getAttribute("onboarding_weight_lbs");
        
        if (gender != null) model.addAttribute("selectedGender", gender);
        if (frequency != null) model.addAttribute("selectedFrequency", frequency);
        if (unitSystem != null) model.addAttribute("selectedUnitSystem", unitSystem);
        if (heightCm != null) model.addAttribute("heightCm", heightCm);
        if (weightKg != null) model.addAttribute("weightKg", weightKg);
        if (heightFeet != null) model.addAttribute("heightFeet", heightFeet);
        if (heightInches != null) model.addAttribute("heightInches", heightInches);
        if (weightLbs != null) model.addAttribute("weightLbs", weightLbs);
        
        return "onboarding/height-weight";
    }

    @GetMapping("/birthdate-direct")
    public String birthdateDirect(HttpSession session, Model model) {
        // Load saved data if exists
        String birthdate = (String) session.getAttribute("onboarding_birthdate");
        if (birthdate != null) {
            model.addAttribute("birthdate", birthdate);
        }
        return "onboarding/birthdate";
    }

    @GetMapping("/goal-direct")
    public String goalDirect(HttpSession session, Model model) {
        // Load saved data if exists
        String goal = (String) session.getAttribute("onboarding_goal");
        if (goal != null) {
            model.addAttribute("selectedGoal", goal);
        }
        return "onboarding/goal";
    }

    @GetMapping("/desired-weight-direct")
    public String desiredWeightDirect(HttpSession session, Model model) {
        // Check if maintain weight was selected - if so, skip to thank you
        String goal = (String) session.getAttribute("onboarding_goal");
        if ("MAINTAIN_WEIGHT".equalsIgnoreCase(goal)) {
            return "redirect:/onboarding/thank-you";
        }
        
        // Load saved data if exists
        String unitSystem = (String) session.getAttribute("onboarding_unit_system");
        Integer desiredWeightKg = (Integer) session.getAttribute("onboarding_desired_weight_kg");
        
        model.addAttribute("goal", goal);
        model.addAttribute("unitSystem", unitSystem != null ? unitSystem : "metric");
        if (desiredWeightKg != null) {
            model.addAttribute("desiredWeightKg", desiredWeightKg);
            if ("imperial".equals(unitSystem)) {
                model.addAttribute("desiredWeightLbs", (int) Math.round(desiredWeightKg * 2.20462));
            }
        }
        return "onboarding/desired-weight";
    }

    @GetMapping("/weight-speed-direct")
    public String weightSpeedDirect(HttpSession session, Model model) {
        // Check if maintain weight was selected - if so, skip to thank you
        String goal = (String) session.getAttribute("onboarding_goal");
        if ("MAINTAIN_WEIGHT".equalsIgnoreCase(goal)) {
            return "redirect:/onboarding/thank-you";
        }
        
        // Load saved data if exists
        String unitSystem = (String) session.getAttribute("onboarding_unit_system");
        Double weightChangeSpeedKg = (Double) session.getAttribute("onboarding_weight_change_speed_kg");
        
        model.addAttribute("goal", goal);
        model.addAttribute("unitSystem", unitSystem != null ? unitSystem : "metric");
        if (weightChangeSpeedKg != null) {
            model.addAttribute("weightChangeSpeedKg", weightChangeSpeedKg);
            if ("imperial".equals(unitSystem)) {
                model.addAttribute("weightChangeSpeedLbs", Math.round(weightChangeSpeedKg * 2.20462 * 10.0) / 10.0);
            }
        }
        return "onboarding/weight-speed";
    }
}
