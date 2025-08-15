package com.example.shiftcraft.persistence.repository;

import com.example.shiftcraft.persistence.entity.ShiftInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftInstanceRepository extends JpaRepository<ShiftInstance, String> {
    
    List<ShiftInstance> findByShiftDate(LocalDate shiftDate);
    
    List<ShiftInstance> findByShiftDateBetween(LocalDate startDate, LocalDate endDate);
    
    List<ShiftInstance> findByTemplateId(String templateId);
    
    Optional<ShiftInstance> findByTemplateIdAndShiftDate(String templateId, LocalDate shiftDate);
    
    List<ShiftInstance> findByStatus(ShiftInstance.ShiftStatus status);
    
    @Query("SELECT si FROM ShiftInstance si WHERE si.shiftDate >= :startDate AND si.shiftDate <= :endDate AND si.status = 'PUBLISHED'")
    List<ShiftInstance> findPublishedShiftsInPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT si FROM ShiftInstance si WHERE si.template.location.id = :locationId AND si.shiftDate = :date")
    List<ShiftInstance> findByLocationAndDate(@Param("locationId") String locationId, @Param("date") LocalDate date);
}