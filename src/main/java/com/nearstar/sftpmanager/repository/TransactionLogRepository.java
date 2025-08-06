package com.nearstar.sftpmanager.repository;

import com.nearstar.sftpmanager.model.entity.TransactionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {

    List<TransactionLog> findBySiteId(Long siteId);

    List<TransactionLog> findByUserId(Long userId);

    List<TransactionLog> findBySuccessFalse();

    @Query("SELECT t FROM TransactionLog t WHERE t.timestamp BETWEEN :startDate AND :endDate")
    List<TransactionLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM TransactionLog t WHERE t.site.id = :siteId AND t.action = :action")
    List<TransactionLog> findBySiteAndAction(@Param("siteId") Long siteId,
                                             @Param("action") String action);
}