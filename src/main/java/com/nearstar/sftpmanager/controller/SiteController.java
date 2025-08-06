package com.nearstar.sftpmanager.controller;

import com.nearstar.sftpmanager.model.dto.SiteDTO;
import com.nearstar.sftpmanager.model.entity.Site;
import com.nearstar.sftpmanager.model.entity.User;
import com.nearstar.sftpmanager.model.enums.ActionType;
import com.nearstar.sftpmanager.service.AuditService;
import com.nearstar.sftpmanager.service.SiteService;
import com.nearstar.sftpmanager.service.SftpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;
    private final SftpService sftpService;
    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<Site>> getAllSites(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Site> sites = siteService.getAccessibleSites(user);
        return ResponseEntity.ok(sites);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@siteService.hasAccessToSite(#id, authentication)")
    public ResponseEntity<Site> getSiteById(@PathVariable Long id) {
        return siteService.getSiteById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createSite(@Valid @RequestBody SiteDTO siteDTO, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Site createdSite = siteService.createSite(siteDTO, user);

            auditService.logAction(ActionType.CREATE, "Site", createdSite.getId(),
                    null, createdSite.getSiteName(),
                    "Created new site: " + createdSite.getSiteName());

            return ResponseEntity.status(HttpStatus.CREATED).body(createdSite);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating site", e);
            auditService.logFailedAction(ActionType.CREATE, "Site", null,
                    "Failed to create site: " + siteDTO.getSiteName(),
                    e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to create site"));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("@siteService.canEditSite(#id, authentication)")
    public ResponseEntity<?> updateSite(@PathVariable Long id,
                                        @Valid @RequestBody SiteDTO siteDTO,
                                        Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Site updatedSite = siteService.updateSite(id, siteDTO, user);

            auditService.logAction(ActionType.UPDATE, "Site", id,
                    null, null, "Updated site: " + updatedSite.getSiteName());

            return ResponseEntity.ok(updatedSite);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating site", e);
            auditService.logFailedAction(ActionType.UPDATE, "Site", id,
                    "Failed to update site", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to update site"));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@siteService.canEditSite(#id, authentication)")
    public ResponseEntity<?> deleteSite(@PathVariable Long id) {
        try {
            siteService.deleteSite(id);

            auditService.logAction(ActionType.DELETE, "Site", id,
                    null, null, "Deleted site ID: " + id);

            return ResponseEntity.ok(Map.of("message", "Site deleted successfully"));

        } catch (Exception e) {
            log.error("Error deleting site", e);
            auditService.logFailedAction(ActionType.DELETE, "Site", id,
                    "Failed to delete site", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to delete site"));
        }
    }

    @PostMapping("/{id}/test")
    @PreAuthorize("@siteService.hasAccessToSite(#id, authentication)")
    public ResponseEntity<?> testConnection(@PathVariable Long id) {
        try {
            Site site = siteService.getSiteById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Site not found"));

            SftpService.TestConnectionResult result = sftpService.testConnection(site);

            // Update site with test results
            siteService.updateConnectionStatus(id, result);

            auditService.logAction(ActionType.TEST_CONNECTION, "Site", id,
                    null, result.isSuccess() ? "SUCCESS" : "FAILED",
                    "Connection test: " + result.getMessage());

            return ResponseEntity.ok(Map.of(
                    "success", result.isSuccess(),
                    "message", result.getMessage(),
                    "responseTime", result.getResponseTime()
            ));

        } catch (Exception e) {
            log.error("Error testing connection", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Test failed: " + e.getMessage()
                    ));
        }
    }

    @PostMapping("/test-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> testAllConnections() {
        try {
            Map<Long, SftpService.TestConnectionResult> results = siteService.testAllConnections();

            long successCount = results.values().stream()
                    .filter(SftpService.TestConnectionResult::isSuccess)
                    .count();

            auditService.logAction(ActionType.TEST_CONNECTION, "Site", null,
                    null, null,
                    String.format("Tested all connections: %d/%d successful",
                            successCount, results.size()));

            return ResponseEntity.ok(Map.of(
                    "totalSites", results.size(),
                    "successfulConnections", successCount,
                    "results", results
            ));

        } catch (Exception e) {
            log.error("Error testing all connections", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to test connections"));
        }
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<Site>> searchSites(@RequestParam String query, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<Site> sites = siteService.searchSites(query, user);
        return ResponseEntity.ok(sites);
    }

    @PostMapping("/{id}/assign-owner")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> assignOwner(@PathVariable Long id, @RequestParam Long userId) {
        try {
            Site site = siteService.assignOwner(id, userId);

            auditService.logAction(ActionType.GRANT_ACCESS, "Site", id,
                    null, "User ID: " + userId,
                    "Assigned owner to site: " + site.getSiteName());

            return ResponseEntity.ok(Map.of(
                    "message", "Owner assigned successfully",
                    "site", site
            ));

        } catch (Exception e) {
            log.error("Error assigning owner", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to assign owner"));
        }
    }
}