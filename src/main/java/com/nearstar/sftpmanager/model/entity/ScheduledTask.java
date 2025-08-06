package com.nearstar.sftpmanager.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "scheduled_tasks")
public class ScheduledTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String taskName;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    // Cron-like scheduling
    private String daysOfWeek; // MON,TUE,WED

    private LocalTime executionTime;

    @Column(nullable = false)
    private String jythonScript;

    private String commandLineParams;

    private boolean enabled = true;

    private LocalDateTime lastExecution;

    private String lastExecutionStatus;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}