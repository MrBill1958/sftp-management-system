package com.nearstar.sftpmanager.service;

import com.nearstar.sftpmanager.model.dto.SiteDTO;
import com.nearstar.sftpmanager.model.entity.Site;
import com.nearstar.sftpmanager.model.entity.User;
import com.nearstar.sftpmanager.model.enums.Status;
import com.nearstar.sftpmanager.repository.SiteRepository;
import com.nearstar.sftpmanager.repository.UserRepository;
import com.nearstar.sftpmanager.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SiteService {

    private final SiteRepository siteRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;
    private final SftpService sftpService;

    @Transactional(readOnly = true)
    public List<Site> getAllSites() {
        return siteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Site> getAccessibleSites(User user) {
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getRoleName().equals("ROLE_ADMIN"));

        return siteRepository.findAccessibleSites(user, isAdmin);
    }

    @Transactional(readOnly = true)
    public Optional<Site> getSiteById(Long id) {
        return siteRepository.findById(id);
    }

    @Transactional
    public Site createSite(SiteDTO siteDTO, User createdBy) {
        // Check if site name already exists
        if (siteRepository.findBySiteName(siteDTO.getSiteName()).isPresent()) {
            throw new IllegalArgumentException("Site name already exists");
        }

        Site site = new Site();
        site.setSiteName(siteDTO.getSiteName());
        site.setIpAddress(siteDTO.getIpAddress());
        site.setPort(siteDTO.getPort());
        site.setUsername(siteDTO.getUsername());
        site.setEncryptedPassword(encryptionUtil.encrypt(siteDTO.getPassword()));
        site.setTargetPath(siteDTO.getTargetPath());
        site.setEmailNotification(siteDTO.getEmailNotification());
        site.setStatus(siteDTO.getStatus());
        site.setDescription(siteDTO.getDescription());
        site.setSshKey(siteDTO.getSshKey());
        site.setCreatedAt(LocalDateTime.now());
        site.setCreatedBy(createdBy);

        // Set owner if specified
        if (siteDTO.getOwnerId() != null) {
            User owner = userRepository.findById(siteDTO.getOwnerId())
                    .orElseThrow(() -> new IllegalArgumentException("Owner not found"));
            site.setOwner(owner);
        }

        Site savedSite = siteRepository.save(site);
        log.info("Created new site: {}", savedSite.getSiteName());

        return savedSite;
    }

    @Transactional
    public Site updateSite(Long id, SiteDTO siteDTO, User updatedBy) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Site not found"));

        // Check if site name is being changed and already exists
        if (!site.getSiteName().equals(siteDTO.getSiteName()) &&
                siteRepository.findBySiteName(siteDTO.getSiteName()).isPresent()) {
            throw new IllegalArgumentException("Site name already exists");
        }

        site.setSiteName(siteDTO.getSiteName());
        site.setIpAddress(siteDTO.getIpAddress());
        site.setPort(siteDTO.getPort());
        site.setUsername(siteDTO.getUsername());

        // Only update password if provided
        if (siteDTO.getPassword() != null && !siteDTO.getPassword().isEmpty()) {
            site.setEncryptedPassword(encryptionUtil.encrypt(siteDTO.getPassword()));
        }

        site.setTargetPath(siteDTO.getTargetPath());
        site.setEmailNotification(siteDTO.getEmailNotification());
        site.setStatus(siteDTO.getStatus());
        site.setDescription(siteDTO.getDescription());
        site.setSshKey(siteDTO.getSshKey());
        site.setUpdatedAt(LocalDateTime.now());
        site.setUpdatedBy(updatedBy);

        // Update owner if specified
        if (siteDTO.getOwnerId() != null) {
            User owner = userRepository.findById(siteDTO.getOwnerId())
                    .orElseThrow(() -> new IllegalArgumentException("Owner not found"));
            site.setOwner(owner);
        } else {
            site.setOwner(null);
        }

        Site updatedSite = siteRepository.save(site);
        log.info("Updated site: {}", updatedSite.getSiteName());

        return updatedSite;
    }

    @Transactional
    public void deleteSite(Long id) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Site not found"));

        siteRepository.delete(site);
        log.info("Deleted site: {}", site.getSiteName());
    }

    @Transactional
    public void updateConnectionStatus(Long siteId, SftpService.TestConnectionResult result) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Site not found"));

        site.setLastTested(LocalDateTime.now());
        site.setLastTestResult(result.getMessage());
        site.setConnectionStatus(result.isSuccess() ?
                Site.ConnectionStatus.SUCCESS : Site.ConnectionStatus.FAILED);

        siteRepository.save(site);
    }

    @Transactional
    public Map<Long, SftpService.TestConnectionResult> testAllConnections() {
        List<Site> sites = siteRepository.findByStatus(Status.ACTIVE);
        Map<Long, SftpService.TestConnectionResult> results = new HashMap<>();

        for (Site site : sites) {
            try {
                SftpService.TestConnectionResult result = sftpService.testConnection(site);
                results.put(site.getId(), result);
                updateConnectionStatus(site.getId(), result);
            } catch (Exception e) {
                log.error("Error testing connection for site {}: {}", site.getSiteName(), e.getMessage());
                SftpService.TestConnectionResult errorResult = new SftpService.TestConnectionResult();
                errorResult.setSuccess(false);
                errorResult.setMessage("Error: " + e.getMessage());
                results.put(site.getId(), errorResult);
            }
        }

        return results;
    }

    @Transactional(readOnly = true)
    public List<Site> searchSites(String query, User user) {
        List<Site> accessibleSites = getAccessibleSites(user);

        return accessibleSites.stream()
                .filter(site ->
                        site.getSiteName().toLowerCase().contains(query.toLowerCase()) ||
                                site.getIpAddress().contains(query) ||
                                (site.getDescription() != null && site.getDescription().toLowerCase().contains(query.toLowerCase()))
                )
                .collect(Collectors.toList());
    }

    @Transactional
    public Site assignOwner(Long siteId, Long userId) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Site not found"));

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        site.setOwner(owner);
        Site updatedSite = siteRepository.save(site);

        log.info("Assigned owner {} to site {}", owner.getUsername(), site.getSiteName());

        return updatedSite;
    }

    // Security helper methods
    public boolean hasAccessToSite(Long siteId, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return false;
        }

        User user = (User) authentication.getPrincipal();

        // Admins have access to all sites
        if (user.getRoles().stream().anyMatch(role -> role.getRoleName().equals("ROLE_ADMIN"))) {
            return true;
        }

        // Check if user is the owner
        Optional<Site> site = siteRepository.findById(siteId);
        return site.map(s -> s.getOwner() != null && s.getOwner().getId().equals(user.getId()))
                .orElse(false);
    }

    public boolean canEditSite(Long siteId, Authentication authentication) {
        return hasAccessToSite(siteId, authentication);
    }
}