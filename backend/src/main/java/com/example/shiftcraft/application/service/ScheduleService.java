package com.example.shiftcraft.application.service;

import com.example.shiftcraft.persistence.entity.*;
import com.example.shiftcraft.persistence.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScheduleService {
    
    private final ShiftInstanceRepository shiftInstanceRepository;
    private final AssignmentRepository assignmentRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public ScheduleService(ShiftInstanceRepository shiftInstanceRepository,
                          AssignmentRepository assignmentRepository,
                          LeaveRequestRepository leaveRequestRepository,
                          UserRepository userRepository) {
        this.shiftInstanceRepository = shiftInstanceRepository;
        this.assignmentRepository = assignmentRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Get weekly schedule for all users
     */
    @Transactional(readOnly = true)
    public WeeklySchedule getWeeklySchedule(LocalDate date) {
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        
        List<ShiftInstance> shifts = shiftInstanceRepository
            .findPublishedShiftsInPeriod(weekStart, weekEnd);
        
        List<Assignment> assignments = assignmentRepository
            .findActiveAssignmentsInPeriod(weekStart, weekEnd);
        
        List<LeaveRequest> approvedLeave = leaveRequestRepository
            .findApprovedLeaveInPeriod(weekStart, weekEnd);
        
        return new WeeklySchedule(weekStart, weekEnd, shifts, assignments, approvedLeave);
    }
    
    /**
     * Get weekly schedule for a specific user
     */
    @Transactional(readOnly = true)
    public UserWeeklySchedule getUserWeeklySchedule(String userId, LocalDate date) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        LocalDate weekStart = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        
        List<Assignment> userAssignments = assignmentRepository
            .findByUserIdAndDateRange(userId, weekStart, weekEnd);
        
        List<LeaveRequest> userLeave = leaveRequestRepository
            .findUserLeaveInPeriod(userId, weekStart, weekEnd);
        
        return new UserWeeklySchedule(user, weekStart, weekEnd, userAssignments, userLeave);
    }
    
    /**
     * Get schedule conflicts for a user (double bookings, etc.)
     */
    @Transactional(readOnly = true)
    public List<ScheduleConflict> getScheduleConflicts(String userId, LocalDate startDate, LocalDate endDate) {
        List<Assignment> assignments = assignmentRepository
            .findByUserIdAndDateRange(userId, startDate, endDate);
        
        // Group assignments by date
        Map<LocalDate, List<Assignment>> assignmentsByDate = assignments.stream()
            .collect(Collectors.groupingBy(a -> a.getShiftInstance().getShiftDate()));
        
        return assignmentsByDate.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1) // Multiple assignments on same date
            .map(entry -> new ScheduleConflict(userId, entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }
    
    /**
     * Check if user is available for a shift
     */
    @Transactional(readOnly = true)
    public boolean isUserAvailableForShift(String userId, LocalDate date) {
        // Check existing assignments
        List<Assignment> existingAssignments = assignmentRepository
            .findByUserIdAndDate(userId, date);
        
        boolean hasActiveAssignment = existingAssignments.stream()
            .anyMatch(a -> a.getStatus() == Assignment.AssignmentStatus.ACTIVE);
        
        if (hasActiveAssignment) {
            return false;
        }
        
        // Check approved leave
        List<LeaveRequest> leave = leaveRequestRepository
            .findUserLeaveInPeriod(userId, date, date);
        
        return leave.stream().noneMatch(LeaveRequest::isApproved);
    }
    
    /**
     * Get staff availability for a date range
     */
    @Transactional(readOnly = true)
    public Map<String, List<LocalDate>> getStaffAvailability(LocalDate startDate, LocalDate endDate) {
        List<User> staff = userRepository.findByRoleName("STAFF");
        
        return staff.stream()
            .collect(Collectors.toMap(
                User::getId,
                user -> getAvailableDates(user.getId(), startDate, endDate)
            ));
    }
    
    /**
     * Helper method to get available dates for a user
     */
    private List<LocalDate> getAvailableDates(String userId, LocalDate startDate, LocalDate endDate) {
        return startDate.datesUntil(endDate.plusDays(1))
            .filter(date -> isUserAvailableForShift(userId, date))
            .collect(Collectors.toList());
    }
    
    /**
     * Data classes for schedule responses
     */
    public static class WeeklySchedule {
        private final LocalDate weekStart;
        private final LocalDate weekEnd;
        private final List<ShiftInstance> shifts;
        private final List<Assignment> assignments;
        private final List<LeaveRequest> approvedLeave;
        
        public WeeklySchedule(LocalDate weekStart, LocalDate weekEnd, 
                             List<ShiftInstance> shifts, List<Assignment> assignments,
                             List<LeaveRequest> approvedLeave) {
            this.weekStart = weekStart;
            this.weekEnd = weekEnd;
            this.shifts = shifts;
            this.assignments = assignments;
            this.approvedLeave = approvedLeave;
        }
        
        // Getters
        public LocalDate getWeekStart() { return weekStart; }
        public LocalDate getWeekEnd() { return weekEnd; }
        public List<ShiftInstance> getShifts() { return shifts; }
        public List<Assignment> getAssignments() { return assignments; }
        public List<LeaveRequest> getApprovedLeave() { return approvedLeave; }
    }
    
    public static class UserWeeklySchedule {
        private final User user;
        private final LocalDate weekStart;
        private final LocalDate weekEnd;
        private final List<Assignment> assignments;
        private final List<LeaveRequest> leave;
        
        public UserWeeklySchedule(User user, LocalDate weekStart, LocalDate weekEnd,
                                 List<Assignment> assignments, List<LeaveRequest> leave) {
            this.user = user;
            this.weekStart = weekStart;
            this.weekEnd = weekEnd;
            this.assignments = assignments;
            this.leave = leave;
        }
        
        // Getters
        public User getUser() { return user; }
        public LocalDate getWeekStart() { return weekStart; }
        public LocalDate getWeekEnd() { return weekEnd; }
        public List<Assignment> getAssignments() { return assignments; }
        public List<LeaveRequest> getLeave() { return leave; }
    }
    
    public static class ScheduleConflict {
        private final String userId;
        private final LocalDate date;
        private final List<Assignment> conflictingAssignments;
        
        public ScheduleConflict(String userId, LocalDate date, List<Assignment> conflictingAssignments) {
            this.userId = userId;
            this.date = date;
            this.conflictingAssignments = conflictingAssignments;
        }
        
        // Getters
        public String getUserId() { return userId; }
        public LocalDate getDate() { return date; }
        public List<Assignment> getConflictingAssignments() { return conflictingAssignments; }
    }
}