package com.example.shiftcraft.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "timesheet_entries")
public class TimesheetEntry {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    // Many-to-one relationship with Timesheet
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timesheet_id", nullable = false)
    private Timesheet timesheet;
    
    // Many-to-one relationship with Assignment (optional - may be manual entry)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;
    
    @NotNull
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;
    
    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @Column(name = "break_minutes", nullable = false)
    private Integer breakMinutes = 0;
    
    @Column(name = "hours", precision = 8, scale = 2, nullable = false)
    private BigDecimal hours;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false)
    private EntryType entryType = EntryType.SHIFT;
    
    @Column
    private String description;
    
    public enum EntryType {
        SHIFT, OVERTIME, MANUAL_ADJUSTMENT, BREAK_DEDUCTION
    }
    
    // Constructors
    public TimesheetEntry() {}
    
    public TimesheetEntry(Timesheet timesheet, LocalDate workDate, LocalTime startTime, LocalTime endTime) {
        this.timesheet = timesheet;
        this.workDate = workDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.calculateHours();
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Timesheet getTimesheet() { return timesheet; }
    public void setTimesheet(Timesheet timesheet) { this.timesheet = timesheet; }
    
    public Assignment getAssignment() { return assignment; }
    public void setAssignment(Assignment assignment) { this.assignment = assignment; }
    
    public LocalDate getWorkDate() { return workDate; }
    public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }
    
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    
    public Integer getBreakMinutes() { return breakMinutes; }
    public void setBreakMinutes(Integer breakMinutes) { this.breakMinutes = breakMinutes; }
    
    public BigDecimal getHours() { return hours; }
    public void setHours(BigDecimal hours) { this.hours = hours; }
    
    public EntryType getEntryType() { return entryType; }
    public void setEntryType(EntryType entryType) { this.entryType = entryType; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    // Helper methods
    public void calculateHours() {
        if (startTime != null && endTime != null) {
            long totalMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
            totalMinutes -= (breakMinutes != null ? breakMinutes : 0);
            this.hours = BigDecimal.valueOf(totalMinutes).divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
        }
    }
    
    @PrePersist
    @PreUpdate
    protected void onSave() {
        calculateHours();
    }
    
    @Override
    public String toString() {
        return "TimesheetEntry{workDate=" + workDate + 
               ", startTime=" + startTime + ", endTime=" + endTime + 
               ", hours=" + hours + "}";
    }
}