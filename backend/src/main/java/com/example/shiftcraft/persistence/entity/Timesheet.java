package com.example.shiftcraft.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "timesheets")
public class Timesheet {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    // Many-to-one relationship with User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotNull
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;
    
    @NotNull
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;
    
    @Column(name = "total_hours", precision = 8, scale = 2)
    private BigDecimal totalHours = BigDecimal.ZERO;
    
    @Column(name = "regular_hours", precision = 8, scale = 2)
    private BigDecimal regularHours = BigDecimal.ZERO;
    
    @Column(name = "overtime_hours", precision = 8, scale = 2)
    private BigDecimal overtimeHours = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimesheetStatus status = TimesheetStatus.DRAFT;
    
    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt = LocalDateTime.now();
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "approved_by")
    private String approvedBy;
    
    // One-to-many relationship with TimesheetEntry
    @OneToMany(mappedBy = "timesheet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TimesheetEntry> entries = new ArrayList<>();
    
    public enum TimesheetStatus {
        DRAFT, SUBMITTED, APPROVED, REJECTED
    }
    
    // Constructors
    public Timesheet() {}
    
    public Timesheet(User user, LocalDate periodStart, LocalDate periodEnd) {
        this.user = user;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    
    public BigDecimal getTotalHours() { return totalHours; }
    public void setTotalHours(BigDecimal totalHours) { this.totalHours = totalHours; }
    
    public BigDecimal getRegularHours() { return regularHours; }
    public void setRegularHours(BigDecimal regularHours) { this.regularHours = regularHours; }
    
    public BigDecimal getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }
    
    public TimesheetStatus getStatus() { return status; }
    public void setStatus(TimesheetStatus status) { this.status = status; }
    
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    
    public List<TimesheetEntry> getEntries() { return entries; }
    public void setEntries(List<TimesheetEntry> entries) { this.entries = entries; }
    
    // Helper methods
    public void calculateTotals() {
        this.totalHours = entries.stream()
            .map(TimesheetEntry::getHours)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Standard 40 hour work week for overtime calculation
        BigDecimal standardHours = new BigDecimal("40");
        if (totalHours.compareTo(standardHours) > 0) {
            this.regularHours = standardHours;
            this.overtimeHours = totalHours.subtract(standardHours);
        } else {
            this.regularHours = totalHours;
            this.overtimeHours = BigDecimal.ZERO;
        }
    }
    
    @Override
    public String toString() {
        return "Timesheet{user=" + (user != null ? user.getFullName() : "null") + 
               ", period=" + periodStart + " to " + periodEnd + 
               ", totalHours=" + totalHours + "}";
    }
}