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
package com.nearstar.sftpmanager.service;

import com.jcraft.jsch.*;
import com.nearstar.sftpmanager.model.dto.FileDTO;
import com.nearstar.sftpmanager.model.entity.Site;
import com.nearstar.sftpmanager.repository.SiteRepository;
import com.nearstar.sftpmanager.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileManagerService
{

    private final SiteRepository siteRepository;
    private final EncryptionUtil encryptionUtil;
    private final Map<String, Session> sessionCache = new HashMap<>();

    /**
     * List files in a directory
     */
    public List<FileDTO> listFiles( Long siteId, String path ) throws Exception
    {
        Site site = siteRepository.findById( siteId )
                .orElseThrow( () -> new RuntimeException( "Site not found" ) );

        ChannelSftp channelSftp = null;
        try
        {
            channelSftp = getChannel( site );

            // Normalize path
            if ( path == null || path.isEmpty() )
            {
                path = "/";
            }

            log.info( "Listing files for site {} at path: {}", site.getSiteName(), path );

            Vector<ChannelSftp.LsEntry> fileList = channelSftp.ls( path );
            List<FileDTO> files = new ArrayList<>();

            for (ChannelSftp.LsEntry entry : fileList)
            {
                // Skip . and .. directories
                if ( ".".equals( entry.getFilename() ) || "..".equals( entry.getFilename() ) )
                {
                    continue;
                }

                FileDTO fileDTO = new FileDTO();
                fileDTO.setName( entry.getFilename() );
                fileDTO.setPath( path.endsWith( "/" ) ? path + entry.getFilename() : path + "/" + entry.getFilename() );

                SftpATTRS attrs = entry.getAttrs();
                fileDTO.setDirectory( attrs.isDir() );
                fileDTO.setSize( attrs.getSize() );
                fileDTO.setPermissions( attrs.getPermissionsString() );

                // Convert modification time
                long mtime = attrs.getMTime() * 1000L; // Convert to milliseconds
                fileDTO.setModified( LocalDateTime.ofInstant( Instant.ofEpochMilli( mtime ), ZoneId.systemDefault() ) );

                files.add( fileDTO );
            }

            // Sort: directories first, then by name
            files.sort( ( a, b ) ->
            {
                if ( a.isDirectory() && !b.isDirectory() )
                {
                    return -1;
                }
                if ( !a.isDirectory() && b.isDirectory() )
                {
                    return 1;
                }
                return a.getName().compareToIgnoreCase( b.getName() );
            } );

            log.info( "Found {} files/directories", files.size() );
            return files;

        }
        finally
        {
            if ( channelSftp != null && !isSessionCached( site ) )
            {
                channelSftp.disconnect();
                if ( channelSftp.getSession() != null )
                {
                    channelSftp.getSession().disconnect();
                }
            }
        }
    }

    /**
     * Download a file
     */
    public byte[] downloadFile( Long siteId, String filePath ) throws Exception
    {
        Site site = siteRepository.findById( siteId )
                .orElseThrow( () -> new RuntimeException( "Site not found" ) );

        ChannelSftp channelSftp = null;
        try
        {
            channelSftp = getChannel( site );
            log.info( "Downloading file from site {}: {}", site.getSiteName(), filePath );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            channelSftp.get( filePath, outputStream );

            byte[] data = outputStream.toByteArray();
            log.info( "Downloaded {} bytes", data.length );
            return data;

        }
        finally
        {
            if ( channelSftp != null && !isSessionCached( site ) )
            {
                channelSftp.disconnect();
            }
        }
    }

    /**
     * Upload a file with enhanced logging and verification
     */
    public void uploadFile( Long siteId, String targetPath, MultipartFile file ) throws Exception
    {
        Site site = siteRepository.findById( siteId )
                .orElseThrow( () -> new RuntimeException( "Site not found" ) );

        ChannelSftp channelSftp = null;
        try
        {
            channelSftp = getChannel( site );

            log.info( "=== Starting Upload Process ===" );
            log.info( "Site: {}", site.getSiteName() );
            log.info( "Target Path: {}", targetPath );
            log.info( "File Name: {}", file.getOriginalFilename() );
            log.info( "File Size: {} bytes", file.getSize() );

            // Ensure the target path exists and is a directory
            try
            {
                SftpATTRS attrs = channelSftp.stat( targetPath );
                if ( !attrs.isDir() )
                {
                    throw new RuntimeException( "Target path is not a directory: " + targetPath );
                }
                log.info( "✓ Target directory exists and is valid" );
            }
            catch (SftpException e)
            {
                if ( e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE )
                {
                    log.error( "✗ Target directory does not exist: {}", targetPath );
                    throw new RuntimeException( "Target directory does not exist: " + targetPath );
                }
                throw e;
            }

            String fullPath = targetPath.endsWith( "/" )
                    ? targetPath + file.getOriginalFilename()
                    : targetPath + "/" + file.getOriginalFilename();

            log.info( "Full upload path: {}", fullPath );

            // Upload with progress monitoring
            try ( InputStream inputStream = file.getInputStream() )
            {
                channelSftp.put( inputStream, fullPath, new SftpProgressMonitor()
                {
                    private long transferred = 0;
                    private long lastLog = 0;

                    @Override
                    public void init( int op, String src, String dest, long max )
                    {
                        log.info( "Initiating transfer to: {} (size: {} bytes)", dest, max );
                    }

                    @Override
                    public boolean count( long count )
                    {
                        transferred += count;
                        // Log progress every 10MB
                        if ( transferred - lastLog >= 10 * 1024 * 1024 )
                        {
                            log.info( "Progress: {} bytes transferred", transferred );
                            lastLog = transferred;
                        }
                        return true;
                    }

                    @Override
                    public void end()
                    {
                        log.info( "✓ Transfer completed. Total transferred: {} bytes", transferred );
                    }
                }, ChannelSftp.OVERWRITE );
            }

            // Verify the file was uploaded
            log.info( "Verifying uploaded file..." );
            try
            {
                SftpATTRS uploadedAttrs = channelSftp.stat( fullPath );
                log.info( "✓ File verified on server:" );
                log.info( "  - Path: {}", fullPath );
                log.info( "  - Size on server: {} bytes", uploadedAttrs.getSize() );
                log.info( "  - Original size: {} bytes", file.getSize() );

                // Compare sizes
                if ( uploadedAttrs.getSize() != file.getSize() )
                {
                    log.warn( "⚠ WARNING: Size mismatch detected!" );
                    log.warn( "  Expected: {} bytes, Actual: {} bytes",
                            file.getSize(), uploadedAttrs.getSize() );
                }
                else
                {
                    log.info( "✓ File size matches - upload successful!" );
                }

                // List directory to double-check
                log.info( "Listing directory contents after upload:" );
                Vector<ChannelSftp.LsEntry> files = channelSftp.ls( targetPath );
                boolean found = false;
                for (ChannelSftp.LsEntry entry : files)
                {
                    if ( entry.getFilename().equals( file.getOriginalFilename() ) )
                    {
                        found = true;
                        log.info( "✓ File found in directory listing: {} ({} bytes)",
                                entry.getFilename(), entry.getAttrs().getSize() );
                        break;
                    }
                }

                if ( !found )
                {
                    log.error( "✗ File NOT found in directory listing!" );
                    throw new RuntimeException( "File not found in directory after upload" );
                }
            }
            catch (SftpException e)
            {
                log.error( "✗ Failed to verify uploaded file: {}", e.getMessage() );
                throw new RuntimeException( "File verification failed after upload", e );
            }

            log.info( "=== Upload Process Completed Successfully ===" );

        }
        catch (Exception e)
        {
            log.error( "=== Upload Failed ===" );
            log.error( "Error uploading file: {} to path: {}",
                    file.getOriginalFilename(), targetPath, e );
            throw e;
        }
        finally
        {
            if ( channelSftp != null && !isSessionCached( site ) )
            {
                channelSftp.disconnect();
            }
        }
    }

    /**
     * Upload file with progress tracking callback
     */
    public void uploadFileWithProgress( Long siteId, String targetPath, InputStream inputStream,
                                        String fileName, long fileSize, ProgressCallback progressCallback ) throws Exception
    {
        Site site = siteRepository.findById( siteId )
                .orElseThrow( () -> new RuntimeException( "Site not found" ) );

        ChannelSftp channelSftp = null;
        try
        {
            channelSftp = getChannel( site );

            String fullPath = targetPath.endsWith( "/" )
                    ? targetPath + fileName
                    : targetPath + "/" + fileName;

            log.info( "Starting tracked upload to site {}: {}", site.getSiteName(), fullPath );

            // Upload with custom progress monitor that calls our callback
            channelSftp.put( inputStream, fullPath, new SftpProgressMonitor()
            {
                private long transferred = 0;
                private long lastCallback = 0;
                private final long callbackInterval = 1024 * 1024; // Call callback every 1MB

                @Override
                public void init( int op, String src, String dest, long max )
                {
                    log.info( "Initiating tracked transfer to: {} (size: {} bytes)", dest, max );
                    if ( progressCallback != null )
                    {
                        progressCallback.onProgress( 0, fileSize );
                    }
                }

                @Override
                public boolean count( long count )
                {
                    transferred += count;

                    // Call callback at intervals to avoid too many updates
                    if ( transferred - lastCallback >= callbackInterval || transferred >= fileSize )
                    {
                        if ( progressCallback != null )
                        {
                            progressCallback.onProgress( transferred, fileSize );
                        }
                        lastCallback = transferred;

                        if ( transferred % (10 * 1024 * 1024) < count )
                        {
                            log.info( "Progress: {} / {} bytes ({:.1f}%)",
                                    transferred, fileSize,
                                    (double) transferred / fileSize * 100 );
                        }
                    }
                    return true; // Continue transfer
                }

                @Override
                public void end()
                {
                    log.info( "✓ Transfer completed. Total transferred: {} bytes", transferred );
                    if ( progressCallback != null )
                    {
                        progressCallback.onProgress( fileSize, fileSize ); // Ensure 100% is sent
                    }
                }
            }, ChannelSftp.OVERWRITE );

            // Verify upload
            SftpATTRS uploadedAttrs = channelSftp.stat( fullPath );
            log.info( "✓ File verified on server: {} (size: {} bytes)",
                    fullPath, uploadedAttrs.getSize() );

        }
        finally
        {
            if ( channelSftp != null && !isSessionCached( site ) )
            {
                channelSftp.disconnect();
            }
        }
    }

    /**
     * Upload file using stream (simple version for backward compatibility)
     */
    public void uploadFileStream( Long siteId, String path, InputStream inputStream, String fileName ) throws Exception
    {
        // Delegate to the progress version without a callback
        uploadFileWithProgress( siteId, path, inputStream, fileName, -1, null );
    }

    /**
     * Create a directory
     */
    public void createDirectory( Long siteId, String path, String directoryName ) throws Exception
    {
        Site site = siteRepository.findById( siteId )
                .orElseThrow( () -> new RuntimeException( "Site not found" ) );

        ChannelSftp channelSftp = null;
        try
        {
            channelSftp = getChannel( site );

            String fullPath = path.endsWith( "/" )
                    ? path + directoryName
                    : path + "/" + directoryName;

            log.info( "Creating directory on site {}: {}", site.getSiteName(), fullPath );
            channelSftp.mkdir( fullPath );
            log.info( "Directory created successfully: {}", fullPath );

        }
        finally
        {
            if ( channelSftp != null && !isSessionCached( site ) )
            {
                channelSftp.disconnect();
            }
        }
    }

    /**
     * Delete a file or directory
     */
    public void deleteFile( Long siteId, String filePath, boolean isDirectory ) throws Exception
    {
        Site site = siteRepository.findById( siteId )
                .orElseThrow( () -> new RuntimeException( "Site not found" ) );

        ChannelSftp channelSftp = null;
        try
        {
            channelSftp = getChannel( site );

            log.info( "Deleting {} on site {}: {}",
                    isDirectory ? "directory" : "file", site.getSiteName(), filePath );

            if ( isDirectory )
            {
                // For directories, we need to delete contents first
                deleteDirectoryRecursive( channelSftp, filePath );
            }
            else
            {
                channelSftp.rm( filePath );
            }

            log.info( "Deleted successfully: {}", filePath );

        }
        finally
        {
            if ( channelSftp != null && !isSessionCached( site ) )
            {
                channelSftp.disconnect();
            }
        }
    }

    /**
     * Rename a file or directory
     */
    public void renameFile( Long siteId, String oldPath, String newPath ) throws Exception
    {
        Site site = siteRepository.findById( siteId )
                .orElseThrow( () -> new RuntimeException( "Site not found" ) );

        ChannelSftp channelSftp = null;
        try
        {
            channelSftp = getChannel( site );

            log.info( "Renaming on site {}: {} -> {}", site.getSiteName(), oldPath, newPath );
            channelSftp.rename( oldPath, newPath );
            log.info( "Renamed successfully: {} -> {}", oldPath, newPath );

        }
        finally
        {
            if ( channelSftp != null && !isSessionCached( site ) )
            {
                channelSftp.disconnect();
            }
        }
    }

    /**
     * Get file information
     */
    public FileDTO getFileInfo( Long siteId, String filePath ) throws Exception
    {
        Site site = siteRepository.findById( siteId )
                .orElseThrow( () -> new RuntimeException( "Site not found" ) );

        ChannelSftp channelSftp = null;
        try
        {
            channelSftp = getChannel( site );

            SftpATTRS attrs = channelSftp.stat( filePath );

            FileDTO fileDTO = new FileDTO();
            fileDTO.setPath( filePath );
            fileDTO.setName( filePath.substring( filePath.lastIndexOf( '/' ) + 1 ) );
            fileDTO.setDirectory( attrs.isDir() );
            fileDTO.setSize( attrs.getSize() );
            fileDTO.setPermissions( attrs.getPermissionsString() );

            long mtime = attrs.getMTime() * 1000L;
            fileDTO.setModified( LocalDateTime.ofInstant( Instant.ofEpochMilli( mtime ), ZoneId.systemDefault() ) );

            return fileDTO;

        }
        finally
        {
            if ( channelSftp != null && !isSessionCached( site ) )
            {
                channelSftp.disconnect();
            }
        }
    }

    /**
     * Change file permissions
     */
    public void changePermissions( Long siteId, String filePath, String permissions ) throws Exception
    {
        Site site = siteRepository.findById( siteId )
                .orElseThrow( () -> new RuntimeException( "Site not found" ) );

        ChannelSftp channelSftp = null;
        try
        {
            channelSftp = getChannel( site );

            int perms = Integer.parseInt( permissions, 8 ); // Parse octal
            log.info( "Changing permissions on site {} for {}: {}",
                    site.getSiteName(), filePath, permissions );

            channelSftp.chmod( perms, filePath );
            log.info( "Permissions changed successfully" );

        }
        finally
        {
            if ( channelSftp != null && !isSessionCached( site ) )
            {
                channelSftp.disconnect();
            }
        }
    }

    /**
     * Test SFTP connection - useful for debugging
     */
    public Map<String, Object> testConnection( Long siteId ) throws Exception
    {
        Site site = siteRepository.findById( siteId )
                .orElseThrow( () -> new RuntimeException( "Site not found" ) );

        ChannelSftp channelSftp = null;
        try
        {
            channelSftp = getChannel( site );

            String pwd = channelSftp.pwd();
            String home = channelSftp.getHome();

            Map<String, Object> info = new HashMap<>();
            info.put( "connected", true );
            info.put( "currentDirectory", pwd );
            info.put( "homeDirectory", home );
            info.put( "siteName", site.getSiteName() );
            info.put( "host", site.getIpAddress() );
            info.put( "port", site.getPort() );
            info.put( "username", site.getUsername() );

            // Test write permissions in home directory
            try
            {
                String testFile = home + "/.write_test_" + System.currentTimeMillis();
                channelSftp.put( new ByteArrayInputStream( "test".getBytes() ), testFile );
                channelSftp.rm( testFile );
                info.put( "writePermission", true );
                log.info( "✓ Write permission verified in home directory" );
            }
            catch (Exception e)
            {
                info.put( "writePermission", false );
                log.warn( "✗ No write permission in home directory: {}", e.getMessage() );
            }

            return info;

        }
        finally
        {
            if ( channelSftp != null && !isSessionCached( site ) )
            {
                channelSftp.disconnect();
            }
        }
    }

    /**
     * Helper method to get SFTP channel - now public for testing
     */
    public ChannelSftp getChannel( Site site ) throws Exception
    {
        Session session = getSession( site );
        ChannelSftp channelSftp = (ChannelSftp) session.openChannel( "sftp" );
        channelSftp.connect( 30000 ); // 30 second timeout
        log.debug( "SFTP channel opened for site: {}", site.getSiteName() );
        return channelSftp;
    }

    /**
     * Helper method to get or create SSH session
     */
    private Session getSession( Site site ) throws Exception
    {
        String sessionKey = site.getId().toString();

        // Check if we have a cached session
        Session session = sessionCache.get( sessionKey );
        if ( session != null && session.isConnected() )
        {
            log.debug( "Using cached session for site: {}", site.getSiteName() );
            return session;
        }

        // Create new session
        log.info( "Creating new SSH session for site: {}", site.getSiteName() );
        JSch jsch = new JSch();
        session = jsch.getSession( site.getUsername(), site.getIpAddress(), site.getPort() );

        String password = encryptionUtil.decrypt( site.getEncryptedPassword() );
        session.setPassword( password );

        Properties config = new Properties();
        config.put( "StrictHostKeyChecking", "no" );
        config.put( "PreferredAuthentications", "password" );
        session.setConfig( config );
        session.setTimeout( 30000 ); // 30 seconds timeout

        log.info( "Connecting to {}@{}:{}", site.getUsername(), site.getIpAddress(), site.getPort() );
        session.connect();
        log.info( "✓ SSH session established successfully" );

        // Cache the session for reuse
        sessionCache.put( sessionKey, session );

        return session;
    }

    /**
     * Check if session is cached
     */
    private boolean isSessionCached( Site site )
    {
        String sessionKey = site.getId().toString();
        Session session = sessionCache.get( sessionKey );
        return session != null && session.isConnected();
    }

    /**
     * Close cached session for a site
     */
    public void closeSession( Long siteId )
    {
        String sessionKey = siteId.toString();
        Session session = sessionCache.remove( sessionKey );
        if ( session != null && session.isConnected() )
        {
            session.disconnect();
            log.info( "Closed cached session for site {}", siteId );
        }
    }

    /**
     * Close all cached sessions
     */
    public void closeAllSessions()
    {
        sessionCache.values().forEach( session ->
        {
            if ( session != null && session.isConnected() )
            {
                session.disconnect();
            }
        } );
        sessionCache.clear();
        log.info( "Closed all cached sessions" );
    }

    /**
     * Helper method to delete directory recursively
     */
    private void deleteDirectoryRecursive( ChannelSftp channelSftp, String path ) throws SftpException
    {
        Vector<ChannelSftp.LsEntry> list = channelSftp.ls( path );
        for (ChannelSftp.LsEntry entry : list)
        {
            if ( !".".equals( entry.getFilename() ) && !"..".equals( entry.getFilename() ) )
            {
                String fullPath = path + "/" + entry.getFilename();
                if ( entry.getAttrs().isDir() )
                {
                    deleteDirectoryRecursive( channelSftp, fullPath );
                }
                else
                {
                    channelSftp.rm( fullPath );
                }
            }
        }
        channelSftp.rmdir( path );
    }

    /**
     * Progress callback interface for file transfers
     */
    public interface ProgressCallback
    {
        void onProgress( long transferred, long total );
    }
}