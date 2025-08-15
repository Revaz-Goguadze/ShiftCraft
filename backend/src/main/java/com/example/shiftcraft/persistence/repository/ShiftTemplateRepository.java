package com.example.shiftcraft.persistence.repository;

import com.example.shiftcraft.persistence.entity.ShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, String> {
    
    List<ShiftTemplate> findByLocationId(String locationId);
    
    List<ShiftTemplate> findByRoleId(String roleId);
    
    List<ShiftTemplate> findByIsActiveTrue();
    
    @Query("SELECT st FROM ShiftTemplate st WHERE st.location.id = :locationId AND st.isActive = true")
    List<ShiftTemplate> findActiveByLocationId(@Param("locationId") String locationId);
    
    @Query("SELECT st FROM ShiftTemplate st JOIN st.requiredSkills s WHERE s.name = :skillName")
    List<ShiftTemplate> findByRequiredSkillName(@Param("skillName") String skillName);
}