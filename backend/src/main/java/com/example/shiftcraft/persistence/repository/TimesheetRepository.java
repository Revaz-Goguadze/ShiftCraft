package com.example.shiftcraft.persistence.repository;

import com.example.shiftcraft.persistence.entity.Timesheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TimesheetRepository extends JpaRepository<Timesheet, String> {
    
    List<Timesheet> findByUserId(String userId);
    
    List<Timesheet> findByStatus(Timesheet.TimesheetStatus status);
    
    Optional<Timesheet> findByUserIdAndPeriodStartAndPeriodEnd(String userId, LocalDate periodStart, LocalDate periodEnd);
    
    @Query("SELECT t FROM Timesheet t WHERE t.user.id = :userId AND t.periodStart >= :startDate AND t.periodEnd <= :endDate")
    List<Timesheet> findByUserIdAndPeriodRange(@Param("userId") String userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT t FROM Timesheet t WHERE t.periodStart >= :startDate AND t.periodEnd <= :endDate")
    List<Timesheet> findByPeriodRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}