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

import com.nearstar.sftpmanager.model.entity.ScheduledTask;
import com.nearstar.sftpmanager.service.SchedulerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class SchedulerController
{

    private final SchedulerService schedulerService;

    @GetMapping("/tasks")
    public ResponseEntity<List<ScheduledTask>> getAllTasks()
    {
        try
        {
            List<ScheduledTask> tasks = schedulerService.getAllTasks();
            return ResponseEntity.ok( tasks );
        }
        catch (Exception e)
        {
            log.error( "Error fetching scheduled tasks", e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR ).build();
        }
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<ScheduledTask> getTaskById( @PathVariable Long id )
    {
        try
        {
            return schedulerService.getTaskById( id )
                    .map( ResponseEntity::ok )
                    .orElse( ResponseEntity.notFound().build() );
        }
        catch (Exception e)
        {
            log.error( "Error fetching task with id: {}", id, e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR ).build();
        }
    }

    @PostMapping("/tasks")
    public ResponseEntity<?> createTask( @Valid @RequestBody ScheduledTask task )
    {
        try
        {
            ScheduledTask createdTask = schedulerService.createTask( task );
            return ResponseEntity.status( HttpStatus.CREATED ).body( createdTask );
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().body( Map.of( "error", e.getMessage() ) );
        }
        catch (Exception e)
        {
            log.error( "Error creating scheduled task", e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to create task" ) );
        }
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<?> updateTask( @PathVariable Long id, @Valid @RequestBody ScheduledTask task )
    {
        try
        {
            task.setId( id );
            ScheduledTask updatedTask = schedulerService.updateTask( task );
            return ResponseEntity.ok( updatedTask );
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().body( Map.of( "error", e.getMessage() ) );
        }
        catch (Exception e)
        {
            log.error( "Error updating task with id: {}", id, e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to update task" ) );
        }
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<?> deleteTask( @PathVariable Long id )
    {
        try
        {
            schedulerService.deleteTask( id );
            return ResponseEntity.ok( Map.of( "message", "Task deleted successfully" ) );
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.notFound().build();
        }
        catch (Exception e)
        {
            log.error( "Error deleting task with id: {}", id, e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to delete task" ) );
        }
    }

    @PostMapping("/tasks/{id}/toggle")
    public ResponseEntity<?> toggleTask( @PathVariable Long id, @RequestParam boolean enable )
    {
        try
        {
            schedulerService.toggleTask( id, enable );
            String status = enable ? "enabled" : "disabled";
            return ResponseEntity.ok( Map.of( "message", "Task " + status + " successfully" ) );
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.notFound().build();
        }
        catch (Exception e)
        {
            log.error( "Error toggling task with id: {}", id, e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to toggle task" ) );
        }
    }

    @PostMapping("/tasks/{id}/execute")
    public ResponseEntity<?> executeTaskNow( @PathVariable Long id )
    {
        try
        {
            Map<String, Object> result = schedulerService.executeTaskNow( id );
            return ResponseEntity.ok( result );
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.notFound().build();
        }
        catch (Exception e)
        {
            log.error( "Error executing task with id: {}", id, e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to execute task", "details", e.getMessage() ) );
        }
    }

    @GetMapping("/tasks/{id}/history")
    public ResponseEntity<?> getTaskHistory( @PathVariable Long id,
                                             @RequestParam(defaultValue = "10") int limit )
    {
        try
        {
            List<Map<String, Object>> history = schedulerService.getTaskExecutionHistory( id, limit );
            return ResponseEntity.ok( history );
        }
        catch (Exception e)
        {
            log.error( "Error fetching task history for id: {}", id, e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to fetch task history" ) );
        }
    }

    @GetMapping("/next-executions")
    public ResponseEntity<?> getNextScheduledExecutions( @RequestParam(defaultValue = "10") int limit )
    {
        try
        {
            List<Map<String, Object>> nextExecutions = schedulerService.getNextScheduledExecutions( limit );
            return ResponseEntity.ok( nextExecutions );
        }
        catch (Exception e)
        {
            log.error( "Error fetching next scheduled executions", e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to fetch next executions" ) );
        }
    }

    @PostMapping("/validate-script")
    public ResponseEntity<?> validateJythonScript( @RequestBody Map<String, String> request )
    {
        try
        {
            String script = request.get( "script" );
            Map<String, Object> validationResult = schedulerService.validateJythonScript( script );
            return ResponseEntity.ok( validationResult );
        }
        catch (Exception e)
        {
            log.error( "Error validating script", e );
            return ResponseEntity.status( HttpStatus.INTERNAL_SERVER_ERROR )
                    .body( Map.of( "error", "Failed to validate script" ) );
        }
    }
}