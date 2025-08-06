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
package com.nearstar.sftpmanager.config;

import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {

    @Value("${scheduler.thread.pool.size:10}")
    private int schedulerThreadPoolSize;

    @Value("${scheduler.thread.name.prefix:SFTPScheduler-}")
    private String schedulerThreadNamePrefix;

    @Value("${scheduler.await.termination.seconds:60}")
    private int awaitTerminationSeconds;

    /**
     * Configures the task scheduler with a thread pool
     * THIS METHOD IS REQUIRED BY SchedulingConfigurer
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskExecutor());
    }

    /**
     * Task executor for scheduled tasks
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.setPoolSize(schedulerThreadPoolSize);
        executor.setThreadNamePrefix(schedulerThreadNamePrefix);
        executor.setAwaitTerminationSeconds(awaitTerminationSeconds);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setRejectedExecutionHandler((r, executor1) -> {
            // Log rejected tasks
            System.err.println("Task rejected: " + r.toString());
        });
        executor.initialize();
        return executor;
    }

    /**
     * Job factory for Quartz
     */
    @Bean
    public JobFactory jobFactory(ApplicationContext applicationContext) {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    /**
     * Quartz scheduler factory bean - UPDATED TO USE MEMORY STORE
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(
            JobFactory jobFactory,
            @Qualifier("quartzProperties") Properties quartzProperties) {

        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        // REMOVED: factory.setDataSource(dataSource); - No longer needed for memory store
        factory.setJobFactory(jobFactory);
        factory.setQuartzProperties(quartzProperties);
        factory.setOverwriteExistingJobs(true);
        factory.setSchedulerName("SFTPTaskScheduler");
        factory.setStartupDelay(10); // 10 seconds delay before starting
        factory.setAutoStartup(true);
        factory.setWaitForJobsToCompleteOnShutdown(true);
        factory.setApplicationContextSchedulerContextKey("applicationContext");

        return factory;
    }

    /**
     * Quartz properties configuration - UPDATED FOR MEMORY STORE
     */
    @Bean
    @Qualifier("quartzProperties")
    public Properties quartzProperties() {
        Properties properties = new Properties();

        // Scheduler properties
        properties.setProperty("org.quartz.scheduler.instanceName", "SFTPTaskScheduler");
        properties.setProperty("org.quartz.scheduler.instanceId", "AUTO");
        properties.setProperty("org.quartz.scheduler.makeSchedulerThreadDaemon", "true");
        properties.setProperty("org.quartz.scheduler.skipUpdateCheck", "true");

        // ThreadPool properties
        properties.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        properties.setProperty("org.quartz.threadPool.threadCount", String.valueOf(schedulerThreadPoolSize));
        properties.setProperty("org.quartz.threadPool.threadPriority", "5");
        properties.setProperty("org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread", "true");

        // JobStore properties - CHANGED TO MEMORY STORE
        properties.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
        // REMOVED all database-related properties

        // Plugin for logging job history
        properties.setProperty("org.quartz.plugin.triggHistory.class",
                "org.quartz.plugins.history.LoggingTriggerHistoryPlugin");
        properties.setProperty("org.quartz.plugin.triggHistory.triggerFiredMessage",
                "Trigger {1}.{0} fired job {6}.{5} at {4, date, yyyy-MM-dd HH:mm:ss}");
        properties.setProperty("org.quartz.plugin.triggHistory.triggerCompleteMessage",
                "Trigger {1}.{0} completed firing job {6}.{5} at {4, date, yyyy-MM-dd HH:mm:ss}");

        return properties;
    }

    /**
     * Custom job factory that supports dependency injection
     */
    public static class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory {

        private ApplicationContext applicationContext;

        public void setApplicationContext(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        @Override
        protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
            Object job = super.createJobInstance(bundle);
            applicationContext.getAutowireCapableBeanFactory().autowireBean(job);
            return job;
        }
    }

    /**
     * Task scheduler for simple cron jobs (non-Quartz)
     */
    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("SimpleScheduler-");
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.initialize();
        return scheduler;
    }

    /**
     * Executor service for async tasks
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        return Executors.newFixedThreadPool(10, r -> {
            Thread thread = new Thread(r);
            thread.setName("AsyncTask-" + thread.getId());
            thread.setDaemon(true);
            return thread;
        });
    }
}