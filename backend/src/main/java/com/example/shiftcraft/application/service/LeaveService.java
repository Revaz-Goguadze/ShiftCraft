package com.example.shiftcraft.application.service;

import com.example.shiftcraft.persistence.entity.LeaveRequest;
import com.example.shiftcraft.persistence.entity.User;
import com.example.shiftcraft.persistence.repository.LeaveRequestRepository;
import com.example.shiftcraft.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LeaveService {
    
    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public LeaveService(LeaveRequestRepository leaveRequestRepository, UserRepository userRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Submit a leave request
     */
    public LeaveRequest submitLeaveRequest(String userId, LocalDate startDate, LocalDate endDate, 
                                         LeaveRequest.LeaveType leaveType, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Validate dates
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        
        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot request leave for past dates");
        }
        
        // Check for overlapping requests
        List<LeaveRequest> overlappingRequests = leaveRequestRepository.findUserLeaveInPeriod(
            userId, startDate, endDate);
        
        if (!overlappingRequests.isEmpty()) {
            throw new IllegalStateException("Leave request overlaps with existing leave");
        }
        
        LeaveRequest leaveRequest = new LeaveRequest(user, startDate, endDate, leaveType, reason);
        return leaveRequestRepository.save(leaveRequest);
    }
    
    /**
     * Get pending leave requests for manager approval
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getPendingLeaveRequests() {
        return leaveRequestRepository.findPendingRequests();
    }
    
    /**
     * Approve a leave request
     */
    public LeaveRequest approveLeaveRequest(String requestId, String approverId, String notes) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + requestId));
        
        if (request.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be approved");
        }
        
        request.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        request.setReviewedBy(approverId);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewNotes(notes);
        
        return leaveRequestRepository.save(request);
    }
    
    /**
     * Reject a leave request
     */
    public LeaveRequest rejectLeaveRequest(String requestId, String reviewerId, String notes) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + requestId));
        
        if (request.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new IllegalStateException("Only pending requests can be rejected");
        }
        
        request.setStatus(LeaveRequest.LeaveStatus.REJECTED);
        request.setReviewedBy(reviewerId);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewNotes(notes);
        
        return leaveRequestRepository.save(request);
    }
    
    /**
     * Get leave requests for a user
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getUserLeaveRequests(String userId) {
        return leaveRequestRepository.findByUserId(userId);
    }
    
    /**
     * Get leave requests by status
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getLeaveRequestsByStatus(LeaveRequest.LeaveStatus status) {
        return leaveRequestRepository.findByStatus(status);
    }
    
    /**
     * Check if user has approved leave during a period
     */
    @Transactional(readOnly = true)
    public boolean hasApprovedLeave(String userId, LocalDate startDate, LocalDate endDate) {
        List<LeaveRequest> approvedLeave = leaveRequestRepository.findUserLeaveInPeriod(
            userId, startDate, endDate);
        return approvedLeave.stream().anyMatch(LeaveRequest::isApproved);
    }
    
    /**
     * Get approved leave requests for a period
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getApprovedLeaveInPeriod(LocalDate startDate, LocalDate endDate) {
        return leaveRequestRepository.findApprovedLeaveInPeriod(startDate, endDate);
    }
    
    /**
     * Cancel a leave request (only if pending)
     */
    public void cancelLeaveRequest(String requestId, String userId) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
            .orElseThrow(() -> new RuntimeException("Leave request not found with id: " + requestId));
        
        if (!request.getUser().getId().equals(userId)) {
            throw new IllegalStateException("Can only cancel own leave requests");
        }
        
        if (request.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new IllegalStateException("Can only cancel pending requests");
        }
        
        request.setStatus(LeaveRequest.LeaveStatus.CANCELLED);
        leaveRequestRepository.save(request);
    }
}