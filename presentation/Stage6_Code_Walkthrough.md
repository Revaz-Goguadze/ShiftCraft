# ShiftCraft - Stage 6 Code Walkthrough
## Presentation-Ready Code Snippets

---

## Overview for Slide 15: Code Architecture Deep Dive

**Time Allocation: 4 minutes**
- Entity Design (1.5 min): User entity with many-to-many relationships
- Controller Layer (1 min): Security and role-based logic
- Service Layer (1 min): Business logic and transactions
- Testing (30 sec): Unit test with Mockito example

---

## 1. Entity Design with JPA (1.5 minutes)

### **User Entity - Many-to-Many Relationships** *(Slide 15 Example)*

```java
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @NotBlank @Email
    @Column(unique = true, nullable = false)
    private String email;
    
    @NotBlank
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @NotBlank
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    // ✨ MANY-TO-MANY: User ↔ Role (user_roles join table)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
    // ✨ MANY-TO-MANY: User ↔ Skill (user_skills join table)  
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_skills",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<UserSkill> skills = new HashSet<>();
    
    // ✨ ONE-TO-MANY: User → LeaveRequest
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<LeaveRequest> leaveRequests = new HashSet<>();
    
    // ✨ ONE-TO-MANY: User → Assignment
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Assignment> assignments = new HashSet<>();
    
    // Constructors, getters, setters...
}
```

**🎤 Presentation Points:**
- "Notice the clean many-to-many relationships using @JoinTable annotations"
- "We have User-Role for security and User-Skill for intelligent shift assignments"
- "UUID primary keys ensure scalability across distributed systems"
- "Proper fetch strategies: EAGER for roles (security), LAZY for large collections"

---

## 2. Controller Layer - Security & Role Logic (1 minute)

### **ScheduleController - Role-Based Views** *(Slide 11 Security Demo)*

```java
@Controller
@RequestMapping("/schedule")
public class ScheduleController {
    
    private final ScheduleService scheduleService;
    private final UserService userService;
    
    @GetMapping
    public String viewSchedule(@RequestParam(required = false) String date, Model model) {
        LocalDate scheduleDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        
        // ✨ SECURITY: Get authenticated user from Spring Security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();
        
        Optional<User> currentUser = userService.findByEmail(currentUserEmail);
        
        if (currentUser.isPresent()) {
            User user = currentUser.get();
            boolean isManager = userService.userHasRole(user.getId(), "MANAGER");
            
            if (isManager) {
                // ✨ MANAGER VIEW: Full schedule for all users
                ScheduleService.WeeklySchedule weeklySchedule = 
                    scheduleService.getWeeklySchedule(scheduleDate);
                model.addAttribute("weeklySchedule", weeklySchedule);
                model.addAttribute("isManager", true);
            } else {
                // ✨ STAFF VIEW: Personal schedule only
                ScheduleService.UserWeeklySchedule userSchedule = 
                    scheduleService.getUserWeeklySchedule(user.getId(), scheduleDate);
                model.addAttribute("userSchedule", userSchedule);
                model.addAttribute("isManager", false);
            }
            
            model.addAttribute("currentUser", user);
            model.addAttribute("selectedDate", scheduleDate);
        }
        
        return "schedule"; // Returns different views based on role
    }
}
```

**🎤 Presentation Points:**
- "Controllers depend only on services, never repositories - perfect layer isolation"
- "Role-based logic determines what data users see"
- "Spring Security integration provides seamless authentication"
- "Single endpoint, different views based on user role - clean and maintainable"

---

## 3. Service Layer - Business Logic (1 minute)

### **ShiftService - Business Workflows** *(Slide 15 Business Logic)*

```java
@Service
@Transactional
public class ShiftService {
    
    private final ShiftTemplateRepository shiftTemplateRepository;
    private final ShiftInstanceRepository shiftInstanceRepository;
    private final AssignmentRepository assignmentRepository;
    // ... other repositories
    
    /**
     * ✨ BUSINESS LOGIC: Create shift instance with validation
     */
    public ShiftInstance createShiftInstance(String templateId, LocalDate shiftDate) {
        ShiftTemplate template = shiftTemplateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Shift template not found"));
        
        // ✨ BUSINESS RULE: Prevent duplicate shifts
        Optional<ShiftInstance> existingInstance = shiftInstanceRepository
            .findByTemplateIdAndShiftDate(templateId, shiftDate);
        
        if (existingInstance.isPresent()) {
            throw new IllegalStateException("Shift instance already exists for this date");
        }
        
        ShiftInstance instance = new ShiftInstance(template, shiftDate);
        return shiftInstanceRepository.save(instance);
    }
    
    /**
     * ✨ BUSINESS LOGIC: Assign user with validation
     */
    public Assignment assignUserToShift(String shiftInstanceId, String userId, String assignedBy) {
        ShiftInstance shiftInstance = shiftInstanceRepository.findById(shiftInstanceId)
            .orElseThrow(() -> new RuntimeException("Shift instance not found"));
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // ✨ BUSINESS RULE: Only assign to draft shifts
        if (shiftInstance.getStatus() != ShiftInstance.ShiftStatus.DRAFT) {
            throw new IllegalStateException("Cannot assign to published shifts");
        }
        
        // ✨ BUSINESS RULE: Prevent double assignments
        List<Assignment> existingAssignments = assignmentRepository
            .findByShiftInstanceId(shiftInstanceId);
        boolean alreadyAssigned = existingAssignments.stream()
            .anyMatch(a -> a.getUser().getId().equals(userId) && 
                          a.getStatus() == Assignment.AssignmentStatus.ACTIVE);
        
        if (alreadyAssigned) {
            throw new IllegalStateException("User is already assigned to this shift");
        }
        
        Assignment assignment = new Assignment(shiftInstance, user, assignedBy);
        return assignmentRepository.save(assignment);
    }
}
```

