package com.nearstar.sftpmanager.repository;

import com.nearstar.sftpmanager.model.entity.ApplicationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationConfigRepository extends JpaRepository<ApplicationConfig, Long> {
    Optional<ApplicationConfig> findByConfigKey(String configKey);
}