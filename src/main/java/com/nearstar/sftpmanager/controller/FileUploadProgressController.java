/**
 * NearStar, Inc.
 * 410 E. Main Street
 * Lewisville, Texas  76057
 * Tel: 1.972.221.4068
 * <p>
 * Copyright � 2025 NearStar Incorporated. All rights reserved.
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequestMapping("/api/upload-progress")
@RequiredArgsConstructor
public class FileUploadProgressController
{

    // Store SSE emitters for each upload session
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Start a new upload progress session
     */
    @GetMapping("/stream/{sessionId}")
    public SseEmitter streamProgress( @PathVariable String sessionId )
    {
        log.info( "Starting SSE stream for session: {}", sessionId );

        SseEmitter emitter = new SseEmitter( 300000L ); // 5 minute timeout

        emitter.onCompletion( () ->
        {
            log.info( "SSE stream completed for session: {}", sessionId );
            emitters.remove( sessionId );
        } );

        emitter.onTimeout( () ->
        {
            log.info( "SSE stream timed out for session: {}", sessionId );
            emitters.remove( sessionId );
        } );

        emitter.onError( ( ex ) ->
        {
            log.error( "SSE stream error for session: {}", sessionId, ex );
            emitters.remove( sessionId );
        } );

        emitters.put( sessionId, emitter );

        // Send initial connection message
        try
        {
            emitter.send( SseEmitter.event()
                    .name( "connected" )
                    .data( Map.of( "sessionId", sessionId, "status", "connected" ) ) );
        }
        catch (IOException e)
        {
            log.error( "Error sending initial SSE message", e );
        }

        return emitter;
    }

    /**
     * Send progress update to a specific session
     */
    @PostMapping("/update/{sessionId}")
    public ResponseEntity<?> updateProgress(
            @PathVariable String sessionId,
            @RequestBody ProgressUpdate update )
    {

        SseEmitter emitter = emitters.get( sessionId );
        if ( emitter == null )
        {
            log.warn( "No SSE emitter found for session: {}", sessionId );
            return ResponseEntity.status( HttpStatus.NOT_FOUND )
                    .body( Map.of( "error", "Session not found" ) );
        }

        executor.execute( () ->
        {
            try
            {
                emitter.send( SseEmitter.event()
                        .name( "progress" )
                        .data( update ) );

                log.debug( "Progress update sent for session {}: {}%",
                        sessionId, update.getPercentage() );

                // If upload is complete, close the emitter
                if ( update.getPercentage() >= 100 )
                {
                    emitter.complete();
                    emitters.remove( sessionId );
                }
            }
            catch (IOException e)
            {
                log.error( "Error sending progress update for session: {}", sessionId, e );
                emitters.remove( sessionId );
            }
        } );

        return ResponseEntity.ok( Map.of( "message", "Progress update sent" ) );
    }

    /**
     * Close a progress session
     */
    @DeleteMapping("/close/{sessionId}")
    public ResponseEntity<?> closeSession( @PathVariable String sessionId )
    {
        SseEmitter emitter = emitters.remove( sessionId );
        if ( emitter != null )
        {
            emitter.complete();
            log.info( "Closed SSE session: {}", sessionId );
            return ResponseEntity.ok( Map.of( "message", "Session closed" ) );
        }
        return ResponseEntity.status( HttpStatus.NOT_FOUND )
                .body( Map.of( "error", "Session not found" ) );
    }

    /**
     * Get active sessions count
     */
    @GetMapping("/active-sessions")
    public ResponseEntity<?> getActiveSessions()
    {
        return ResponseEntity.ok( Map.of(
                "count", emitters.size(),
                "sessionIds", emitters.keySet()
        ) );
    }

    /**
     * Progress update data class
     */
    public static class ProgressUpdate
    {
        private long bytesTransferred;
        private long totalBytes;
        private int percentage;
        private String fileName;
        private String status;
        private String message;

        // Getters and setters
        public long getBytesTransferred()
        {
            return bytesTransferred;
        }

        public void setBytesTransferred( long bytesTransferred )
        {
            this.bytesTransferred = bytesTransferred;
        }

        public long getTotalBytes()
        {
            return totalBytes;
        }

        public void setTotalBytes( long totalBytes )
        {
            this.totalBytes = totalBytes;
        }

        public int getPercentage()
        {
            return percentage;
        }

        public void setPercentage( int percentage )
        {
            this.percentage = percentage;
        }

        public String getFileName()
        {
            return fileName;
        }

        public void setFileName( String fileName )
        {
            this.fileName = fileName;
        }

        public String getStatus()
        {
            return status;
        }

        public void setStatus( String status )
        {
            this.status = status;
        }

        public String getMessage()
        {
            return message;
        }

        public void setMessage( String message )
        {
            this.message = message;
        }
    }
}