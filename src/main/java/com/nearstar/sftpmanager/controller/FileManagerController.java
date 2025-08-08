/**
 * NearStar, Inc.
 * 410 E. Main Street
 * Lewisville, Texas  76057
 * Tel: 1.972.221.4068
 * <p>
 * Copyright © 2025 NearStar Incorporated. All rights reserved.
 * <p>
 * <p>
 * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF NEARSTAR Inc.
 * <p>
 * THIS COPYRIGHT NOTICE DOES NOT EVIDENCE ANY
 * ACTUAL OR INTENDED PUBLICATION OF SUCH SOURCE CODE.
 * This software and its source code are proprietary and confidential to NearStar Incorporated.
 * Unauthorized copying, modification, distribution, or use of this software, in whole or in part,
 * is strictly prohibited without the prior written consent of the copyright holder.
 * Portions of this software may utilize or be derived from open-source software
 * and publicly available frameworks licensed under their respective licenses.
 * <p>
 * This code may also include contributions developed with the assistance of AI-based tools.
 * All open-source dependencies are used in accordance with their applicable licenses,
 * and full attribution is maintained in the corresponding documentation (e.g., NOTICE or LICENSE files).
 * For inquiries regarding licensing or usage, please make request by going to nearstar.com.
 *
 * @file ${NAME}.java
 * @author ${USER} <${USER}@nearstar.com>
 * @version 1.0.0
 * @date ${DATE}
 * @project SFTP Site Management System
 * @package com.nearstar.sftpmanager
 * <p>
 * Copyright    ${YEAR} Nearstar
 * @license Proprietary
 * @modified
 */
package com.nearstar.sftpmanager.controller;