**🎤 Presentation Points:**
- "Services encapsulate complex business rules and validation logic"
- "Transactional annotations ensure data consistency"
- "Clear error handling with meaningful exception messages"
- "Business rules prevent scheduling conflicts and invalid assignments"

---

## 4. Testing Strategy - Mockito Unit Tests (30 seconds)

### **LeaveServiceTest - Comprehensive Testing** *(Slide 16 Example)*

```java
@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private LeaveService leaveService;
    
    @Test
    void submitLeaveRequest_Success() {
        // ✨ ARRANGE: Set up test data and mocks
        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(7);
        
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(leaveRequestRepository.findUserLeaveInPeriod("user123", startDate, endDate))
            .thenReturn(Collections.emptyList());
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(testLeaveRequest);

        // ✨ ACT: Execute the business logic
        LeaveRequest result = leaveService.submitLeaveRequest(
            "user123", startDate, endDate, LeaveRequest.LeaveType.VACATION, "Family vacation");

        // ✨ ASSERT: Verify behavior and interactions
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(LeaveRequest.LeaveStatus.PENDING);
        
        verify(userRepository).findById("user123");
        verify(leaveRequestRepository).findUserLeaveInPeriod("user123", startDate, endDate);
        verify(leaveRequestRepository).save(any(LeaveRequest.class));
    }
    
    @Test
    void submitLeaveRequest_OverlappingRequest_ThrowsException() {
        // ✨ BUSINESS RULE TESTING: Verify overlap prevention
        when(userRepository.findById("user123")).thenReturn(Optional.of(testUser));
        when(leaveRequestRepository.findUserLeaveInPeriod("user123", startDate, endDate))
            .thenReturn(Arrays.asList(existingLeaveRequest));

        assertThatThrownBy(() -> 
            leaveService.submitLeaveRequest("user123", startDate, endDate, 
                LeaveRequest.LeaveType.VACATION, "reason"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Leave request overlaps with existing leave");
    }
}
```

**🎤 Presentation Points:**
- "Mockito provides clean isolation for unit testing business logic"
- "Test coverage includes both happy path and edge cases"
- "Verification ensures proper repository interactions"
- "85%+ test coverage on all business logic services"

---

## Code Walkthrough Presentation Script

### **Opening (30 seconds)**
> "Let's dive into the code architecture that powers ShiftCraft. I'll show you how we implemented clean separation of concerns with Spring Boot."

### **Entity Layer (1.5 minutes)**
> "Starting with our data model - here's the User entity showing our many-to-many relationships..."
- Show User.java snippet
- Point out @ManyToMany annotations
- Explain join tables for User-Role and User-Skill
- Highlight fetch strategies and cascade settings

### **Controller Layer (1 minute)**  
> "Moving up to the presentation layer - our controllers handle role-based views..."
- Show ScheduleController.java snippet
- Demonstrate Spring Security integration
- Explain manager vs staff view logic
- Highlight layer isolation principles

### **Service Layer (1 minute)**
> "The heart of our application - business logic in the service layer..."
- Show ShiftService.java snippet
- Point out business rule validation
- Explain transactional behavior
- Highlight error handling patterns

### **Testing (30 seconds)**
> "Finally, our testing strategy with Mockito ensures reliable business logic..."
- Show LeaveServiceTest.java snippet
- Demonstrate arrange-act-assert pattern
- Point out mock verification
- Mention 85% test coverage

### **Closing (30 seconds)**
> "This architecture ensures maintainability, testability, and scalability - perfect for healthcare environments where reliability is critical."

---

## Additional Code Examples (Backup/Extended Discussion)

### **Security Configuration**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**").permitAll()
                .requestMatchers("/approvals/**").hasRole("MANAGER")
                .requestMatchers("/templates/create").hasRole("MANAGER") 
                .requestMatchers("/timesheets/export").hasRole("MANAGER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/schedule", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            .build();
    }
}
```

### **Repository Layer Example**
```java
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, String> {
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = 'PENDING' ORDER BY lr.requestedAt ASC")
    List<LeaveRequest> findPendingRequests();
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.user.id = :userId " +
           "AND ((lr.startDate BETWEEN :startDate AND :endDate) " +
           "OR (lr.endDate BETWEEN :startDate AND :endDate) " +
           "OR (lr.startDate <= :startDate AND lr.endDate >= :endDate))")
    List<LeaveRequest> findUserLeaveInPeriod(@Param("userId") String userId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);
}
```

---

## Presentation Tips

### **IDE Setup for Live Code Review**
- **Font Size**: Increase to 16-18pt for visibility
- **Theme**: Light theme for better projection
- **File Structure**: Have key files bookmarked
- **Code Folding**: Collapse irrelevant sections

### **Backup Plan**
- Screenshots of all code snippets ready
- Slide versions of code formatted for readability
- Printed copies as ultimate backup

### **Q&A Preparation**
**"Why Spring Boot over other frameworks?"**
- Rapid development with auto-configuration
- Excellent testing support
- Production-ready features out of the box
- Strong ecosystem and community

**"How does this scale to larger healthcare systems?"**
- Stateless design enables horizontal scaling
- Database optimization with proper indexing
- Caching layers (Redis) for frequently accessed data
- Microservices decomposition for large deployments