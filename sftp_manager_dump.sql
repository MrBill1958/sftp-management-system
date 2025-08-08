/*M!999999\- enable the sandbox mode */
-- MariaDB dump 10.19-11.8.2-MariaDB, for osx10.20 (arm64)
--
-- Host: localhost    Database: sftp_manager
-- ------------------------------------------------------
-- Server version	11.8.2-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS = @@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION = @@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE = @@TIME_ZONE */;
/*!40103 SET TIME_ZONE = '+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;

--
-- Table structure for table `access_group_sites`
--

DROP TABLE IF EXISTS `access_group_sites`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `access_group_sites`
(
    `access_group_id` bigint(20) NOT NULL,
    `site_id`         bigint(20) NOT NULL,
    PRIMARY KEY (`access_group_id`, `site_id`),
    KEY `FK8ka4minnktt7dee0fmn1p51ym` (`site_id`),
    CONSTRAINT `FK7vgs7kfpgkjr6uix2mwr2peb0` FOREIGN KEY (`access_group_id`) REFERENCES `access_groups` (`id`),
    CONSTRAINT `FK8ka4minnktt7dee0fmn1p51ym` FOREIGN KEY (`site_id`) REFERENCES `sites` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `access_group_sites`
--

LOCK TABLES `access_group_sites` WRITE;
/*!40000 ALTER TABLE `access_group_sites`
    DISABLE KEYS */;
set autocommit = 0;
/*!40000 ALTER TABLE `access_group_sites`
    ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `access_groups`
--

DROP TABLE IF EXISTS `access_groups`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `access_groups`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT,
    `description` varchar(255) DEFAULT NULL,
    `name`        varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_hyfvgm64p0c71lu89or53avgr` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `access_groups`
--

LOCK TABLES `access_groups` WRITE;
/*!40000 ALTER TABLE `access_groups`
    DISABLE KEYS */;
set autocommit = 0;
/*!40000 ALTER TABLE `access_groups`
    ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `activity_logs`
--

DROP TABLE IF EXISTS `activity_logs`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `activity_logs`
(
    `id`         bigint(20)   NOT NULL AUTO_INCREMENT,
    `username`   varchar(50)  NOT NULL,
    `type`       varchar(20)  NOT NULL,
    `title`      varchar(255) NOT NULL,
    `details`    text              DEFAULT NULL,
    `site_name`  varchar(100)      DEFAULT NULL,
    `created_at` timestamp    NULL DEFAULT current_timestamp(),
    PRIMARY KEY (`id`),
    KEY `idx_username` (`username`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_type` (`type`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 10
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `activity_logs`
--

LOCK TABLES `activity_logs` WRITE;
/*!40000 ALTER TABLE `activity_logs`
    DISABLE KEYS */;
set autocommit = 0;
INSERT INTO `activity_logs`
VALUES (1, 'admin', 'LOGIN', 'User logged in', NULL, NULL, '2025-08-07 14:58:44'),
       (2, 'admin', 'CREATE', 'ShareTru site created', 'New SFTP site added', 'ShareTru', '2025-08-07 14:58:44'),
       (3, 'admin', 'EDIT', 'Site configuration updated', 'Updated connection settings', 'ShareFile',
        '2025-08-07 14:58:44'),
       (4, 'user', 'LOGIN', 'User logged in', NULL, NULL, '2025-08-07 14:58:44'),
       (5, 'user', 'UPLOAD', 'report.pdf uploaded', 'Uploaded to /documents/', 'ShareFile', '2025-08-07 14:58:44'),
       (6, 'user', 'DOWNLOAD', 'data.csv downloaded', 'Downloaded from /exports/', 'ShareTru', '2025-08-07 14:58:44'),
       (7, 'user', 'VIEW', 'Viewed file list', '/reports/ directory', 'ShareTru', '2025-08-07 14:58:44'),
       (8, 'viewer', 'LOGIN', 'User logged in', NULL, NULL, '2025-08-07 14:58:44'),
       (9, 'viewer', 'VIEW', 'Viewed dashboard', NULL, NULL, '2025-08-07 14:58:44');
/*!40000 ALTER TABLE `activity_logs`
    ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `application_config`
--

DROP TABLE IF EXISTS `application_config`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `application_config`
(
    `id`           bigint(20)   NOT NULL AUTO_INCREMENT,
    `config_key`   varchar(255) NOT NULL,
    `config_value` text         DEFAULT NULL,
    `created_at`   datetime(6)  DEFAULT NULL,
    `description`  varchar(255) DEFAULT NULL,
    `encrypted`    bit(1)       NOT NULL,
    `updated_at`   datetime(6)  DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_7rpi3qyn2m09cnvk10ilm6s78` (`config_key`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `application_config`
--

LOCK TABLES `application_config` WRITE;
/*!40000 ALTER TABLE `application_config`
    DISABLE KEYS */;
