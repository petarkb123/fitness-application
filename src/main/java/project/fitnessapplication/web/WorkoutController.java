package project.fitnessapplication.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import project.fitnessapplication.config.SystemDefault;
import project.fitnessapplication.template.service.TemplateService;
import project.fitnessapplication.user.service.UserService;
import project.fitnessapplication.workout.dto.FinishWorkoutRequest;
import project.fitnessapplication.workout.dto.ExerciseSetData;
import project.fitnessapplication.workout.dto.SetData;
import project.fitnessapplication.workout.dto.ExerciseBlock;
import project.fitnessapplication.workout.dto.WorkoutView;
import project.fitnessapplication.workout.model.WorkoutSet;
import project.fitnessapplication.exercise.dto.ExerciseSelect;
import project.fitnessapplication.template.dto.ExerciseOption;
import project.fitnessapplication.workout.service.WorkoutService;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;
    private final TemplateService templateService;
    private final UserService users;


    @GetMapping
    public String history(@AuthenticationPrincipal UserDetails me, Model model) {
        var u = users.findByUsernameOrThrow(me.getUsername());
        var userId = u.getId();

        model.addAttribute("navAvatar", u.getProfilePicture());
        model.addAttribute("username", u.getUsername());
        
        // Get sessions and convert times to user's regional timezone
        var sessions = workoutService.getRecentSessions(userId, 50);
        var regionalSessions = new ArrayList<>();
        for (var session : sessions) {
            var builder = session.toBuilder();
            if (session.getStartedAt() != null && u.getRegion() != null) {
                builder.startedAt(project.fitnessapplication.config.TimezoneConfig.convertToRegionalTime(
                    session.getStartedAt(), u.getRegion()));
            }
            if (session.getFinishedAt() != null && u.getRegion() != null) {
                builder.finishedAt(project.fitnessapplication.config.TimezoneConfig.convertToRegionalTime(
                    session.getFinishedAt(), u.getRegion()));
            }
            regionalSessions.add(builder.build());
        }
        model.addAttribute("sessions", regionalSessions);
        model.addAttribute("templates", templateService.list(userId));

        return "workouts/history";
    }

    @GetMapping("/session")
    public String session(@AuthenticationPrincipal UserDetails me,
                          @RequestParam(required = false) UUID sessionId,
                          @RequestParam(required = false) UUID templateId,
                          RedirectAttributes ra) {
        var u = users.findByUsernameOrThrow(me.getUsername());
        var userId = u.getId();

        var s = (sessionId != null)
                ? workoutService.findById(sessionId).orElseThrow()
                : workoutService.start(userId);

        if (!Objects.equals(s.getUserId(), userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        
        // Redirect to include sessionId in URL so refresh doesn't create new session
        ra.addAttribute("sessionId", s.getId());
        if (templateId != null) {
            ra.addAttribute("templateId", templateId);
        }
        return "redirect:/workouts/session";
    }
    
    @GetMapping(value = "/session", params = "sessionId")
    public String sessionWithId(@AuthenticationPrincipal UserDetails me,
                                @RequestParam UUID sessionId,
                                @RequestParam(required = false) UUID templateId,
                                @RequestParam(required = false, defaultValue = "false") boolean edit,
                                Model model) {
        var u = users.findByUsernameOrThrow(me.getUsername());
        var userId = u.getId();
        
        var s = workoutService.findById(sessionId).orElseThrow();
        
        if (!Objects.equals(s.getUserId(), userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        model.addAttribute("navAvatar", u.getProfilePicture());
        model.addAttribute("username", u.getUsername());
        model.addAttribute("sessionId", s.getId());
        model.addAttribute("isEditMode", edit);
        
        // Calculate elapsed seconds to avoid timezone issues (only for in-progress workouts)
        if (!edit && s.getStartedAt() != null) {
            long elapsedSeconds = java.time.Duration.between(s.getStartedAt(), java.time.LocalDateTime.now()).getSeconds();
            if (elapsedSeconds < 0) elapsedSeconds = 0; // Handle timezone edge cases
            model.addAttribute("elapsedSeconds", elapsedSeconds);
        } else {
            model.addAttribute("elapsedSeconds", 0);
        }
        
        // For edit mode, provide original workout duration and startedAt
        if (edit && s.getStartedAt() != null && s.getFinishedAt() != null) {
            long durationSeconds = java.time.Duration.between(s.getStartedAt(), s.getFinishedAt()).getSeconds();
            if (durationSeconds < 0) durationSeconds = 0;
            model.addAttribute("workoutDurationSeconds", durationSeconds);
            
            // Convert startedAt from UTC to regional time for proper JavaScript Date parsing
            var regionalStartedAt = (u.getRegion() != null) 
                ? project.fitnessapplication.config.TimezoneConfig.convertFromUtcToRegionalTime(s.getStartedAt(), u.getRegion())
                : s.getStartedAt();
            model.addAttribute("originalStartedAt", regionalStartedAt.toString());
        } else {
            model.addAttribute("workoutDurationSeconds", 0);
            model.addAttribute("originalStartedAt", "");
        }

        var options = workoutService.getAvailableExercises(userId)
                .stream()
                .map(ex -> new ExerciseSelect(
                        ex.getId(),
                        ex.getName(),
                        ex.getPrimaryMuscle(),
                        SystemDefault.SYSTEM_USER_ID.equals(ex.getOwnerUserId())
                ))
                .toList();

        model.addAttribute("exercises", options);
        model.addAttribute("existingSets", workoutService.getSessionSets(s.getId()));
        
        // Get last performance data for each exercise
        java.util.Map<String, project.fitnessapplication.workout.dto.LastPerformanceData> lastPerformanceData = new java.util.HashMap<>();
        for (var exercise : options) {
            lastPerformanceData.put(exercise.id().toString(), workoutService.getLastPerformance(exercise.id(), userId));
        }
        model.addAttribute("lastPerformanceData", lastPerformanceData);
        
        return "workouts/session";
    }

    @GetMapping("/{id}")
    public String details(@PathVariable UUID id,
                          @AuthenticationPrincipal UserDetails me,
                          Model model,
                          RedirectAttributes ra) {

        var u = users.findByUsernameOrThrow(me.getUsername());
        model.addAttribute("navAvatar", u.getProfilePicture());
        model.addAttribute("username", u.getUsername());

        var opt = workoutService.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Workout not found.");
            return "redirect:/workouts";
        }
        var session = opt.get();
        if (!Objects.equals(session.getUserId(), u.getId())) {
            ra.addFlashAttribute("error", "Workout not found.");
            return "redirect:/workouts";
        }

        var sets = workoutService.getSessionSets(id);
        
        var exIds = sets.stream()
                .map(WorkoutSet::getExerciseId)
                .distinct()
                .collect(Collectors.toList());
        var exercises = workoutService.getExercisesByIds(exIds);

        
        var blocks = new ArrayList<ExerciseBlock>();
        UUID lastExerciseId = null;
        List<WorkoutSet> currentBlock = new ArrayList<>();
        
        for (WorkoutSet set : sets) {
            if (lastExerciseId == null || !lastExerciseId.equals(set.getExerciseId())) {
                
                if (lastExerciseId != null && !currentBlock.isEmpty()) {
                    var ex = exercises.get(lastExerciseId);
                    if (ex != null) {
                        blocks.add(new ExerciseBlock(ex, new ArrayList<>(currentBlock)));
                    }
                    currentBlock.clear();
                }
                lastExerciseId = set.getExerciseId();
            }
            currentBlock.add(set);
        }
        
        
        if (lastExerciseId != null && !currentBlock.isEmpty()) {
            var ex = exercises.get(lastExerciseId);
            if (ex != null) {
                blocks.add(new ExerciseBlock(ex, currentBlock));
            }
        }

        var workoutView = new WorkoutView(session.getStartedAt(), session.getFinishedAt(), blocks, u.getRegion());
        model.addAttribute("workout", workoutView);
        model.addAttribute("totalSets", sets.size());
        model.addAttribute("sessionId", id); // Add sessionId for edit button

        return "workouts/details";
    }


    @RequestMapping(value = "/{id}/finish", method = {RequestMethod.GET, RequestMethod.POST})
    public String finishQuick(@PathVariable UUID id, @AuthenticationPrincipal UserDetails me) {
        var u = users.findByUsernameOrThrow(me.getUsername());
        workoutService.finishSession(id, u.getId());
        return "redirect:/workouts";
    }

    @PostMapping(value = "/finish", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Transactional
    public ResponseEntity<?> finishRich(@AuthenticationPrincipal UserDetails me,
                                        @RequestBody FinishWorkoutRequest body) {
        if (body == null || body.getSessionId() == null) {
            return ResponseEntity.badRequest().body("Missing sessionId");
        }

        var u = users.findByUsernameOrThrow(me.getUsername());
        
        
        List<ExerciseSetData> exerciseSets = null;
        if (body.getExercises() != null && !body.getExercises().isEmpty()) {
            exerciseSets = body.getExercises().stream()
                    .filter(ex -> ex != null && ex.getExerciseId() != null)
                    .map(ex -> new ExerciseSetData(
                            ex.getExerciseId(),
                            ex.getSets() == null ? List.of() : ex.getSets().stream()
                                    .filter(set -> set != null)
                                    .map(set -> new SetData(
                                            set.getWeight(),
                                            set.getReps(),
                                            set.getGroupId(),
                                            set.getGroupType(),
                                            set.getGroupOrder(),
                                            set.getSetNumber()
                                    ))
                                    .toList()
                    ))
                    .toList();
        }
        
        try {
            workoutService.finishSessionWithSets(body.getSessionId(), u.getId(), exerciseSets);
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found");
            }
            return ResponseEntity.badRequest().body(e.getReason());
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Transactional
    public ResponseEntity<?> updateWorkout(@PathVariable UUID id,
                                          @AuthenticationPrincipal UserDetails me,
                                          @RequestBody FinishWorkoutRequest body) {
        if (body == null || body.getSessionId() == null || !id.equals(body.getSessionId())) {
            return ResponseEntity.badRequest().body("Invalid sessionId");
        }

        var u = users.findByUsernameOrThrow(me.getUsername());
        
        List<ExerciseSetData> exerciseSets = null;
        if (body.getExercises() != null && !body.getExercises().isEmpty()) {
            exerciseSets = body.getExercises().stream()
                    .filter(ex -> ex != null && ex.getExerciseId() != null)
                    .map(ex -> new ExerciseSetData(
                            ex.getExerciseId(),
                            ex.getSets() == null ? List.of() : ex.getSets().stream()
                                    .filter(set -> set != null)
                                    .map(set -> new SetData(
                                            set.getWeight(),
                                            set.getReps(),
                                            set.getGroupId(),
                                            set.getGroupType(),
                                            set.getGroupOrder(),
                                            set.getSetNumber()
                                    ))
                                    .toList()
                    ))
                    .toList();
        }
        
        try {
            workoutService.updateSessionWithSets(body.getSessionId(), u.getId(), exerciseSets,
                                                 body.getStartedAt(), body.getFinishedAt());
            return ResponseEntity.ok().build();
        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Session not found");
            }
            return ResponseEntity.badRequest().body(e.getReason());
        }
    }

    @GetMapping("/templates/{templateId}/exercises")
    @ResponseBody
    public List<ExerciseOption> templateExercises(@PathVariable UUID templateId,
                                                  @AuthenticationPrincipal UserDetails me) {
        var u = users.findByUsernameOrThrow(me.getUsername());
        var ownerId = u.getId();

        templateService.findByIdAndOwner(templateId, ownerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var items = templateService.getTemplateItems(templateId);

        var exerciseIds = items.stream().map(it -> it.getExerciseId()).toList();
        var byId = templateService.getExercisesByIds(exerciseIds);

        var out = new ArrayList<ExerciseOption>(items.size());
        for (var it : items) {
            var ex = byId.get(it.getExerciseId());
            if (ex != null) {
                out.add(new ExerciseOption(
                        ex.getId(),
                        ex.getName(),
                        ex.getPrimaryMuscle(),
                        it.getTargetSets(),
                        it.getGroupId(),
                        it.getGroupType(),
                        it.getGroupOrder(),
                        it.getPosition(),
                        it.getSetNumber()
                ));
            }
        }
        return out;
    }

    @PostMapping("/{id}/delete")
    @Transactional
    public String deleteWorkout(@PathVariable UUID id,
                                @AuthenticationPrincipal UserDetails me,
                                RedirectAttributes ra) {
        var u = users.findByUsernameOrThrow(me.getUsername());

        try {
            workoutService.deleteSession(id, u.getId());
            ra.addFlashAttribute("success", "Workout deleted.");
        } catch (ResponseStatusException e) {
            ra.addFlashAttribute("error", "Workout not found.");
        }

        return "redirect:/workouts";
    }
}
