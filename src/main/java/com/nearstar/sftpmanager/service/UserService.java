/**
 * Copyright © 2025 NearStar Incorporated. All rights reserved.
 *
 * This software and its source code are proprietary and confidential
 * to NearStar Incorporated. Unauthorized copying, modification,
 * distribution, or use of this software, in whole or in part,
 * is strictly prohibited without the prior written consent of the copyright holder.
 *
 * Portions of this software may utilize or be derived from open-source software
 * and publicly available frameworks licensed under their respective licenses.
 *
 * This code may also include contributions developed with the assistance of AI-based tools.
 *
 * All open-source dependencies are used in accordance with their applicable licenses,
 * and full attribution is maintained in the corresponding documentation (e.g., NOTICE or LICENSE files).
 *
 * For inquiries regarding licensing or usage, please contact: bill.sanders@nearstar.com
 *
 * @file        UserService.java
 * @author      Bill Sanders <bill.sanders@nearstar.com>
 * @version     1.0.0
 * @date        2025-08-03
 * @brief       Brief description of the file's purpose
 *
 * @copyright   Copyright (c) 2025 NearStar Incorporated
 * @license     MIT License
 *
 * @modified    2025-08-06 - Bill Sanders - Initialized in Git
 *
 * @todo
 * @bug
 * @deprecated
 */
package com.nearstar.sftpmanager.service;

import com.nearstar.sftpmanager.model.dto.UserDTO;
import com.nearstar.sftpmanager.model.entity.AccessGroup;
import com.nearstar.sftpmanager.model.entity.Role;
import com.nearstar.sftpmanager.model.entity.TransactionLog;
import com.nearstar.sftpmanager.model.entity.User;
import com.nearstar.sftpmanager.repository.AccessGroupRepository;
import com.nearstar.sftpmanager.repository.RoleRepository;
import com.nearstar.sftpmanager.repository.TransactionLogRepository;
import com.nearstar.sftpmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AccessGroupRepository accessGroupRepository;
    private final TransactionLogRepository transactionLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        // Check if username already exists
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEnabled(userDTO.isEnabled());
        user.setCreatedAt(LocalDateTime.now());

        // Set roles
        Set<Role> roles = new HashSet<>();
        for (String roleName : userDTO.getRoles()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
            roles.add(role);
        }
        user.setRoles(roles);

        // Set access group
        if (userDTO.getAccessGroup() != null) {
            AccessGroup accessGroup = accessGroupRepository.findByName(userDTO.getAccessGroup())
                    .orElseThrow(() -> new IllegalArgumentException("Access group not found"));
            user.setAccessGroup(accessGroup);
        }

        User savedUser = userRepository.save(user);
        log.info("Created new user: {}", savedUser.getUsername());

        return convertToDTO(savedUser);
    }

    @Transactional
    public UserDTO updateUser(UserDTO userDTO) {
        User user = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if email is being changed and already exists
        if (!user.getEmail().equals(userDTO.getEmail()) &&
                userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setEmail(userDTO.getEmail());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEnabled(userDTO.isEnabled());

        // Update password if provided
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            user.setPasswordChangedAt(LocalDateTime.now());
        }

        // Update roles
        Set<Role> roles = new HashSet<>();
        for (String roleName : userDTO.getRoles()) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleName));
            roles.add(role);
        }
        user.setRoles(roles);

        // Update access group
        if (userDTO.getAccessGroup() != null) {
            AccessGroup accessGroup = accessGroupRepository.findByName(userDTO.getAccessGroup())
                    .orElseThrow(() -> new IllegalArgumentException("Access group not found"));
            user.setAccessGroup(accessGroup);
        }

        User updatedUser = userRepository.save(user);
        log.info("Updated user: {}", updatedUser.getUsername());

        return convertToDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        userRepository.delete(user);
        log.info("Deleted user: {}", user.getUsername());
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Validate new password
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setMustChangePassword(false);

        userRepository.save(user);
        log.info("Password changed for user: {}", user.getUsername());
    }

    @Transactional
    public String resetUserPassword(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Generate temporary password
        String tempPassword = generateTemporaryPassword();

        user.setPassword(passwordEncoder.encode(tempPassword));
        user.setPasswordChangedAt(LocalDateTime.now());
        user.setMustChangePassword(true);

        userRepository.save(user);
        log.info("Password reset for user: {}", user.getUsername());

        return tempPassword;
    }

    @Transactional
    public void toggleUserStatus(Long userId, boolean enable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setEnabled(enable);
        userRepository.save(user);

        log.info("User {} status changed to: {}", user.getUsername(), enable ? "enabled" : "disabled");
    }

    @Transactional
    public void updateLastLogin(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    @Transactional
    public int clearOldTransactionLogs(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        List<TransactionLog> oldLogs = transactionLogRepository.findByDateRange(
                LocalDateTime.MIN, cutoffDate);

        transactionLogRepository.deleteAll(oldLogs);
        log.info("Deleted {} transaction logs older than {} days", oldLogs.size(), daysOld);

        return oldLogs.size();
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEnabled(user.isEnabled());

        dto.setRoles(user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));

        if (user.getAccessGroup() != null) {
            dto.setAccessGroup(user.getAccessGroup().getName());
        }

        return dto;
    }

    private String generateTemporaryPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        Random random = new Random();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 12; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return password.toString();
    }
}