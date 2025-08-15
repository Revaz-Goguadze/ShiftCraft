package com.example.shiftcraft.persistence.repository;

import com.example.shiftcraft.persistence.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, String> {
    
    List<LeaveRequest> findByUserId(String userId);
    
    List<LeaveRequest> findByStatus(LeaveRequest.LeaveStatus status);
    
    List<LeaveRequest> findByLeaveType(LeaveRequest.LeaveType leaveType);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = 'PENDING' ORDER BY lr.requestedAt ASC")
    List<LeaveRequest> findPendingRequests();
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId AND lr.status = :status")
    List<LeaveRequest> findByUserIdAndStatus(@Param("userId") String userId, @Param("status") LeaveRequest.LeaveStatus status);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.startDate <= :endDate AND lr.endDate >= :startDate AND lr.status = 'APPROVED'")
    List<LeaveRequest> findApprovedLeaveInPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId AND lr.startDate <= :endDate AND lr.endDate >= :startDate AND lr.status IN ('PENDING', 'APPROVED')")
    List<LeaveRequest> findUserLeaveInPeriod(@Param("userId") String userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}