import com.jcraft.jsch.ChannelSftp;
import com.nearstar.sftpmanager.model.dto.FileDTO;
import com.nearstar.sftpmanager.model.dto.FileOperationDTO;
import com.nearstar.sftpmanager.model.entity.Site;
import com.nearstar.sftpmanager.repository.SiteRepository;
import com.nearstar.sftpmanager.service.FileManagerService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileManagerController
{

    private final FileManagerService fileManagerService;
    private final SiteRepository siteRepository;

    /**
     * List files in a directory
     */
    @GetMapping("/list/{siteId}")
    public ResponseEntity<?> listFiles(
            @PathVariable Long siteId,
            @RequestParam(defaultValue = "/") String path )
    {
        try
        {
            log.info( "Listing files for site {} at path: {}", siteId, path );
            List<FileDTO> files = fileManagerService.listFiles( siteId, path );

            Map<String, Object> response = new HashMap<>();
            response.put( "path", path );
            response.put( "files", files );
            response.put( "count", files.size() );

            return ResponseEntity.ok( response );
        }
        catch (Exception e)
        {
            log.error( "Error listing files for site {}: {}", siteId, e.getMessage(), e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to list files: " + e.getMessage() ) );
        }
    }

    /**
     * Download a file
     */
    @GetMapping("/download/{siteId}")
    public void downloadFile(
            @PathVariable Long siteId,
            @RequestParam String path,
            HttpServletResponse response )
    {
        try
        {
            log.info( "Downloading file from site {}: {}", siteId, path );

            // Get file info first to get the name
            FileDTO fileInfo = fileManagerService.getFileInfo( siteId, path );
            if ( fileInfo.isDirectory() )
            {
                response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Cannot download a directory" );
                return;
            }

            // Download file data
            byte[] fileData = fileManagerService.downloadFile( siteId, path );

            // Set response headers
            response.setContentType( "application/octet-stream" );
            response.setContentLength( fileData.length );
            response.setHeader( "Content-Disposition",
                    "attachment; filename=\"" + fileInfo.getName() + "\"" );

            // Write file data to response
            response.getOutputStream().write( fileData );
            response.getOutputStream().flush();

            log.info( "File downloaded successfully: {} ({} bytes)",
                    fileInfo.getName(), fileData.length );

        }
        catch (Exception e)
        {
            log.error( "Error downloading file from site {}: {}", siteId, e.getMessage(), e );
            try
            {
                response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        "Failed to download file: " + e.getMessage() );
            }
            catch (IOException ex)
            {
                log.error( "Error sending error response", ex );
            }
        }
    }

    /**
     * Download a file using streaming (better for large files)
     */
    @GetMapping("/download-stream/{siteId}")
    public void downloadFileStream(
            @PathVariable Long siteId,
            @RequestParam String path,
            HttpServletResponse response )
    {

        ChannelSftp channelSftp = null;
        InputStream sftpInputStream = null;

        try
        {
            log.info( "Stream downloading file from site {}: {}", siteId, path );

            // Get site and file info
            Site site = siteRepository.findById( siteId )
                    .orElseThrow( () -> new RuntimeException( "Site not found" ) );

            FileDTO fileInfo = fileManagerService.getFileInfo( siteId, path );
            if ( fileInfo.isDirectory() )
            {
                response.sendError( HttpServletResponse.SC_BAD_REQUEST, "Cannot download a directory" );
                return;
            }

            // Set response headers before streaming
            response.setContentType( "application/octet-stream" );
            response.setContentLengthLong( fileInfo.getSize() );
            response.setHeader( "Content-Disposition",
                    "attachment; filename=\"" + fileInfo.getName() + "\"" );

            // Get SFTP channel and stream file directly to response
            channelSftp = fileManagerService.getChannel( site );
            sftpInputStream = channelSftp.get( path );

            // Stream directly to response
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;

            try ( OutputStream out = response.getOutputStream() )
            {
                while ((bytesRead = sftpInputStream.read( buffer )) != -1)
                {
                    out.write( buffer, 0, bytesRead );
                    totalBytes += bytesRead;

                    // Log progress for large files (every 10MB)
                    if ( totalBytes % (10 * 1024 * 1024) == 0 )
                    {
                        log.debug( "Streamed {} MB of {}",
                                totalBytes / (1024 * 1024), fileInfo.getName() );
                    }
                }
                out.flush();
            }

            log.info( "File streamed successfully: {} ({} bytes)",
                    fileInfo.getName(), totalBytes );

        }
        catch (Exception e)
        {
            log.error( "Error streaming file from site {}: {}", siteId, e.getMessage(), e );
            try
            {
                if ( !response.isCommitted() )
                {
                    response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Failed to download file: " + e.getMessage() );
                }
            }
            catch (IOException ex)
            {
                log.error( "Error sending error response", ex );
            }
        }
        finally
        {
            // Clean up resources
            if ( sftpInputStream != null )
            {
                try
                {
                    sftpInputStream.close();
                }
                catch (IOException e)
                {
                    log.error( "Error closing SFTP input stream", e );
                }
            }
            if ( channelSftp != null && channelSftp.isConnected() )
            {
                channelSftp.disconnect();
            }
        }
    }

    /**
     * Upload a file with enhanced debugging
     */
    @PostMapping("/upload/{siteId}")
    public ResponseEntity<?> uploadFile(
            @PathVariable Long siteId,
            @RequestParam String path,
            @RequestParam("file") MultipartFile file )
    {

        // Input validation
        if ( file.isEmpty() )
        {
            log.error( "Upload attempt with empty file" );
            return ResponseEntity.badRequest()
                    .body( Map.of( "error", "File is empty" ) );
        }

        try
        {
            log.info( "=== Upload Request Received ===" );
            log.info( "Site ID: {}", siteId );
            log.info( "Target Path: {}", path );
            log.info( "File Name: {}", file.getOriginalFilename() );
            log.info( "File Size: {} bytes ({} MB)",
                    file.getSize(), file.getSize() / (1024.0 * 1024.0) );
            log.info( "Content Type: {}", file.getContentType() );

            // Call the service to perform upload
            fileManagerService.uploadFile( siteId, path, file );

            log.info( "=== Upload Request Completed Successfully ===" );
            return ResponseEntity.ok( Map.of(
                    "message", "File uploaded successfully",
                    "fileName", file.getOriginalFilename(),
                    "size", file.getSize(),
                    "path", path
            ) );

        }
        catch (Exception e)
        {
            log.error( "=== Upload Request Failed ===" );
            log.error( "Error uploading file to site {}: ", siteId, e );

            String errorMessage = e.getMessage();
            if ( e.getCause() != null )
            {
                errorMessage += " - " + e.getCause().getMessage();
            }

            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of(
                            "error", "Failed to upload file",
                            "details", errorMessage,
                            "fileName", file.getOriginalFilename()
                    ) );
        }
    }

    /**
     * Upload file using stream (alternative for large files)
     */
    @PostMapping("/upload-stream/{siteId}")
    public ResponseEntity<?> uploadFileStream(
            @PathVariable Long siteId,
            @RequestParam String path,
            @RequestParam("file") MultipartFile file )
    {
        try
        {
            log.info( "Stream uploading file to site {}: {} ({} bytes)",
                    siteId, file.getOriginalFilename(), file.getSize() );

            // Stream directly to SFTP instead of loading into memory
            try ( InputStream inputStream = file.getInputStream() )
            {
                // Use the uploadFileWithProgress method without progress callback for simple streaming
                fileManagerService.uploadFileWithProgress( siteId, path, inputStream,
                        file.getOriginalFilename(), file.getSize(), null );
            }

            return ResponseEntity.ok( Map.of(
                    "message", "File uploaded successfully (stream)",
                    "fileName", file.getOriginalFilename(),
                    "size", file.getSize()
            ) );

        }
        catch (Exception e)
        {
            log.error( "Error stream uploading file to site {}: {}", siteId, e.getMessage(), e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to upload file: " + e.getMessage() ) );
        }
    }

    /**
     * Test SFTP connection and permissions
     */
    @GetMapping("/test-connection/{siteId}")
    public ResponseEntity<?> testConnection( @PathVariable Long siteId )
    {
        try
        {
            log.info( "Testing connection for site ID: {}", siteId );
            Map<String, Object> connectionInfo = fileManagerService.testConnection( siteId );
            log.info( "Connection test successful: {}", connectionInfo );
            return ResponseEntity.ok( connectionInfo );
        }
        catch (Exception e)
        {
            log.error( "Connection test failed for site {}: ", siteId, e );
            return ResponseEntity.status( HttpStatus.SERVICE_UNAVAILABLE )
                    .body( Map.of(
                            "error", "Connection test failed",
                            "details", e.getMessage(),
                            "connected", false
                    ) );
        }
    }

    /**
     * Create a directory
     */
    @PostMapping("/mkdir/{siteId}")
    public ResponseEntity<?> createDirectory(
            @PathVariable Long siteId,
            @RequestBody FileOperationDTO operation )
    {
        try
        {
            log.info( "Creating directory on site {}: {}/{}",
                    siteId, operation.getPath(), operation.getName() );

            fileManagerService.createDirectory( siteId, operation.getPath(), operation.getName() );

            return ResponseEntity.ok( Map.of(
                    "message", "Directory created successfully",
                    "path", operation.getPath() + "/" + operation.getName()
            ) );
        }
        catch (Exception e)
        {
            log.error( "Error creating directory on site {}: {}", siteId, e.getMessage(), e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to create directory: " + e.getMessage() ) );
        }
    }

    /**
     * Delete a file or directory
     */
    @DeleteMapping("/delete/{siteId}")
    public ResponseEntity<?> deleteFile(
            @PathVariable Long siteId,
            @RequestParam String path,
            @RequestParam(defaultValue = "false") boolean isDirectory )
    {
        try
        {
            log.info( "Deleting {} on site {}: {}",
                    isDirectory ? "directory" : "file", siteId, path );

            fileManagerService.deleteFile( siteId, path, isDirectory );

            return ResponseEntity.ok( Map.of(
                    "message", (isDirectory ? "Directory" : "File") + " deleted successfully",
                    "path", path
            ) );
        }
        catch (Exception e)
        {
            log.error( "Error deleting on site {}: {}", siteId, e.getMessage(), e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to delete: " + e.getMessage() ) );
        }
    }

    /**
     * Rename a file or directory
     */
    @PutMapping("/rename/{siteId}")
    public ResponseEntity<?> renameFile(
            @PathVariable Long siteId,
            @RequestBody FileOperationDTO operation )
    {
        try
        {
            log.info( "Renaming on site {}: {} -> {}",
                    siteId, operation.getPath(), operation.getNewPath() );

            fileManagerService.renameFile( siteId, operation.getPath(), operation.getNewPath() );

            return ResponseEntity.ok( Map.of(
                    "message", "Renamed successfully",
                    "oldPath", operation.getPath(),
                    "newPath", operation.getNewPath()
            ) );
        }
        catch (Exception e)
        {
            log.error( "Error renaming on site {}: {}", siteId, e.getMessage(), e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to rename: " + e.getMessage() ) );
        }
    }

    /**
     * Change file permissions
     */
    @PutMapping("/chmod/{siteId}")
    public ResponseEntity<?> changePermissions(
            @PathVariable Long siteId,
            @RequestBody FileOperationDTO operation )
    {
        try
        {
            log.info( "Changing permissions on site {} for {}: {}",
                    siteId, operation.getPath(), operation.getPermissions() );

            fileManagerService.changePermissions( siteId, operation.getPath(), operation.getPermissions() );

            return ResponseEntity.ok( Map.of(
                    "message", "Permissions changed successfully",
                    "path", operation.getPath(),
                    "permissions", operation.getPermissions()
            ) );
        }
        catch (Exception e)
        {
            log.error( "Error changing permissions on site {}: {}", siteId, e.getMessage(), e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to change permissions: " + e.getMessage() ) );
        }
    }

    /**
     * Get file information
     */
    @GetMapping("/info/{siteId}")
    public ResponseEntity<?> getFileInfo(
            @PathVariable Long siteId,
            @RequestParam String path )
    {
        try
        {
            log.info( "Getting file info for site {}: {}", siteId, path );
            FileDTO fileInfo = fileManagerService.getFileInfo( siteId, path );
            return ResponseEntity.ok( fileInfo );
        }
        catch (Exception e)
        {
            log.error( "Error getting file info for site {}: {}", siteId, e.getMessage(), e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to get file info: " + e.getMessage() ) );
        }
    }

    /**
     * Close session for a site (cleanup)
     */
    @PostMapping("/close-session/{siteId}")
    public ResponseEntity<?> closeSession( @PathVariable Long siteId )
    {
        try
        {
            fileManagerService.closeSession( siteId );
            log.info( "Session closed for site {}", siteId );
            return ResponseEntity.ok( Map.of( "message", "Session closed" ) );
        }
        catch (Exception e)
        {
            log.error( "Error closing session for site {}: {}", siteId, e.getMessage() );
            return ResponseEntity.ok( Map.of( "message", "Session close attempted" ) );
        }
    }

    /**
     * Clear all cached sessions (useful for debugging)
     */
    @PostMapping("/clear-all-sessions")
    public ResponseEntity<?> clearAllSessions()
    {
        try
        {
            fileManagerService.closeAllSessions();
            log.info( "All sessions cleared" );
            return ResponseEntity.ok( Map.of( "message", "All sessions cleared" ) );
        }
        catch (Exception e)
        {
            log.error( "Error clearing sessions: {}", e.getMessage() );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to clear sessions: " + e.getMessage() ) );
        }
    }

    /**
     * Get site information (for debugging)
     */
    @GetMapping("/site-info/{siteId}")
    public ResponseEntity<?> getSiteInfo( @PathVariable Long siteId )
    {
        try
        {
            Site site = siteRepository.findById( siteId )
                    .orElseThrow( () -> new RuntimeException( "Site not found" ) );

            Map<String, Object> info = new HashMap<>();
            info.put( "id", site.getId() );
            info.put( "name", site.getSiteName() );
            info.put( "host", site.getIpAddress() );
            info.put( "port", site.getPort() );
            info.put( "username", site.getUsername() );

            return ResponseEntity.ok( info );
        }
        catch (Exception e)
        {
            log.error( "Error getting site info: {}", e.getMessage() );
            return ResponseEntity.status( HttpStatus.NOT_FOUND )
                    .body( Map.of( "error", "Site not found" ) );
        }
    }
}