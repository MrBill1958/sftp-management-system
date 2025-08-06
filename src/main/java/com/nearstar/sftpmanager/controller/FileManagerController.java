package com.nearstar.sftpmanager.controller;

import com.nearstar.sftpmanager.model.dto.FileTransferRequest;
import com.nearstar.sftpmanager.model.entity.Site;
import com.nearstar.sftpmanager.service.FileManagerService;
import com.nearstar.sftpmanager.service.SiteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileManagerController {

    private final FileManagerService fileManagerService;
    private final SiteService siteService;

    @GetMapping("/{siteId}/list")
    @PreAuthorize("@siteService.hasAccessToSite(#siteId, authentication)")
    public ResponseEntity<Map<String, Object>> listFiles(
            @PathVariable Long siteId,
            @RequestParam(defaultValue = "/") String path,
            @RequestParam(defaultValue = "false") boolean detailed) {

        try {
            Map<String, Object> result = fileManagerService.listFiles(siteId, path, detailed);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error listing files for site {}: {}", siteId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to list files: " + e.getMessage()));
        }
    }

    @PostMapping("/{siteId}/upload")
    @PreAuthorize("@siteService.hasAccessToSite(#siteId, authentication)")
    public ResponseEntity<?> uploadFile(
            @PathVariable Long siteId,
            @RequestParam("file") MultipartFile file,
            @RequestParam String path,
            @RequestParam(defaultValue = "true") boolean verify,
            @RequestParam(defaultValue = "false") boolean overwrite) {

        try {
            FileTransferRequest request = new FileTransferRequest();
            request.setSiteId(siteId);
            request.setRemotePath(path);
            request.setVerifyTransfer(verify);

            Map<String, Object> result = fileManagerService.uploadFile(
                    siteId, file, path, verify, overwrite);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error uploading file to site {}: {}", siteId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/{siteId}/download")
    @PreAuthorize("@siteService.hasAccessToSite(#siteId, authentication)")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long siteId,
            @RequestParam String path) {

        try {
            Resource resource = fileManagerService.downloadFile(siteId, path);
            String filename = path.substring(path.lastIndexOf('/') + 1);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading file from site {}: {}", siteId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{siteId}/download-multiple")
    @PreAuthorize("@siteService.hasAccessToSite(#siteId, authentication)")
    public ResponseEntity<Resource> downloadMultiple(
            @PathVariable Long siteId,
            @RequestBody List<String> paths) {

        try {
            Resource resource = fileManagerService.downloadMultipleAsZip(siteId, paths);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"download.zip\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading multiple files from site {}: {}", siteId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{siteId}/delete")
    @PreAuthorize("@siteService.hasAccessToSite(#siteId, authentication)")
    public ResponseEntity<?> deleteFile(
            @PathVariable Long siteId,
            @RequestParam String path,
            @RequestParam(defaultValue = "false") boolean recursive) {

        try {
            boolean deleted = fileManagerService.deleteFile(siteId, path, recursive);

            return ResponseEntity.ok(Map.of(
                    "success", deleted,
                    "message", deleted ? "File deleted successfully" : "Failed to delete file"
            ));

        } catch (Exception e) {
            log.error("Error deleting file from site {}: {}", siteId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Delete failed: " + e.getMessage()));
        }
    }

    @PostMapping("/{siteId}/mkdir")
    @PreAuthorize("@siteService.hasAccessToSite(#siteId, authentication)")
    public ResponseEntity<?> createDirectory(
            @PathVariable Long siteId,
            @RequestParam String path,
            @RequestParam(defaultValue = "false") boolean recursive) {

        try {
            boolean created = fileManagerService.createDirectory(siteId, path, recursive);

            return ResponseEntity.ok(Map.of(
                    "success", created,
                    "message", created ? "Directory created successfully" : "Failed to create directory"
            ));

        } catch (Exception e) {
            log.error("Error creating directory on site {}: {}", siteId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Create directory failed: " + e.getMessage()));
        }
    }

    @PostMapping("/{siteId}/rename")
    @PreAuthorize("@siteService.hasAccessToSite(#siteId, authentication)")
    public ResponseEntity<?> renameFile(
            @PathVariable Long siteId,
            @RequestParam String oldPath,
            @RequestParam String newPath) {

        try {
            boolean renamed = fileManagerService.renameFile(siteId, oldPath, newPath);

            return ResponseEntity.ok(Map.of(
                    "success", renamed,
                    "message", renamed ? "File renamed successfully" : "Failed to rename file"
            ));

        } catch (Exception e) {
            log.error("Error renaming file on site {}: {}", siteId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Rename failed: " + e.getMessage()));
        }
    }

    @GetMapping("/{siteId}/info")
    @PreAuthorize("@siteService.hasAccessToSite(#siteId, authentication)")
    public ResponseEntity<?> getFileInfo(
            @PathVariable Long siteId,
            @RequestParam String path) {

        try {
            Map<String, Object> fileInfo = fileManagerService.getFileInfo(siteId, path);
            return ResponseEntity.ok(fileInfo);

        } catch (Exception e) {
            log.error("Error getting file info from site {}: {}", siteId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get file info: " + e.getMessage()));
        }
    }

    @PostMapping("/{siteId}/search")
    @PreAuthorize("@siteService.hasAccessToSite(#siteId, authentication)")
    public ResponseEntity<?> searchFiles(
            @PathVariable Long siteId,
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "/") String basePath,
            @RequestParam(defaultValue = "false") boolean recursive) {

        try {
            List<Map<String, Object>> results = fileManagerService.searchFiles(
                    siteId, searchTerm, basePath, recursive);

            return ResponseEntity.ok(Map.of(
                    "results", results,
                    "count", results.size()
            ));

        } catch (Exception e) {
            log.error("Error searching files on site {}: {}", siteId, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Search failed: " + e.getMessage()));
        }
    }
}