package com.example.shiftcraft.application.config;

import com.example.shiftcraft.persistence.entity.*;
import com.example.shiftcraft.persistence.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Component
@Profile("!test") // Don't run in test profile
public class DataLoader implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final LocationRepository locationRepository;
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final ShiftInstanceRepository shiftInstanceRepository;
    private final AssignmentRepository assignmentRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public DataLoader(UserRepository userRepository, RoleRepository roleRepository,
                     SkillRepository skillRepository, UserSkillRepository userSkillRepository,
                     LocationRepository locationRepository, ShiftTemplateRepository shiftTemplateRepository,
                     ShiftInstanceRepository shiftInstanceRepository, AssignmentRepository assignmentRepository,
                     LeaveRequestRepository leaveRequestRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
        this.locationRepository = locationRepository;
        this.shiftTemplateRepository = shiftTemplateRepository;
        this.shiftInstanceRepository = shiftInstanceRepository;
        this.assignmentRepository = assignmentRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Skip if data already exists
        if (userRepository.count() > 0) {
            System.out.println("Demo data already exists, skipping data loading...");
            return;
        }
        
        System.out.println("Loading demo data...");
        
        // Create Roles
        Role adminRole = createRole("ADMIN", "System Administrator");
        Role managerRole = createRole("MANAGER", "Department Manager");
        Role staffRole = createRole("STAFF", "Staff Member");
        Role financeRole = createRole("FINANCE", "Finance Department");
        
        // Create Skills
        Skill nursingSkill = createSkill("Nursing", "General nursing care", "Healthcare");
        Skill emergencySkill = createSkill("Emergency Care", "Emergency and critical care", "Healthcare");
        Skill laboratorySkill = createSkill("Laboratory", "Lab testing and analysis", "Laboratory");
        Skill receptionSkill = createSkill("Reception", "Front desk and customer service", "Administrative");
        Skill pharmacySkill = createSkill("Pharmacy", "Medication dispensing and consultation", "Healthcare");
        
        // Create Location
        Location mainClinic = createLocation("Main Clinic", "America/New_York", "123 Healthcare Ave", "Medical City");
        
        // Create Users with passwords
        String defaultPassword = passwordEncoder.encode("password123");
        
        User adminUser = createUser("admin@shiftcraft.com", defaultPassword, "Admin", "User", Set.of(adminRole));
        User managerUser = createUser("manager@shiftcraft.com", defaultPassword, "John", "Manager", Set.of(managerRole));
        User nurse1 = createUser("nurse1@shiftcraft.com", defaultPassword, "Alice", "Johnson", Set.of(staffRole));
        User nurse2 = createUser("nurse2@shiftcraft.com", defaultPassword, "Bob", "Wilson", Set.of(staffRole));
        User financeUser = createUser("finance@shiftcraft.com", defaultPassword, "Carol", "Finance", Set.of(financeRole));
        User receptionist = createUser("reception@shiftcraft.com", defaultPassword, "Dana", "Smith", Set.of(staffRole));
        
        // Assign skills to users
        createUserSkill(nurse1, nursingSkill, UserSkill.SkillLevel.EXPERT);
        createUserSkill(nurse1, emergencySkill, UserSkill.SkillLevel.INTERMEDIATE);
        createUserSkill(nurse2, nursingSkill, UserSkill.SkillLevel.INTERMEDIATE);
        createUserSkill(nurse2, laboratorySkill, UserSkill.SkillLevel.EXPERT);
        createUserSkill(receptionist, receptionSkill, UserSkill.SkillLevel.EXPERT);
        
        // Create Shift Templates
        ShiftTemplate dayNursingShift = createShiftTemplate(
            "Day Shift Nursing", mainClinic, staffRole,
            LocalTime.of(8, 0), LocalTime.of(16, 0), 30,
            "Regular day shift for nursing staff"
        );
        dayNursingShift.getRequiredSkills().add(nursingSkill);
        shiftTemplateRepository.save(dayNursingShift);
        
        ShiftTemplate nightNursingShift = createShiftTemplate(
            "Night Shift Nursing", mainClinic, staffRole,
            LocalTime.of(20, 0), LocalTime.of(4, 0), 30,
            "Night shift for nursing staff with emergency care capability"
        );
        nightNursingShift.getRequiredSkills().add(nursingSkill);
        nightNursingShift.getRequiredSkills().add(emergencySkill);
        shiftTemplateRepository.save(nightNursingShift);
        
        ShiftTemplate labShift = createShiftTemplate(
            "Laboratory Shift", mainClinic, staffRole,
            LocalTime.of(9, 0), LocalTime.of(17, 0), 45,
            "Laboratory shift for testing and analysis"
        );
        labShift.getRequiredSkills().add(laboratorySkill);
        shiftTemplateRepository.save(labShift);
        
        ShiftTemplate receptionShift = createShiftTemplate(
            "Reception Shift", mainClinic, staffRole,
            LocalTime.of(8, 0), LocalTime.of(17, 0), 60,
            "Front desk and patient reception"
        );
        receptionShift.getRequiredSkills().add(receptionSkill);
        shiftTemplateRepository.save(receptionShift);
        
        // Create shift instances for current week
        LocalDate today = LocalDate.now();
        LocalDate mondayThisWeek = today.minusDays(today.getDayOfWeek().getValue() - 1);
        
        for (int i = 0; i < 7; i++) {
            LocalDate shiftDate = mondayThisWeek.plusDays(i);
            
            // Day nursing shifts (weekdays only)
            if (i < 5) {
                ShiftInstance dayShiftInstance = createShiftInstance(dayNursingShift, shiftDate);
                createAssignment(dayShiftInstance, nurse1, managerUser);
                
                ShiftInstance labShiftInstance = createShiftInstance(labShift, shiftDate);
                createAssignment(labShiftInstance, nurse2, managerUser);
                
                ShiftInstance receptionShiftInstance = createShiftInstance(receptionShift, shiftDate);
                createAssignment(receptionShiftInstance, receptionist, managerUser);
            }
            
            // Night nursing shifts (all days)
            ShiftInstance nightShiftInstance = createShiftInstance(nightNursingShift, shiftDate);
            User nightNurse = (i % 2 == 0) ? nurse1 : nurse2;
            createAssignment(nightShiftInstance, nightNurse, managerUser);
        }
        
        // Create some leave requests
        createLeaveRequest(nurse1, today.plusDays(10), today.plusDays(12), 
            LeaveRequest.LeaveType.VACATION, "Family vacation");
        
        createLeaveRequest(nurse2, today.plusDays(5), today.plusDays(5), 
            LeaveRequest.LeaveType.SICK, "Medical appointment");
        
        // Create approved leave request
        LeaveRequest approvedLeave = createLeaveRequest(receptionist, today.minusDays(3), today.minusDays(1), 
            LeaveRequest.LeaveType.PERSONAL, "Personal matters");
        approvedLeave.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        approvedLeave.setReviewedBy(managerUser.getId());
        approvedLeave.setReviewedAt(java.time.LocalDateTime.now().minusDays(5));
        approvedLeave.setReviewNotes("Approved. Have a good break!");
        leaveRequestRepository.save(approvedLeave);
        
        System.out.println("Demo data loaded successfully!");
        System.out.println("Login credentials:");
        System.out.println("Manager: manager@shiftcraft.com / password123");
        System.out.println("Staff (Nurse): nurse1@shiftcraft.com / password123");
        System.out.println("Staff (Nurse): nurse2@shiftcraft.com / password123");
        System.out.println("Staff (Reception): reception@shiftcraft.com / password123");
        System.out.println("Finance: finance@shiftcraft.com / password123");
        System.out.println("Admin: admin@shiftcraft.com / password123");
    }
    
    private Role createRole(String name, String description) {
        Role role = new Role(name, description);
        return roleRepository.save(role);
    }
    
    private Skill createSkill(String name, String description, String category) {
        Skill skill = new Skill(name, description);
        skill.setCategory(category);
        return skillRepository.save(skill);
    }
    
    private Location createLocation(String name, String timezone, String address, String city) {
        Location location = new Location(name, timezone);
        location.setAddressLine(address);
        location.setCity(city);
        location.setState("NY");
        location.setCountry("USA");
        return locationRepository.save(location);
    }
    
    private User createUser(String email, String passwordHash, String firstName, String lastName, Set<Role> roles) {
        User user = new User(email, passwordHash, firstName, lastName);
        user.setRoles(roles);
        return userRepository.save(user);
    }
    
    private UserSkill createUserSkill(User user, Skill skill, UserSkill.SkillLevel level) {
        UserSkill userSkill = new UserSkill(user, skill, level);
        return userSkillRepository.save(userSkill);
    }
    
    private ShiftTemplate createShiftTemplate(String name, Location location, Role role,
                                            LocalTime startTime, LocalTime endTime, 
                                            Integer breakMinutes, String description) {
        ShiftTemplate template = new ShiftTemplate(name, location, role, startTime, endTime);
        template.setBreakMinutes(breakMinutes);
        template.setDescription(description);
        return shiftTemplateRepository.save(template);
    }
    
    private ShiftInstance createShiftInstance(ShiftTemplate template, LocalDate date) {
        ShiftInstance instance = new ShiftInstance(template, date);
        instance.setStatus(ShiftInstance.ShiftStatus.PUBLISHED);
        instance.setPublishedAt(java.time.LocalDateTime.now().minusDays(1));
        return shiftInstanceRepository.save(instance);
    }
    
    private Assignment createAssignment(ShiftInstance shiftInstance, User user, User assignedBy) {
        Assignment assignment = new Assignment(shiftInstance, user, assignedBy.getId());
        return assignmentRepository.save(assignment);
    }
    
    private LeaveRequest createLeaveRequest(User user, LocalDate startDate, LocalDate endDate,
                                          LeaveRequest.LeaveType type, String reason) {
        LeaveRequest request = new LeaveRequest(user, startDate, endDate, type, reason);
        return leaveRequestRepository.save(request);
    }
}