set autocommit = 0;
/*!40000 ALTER TABLE `application_config`
    ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `audit_logs`
--

DROP TABLE IF EXISTS `audit_logs`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_logs`
(
    `id`           bigint(20)                                                                                                                                                                                                                     NOT NULL AUTO_INCREMENT,
    `action_type`  enum ('CREATE','UPDATE','DELETE','LOGIN','LOGOUT','VIEW','TEST_CONNECTION','UPLOAD_FILE','DOWNLOAD_FILE','DELETE_FILE','CREATE_FOLDER','CHANGE_PASSWORD','GRANT_ACCESS','REVOKE_ACCESS','EXPORT_AUDIT','SYSTEM_CONFIG_CHANGE') NOT NULL,
    `description`  varchar(500) DEFAULT NULL,
    `entityId`     bigint(20)   DEFAULT NULL,
    `entityType`   varchar(255)                                                                                                                                                                                                                   NOT NULL,
    `errorMessage` varchar(255) DEFAULT NULL,
    `ipAddress`    varchar(255)                                                                                                                                                                                                                   NOT NULL,
    `newValue`     text         DEFAULT NULL,
    `oldValue`     text         DEFAULT NULL,
    `successful`   bit(1)                                                                                                                                                                                                                         NOT NULL,
    `timestamp`    datetime(6)                                                                                                                                                                                                                    NOT NULL,
    `userAgent`    varchar(255) DEFAULT NULL,
    `username`     varchar(255)                                                                                                                                                                                                                   NOT NULL,
    `user_id`      bigint(20)   DEFAULT NULL,
    `actionType`   enum ('CREATE','UPDATE','DELETE','LOGIN','LOGOUT','VIEW','TEST_CONNECTION','UPLOAD_FILE','DOWNLOAD_FILE','DELETE_FILE','CREATE_FOLDER','CHANGE_PASSWORD','GRANT_ACCESS','REVOKE_ACCESS','EXPORT_AUDIT','SYSTEM_CONFIG_CHANGE') NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_audit_timestamp` (`timestamp`),
    KEY `idx_audit_user` (`user_id`),
    KEY `idx_audit_action` (`action_type`),
    CONSTRAINT `FKjs4iimve3y0xssbtve5ysyef0` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_logs`
--

LOCK TABLES `audit_logs` WRITE;
/*!40000 ALTER TABLE `audit_logs`
    DISABLE KEYS */;
set autocommit = 0;
/*!40000 ALTER TABLE `audit_logs`
    ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles`
(
    `id`          bigint(20)   NOT NULL AUTO_INCREMENT,
    `description` varchar(255) DEFAULT NULL,
    `name`        varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_ofx66keruapi6vyqpv6f2or37` (`name`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles`
    DISABLE KEYS */;
set autocommit = 0;
INSERT INTO `roles`
VALUES (1, 'Administrator role', 'ADMIN'),
       (2, 'Regular user role', 'USER');
/*!40000 ALTER TABLE `roles`
    ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `scheduled_tasks`
--

DROP TABLE IF EXISTS `scheduled_tasks`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `scheduled_tasks`
(
    `id`                  bigint(20)   NOT NULL AUTO_INCREMENT,
    `commandLineParams`   varchar(255) DEFAULT NULL,
    `created_at`          datetime(6)  DEFAULT NULL,
    `daysOfWeek`          varchar(255) DEFAULT NULL,
    `enabled`             bit(1)       NOT NULL,
    `executionTime`       time(6)      DEFAULT NULL,
    `jythonScript`        varchar(255) NOT NULL,
    `lastExecution`       datetime(6)  DEFAULT NULL,
    `lastExecutionStatus` varchar(255) DEFAULT NULL,
    `taskName`            varchar(255) NOT NULL,
    `created_by`          bigint(20)   DEFAULT NULL,
    `site_id`             bigint(20)   NOT NULL,
    PRIMARY KEY (`id`),
    KEY `FKoanb8igvi5h8wchyxy5blr0bt` (`created_by`),
    KEY `FKegcx5h0mxr28h15hemly52i8m` (`site_id`),
    CONSTRAINT `FKegcx5h0mxr28h15hemly52i8m` FOREIGN KEY (`site_id`) REFERENCES `sites` (`id`),
    CONSTRAINT `FKoanb8igvi5h8wchyxy5blr0bt` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scheduled_tasks`
--

LOCK TABLES `scheduled_tasks` WRITE;
/*!40000 ALTER TABLE `scheduled_tasks`
    DISABLE KEYS */;
