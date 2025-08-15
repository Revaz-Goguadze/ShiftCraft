package com.example.shiftcraft.persistence.repository;

import com.example.shiftcraft.persistence.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    
    Optional<Role> findByName(String name);
    
    boolean existsByName(String name);
}