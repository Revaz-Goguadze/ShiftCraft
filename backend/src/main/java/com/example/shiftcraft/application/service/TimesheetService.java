package com.example.shiftcraft.application.service;

import com.example.shiftcraft.persistence.entity.*;
import com.example.shiftcraft.persistence.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TimesheetService {
    
    private final TimesheetRepository timesheetRepository;
    private final TimesheetEntryRepository timesheetEntryRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public TimesheetService(TimesheetRepository timesheetRepository,
                           TimesheetEntryRepository timesheetEntryRepository,
                           AssignmentRepository assignmentRepository,
                           UserRepository userRepository) {
        this.timesheetRepository = timesheetRepository;
        this.timesheetEntryRepository = timesheetEntryRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Generate timesheet for a user for a specific period
     */
    public Timesheet generateTimesheet(String userId, LocalDate periodStart, LocalDate periodEnd) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Validate period
        if (periodStart.isAfter(periodEnd)) {
            throw new IllegalArgumentException("Period start must be before period end");
        }
        
        // Check if timesheet already exists for this period
        Optional<Timesheet> existingTimesheet = timesheetRepository
            .findByUserIdAndPeriodStartAndPeriodEnd(userId, periodStart, periodEnd);
        
        if (existingTimesheet.isPresent()) {
            throw new IllegalStateException("Timesheet already exists for this period");
        }
        
        // Create new timesheet
        Timesheet timesheet = new Timesheet(user, periodStart, periodEnd);
        timesheet = timesheetRepository.save(timesheet);
        
        // Get assignments for the period
        List<Assignment> assignments = assignmentRepository
            .findByUserIdAndDateRange(userId, periodStart, periodEnd);
        
        // Create timesheet entries from assignments
        for (Assignment assignment : assignments) {
            if (assignment.getStatus() == Assignment.AssignmentStatus.ACTIVE || 
                assignment.getStatus() == Assignment.AssignmentStatus.COMPLETED) {
                
                TimesheetEntry entry = createEntryFromAssignment(timesheet, assignment);
                timesheetEntryRepository.save(entry);
                timesheet.getEntries().add(entry);
            }
        }
        
        // Calculate totals
        timesheet.calculateTotals();
        return timesheetRepository.save(timesheet);
    }
    
    /**
     * Add manual timesheet entry
     */
    public TimesheetEntry addManualEntry(String timesheetId, LocalDate workDate, 
                                       LocalTime startTime, LocalTime endTime, 
                                       Integer breakMinutes, String description) {
        Timesheet timesheet = timesheetRepository.findById(timesheetId)
            .orElseThrow(() -> new RuntimeException("Timesheet not found with id: " + timesheetId));
        
        if (timesheet.getStatus() != Timesheet.TimesheetStatus.DRAFT) {
            throw new IllegalStateException("Can only add entries to draft timesheets");
        }
        
        TimesheetEntry entry = new TimesheetEntry(timesheet, workDate, startTime, endTime);
        entry.setBreakMinutes(breakMinutes);
        entry.setDescription(description);
        entry.setEntryType(TimesheetEntry.EntryType.MANUAL_ADJUSTMENT);
        
        entry = timesheetEntryRepository.save(entry);
        
        // Recalculate timesheet totals
        timesheet.calculateTotals();
        timesheetRepository.save(timesheet);
        
        return entry;
    }
    
    /**
     * Calculate overtime hours for a timesheet
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateOvertimeHours(String timesheetId) {
        Timesheet timesheet = timesheetRepository.findById(timesheetId)
            .orElseThrow(() -> new RuntimeException("Timesheet not found with id: " + timesheetId));
        
        return timesheet.getOvertimeHours();
    }
    
    /**
     * Get timesheet for user and period
     */
    @Transactional(readOnly = true)
    public Optional<Timesheet> getTimesheet(String userId, LocalDate periodStart, LocalDate periodEnd) {
        return timesheetRepository.findByUserIdAndPeriodStartAndPeriodEnd(userId, periodStart, periodEnd);
    }
    
    /**
     * Get all timesheets for a user
     */
    @Transactional(readOnly = true)
    public List<Timesheet> getUserTimesheets(String userId) {
        return timesheetRepository.findByUserId(userId);
    }
    
    /**
     * Submit timesheet for approval
     */
    public Timesheet submitTimesheet(String timesheetId) {
        Timesheet timesheet = timesheetRepository.findById(timesheetId)
            .orElseThrow(() -> new RuntimeException("Timesheet not found with id: " + timesheetId));
        
        if (timesheet.getStatus() != Timesheet.TimesheetStatus.DRAFT) {
            throw new IllegalStateException("Only draft timesheets can be submitted");
        }
        
        timesheet.setStatus(Timesheet.TimesheetStatus.SUBMITTED);
        return timesheetRepository.save(timesheet);
    }
    
    /**
     * Approve timesheet
     */
    public Timesheet approveTimesheet(String timesheetId, String approverId) {
        Timesheet timesheet = timesheetRepository.findById(timesheetId)
            .orElseThrow(() -> new RuntimeException("Timesheet not found with id: " + timesheetId));
        
        if (timesheet.getStatus() != Timesheet.TimesheetStatus.SUBMITTED) {
            throw new IllegalStateException("Only submitted timesheets can be approved");
        }
        
        timesheet.setStatus(Timesheet.TimesheetStatus.APPROVED);
        timesheet.setApprovedBy(approverId);
        timesheet.setApprovedAt(java.time.LocalDateTime.now());
        
        return timesheetRepository.save(timesheet);
    }
    
    /**
     * Get timesheets by status
     */
    @Transactional(readOnly = true)
    public List<Timesheet> getTimesheetsByStatus(Timesheet.TimesheetStatus status) {
        return timesheetRepository.findByStatus(status);
    }
    
    /**
     * Generate weekly timesheet (common use case)
     */
    public Timesheet generateWeeklyTimesheet(String userId, LocalDate weekStart) {
        LocalDate weekEnd = weekStart.plusDays(6);
        return generateTimesheet(userId, weekStart, weekEnd);
    }
    
    /**
     * Helper method to create timesheet entry from assignment
     */
    private TimesheetEntry createEntryFromAssignment(Timesheet timesheet, Assignment assignment) {
        ShiftTemplate template = assignment.getShiftInstance().getTemplate();
        LocalDate workDate = assignment.getShiftInstance().getShiftDate();
        
        TimesheetEntry entry = new TimesheetEntry(timesheet, workDate, 
            template.getStartTime(), template.getEndTime());
        entry.setAssignment(assignment);
        entry.setBreakMinutes(template.getBreakMinutes());
        entry.setEntryType(TimesheetEntry.EntryType.SHIFT);
        entry.setDescription("Shift: " + template.getName());
        
        return entry;
    }
}