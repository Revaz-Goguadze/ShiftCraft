package com.example.shiftcraft.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
public class Assignment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    // Many-to-one relationship with ShiftInstance
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_instance_id", nullable = false)
    private ShiftInstance shiftInstance;
    
    // Many-to-one relationship with User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status = AssignmentStatus.ACTIVE;
    
    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();
    
    @Column(name = "assigned_by", nullable = false)
    private String assignedBy;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column
    private String notes;
    
    public enum AssignmentStatus {
        ACTIVE, CANCELLED, COMPLETED, SWAP_REQUESTED, SWAPPED
    }
    
    // Constructors
    public Assignment() {}
    
    public Assignment(ShiftInstance shiftInstance, User user, String assignedBy) {
        this.shiftInstance = shiftInstance;
        this.user = user;
        this.assignedBy = assignedBy;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public ShiftInstance getShiftInstance() { return shiftInstance; }
    public void setShiftInstance(ShiftInstance shiftInstance) { this.shiftInstance = shiftInstance; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }
    
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    
    public String getAssignedBy() { return assignedBy; }
    public void setAssignedBy(String assignedBy) { this.assignedBy = assignedBy; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "Assignment{user=" + (user != null ? user.getFullName() : "null") + 
               ", shift=" + (shiftInstance != null ? shiftInstance.toString() : "null") + 
               ", status=" + status + "}";
    }
}