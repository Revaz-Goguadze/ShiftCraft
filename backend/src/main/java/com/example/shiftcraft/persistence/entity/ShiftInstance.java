package com.example.shiftcraft.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "shift_instances")
public class ShiftInstance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    // Many-to-one relationship with ShiftTemplate
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ShiftTemplate template;
    
    @NotNull
    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShiftStatus status = ShiftStatus.DRAFT;
    
    @Column
    private String notes;
    
    @Column(name = "published_at")
    private LocalDateTime publishedAt;
    
    @Column(name = "published_by")
    private String publishedBy;
    
    // One-to-many relationship with Assignment
    @OneToMany(mappedBy = "shiftInstance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Assignment> assignments = new HashSet<>();
    
    public enum ShiftStatus {
        DRAFT, PUBLISHED, CANCELLED
    }
    
    // Constructors
    public ShiftInstance() {}
    
    public ShiftInstance(ShiftTemplate template, LocalDate shiftDate) {
        this.template = template;
        this.shiftDate = shiftDate;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public ShiftTemplate getTemplate() { return template; }
    public void setTemplate(ShiftTemplate template) { this.template = template; }
    
    public LocalDate getShiftDate() { return shiftDate; }
    public void setShiftDate(LocalDate shiftDate) { this.shiftDate = shiftDate; }
    
    public ShiftStatus getStatus() { return status; }
    public void setStatus(ShiftStatus status) { this.status = status; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    
    public String getPublishedBy() { return publishedBy; }
    public void setPublishedBy(String publishedBy) { this.publishedBy = publishedBy; }
    
    public Set<Assignment> getAssignments() { return assignments; }
    public void setAssignments(Set<Assignment> assignments) { this.assignments = assignments; }
    
    // Helper methods
    public boolean isPublished() {
        return status == ShiftStatus.PUBLISHED;
    }
    
    public boolean canBeModified() {
        return status == ShiftStatus.DRAFT;
    }
    
    @Override
    public String toString() {
        return "ShiftInstance{template=" + (template != null ? template.getName() : "null") + 
               ", date=" + shiftDate + ", status=" + status + "}";
    }
}