set autocommit = 0;
/*!40000 ALTER TABLE `scheduled_tasks`
    ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `sites`
--

DROP TABLE IF EXISTS `sites`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `sites`
(
    `id`                bigint(20)   NOT NULL AUTO_INCREMENT,
    `connection_status` enum ('SUCCESS','FAILED','ERROR','UNKNOWN')             DEFAULT NULL,
    `created_at`        datetime(6)                                             DEFAULT NULL,
    `description`       varchar(500)                                            DEFAULT NULL,
    `emailNotification` varchar(255)                                            DEFAULT NULL,
    `encryptedPassword` varchar(255) NOT NULL,
    `ipAddress`         varchar(255) NOT NULL,
    `known_hosts_entry` tinytext                                                DEFAULT NULL,
    `lastTestResult`    varchar(255)                                            DEFAULT NULL,
    `lastTested`        datetime(6)                                             DEFAULT NULL,
    `port`              int(11)      NOT NULL,
    `siteName`          varchar(255) NOT NULL,
    `ssh_key`           tinytext                                                DEFAULT NULL,
    `status`            enum ('ACTIVE','INACTIVE','TESTING','ERROR','DISABLED') DEFAULT NULL,
    `targetPath`        varchar(255)                                            DEFAULT NULL,
    `updated_at`        datetime(6)                                             DEFAULT NULL,
    `username`          varchar(255) NOT NULL,
    `created_by`        bigint(20)                                              DEFAULT NULL,
    `owner_id`          bigint(20)                                              DEFAULT NULL,
    `updated_by`        bigint(20)                                              DEFAULT NULL,
    `is_active`         tinyint(1)                                              DEFAULT 1,
    `last_tested`       timestamp    NULL                                       DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_437jj3ameptbfkjm87d9fp9hk` (`siteName`),
    KEY `FK99u8hcgd6236hh4wccugreup` (`created_by`),
    KEY `FKd6ojtyjut1tknyf95299xqamu` (`owner_id`),
    KEY `FKqc0kdals2sil6rnwdstuuwlpo` (`updated_by`),
    CONSTRAINT `FK99u8hcgd6236hh4wccugreup` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`),
    CONSTRAINT `FKd6ojtyjut1tknyf95299xqamu` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`),
    CONSTRAINT `FKqc0kdals2sil6rnwdstuuwlpo` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 7
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sites`
--

LOCK TABLES `sites` WRITE;
/*!40000 ALTER TABLE `sites`
    DISABLE KEYS */;
set autocommit = 0;
INSERT INTO `sites`
VALUES (1, 'SUCCESS', '2025-08-06 14:24:55.427797', 'ShareTru sFTP Site', 'bill.sanders@nearstar.com',
        'zyIjudLSAuYfADGWpYn7uA==', 'nearstar.sharetru.com', NULL, 'Connection successful',
        '2025-08-07 07:32:18.114380', 22, 'ShareTru', NULL, 'ACTIVE', '/', '2025-08-07 07:32:18.125449',
        'bill.sanders@nearstar.com', NULL, NULL, NULL, 1, NULL),
       (6, 'SUCCESS', '2025-08-07 07:56:46.048689', NULL, NULL, 'zyIjudLSAuYfADGWpYn7uA==', 'nearstar.sharetru.com',
        NULL, 'Connection successful', '2025-08-07 07:56:56.535395', 22, 'ShareFile', NULL, 'ACTIVE', '/',
        '2025-08-07 07:56:56.538663', 'bill.sanders@nearstar.com', NULL, NULL, NULL, 1, NULL);
