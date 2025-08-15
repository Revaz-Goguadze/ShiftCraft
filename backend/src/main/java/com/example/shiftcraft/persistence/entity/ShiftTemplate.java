package com.example.shiftcraft.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "shift_templates")
public class ShiftTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    // Many-to-one relationship with Location
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;
    
    // Many-to-one relationship with Role
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    
    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    
    @PositiveOrZero
    @Column(name = "break_minutes", nullable = false)
    private Integer breakMinutes = 0;
    
    @Column
    private String description;
    
    @Column(name = "max_assignments")
    private Integer maxAssignments = 1;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // Many-to-many relationship with Skill via template_skill_requirements
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "template_skill_requirements",
        joinColumns = @JoinColumn(name = "template_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> requiredSkills = new HashSet<>();
    
    // One-to-many relationship with ShiftInstance
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ShiftInstance> shiftInstances = new HashSet<>();
    
    // Constructors
    public ShiftTemplate() {}
    
    public ShiftTemplate(String name, Location location, Role role, LocalTime startTime, LocalTime endTime) {
        this.name = name;
        this.location = location;
        this.role = role;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public Location getLocation() { return location; }
    public void setLocation(Location location) { this.location = location; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    
    public Integer getBreakMinutes() { return breakMinutes; }
    public void setBreakMinutes(Integer breakMinutes) { this.breakMinutes = breakMinutes; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Integer getMaxAssignments() { return maxAssignments; }
    public void setMaxAssignments(Integer maxAssignments) { this.maxAssignments = maxAssignments; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Set<Skill> getRequiredSkills() { return requiredSkills; }
    public void setRequiredSkills(Set<Skill> requiredSkills) { this.requiredSkills = requiredSkills; }
    
    public Set<ShiftInstance> getShiftInstances() { return shiftInstances; }
    public void setShiftInstances(Set<ShiftInstance> shiftInstances) { this.shiftInstances = shiftInstances; }
    
    // Helper methods
    public long getDurationMinutes() {
        return java.time.Duration.between(startTime, endTime).toMinutes() - breakMinutes;
    }
    
    @Override
    public String toString() {
        return "ShiftTemplate{name='" + name + "', startTime=" + startTime + ", endTime=" + endTime + "}";
    }
}