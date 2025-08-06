package com.nearstar.sftpmanager.repository;

import com.nearstar.sftpmanager.model.entity.ScheduledTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, Long> {

    List<ScheduledTask> findByEnabledTrue();

    List<ScheduledTask> findBySiteId(Long siteId);

    List<ScheduledTask> findByTaskName(String taskName);
}