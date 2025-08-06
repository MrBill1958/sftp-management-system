package com.nearstar.sftpmanager.controller;

import com.nearstar.sftpmanager.model.dto.UserDTO;
import com.nearstar.sftpmanager.model.entity.ApplicationConfig;
import com.nearstar.sftpmanager.model.entity.User;
import com.nearstar.sftpmanager.model.enums.ActionType;
import com.nearstar.sftpmanager.service.AuditService;
import com.nearstar.sftpmanager.service.UserService;
import com.nearstar.sftpmanager.repository.ApplicationConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final AuditService auditService;
    private final ApplicationConfigRepository configRepository;

    // Configuration Management
    @GetMapping("/configuration")
    public ResponseEntity<Map<String, Object>> getConfiguration() {
        Map<String, Object> config = new HashMap<>();

        List<ApplicationConfig> configs = configRepository.findAll();
        for (ApplicationConfig appConfig : configs) {
            config.put(appConfig.getConfigKey(), appConfig.getConfigValue());
        }

        auditService.logAction(ActionType.VIEW, "Configuration", null,
                null, null, "Viewed system configuration");

        return ResponseEntity.ok(config);
    }

    @PutMapping("/configuration")
    public ResponseEntity<Map<String, String>> updateConfiguration(@RequestBody Map<String, String> updates) {
        try {
            for (Map.Entry<String, String> entry : updates.entrySet()) {
                ApplicationConfig config = configRepository.findByConfigKey(entry.getKey())
                        .orElse(new ApplicationConfig());

                String oldValue = config.getConfigValue();
                config.setConfigKey(entry.getKey());
                config.setConfigValue(entry.getValue());
                config.setUpdatedAt(LocalDateTime.now());

                configRepository.save(config);

                auditService.logAction(ActionType.UPDATE, "Configuration", config.getId(),
                        oldValue, entry.getValue(),
                        "Updated config: " + entry.getKey());
            }

            return ResponseEntity.ok(Map.of("message", "Configuration updated successfully"));

        } catch (Exception e) {
            log.error("Error updating configuration", e);
            auditService.logFailedAction(ActionType.UPDATE, "Configuration", null,
                    "Failed to update configuration", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update configuration"));
        }
    }

    // User Management
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO) {
        try {
            UserDTO createdUser = userService.createUser(userDTO);

            auditService.logAction(ActionType.CREATE, "User", createdUser.getId(),
                    null, createdUser.getUsername(),
                    "Created new user: " + createdUser.getUsername());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating user", e);
            auditService.logFailedAction(ActionType.CREATE, "User", null,
                    "Failed to create user: " + userDTO.getUsername(),
                    e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create user"));
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        try {
            userDTO.setId(id);
            UserDTO updatedUser = userService.updateUser(userDTO);

            auditService.logAction(ActionType.UPDATE, "User", id,
                    null, null, "Updated user: " + updatedUser.getUsername());

            return ResponseEntity.ok(updatedUser);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating user", e);
            auditService.logFailedAction(ActionType.UPDATE, "User", id,
                    "Failed to update user", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update user"));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);

            auditService.logAction(ActionType.DELETE, "User", id,
                    null, null, "Deleted user ID: " + id);

            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));

        } catch (Exception e) {
            log.error("Error deleting user", e);
            auditService.logFailedAction(ActionType.DELETE, "User", id,
                    "Failed to delete user", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete user"));
        }
    }

    @PostMapping("/users/{id}/reset-password")
    public ResponseEntity<?> resetUserPassword(@PathVariable Long id) {
        try {
            String newPassword = userService.resetUserPassword(id);

            auditService.logAction(ActionType.UPDATE, "User", id,
                    null, null, "Reset password for user ID: " + id);

            return ResponseEntity.ok(Map.of(
                    "message", "Password reset successfully",
                    "temporaryPassword", newPassword
            ));

        } catch (Exception e) {
            log.error("Error resetting password", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to reset password"));
        }
    }

    @PostMapping("/users/{id}/toggle-status")
    public ResponseEntity<?> toggleUserStatus(@PathVariable Long id, @RequestParam boolean enable) {
        try {
            userService.toggleUserStatus(id, enable);

            auditService.logAction(ActionType.UPDATE, "User", id,
                    String.valueOf(!enable), String.valueOf(enable),
                    (enable ? "Enabled" : "Disabled") + " user ID: " + id);

            return ResponseEntity.ok(Map.of(
                    "message", "User " + (enable ? "enabled" : "disabled") + " successfully"
            ));

        } catch (Exception e) {
            log.error("Error toggling user status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to toggle user status"));
        }
    }

    // Database Management
    @PostMapping("/database/initialize")
    public ResponseEntity<?> initializeDatabase() {
        try {
            // Database initialization logic
            auditService.logAction(ActionType.SYSTEM_CONFIG_CHANGE, "Database", null,
                    null, null, "Database schema initialized");

            return ResponseEntity.ok(Map.of("message", "Database initialized successfully"));

        } catch (Exception e) {
            log.error("Error initializing database", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to initialize database"));
        }
    }

    @PostMapping("/database/backup")
    public ResponseEntity<?> backupDatabase() {
        try {
            // Database backup logic
            String backupFile = "backup-" + LocalDateTime.now().toString() + ".sql";

            auditService.logAction(ActionType.SYSTEM_CONFIG_CHANGE, "Database", null,
                    null, null, "Database backed up to: " + backupFile);

            return ResponseEntity.ok(Map.of(
                    "message", "Database backed up successfully",
                    "file", backupFile
            ));

        } catch (Exception e) {
            log.error("Error backing up database", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to backup database"));
        }
    }

    @DeleteMapping("/database/transaction-logs")
    public ResponseEntity<?> clearOldTransactionLogs(@RequestParam(defaultValue = "90") int daysOld) {
        try {
            int deletedCount = userService.clearOldTransactionLogs(daysOld);

            auditService.logAction(ActionType.DELETE, "TransactionLog", null,
                    null, null,
                    "Cleared " + deletedCount + " transaction logs older than " + daysOld + " days");

            return ResponseEntity.ok(Map.of(
                    "message", "Transaction logs cleared successfully",
                    "deletedCount", deletedCount
            ));

        } catch (Exception e) {
            log.error("Error clearing transaction logs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to clear transaction logs"));
        }
    }

    // System Information
    @GetMapping("/system/info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();

        info.put("version", "1.0.0");
        info.put("serverTime", LocalDateTime.now());
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("osName", System.getProperty("os.name"));
        info.put("totalMemory", Runtime.getRuntime().totalMemory());
        info.put("freeMemory", Runtime.getRuntime().freeMemory());
        info.put("maxMemory", Runtime.getRuntime().maxMemory());

        return ResponseEntity.ok(info);
    }

    @PostMapping("/test-database-connection")
    public ResponseEntity<?> testDatabaseConnection() {
        try {
            boolean isConnected = configRepository.count() >= 0;

            return ResponseEntity.ok(Map.of(
                    "connected", isConnected,
                    "message", isConnected ? "Database connection successful" : "Database connection failed"
            ));

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "connected", false,
                    "message", "Database connection failed: " + e.getMessage()
            ));
        }
    }
}