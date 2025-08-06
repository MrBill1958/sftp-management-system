package com.nearstar.sftpmanager.repository;

import com.nearstar.sftpmanager.model.entity.Site;
import com.nearstar.sftpmanager.model.entity.User;
import com.nearstar.sftpmanager.model.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<Site, Long> {

    List<Site> findByStatus(Status status);

    List<Site> findByOwner(User owner);

    Optional<Site> findBySiteName(String siteName);

    @Query("SELECT s FROM Site s WHERE s.owner = :user OR :isAdmin = true")
    List<Site> findAccessibleSites(@Param("user") User user, @Param("isAdmin") boolean isAdmin);

    @Query("SELECT s FROM Site s WHERE s.emailNotification IS NOT NULL AND s.status = :status")
    List<Site> findSitesWithNotification(@Param("status") Status status);
}