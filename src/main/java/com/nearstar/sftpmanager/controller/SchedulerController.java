package com.nearstar.sftpmanager.controller;

import com.nearstar.sftpmanager.model.entity.ScheduledTask;
import com.nearstar.sftpmanager.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class SchedulerController {

    private final SchedulerService schedulerService;

    @GetMapping("/tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<ScheduledTask>> getAllTasks() {
        List<ScheduledTask> tasks = schedulerService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/tasks/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ScheduledTask> getTaskById(@PathVariable Long id) {
        return schedulerService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/tasks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTask(@Valid @RequestBody ScheduledTask task) {
        try {
            ScheduledTask createdTask = schedulerService.createTask(task);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);

        } catch (Exception e) {
            log.error("Error creating scheduled task", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to create task: " + e.getMessage()));
        }
    }

    @PutMapping("/tasks/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @Valid @RequestBody ScheduledTask task) {
        try {
            task.setId(id);
            ScheduledTask updatedTask = schedulerService.updateTask(task);
            return ResponseEntity.ok(updatedTask);

        } catch (Exception e) {
            log.error("Error updating scheduled task", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to update task: " + e.getMessage()));
        }
    }

    @DeleteMapping("/tasks/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        try {
            schedulerService.deleteTask(id);
            return ResponseEntity.ok(Map.of("message", "Task deleted successfully"));

        } catch (Exception e) {
            log.error("Error deleting scheduled task", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to delete task: " + e.getMessage()));
        }
    }

    @PostMapping("/tasks/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleTask(@PathVariable Long id, @RequestParam boolean enable) {
        try {
            schedulerService.toggleTask(id, enable);

            return ResponseEntity.ok(Map.of(
                    "message", "Task " + (enable ? "enabled" : "disabled") + " successfully"
            ));

        } catch (Exception e) {
            log.error("Error toggling scheduled task", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to toggle task: " + e.getMessage()));
        }
    }

    @PostMapping("/tasks/{id}/execute")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> executeTask(@PathVariable Long id) {
        try {
            Map<String, Object> result = schedulerService.executeTaskNow(id);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error executing scheduled task", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Failed to execute task: " + e.getMessage()
                    ));
        }
    }

    @GetMapping("/tasks/{id}/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getTaskHistory(@PathVariable Long id,
                                            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> history = schedulerService.getTaskExecutionHistory(id, limit);
            return ResponseEntity.ok(history);

        } catch (Exception e) {
            log.error("Error getting task history", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get task history: " + e.getMessage()));
        }
    }

    @GetMapping("/next-executions")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getNextExecutions(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> nextExecutions = schedulerService.getNextScheduledExecutions(limit);
            return ResponseEntity.ok(nextExecutions);

        } catch (Exception e) {
            log.error("Error getting next executions", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get next executions: " + e.getMessage()));
        }
    }

    @PostMapping("/tasks/validate-script")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> validateJythonScript(@RequestBody Map<String, String> request) {
        String script = request.get("script");

        try {
            Map<String, Object> validationResult = schedulerService.validateJythonScript(script);
            return ResponseEntity.ok(validationResult);

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "error", e.getMessage()
            ));
        }
    }
}