package com.example.shiftcraft.persistence.repository;

import com.example.shiftcraft.persistence.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, String> {
    
    List<UserSkill> findByUserId(String userId);
    
    List<UserSkill> findBySkillId(String skillId);
    
    Optional<UserSkill> findByUserIdAndSkillId(String userId, String skillId);
    
    @Query("SELECT us FROM UserSkill us WHERE us.user.id = :userId AND us.level = :level")
    List<UserSkill> findByUserIdAndLevel(@Param("userId") String userId, @Param("level") UserSkill.SkillLevel level);
    
    @Query("SELECT us FROM UserSkill us WHERE us.skill.name = :skillName AND us.level IN :levels")
    List<UserSkill> findBySkillNameAndLevels(@Param("skillName") String skillName, @Param("levels") List<UserSkill.SkillLevel> levels);
}