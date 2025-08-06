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

import com.nearstar.sftpmanager.model.entity.Site;
import com.nearstar.sftpmanager.model.entity.TransactionLog;
import com.nearstar.sftpmanager.repository.TransactionLogRepository;
import com.nearstar.sftpmanager.util.EncryptionUtil;
import com.nearstar.sftpmanager.util.FileChecksum;
import com.jcraft.jsch.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import org.springframework.stereotype.Service;
import java.io.*;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SftpService {

    private final EncryptionUtil encryptionUtil;
    private final TransactionLogRepository transactionLogRepository;
    private final AuditService auditService;

    public TestConnectionResult testConnection(Site site) {
        Session session = null;
        ChannelSftp channelSftp = null;
        TestConnectionResult result = new TestConnectionResult();

        try {
            JSch jsch = new JSch();

            // Handle SSH key if present
            if (site.getSshKey() != null && !site.getSshKey().isEmpty()) {
                jsch.addIdentity("site-" + site.getId(),
                        site.getSshKey().getBytes(),
                        null,
                        encryptionUtil.decrypt(site.getEncryptedPassword()).getBytes());
            }

            session = jsch.getSession(site.getUsername(), site.getIpAddress(), site.getPort());

            // Set password if no SSH key
            if (site.getSshKey() == null || site.getSshKey().isEmpty()) {
                session.setPassword(encryptionUtil.decrypt(site.getEncryptedPassword()));
            }

            // Configure known hosts
            if (site.getKnownHostsEntry() != null) {
                session.setConfig("StrictHostKeyChecking", "yes");
                // Add known host entry
            } else {
                session.setConfig("StrictHostKeyChecking", "no");
            }

            session.setConfig("PreferredAuthentications", "publickey,password");
            session.setTimeout(30000);
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            // Test access to target path
            channelSftp.ls(site.getTargetPath());

            result.setSuccess(true);
            result.setMessage("Connection successful");

            // Log successful test
            logTransaction(site, "CONNECTION_TEST", true, "Connection test successful");

        } catch (JSchException e) {
            result.setSuccess(false);
            result.setMessage("Connection failed: " + e.getMessage());
            result.setErrorCode(determineErrorCode(e));

            // Log failed test
            logTransaction(site, "CONNECTION_TEST", false, e.getMessage());

        } catch (SftpException e) {
            result.setSuccess(false);
            result.setMessage("SFTP error: " + e.getMessage());
            result.setErrorCode("SFTP_ERROR");

            logTransaction(site, "CONNECTION_TEST", false, e.getMessage());

        } finally {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }

        return result;
    }

    public FileTransferResult uploadFile(Site site, InputStream inputStream, String remotePath,
                                         boolean verifyTransfer) throws Exception {
        Session session = null;
        ChannelSftp channelSftp = null;
        FileTransferResult result = new FileTransferResult();

        try {
            // Create SFTP connection
            session = createSession(site);
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            // Calculate checksum of source file if verification requested
            String sourceChecksum = null;
            byte[] fileData = null;

            if (verifyTransfer) {
                fileData = inputStream.readAllBytes();
                sourceChecksum = FileChecksum.calculateMD5(new ByteArrayInputStream(fileData));
                inputStream = new ByteArrayInputStream(fileData);
            }

            // Upload file
            channelSftp.put(inputStream, remotePath, ChannelSftp.OVERWRITE);

            // Verify transfer if requested
            if (verifyTransfer && sourceChecksum != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                channelSftp.get(remotePath, baos);
                String remoteChecksum = FileChecksum.calculateMD5(
                        new ByteArrayInputStream(baos.toByteArray()));

                if (!sourceChecksum.equals(remoteChecksum)) {
                    throw new IOException("File verification failed - checksums don't match");
                }

                result.setVerified(true);
                result.setChecksum(sourceChecksum);
            }

            // Get file info
            SftpATTRS attrs = channelSftp.stat(remotePath);
            result.setFileSize(attrs.getSize());
            result.setSuccess(true);
            result.setRemotePath(remotePath);

            // Log transaction
            logTransaction(site, "FILE_UPLOAD", true,
                    "Uploaded file to " + remotePath + " (Size: " + attrs.getSize() + " bytes)");

        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());

            logTransaction(site, "FILE_UPLOAD", false,
                    "Failed to upload to " + remotePath + ": " + e.getMessage());

            throw e;

        } finally {
            disconnect(channelSftp, session);
        }

        return result;
    }

    public FileTransferResult downloadFile(Site site, String remotePath, OutputStream outputStream,
                                           boolean verifyTransfer) throws Exception {
        Session session = null;
        ChannelSftp channelSftp = null;
        FileTransferResult result = new FileTransferResult();

        try {
            session = createSession(site);
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            // Get file attributes
            SftpATTRS attrs = channelSftp.stat(remotePath);
            result.setFileSize(attrs.getSize());

            // Download file
            if (verifyTransfer) {
                // Download to memory first for verification
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                channelSftp.get(remotePath, baos);
                byte[] fileData = baos.toByteArray();

                // Calculate checksum
                String checksum = FileChecksum.calculateMD5(new ByteArrayInputStream(fileData));
                result.setChecksum(checksum);
                result.setVerified(true);

                // Write to output stream
                outputStream.write(fileData);
            } else {
                channelSftp.get(remotePath, outputStream);
            }

            result.setSuccess(true);
            result.setRemotePath(remotePath);

            // Log transaction
            logTransaction(site, "FILE_DOWNLOAD", true,
                    "Downloaded file from " + remotePath + " (Size: " + attrs.getSize() + " bytes)");

        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());

            logTransaction(site, "FILE_DOWNLOAD", false,
                    "Failed to download from " + remotePath + ": " + e.getMessage());

            throw e;

        } finally {
            disconnect(channelSftp, session);
        }

        return result;
    }

    private Session createSession(Site site) throws JSchException {
        JSch jsch = new JSch();

        if (site.getSshKey() != null && !site.getSshKey().isEmpty()) {
            jsch.addIdentity("site-" + site.getId(),
                    site.getSshKey().getBytes(),
                    null,
                    encryptionUtil.decrypt(site.getEncryptedPassword()).getBytes());
        }

        Session session = jsch.getSession(site.getUsername(), site.getIpAddress(), site.getPort());

        if (site.getSshKey() == null || site.getSshKey().isEmpty()) {
            session.setPassword(encryptionUtil.decrypt(site.getEncryptedPassword()));
        }

        session.setConfig("StrictHostKeyChecking",
                site.getKnownHostsEntry() != null ? "yes" : "no");
        session.setTimeout(30000);
        session.connect();

        return session;
    }

    private void disconnect(ChannelSftp channelSftp, Session session) {
        if (channelSftp != null && channelSftp.isConnected()) {
            channelSftp.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    private void logTransaction(Site site, String action, boolean success, String details) {
        TransactionLog log = new TransactionLog();
        log.setSite(site);
        log.setAction(action);
        log.setSuccess(success);
        log.setDetails(details);
        log.setTimestamp( LocalDateTime.now());
        transactionLogRepository.save(log);
    }

    private String determineErrorCode(JSchException e) {
        String message = e.getMessage().toLowerCase();
        if (message.contains("auth fail") || message.contains("auth cancel")) {
            return "AUTH_FAILED";
        } else if (message.contains("timeout")) {
            return "TIMEOUT";
        } else if (message.contains("unknownhost")) {
            return "UNKNOWN_HOST";
        } else if (message.contains("connection refused")) {
            return "CONNECTION_REFUSED";
        }
        return "UNKNOWN_ERROR";
    }

    @Data
    public static class TestConnectionResult {
        private boolean success;
        private String message;
        private String errorCode;
        private long responseTime;
    }

    @Data
    public static class FileTransferResult {
        private boolean success;
        private String remotePath;
        private long fileSize;
        private String checksum;
        private boolean verified;
        private String errorMessage;
    }
}