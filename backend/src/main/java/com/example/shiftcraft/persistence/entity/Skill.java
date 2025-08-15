package com.example.shiftcraft.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "skills")
public class Skill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @NotBlank
    @Column(unique = true, nullable = false)
    private String name;
    
    @Column
    private String description;
    
    @Column
    private String category;
    
    // One-to-many relationship with UserSkill (for many-to-many with User)
    @OneToMany(mappedBy = "skill", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserSkill> userSkills = new HashSet<>();
    
    // Many-to-many relationship with ShiftTemplate via template_skill_requirements
    @ManyToMany(mappedBy = "requiredSkills", fetch = FetchType.LAZY)
    private Set<ShiftTemplate> shiftTemplates = new HashSet<>();
    
    // Constructors
    public Skill() {}
    
    public Skill(String name) {
        this.name = name;
    }
    
    public Skill(String name, String description) {
        this.name = name;
        this.description = description;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public Set<UserSkill> getUserSkills() { return userSkills; }
    public void setUserSkills(Set<UserSkill> userSkills) { this.userSkills = userSkills; }
    
    public Set<ShiftTemplate> getShiftTemplates() { return shiftTemplates; }
    public void setShiftTemplates(Set<ShiftTemplate> shiftTemplates) { this.shiftTemplates = shiftTemplates; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Skill)) return false;
        Skill skill = (Skill) o;
        return name != null && name.equals(skill.name);
    }
    
    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
    
    @Override
    public String toString() {
        return "Skill{name='" + name + "'}";
    }
}