package com.nearstar.sftpmanager.repository;

import com.nearstar.sftpmanager.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Change from findByRoleName to findByName
    Optional<Role> findByName(String name);

    boolean existsByName(String name);
}