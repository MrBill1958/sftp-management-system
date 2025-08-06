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
package com.nearstar.sftpmanager.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "access_groups")
public class AccessGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @ManyToMany
    @JoinTable(
            name = "access_group_sites",
            joinColumns = @JoinColumn(name = "access_group_id"),
            inverseJoinColumns = @JoinColumn(name = "site_id")
    )
    private Set<Site> sites;

    // Add this method if you want getGroupName() to work
    public String getGroupName() {
        return name;
    }
}