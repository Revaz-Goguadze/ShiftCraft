package com.example.shiftcraft.application.service;

import com.example.shiftcraft.persistence.entity.*;
import com.example.shiftcraft.persistence.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class ShiftService {
    
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final ShiftInstanceRepository shiftInstanceRepository;
    private final AssignmentRepository assignmentRepository;
    private final LocationRepository locationRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public ShiftService(ShiftTemplateRepository shiftTemplateRepository,
                       ShiftInstanceRepository shiftInstanceRepository,
                       AssignmentRepository assignmentRepository,
                       LocationRepository locationRepository,
                       RoleRepository roleRepository,
                       UserRepository userRepository) {
        this.shiftTemplateRepository = shiftTemplateRepository;
        this.shiftInstanceRepository = shiftInstanceRepository;
        this.assignmentRepository = assignmentRepository;
        this.locationRepository = locationRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Create a new shift template
     */
    public ShiftTemplate createShiftTemplate(String name, String locationId, String roleId,
                                           java.time.LocalTime startTime, java.time.LocalTime endTime,
                                           Integer breakMinutes) {
        Location location = locationRepository.findById(locationId)
            .orElseThrow(() -> new RuntimeException("Location not found with id: " + locationId));
        
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));
        
        ShiftTemplate template = new ShiftTemplate(name, location, role, startTime, endTime);
        template.setBreakMinutes(breakMinutes);
        
        return shiftTemplateRepository.save(template);
    }
    
    /**
     * Get all active shift templates
     */
    @Transactional(readOnly = true)
    public List<ShiftTemplate> getActiveShiftTemplates() {
        return shiftTemplateRepository.findByIsActiveTrue();
    }
    
    /**
     * Get shift templates by location
     */
    @Transactional(readOnly = true)
    public List<ShiftTemplate> getShiftTemplatesByLocation(String locationId) {
        return shiftTemplateRepository.findActiveByLocationId(locationId);
    }
    
    /**
     * Create shift instance from template
     */
    public ShiftInstance createShiftInstance(String templateId, LocalDate shiftDate) {
        ShiftTemplate template = shiftTemplateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Shift template not found with id: " + templateId));
        
        // Check if instance already exists
        Optional<ShiftInstance> existingInstance = shiftInstanceRepository
            .findByTemplateIdAndShiftDate(templateId, shiftDate);
        
        if (existingInstance.isPresent()) {
            throw new IllegalStateException("Shift instance already exists for this date");
        }
        
        ShiftInstance instance = new ShiftInstance(template, shiftDate);
        return shiftInstanceRepository.save(instance);
    }
    
    /**
     * Publish shift instance
     */
    public ShiftInstance publishShiftInstance(String instanceId, String publishedBy) {
        ShiftInstance instance = shiftInstanceRepository.findById(instanceId)
            .orElseThrow(() -> new RuntimeException("Shift instance not found with id: " + instanceId));
        
        if (instance.getStatus() != ShiftInstance.ShiftStatus.DRAFT) {
            throw new IllegalStateException("Only draft shifts can be published");
        }
        
        instance.setStatus(ShiftInstance.ShiftStatus.PUBLISHED);
        instance.setPublishedBy(publishedBy);
        instance.setPublishedAt(LocalDateTime.now());
        
        return shiftInstanceRepository.save(instance);
    }
    
    /**
     * Assign user to shift instance
     */
    public Assignment assignUserToShift(String shiftInstanceId, String userId, String assignedBy) {
        ShiftInstance shiftInstance = shiftInstanceRepository.findById(shiftInstanceId)
            .orElseThrow(() -> new RuntimeException("Shift instance not found with id: " + shiftInstanceId));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        if (shiftInstance.getStatus() != ShiftInstance.ShiftStatus.DRAFT) {
            throw new IllegalStateException("Cannot assign to published shifts");
        }
        
        // Check if user is already assigned to this shift
        List<Assignment> existingAssignments = assignmentRepository.findByShiftInstanceId(shiftInstanceId);
        boolean alreadyAssigned = existingAssignments.stream()
            .anyMatch(a -> a.getUser().getId().equals(userId) && 
                          a.getStatus() == Assignment.AssignmentStatus.ACTIVE);
        
        if (alreadyAssigned) {
            throw new IllegalStateException("User is already assigned to this shift");
        }
        
        Assignment assignment = new Assignment(shiftInstance, user, assignedBy);
        return assignmentRepository.save(assignment);
    }
    
    /**
     * Get shifts for a date range
     */
    @Transactional(readOnly = true)
    public List<ShiftInstance> getShiftsInDateRange(LocalDate startDate, LocalDate endDate) {
        return shiftInstanceRepository.findByShiftDateBetween(startDate, endDate);
    }
    
    /**
     * Get published shifts for a date range
     */
    @Transactional(readOnly = true)
    public List<ShiftInstance> getPublishedShiftsInDateRange(LocalDate startDate, LocalDate endDate) {
        return shiftInstanceRepository.findPublishedShiftsInPeriod(startDate, endDate);
    }
    
    /**
     * Get assignments for user in date range
     */
    @Transactional(readOnly = true)
    public List<Assignment> getUserAssignmentsInDateRange(String userId, LocalDate startDate, LocalDate endDate) {
        return assignmentRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }
    
    /**
     * Cancel assignment
     */
    public Assignment cancelAssignment(String assignmentId, String reason) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new RuntimeException("Assignment not found with id: " + assignmentId));
        
        if (assignment.getStatus() != Assignment.AssignmentStatus.ACTIVE) {
            throw new IllegalStateException("Only active assignments can be cancelled");
        }
        
        assignment.setStatus(Assignment.AssignmentStatus.CANCELLED);
        assignment.setNotes(reason);
        assignment.setUpdatedAt(LocalDateTime.now());
        
        return assignmentRepository.save(assignment);
    }
    
    /**
     * Get all shift templates
     */
    @Transactional(readOnly = true)
    public List<ShiftTemplate> getAllShiftTemplates() {
        return shiftTemplateRepository.findAll();
    }
    
    /**
     * Update shift template
     */
    public ShiftTemplate updateShiftTemplate(String templateId, String name, String description) {
        ShiftTemplate template = shiftTemplateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Shift template not found with id: " + templateId));
        
        template.setName(name);
        template.setDescription(description);
        
        return shiftTemplateRepository.save(template);
    }
    
    /**
     * Deactivate shift template
     */
    public ShiftTemplate deactivateShiftTemplate(String templateId) {
        ShiftTemplate template = shiftTemplateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Shift template not found with id: " + templateId));
        
        template.setIsActive(false);
        return shiftTemplateRepository.save(template);
    }
    
    /**
     * Get all locations for template creation dropdowns
     */
    @Transactional(readOnly = true)
    public List<Location> findAllLocations() {
        return locationRepository.findAll();
    }
    
    /**
     * Get all roles for template creation dropdowns
     */
    @Transactional(readOnly = true)
    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }
}