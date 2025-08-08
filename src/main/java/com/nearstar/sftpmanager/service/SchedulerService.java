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
package com.nearstar.sftpmanager.service;

import com.nearstar.sftpmanager.model.entity.ScheduledTask;
import com.nearstar.sftpmanager.model.entity.Site;
import com.nearstar.sftpmanager.repository.ScheduledTaskRepository;
import com.nearstar.sftpmanager.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService
{

    private final ScheduledTaskRepository taskRepository;
    private final SiteRepository siteRepository;
    private final JythonExecutorService jythonExecutorService;
    private final Scheduler quartzScheduler;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern( "HH:mm" );

    @Transactional(readOnly = true)
    public List<ScheduledTask> getAllTasks()
    {
        return taskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ScheduledTask> getTaskById( Long id )
    {
        return taskRepository.findById( id );
    }

    @Transactional
    public ScheduledTask createTask( ScheduledTask task )
    {
        // Validate site exists
        Site site = siteRepository.findById( task.getSite().getId() )
                .orElseThrow( () -> new IllegalArgumentException( "Site not found" ) );
        task.setSite( site );

        // Save task
        ScheduledTask savedTask = taskRepository.save( task );

        // Schedule with Quartz if enabled
        if ( task.isEnabled() )
        {
            scheduleTask( savedTask );
        }

        log.info( "Created scheduled task: {}", savedTask.getTaskName() );
        return savedTask;
    }

    @Transactional
    public ScheduledTask updateTask( ScheduledTask task )
    {
        ScheduledTask existingTask = taskRepository.findById( task.getId() )
                .orElseThrow( () -> new IllegalArgumentException( "Task not found" ) );

        // Update fields
        existingTask.setTaskName( task.getTaskName() );
        existingTask.setSite( task.getSite() );
        existingTask.setDaysOfWeek( task.getDaysOfWeek() );
        existingTask.setExecutionTime( task.getExecutionTime() );
        existingTask.setJythonScript( task.getJythonScript() );
        existingTask.setCommandLineParams( task.getCommandLineParams() );
        existingTask.setEnabled( task.isEnabled() );

        ScheduledTask updatedTask = taskRepository.save( existingTask );

        // Reschedule with Quartz
        unscheduleTask( updatedTask.getId() );
        if ( updatedTask.isEnabled() )
        {
            scheduleTask( updatedTask );
        }

        log.info( "Updated scheduled task: {}", updatedTask.getTaskName() );
        return updatedTask;
    }

    @Transactional
    public void deleteTask( Long id )
    {
        ScheduledTask task = taskRepository.findById( id )
                .orElseThrow( () -> new IllegalArgumentException( "Task not found" ) );

        // Remove from Quartz
        unscheduleTask( id );

        // Delete from database
        taskRepository.delete( task );
        log.info( "Deleted scheduled task: {}", task.getTaskName() );
    }

    @Transactional
    public void toggleTask( Long id, boolean enable )
    {
        ScheduledTask task = taskRepository.findById( id )
                .orElseThrow( () -> new IllegalArgumentException( "Task not found" ) );

        task.setEnabled( enable );
        taskRepository.save( task );

        if ( enable )
        {
            scheduleTask( task );
        }
        else
        {
            unscheduleTask( id );
        }

        log.info( "Task {} {}", task.getTaskName(), enable ? "enabled" : "disabled" );
    }

    @Transactional
    public Map<String, Object> executeTaskNow( Long taskId )
    {
        ScheduledTask task = taskRepository.findById( taskId )
                .orElseThrow( () -> new IllegalArgumentException( "Task not found" ) );

        log.info( "Executing task manually: {}", task.getTaskName() );
        JythonExecutorService.ExecutionResult result = jythonExecutorService.executeScript( task );

        // Update task execution info
        task.setLastExecution( LocalDateTime.now() );
        task.setLastExecutionStatus( result.isSuccess() ? "SUCCESS" : "FAILED: " + result.getError() );
        taskRepository.save( task );

        Map<String, Object> response = new HashMap<>();
        response.put( "success", result.isSuccess() );
        response.put( "message", result.isSuccess() ? "Task executed successfully" : "Task execution failed" );
        response.put( "output", result.getOutput() );
        response.put( "error", result.getError() );

        return response;
    }

    public List<Map<String, Object>> getTaskExecutionHistory( Long taskId, int limit )
    {
        // In a full implementation, this would query a separate execution history table
        List<Map<String, Object>> history = new ArrayList<>();
        ScheduledTask task = taskRepository.findById( taskId ).orElse( null );

        if ( task != null && task.getLastExecution() != null )
        {
            Map<String, Object> lastExecution = new HashMap<>();
            lastExecution.put( "executionTime", task.getLastExecution() );
            lastExecution.put( "status", task.getLastExecutionStatus() );
            lastExecution.put( "taskName", task.getTaskName() );
            history.add( lastExecution );
        }

        return history;
    }

    public List<Map<String, Object>> getNextScheduledExecutions( int limit )
    {
        List<ScheduledTask> enabledTasks = taskRepository.findByEnabledTrue();
        List<Map<String, Object>> nextExecutions = new ArrayList<>();

        for (ScheduledTask task : enabledTasks)
        {
            LocalDateTime nextRun = calculateNextExecution( task );
            if ( nextRun != null )
            {
                Map<String, Object> execution = new HashMap<>();
                execution.put( "taskId", task.getId() );
                execution.put( "taskName", task.getTaskName() );
                execution.put( "siteName", task.getSite().getSiteName() );
                execution.put( "nextExecution", nextRun );
                nextExecutions.add( execution );
            }
        }

        // Sort by next execution time
        nextExecutions.sort( ( a, b ) ->
        {
            LocalDateTime timeA = (LocalDateTime) a.get( "nextExecution" );
            LocalDateTime timeB = (LocalDateTime) b.get( "nextExecution" );
            return timeA.compareTo( timeB );
        } );

        // Limit results
        return nextExecutions.stream()
                .limit( limit )
                .collect( Collectors.toList() );
    }

    public Map<String, Object> validateJythonScript( String script )
    {
        Map<String, Object> result = new HashMap<>();
        try
        {
            // Basic syntax validation
            // In a real implementation, you would use Jython's parser
            if ( script == null || script.trim().isEmpty() )
            {
                throw new IllegalArgumentException( "Script cannot be empty" );
            }

            // Check for required imports
            if ( !script.contains( "from com.jcraft.jsch import" ) &&
                    !script.contains( "import com.jcraft.jsch" ) )
            {
                result.put( "warning", "Script doesn't import JSch library" );
            }

            result.put( "valid", true );
            result.put( "message", "Script validation passed" );
        }
        catch (Exception e)
        {
            result.put( "valid", false );
            result.put( "error", e.getMessage() );
        }

        return result;
    }

    // Quartz scheduling methods
    private void scheduleTask( ScheduledTask task )
    {
        try
        {
            JobDetail jobDetail = JobBuilder.newJob( SftpTaskJob.class )
                    .withIdentity( "task-" + task.getId(), "sftp-tasks" )
                    .usingJobData( "taskId", task.getId() )
                    .build();

            // Create trigger based on schedule
            Trigger trigger = createTrigger( task );

            quartzScheduler.scheduleJob( jobDetail, trigger );
            log.info( "Scheduled task {} with Quartz", task.getTaskName() );
        }
        catch (SchedulerException e)
        {
            log.error( "Error scheduling task with Quartz", e );
        }
    }

    private void unscheduleTask( Long taskId )
    {
        try
        {
            JobKey jobKey = new JobKey( "task-" + taskId, "sftp-tasks" );
            quartzScheduler.deleteJob( jobKey );
            log.info( "Unscheduled task {} from Quartz", taskId );
        }
        catch (SchedulerException e)
        {
            log.error( "Error unscheduling task from Quartz", e );
        }
    }

    private Trigger createTrigger( ScheduledTask task )
    {
        // Parse days of week
        Set<Integer> daysOfWeek = new HashSet<>();
        for (String day : task.getDaysOfWeek().split( "," ))
        {
            switch (day.trim())
            {
                case "MON":
                    daysOfWeek.add( 2 );
                    break;
                case "TUE":
                    daysOfWeek.add( 3 );
                    break;
                case "WED":
                    daysOfWeek.add( 4 );
                    break;
                case "THU":
                    daysOfWeek.add( 5 );
                    break;
                case "FRI":
                    daysOfWeek.add( 6 );
                    break;
                case "SAT":
                    daysOfWeek.add( 7 );
                    break;
                case "SUN":
                    daysOfWeek.add( 1 );
                    break;
            }
        }

        // Create cron expression
        LocalTime time = task.getExecutionTime();
        String cronExpression = String.format( "0 %d %d ? * %s",
                time.getMinute(),
                time.getHour(),
                daysOfWeek.stream().map( String::valueOf ).collect( Collectors.joining( "," ) ) );

        return TriggerBuilder.newTrigger()
                .withIdentity( "trigger-" + task.getId(), "sftp-tasks" )
                .withSchedule( CronScheduleBuilder.cronSchedule( cronExpression ) )
                .build();
    }

    private LocalDateTime calculateNextExecution( ScheduledTask task )
    {
        LocalDateTime now = LocalDateTime.now();
        LocalTime executionTime = task.getExecutionTime();

        Set<DayOfWeek> scheduledDays = new HashSet<>();
        for (String day : task.getDaysOfWeek().split( "," ))
        {
            switch (day.trim())
            {
                case "MON":
                    scheduledDays.add( DayOfWeek.MONDAY );
                    break;
                case "TUE":
                    scheduledDays.add( DayOfWeek.TUESDAY );
                    break;
                case "WED":
                    scheduledDays.add( DayOfWeek.WEDNESDAY );
                    break;
                case "THU":
                    scheduledDays.add( DayOfWeek.THURSDAY );
                    break;
                case "FRI":
                    scheduledDays.add( DayOfWeek.FRIDAY );
                    break;
                case "SAT":
                    scheduledDays.add( DayOfWeek.SATURDAY );
                    break;
                case "SUN":
                    scheduledDays.add( DayOfWeek.SUNDAY );
                    break;
            }
        }

        // Find next execution
        LocalDateTime nextRun = now.with( executionTime );

        // If time has passed today, start from tomorrow
        if ( nextRun.isBefore( now ) || !scheduledDays.contains( now.getDayOfWeek() ) )
        {
            nextRun = nextRun.plusDays( 1 );
        }

        // Find next scheduled day
        int daysChecked = 0;
        while (!scheduledDays.contains( nextRun.getDayOfWeek() ) && daysChecked < 7)
        {
            nextRun = nextRun.plusDays( 1 );
            daysChecked++;
        }

        return daysChecked < 7 ? nextRun : null;
    }

    // Scheduled method to check and execute tasks
    @Scheduled(fixedDelay = 60000) // Check every minute
    public void checkAndExecuteTasks()
    {
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledTask> enabledTasks = taskRepository.findByEnabledTrue();

        for (ScheduledTask task : enabledTasks)
        {
            if ( shouldExecuteNow( task, now ) )
            {
                executeTask( task );
            }
        }
    }

    private boolean shouldExecuteNow( ScheduledTask task, LocalDateTime now )
    {
        // Check if it's the right day and time
        String currentDay = now.getDayOfWeek().name().substring( 0, 3 );
        LocalTime currentTime = now.toLocalTime();
        LocalTime scheduledTime = task.getExecutionTime();

        return task.getDaysOfWeek().contains( currentDay ) &&
                currentTime.getHour() == scheduledTime.getHour() &&
                currentTime.getMinute() == scheduledTime.getMinute() &&
                (task.getLastExecution() == null ||
                        Duration.between( task.getLastExecution(), now ).toMinutes() > 1);
    }

    private void executeTask( ScheduledTask task )
    {
        log.info( "Executing scheduled task: {}", task.getTaskName() );
        executeTaskNow( task.getId() );
    }

    // Quartz Job implementation
    public static class SftpTaskJob implements Job
    {
        @Override
        public void execute( JobExecutionContext context ) throws JobExecutionException
        {
            // The actual execution is handled by the service
            Long taskId = context.getJobDetail().getJobDataMap().getLong( "taskId" );
            log.info( "Quartz executing task ID: {}", taskId );
        }
    }
}