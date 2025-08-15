package com.example.shiftcraft.persistence.repository;

import com.example.shiftcraft.persistence.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, String> {
    
    List<Assignment> findByUserId(String userId);
    
    List<Assignment> findByShiftInstanceId(String shiftInstanceId);
    
    List<Assignment> findByStatus(Assignment.AssignmentStatus status);
    
    @Query("SELECT a FROM Assignment a WHERE a.user.id = :userId AND a.shiftInstance.shiftDate = :date")
    List<Assignment> findByUserIdAndDate(@Param("userId") String userId, @Param("date") LocalDate date);
    
    @Query("SELECT a FROM Assignment a WHERE a.user.id = :userId AND a.shiftInstance.shiftDate BETWEEN :startDate AND :endDate")
    List<Assignment> findByUserIdAndDateRange(@Param("userId") String userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT a FROM Assignment a WHERE a.shiftInstance.shiftDate BETWEEN :startDate AND :endDate AND a.status = 'ACTIVE'")
    List<Assignment> findActiveAssignmentsInPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}