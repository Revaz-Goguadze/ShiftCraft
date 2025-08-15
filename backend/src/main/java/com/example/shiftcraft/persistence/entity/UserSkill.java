package com.example.shiftcraft.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_skills")
public class UserSkill {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    // Many-to-one relationship with User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    // Many-to-one relationship with Skill
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillLevel level = SkillLevel.BEGINNER;
    
    @Column(name = "acquired_at")
    private LocalDateTime acquiredAt = LocalDateTime.now();
    
    @Column(name = "verified_by")
    private String verifiedBy;
    
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    
    public enum SkillLevel {
        BEGINNER, INTERMEDIATE, EXPERT, CERTIFIED
    }
    
    // Constructors
    public UserSkill() {}
    
    public UserSkill(User user, Skill skill, SkillLevel level) {
        this.user = user;
        this.skill = skill;
        this.level = level;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Skill getSkill() { return skill; }
    public void setSkill(Skill skill) { this.skill = skill; }
    
    public SkillLevel getLevel() { return level; }
    public void setLevel(SkillLevel level) { this.level = level; }
    
    public LocalDateTime getAcquiredAt() { return acquiredAt; }
    public void setAcquiredAt(LocalDateTime acquiredAt) { this.acquiredAt = acquiredAt; }
    
    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }
    
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserSkill)) return false;
        UserSkill userSkill = (UserSkill) o;
        return user != null && skill != null && 
               user.equals(userSkill.user) && skill.equals(userSkill.skill);
    }
    
    @Override
    public int hashCode() {
        return 31 * (user != null ? user.hashCode() : 0) + 
               (skill != null ? skill.hashCode() : 0);
    }
}