/*!40000 ALTER TABLE `sites`
    ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `transaction_logs`
--

DROP TABLE IF EXISTS `transaction_logs`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `transaction_logs`
(
    `id`           bigint(20)   NOT NULL AUTO_INCREMENT,
    `action`       varchar(255) NOT NULL,
    `checksum`     varchar(255) DEFAULT NULL,
    `details`      text         DEFAULT NULL,
    `durationMs`   bigint(20)   DEFAULT NULL,
    `errorMessage` text         DEFAULT NULL,
    `filePath`     varchar(255) DEFAULT NULL,
    `fileSize`     bigint(20)   DEFAULT NULL,
    `success`      bit(1)       NOT NULL,
    `timestamp`    datetime(6)  NOT NULL,
    `site_id`      bigint(20)   NOT NULL,
    `user_id`      bigint(20)   DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `FKosltlkkj0xc9jncroyhrwa68r` (`site_id`),
    KEY `FKjnwbqmodm3plddipm3feysfup` (`user_id`),
    CONSTRAINT `FKjnwbqmodm3plddipm3feysfup` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
    CONSTRAINT `FKosltlkkj0xc9jncroyhrwa68r` FOREIGN KEY (`site_id`) REFERENCES `sites` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transaction_logs`
--

LOCK TABLES `transaction_logs` WRITE;
/*!40000 ALTER TABLE `transaction_logs`
    DISABLE KEYS */;
set autocommit = 0;
/*!40000 ALTER TABLE `transaction_logs`
    ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_roles`
(
    `user_id` bigint(20) NOT NULL,
    `role_id` bigint(20) NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`),
    KEY `FKh8ciramu9cc9q3qcqiv4ue8a6` (`role_id`),
    CONSTRAINT `FKh8ciramu9cc9q3qcqiv4ue8a6` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
    CONSTRAINT `FKhfh9dx7w3ubf1co1vdev94g3f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_roles`
--

LOCK TABLES `user_roles` WRITE;
/*!40000 ALTER TABLE `user_roles`
    DISABLE KEYS */;
set autocommit = 0;
INSERT INTO `user_roles`
VALUES (1, 1);
/*!40000 ALTER TABLE `user_roles`
    ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `user_sites`
--

DROP TABLE IF EXISTS `user_sites`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_sites`
(
    `user_id`     bigint(20) NOT NULL,
    `site_id`     bigint(20) NOT NULL,
    `assigned_at` timestamp  NULL DEFAULT current_timestamp(),
    PRIMARY KEY (`user_id`, `site_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_site_id` (`site_id`),
    CONSTRAINT `user_sites_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `user_sites_ibfk_2` FOREIGN KEY (`site_id`) REFERENCES `sites` (`id`) ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_sites`
--

LOCK TABLES `user_sites` WRITE;
/*!40000 ALTER TABLE `user_sites`
    DISABLE KEYS */;
set autocommit = 0;
INSERT INTO `user_sites`
VALUES (1, 1, '2025-08-07 14:58:44'),
       (1, 6, '2025-08-07 14:58:44');
/*!40000 ALTER TABLE `user_sites`
    ENABLE KEYS */;
UNLOCK TABLES;
commit;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `users`
(
    `id`                    bigint(20)   NOT NULL AUTO_INCREMENT,
    `accountNonExpired`     bit(1)       NOT NULL,
    `accountNonLocked`      bit(1)       NOT NULL,
    `created_at`            datetime(6)  DEFAULT NULL,
    `credentialsNonExpired` bit(1)       NOT NULL,
    `email`                 varchar(255) NOT NULL,
    `enabled`               bit(1)       NOT NULL,
    `firstName`             varchar(255) DEFAULT NULL,
    `lastLogin`             datetime(6)  DEFAULT NULL,
    `lastName`              varchar(255) DEFAULT NULL,
    `mustChangePassword`    bit(1)       NOT NULL,
    `password`              varchar(255) NOT NULL,
    `passwordChangedAt`     datetime(6)  DEFAULT NULL,
    `username`              varchar(255) NOT NULL,
    `access_group_id`       bigint(20)   DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_r43af9ap4edm43mmtq01oddj6` (`username`),
    KEY `FKd9c4ifcm6lqan5sqq5qf4imb2` (`access_group_id`),
    CONSTRAINT `FKd9c4ifcm6lqan5sqq5qf4imb2` FOREIGN KEY (`access_group_id`) REFERENCES `access_groups` (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users`
    DISABLE KEYS */;
set autocommit = 0;
INSERT INTO `users`
VALUES (1, 0x01, 0x01, '2025-08-06 10:35:57.000000', 0x01, 'admin@example.com', 0x01, 'Admin', NULL, 'User', 0x00,
        '$2a$10$otQ68nq7zGM.GSbsj0uCgevlitwiY7FAQqzaed/aQGjJNnZAz0mDG', NULL, 'admin', NULL);
/*!40000 ALTER TABLE `users`
    ENABLE KEYS */;
UNLOCK TABLES;
commit;
/*!40103 SET TIME_ZONE = @OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE = @OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS = @OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION = @OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

-- Dump completed on 2025-08-07 10:01:29
