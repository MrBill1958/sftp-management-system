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
import com.nearstar.sftpmanager.model.enums.ActionType;
import com.nearstar.sftpmanager.repository.SiteRepository;
import com.nearstar.sftpmanager.repository.TransactionLogRepository;
import com.nearstar.sftpmanager.util.EncryptionUtil;
import com.nearstar.sftpmanager.util.FileChecksum;
import com.jcraft.jsch.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileManagerService {

    private final SiteRepository siteRepository;
    private final TransactionLogRepository transactionLogRepository;
    private final EncryptionUtil encryptionUtil;
    private final AuditService auditService;

    public Map<String, Object> listFiles(Long siteId, String path, boolean detailed) {
        Site site = getSiteById(siteId);
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            session = createSession(site);
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            List<Map<String, Object>> files = new ArrayList<>();
            List<Map<String, Object>> directories = new ArrayList<>();

            @SuppressWarnings("unchecked")
            Vector<ChannelSftp.LsEntry> entries = channelSftp.ls(path);

            for (ChannelSftp.LsEntry entry : entries) {
                String filename = entry.getFilename();
                if (filename.equals(".") || filename.equals("..")) {
                    continue;
                }

                SftpATTRS attrs = entry.getAttrs();
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("name", filename);

                if (detailed) {
                    fileInfo.put("size", attrs.getSize());
                    fileInfo.put("permissions", attrs.getPermissionsString());
                    fileInfo.put("modifiedTime", new Date(attrs.getMTime() * 1000L));
                    fileInfo.put("owner", attrs.getUId());
                    fileInfo.put("group", attrs.getGId());
                }

                if (attrs.isDir()) {
                    directories.add(fileInfo);
                } else {
                    files.add(fileInfo);
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("files", files);
            result.put("directories", directories);
            result.put("path", path);

            logTransaction(site, "LIST_FILES", path, null, true, "Listed " + (files.size() + directories.size()) + " items");

            return result;

        } catch (Exception e) {
            log.error("Error listing files: {}", e.getMessage());
            logTransaction(site, "LIST_FILES", path, null, false, e.getMessage());

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;

        } finally {
            disconnect(channelSftp, session);
        }
    }

    public Map<String, Object> uploadFile(Long siteId, MultipartFile file, String remotePath,
                                          boolean verifyTransfer, boolean overwrite) throws Exception {
        Site site = getSiteById(siteId);
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            session = createSession(site);
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            // Check if file exists and overwrite is false
            if (!overwrite) {
                try {
                    channelSftp.stat(remotePath);
                    throw new IllegalArgumentException("File already exists and overwrite is disabled");
                } catch (SftpException e) {
                    // File doesn't exist, proceed
                }
            }

            // Upload file
            String checksum = null;
            long startTime = System.currentTimeMillis();

            try (InputStream inputStream = file.getInputStream()) {
                if (verifyTransfer) {
                    // Calculate checksum before upload
                    byte[] fileData = file.getBytes();
                    checksum = FileChecksum.calculateMD5(new ByteArrayInputStream(fileData));
                    channelSftp.put(new ByteArrayInputStream(fileData), remotePath, ChannelSftp.OVERWRITE);
                } else {
                    channelSftp.put(inputStream, remotePath, ChannelSftp.OVERWRITE);
                }
            }

            long uploadTime = System.currentTimeMillis() - startTime;

            // Get uploaded file info
            SftpATTRS attrs = channelSftp.stat(remotePath);

            // Verify if requested
            boolean verified = false;
            if (verifyTransfer && checksum != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                channelSftp.get(remotePath, baos);
                String remoteChecksum = FileChecksum.calculateMD5(new ByteArrayInputStream(baos.toByteArray()));
                verified = checksum.equals(remoteChecksum);

                if (!verified) {
                    // Delete the corrupted file
                    channelSftp.rm(remotePath);
                    throw new IOException("File verification failed - checksums don't match");
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("filename", file.getOriginalFilename());
            result.put("remotePath", remotePath);
            result.put("size", attrs.getSize());
            result.put("uploadTime", uploadTime);
            result.put("verified", verified);
            if (checksum != null) {
                result.put("checksum", checksum);
            }

            logTransaction(site, "UPLOAD_FILE", remotePath, checksum, true,
                    "Uploaded " + file.getOriginalFilename() + " (" + attrs.getSize() + " bytes)");

            auditService.logAction(ActionType.UPLOAD_FILE, "Site", siteId, null, remotePath,
                    "Uploaded file: " + file.getOriginalFilename());

            return result;

        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage());
            logTransaction(site, "UPLOAD_FILE", remotePath, null, false, e.getMessage());

            auditService.logFailedAction(ActionType.UPLOAD_FILE, "Site", siteId,
                    "Failed to upload file: " + file.getOriginalFilename(), e.getMessage());
            throw e;

        } finally {
            disconnect(channelSftp, session);
        }
    }

    public Resource downloadFile(Long siteId, String remotePath) throws Exception {
        Site site = getSiteById(siteId);
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            session = createSession(site);
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            // Get file info
            SftpATTRS attrs = channelSftp.stat(remotePath);
            if (attrs.isDir()) {
                throw new IllegalArgumentException("Cannot download directory");
            }

            // Download file
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            channelSftp.get(remotePath, baos);

            byte[] fileData = baos.toByteArray();
            String checksum = FileChecksum.calculateMD5(new ByteArrayInputStream(fileData));

            logTransaction(site, "DOWNLOAD_FILE", remotePath, checksum, true,
                    "Downloaded file (" + fileData.length + " bytes)");

            auditService.logAction(ActionType.DOWNLOAD_FILE, "Site", siteId, null, remotePath,
                    "Downloaded file: " + remotePath);

            return new ByteArrayResource(fileData);

        } catch (Exception e) {
            log.error("Error downloading file: {}", e.getMessage());
            logTransaction(site, "DOWNLOAD_FILE", remotePath, null, false, e.getMessage());

            auditService.logFailedAction(ActionType.DOWNLOAD_FILE, "Site", siteId,
                    "Failed to download file: " + remotePath, e.getMessage());
            throw e;

        } finally {
            disconnect(channelSftp, session);
        }
    }

    public Resource downloadMultipleAsZip(Long siteId, List<String> paths) throws Exception {
        Site site = getSiteById(siteId);
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            session = createSession(site);
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {

                for (String path : paths) {
                    try {
                        // Check if it's a file
                        SftpATTRS attrs = channelSftp.stat(path);
                        if (!attrs.isDir()) {
                            // Download and add to zip
                            ByteArrayOutputStream fileStream = new ByteArrayOutputStream();
                            channelSftp.get(path, fileStream);

                            String filename = path.substring(path.lastIndexOf('/') + 1);
                            ZipEntry entry = new ZipEntry(filename);
                            entry.setSize(attrs.getSize());
                            entry.setTime(attrs.getMTime() * 1000L);

                            zos.putNextEntry(entry);
                            zos.write(fileStream.toByteArray());
                            zos.closeEntry();
                        }
                    } catch (Exception e) {
                        log.error("Error adding file to zip: {} - {}", path, e.getMessage());
                    }
                }
            }

            byte[] zipData = baos.toByteArray();

            logTransaction(site, "DOWNLOAD_MULTIPLE", String.join(", ", paths), null, true,
                    "Downloaded " + paths.size() + " files as zip");

            return new ByteArrayResource(zipData);

        } catch (Exception e) {
            log.error("Error creating zip download: {}", e.getMessage());
            logTransaction(site, "DOWNLOAD_MULTIPLE", String.join(", ", paths), null, false, e.getMessage());
            throw e;

        } finally {
            disconnect(channelSftp, session);
        }
    }

    public boolean deleteFile(Long siteId, String remotePath, boolean recursive) throws Exception {
        Site site = getSiteById(siteId);
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            session = createSession(site);
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            // Check if it's a directory
            SftpATTRS attrs = channelSftp.stat(remotePath);

            if (attrs.isDir()) {
                if (recursive) {
                    deleteDirectoryRecursive(channelSftp, remotePath);
                } else {
                    channelSftp.rmdir(remotePath);
                }
            } else {
                channelSftp.rm(remotePath);
            }

            logTransaction(site, "DELETE_FILE", remotePath, null, true,
                    "Deleted " + (attrs.isDir() ? "directory" : "file"));

            auditService.logAction(ActionType.DELETE_FILE, "Site", siteId, null, remotePath,
                    "Deleted: " + remotePath);

            return true;

        } catch (Exception e) {
            log.error("Error deleting file: {}", e.getMessage());
            logTransaction(site, "DELETE_FILE", remotePath, null, false, e.getMessage());

            auditService.logFailedAction(ActionType.DELETE_FILE, "Site", siteId,
                    "Failed to delete: " + remotePath, e.getMessage());
            throw e;

        } finally {
            disconnect(channelSftp, session);
        }
    }

    public boolean createDirectory(Long siteId, String remotePath, boolean recursive) throws Exception {
        Site site = getSiteById(siteId);
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            session = createSession(site);
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            if (recursive) {
                createDirectoryRecursive(channelSftp, remotePath);
            } else {
                channelSftp.mkdir(remotePath);
            }

            logTransaction(site, "CREATE_DIRECTORY", remotePath, null, true,
                    "Created directory");

            auditService.logAction(ActionType.CREATE_FOLDER, "Site", siteId, null, remotePath,
                    "Created directory: " + remotePath);

            return true;

        } catch (Exception e) {
            log.error("Error creating directory: {}", e.getMessage());
            logTransaction(site, "CREATE_DIRECTORY", remotePath, null, false, e.getMessage());
            throw e;

        } finally {
            disconnect(channelSftp, session);
        }
    }

    public boolean renameFile(Long siteId, String oldPath, String newPath) throws Exception {
        Site site = getSiteById(siteId);
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            session = createSession(site);
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            channelSftp.rename(oldPath, newPath);

            logTransaction(site, "RENAME_FILE", oldPath + " -> " + newPath, null, true,
                    "Renamed file/directory");

            return true;

        } catch (Exception e) {
            log.error("Error renaming file: {}", e.getMessage());
            logTransaction(site, "RENAME_FILE", oldPath + " -> " + newPath, null, false, e.getMessage());
            throw e;

        } finally {
            disconnect(channelSftp, session);
        }
    }

    public Map<String, Object> getFileInfo(Long siteId, String remotePath) throws Exception {
        Site site = getSiteById(siteId);
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            session = createSession(site);
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            SftpATTRS attrs = channelSftp.stat(remotePath);

            Map<String, Object> info = new HashMap<>();
            info.put("name", remotePath.substring(remotePath.lastIndexOf('/') + 1));
            info.put("path", remotePath);
            info.put("size", attrs.getSize());
            info.put("isDirectory", attrs.isDir());
            info.put("permissions", attrs.getPermissionsString());
            info.put("owner", attrs.getUId());
            info.put("group", attrs.getGId());
            info.put("modifiedTime", new Date(attrs.getMTime() * 1000L));
            info.put("accessTime", new Date(attrs.getATime() * 1000L));

            return info;

        } catch (Exception e) {
            log.error("Error getting file info: {}", e.getMessage());
            throw e;

        } finally {
            disconnect(channelSftp, session);
        }
    }

    public List<Map<String, Object>> searchFiles(Long siteId, String searchTerm,
                                                 String basePath, boolean recursive) throws Exception {
        Site site = getSiteById(siteId);
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            session = createSession(site);
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            List<Map<String, Object>> results = new ArrayList<>();
            searchFilesRecursive(channelSftp, basePath, searchTerm.toLowerCase(), recursive, results);

            return results;

        } catch (Exception e) {
            log.error("Error searching files: {}", e.getMessage());
            throw e;

        } finally {
            disconnect(channelSftp, session);
        }
    }

    // Helper methods
    private Site getSiteById(Long siteId) {
        return siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Site not found"));
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

    private void deleteDirectoryRecursive(ChannelSftp channelSftp, String path) throws SftpException {
        @SuppressWarnings("unchecked")
        Vector<ChannelSftp.LsEntry> entries = channelSftp.ls(path);

        for (ChannelSftp.LsEntry entry : entries) {
            String filename = entry.getFilename();
            if (!filename.equals(".") && !filename.equals("..")) {
                String fullPath = path + "/" + filename;

                if (entry.getAttrs().isDir()) {
                    deleteDirectoryRecursive(channelSftp, fullPath);
                } else {
                    channelSftp.rm(fullPath);
                }
            }
        }

        channelSftp.rmdir(path);
    }

    private void createDirectoryRecursive(ChannelSftp channelSftp, String path) throws SftpException {
        String[] folders = path.split("/");
        String currentPath = "";

        for (String folder : folders) {
            if (folder.length() > 0) {
                currentPath += "/" + folder;
                try {
                    channelSftp.stat(currentPath);
                } catch (SftpException e) {
                    channelSftp.mkdir(currentPath);
                }
            }
        }
    }

    private void searchFilesRecursive(ChannelSftp channelSftp, String path, String searchTerm,
                                      boolean recursive, List<Map<String, Object>> results) throws SftpException {
        @SuppressWarnings("unchecked")
        Vector<ChannelSftp.LsEntry> entries = channelSftp.ls(path);

        for (ChannelSftp.LsEntry entry : entries) {
            String filename = entry.getFilename();
            if (filename.equals(".") || filename.equals("..")) {
                continue;
            }

            String fullPath = path.equals("/") ? "/" + filename : path + "/" + filename;

            if (filename.toLowerCase().contains(searchTerm)) {
                Map<String, Object> result = new HashMap<>();
                result.put("name", filename);
                result.put("path", fullPath);
                result.put("size", entry.getAttrs().getSize());
                result.put("isDirectory", entry.getAttrs().isDir());
                result.put("modifiedTime", new Date(entry.getAttrs().getMTime() * 1000L));
                results.add(result);
            }

            if (recursive && entry.getAttrs().isDir()) {
                searchFilesRecursive(channelSftp, fullPath, searchTerm, recursive, results);
            }
        }
    }

    private void logTransaction(Site site, String action, String filePath, String checksum,
                                boolean success, String details) {
        TransactionLog log = new TransactionLog();
        log.setSite(site);
        log.setAction(action);
        log.setFilePath(filePath);
        log.setChecksum(checksum);
        log.setSuccess(success);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());

        transactionLogRepository.save(log);
    }
}