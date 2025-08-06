package com.nearstar.sftpmanager.model.entity;

import com.nearstar.sftpmanager.model.enums.Status;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "sites")
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String siteName;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private int port = 22;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String encryptedPassword;

    private String targetPath = "/";

    private String emailNotification;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @Column(length = 500)
    private String description;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    private LocalDateTime lastTested;

    private String lastTestResult;

    @Column(name = "ssh_key")
    @Lob
    private String sshKey;

    @Column(name = "known_hosts_entry")
    @Lob
    private String knownHostsEntry;

    // ADD THESE FIELDS
    @Enumerated(EnumType.STRING)
    @Column(name = "connection_status")
    private ConnectionStatus connectionStatus;

    @OneToMany(mappedBy = "site", cascade = CascadeType.ALL)
    private Set<ScheduledTask> scheduledTasks;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ADD THIS INNER ENUM
    public enum ConnectionStatus {
        SUCCESS, FAILED, ERROR, UNKNOWN
    }
}