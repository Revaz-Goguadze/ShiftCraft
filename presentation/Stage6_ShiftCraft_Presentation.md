# ShiftCraft - Stage 6 EPAM Capstone Presentation
## Healthcare Staff Scheduling & Time Management System

---

## Slide 1: Title Slide
ShiftCraft
Healthcare Staff Scheduling & Time Management System

EPAM Capstone Stage 6 Final Presentation
By: [Your Name]
Date: [Presentation Date]

Preventing illegal rosters, making swaps auditable

---

## Slide 2: Introduction & Agenda
About Me
- [Your Background]
- [Your Role/Experience in Healthcare/Tech]

Today's Agenda
- Problem & Solution Overview
- Live System Demonstration 
- Architecture Deep Dive
- Code Walkthrough
- Q&A Session

Duration: 15 minutes including questions

---

## Slide 3: The Problem
Healthcare Scheduling Challenges

Current Pain Points:
- Manual scheduling prone to errors
- Skill-based assignment complexity
- Leave request approval bottlenecks
- Timesheet generation inefficiency
- Compliance and audit trails missing

Business Impact:
- Overtime cost overruns
- Patient care quality risks
- Staff burnout from poor scheduling
- Administrative overhead

---

## Slide 4: ShiftCraft Solution
Purpose & Vision

Mission:Streamline healthcare staff scheduling with automated workflows and intelligent assignment

Target Users:
-Healthcare Managers: Schedule oversight, approvals, reporting
-Clinical Staff: Personal schedules, leave requests, timesheets
-Administrators: System configuration, user management

Value Proposition:
- Reduce scheduling errors by 90%
- Cut timesheet processing time in half
- Ensure skill-based assignments
- Maintain complete audit trails

---

## Slide 5: User Stories Implemented
Manager Persona: "Sarah - Charge Nurse"
- "As a manager, I want to view weekly schedules for all staff"
- "As a manager, I need to approve leave requests efficiently"
- "As a manager, I want to generate payroll timesheets"
- "As a manager, I need to create shift templates"

Staff Persona: "Mike - Registered Nurse"
- "As staff, I want to see my personal schedule"
- "As staff, I need to submit leave requests"
- "As staff, I want to view my timesheets"
- "As staff, I need skill-based shift assignments"

---

## Slide 6: Core Features Overview
Six Key Functional Areas

Weekly Scheduling
- Role-based schedule views
- Skill-based assignments
- Real-time availability

Leave Management
- Request submission workflow
- Manager approval process
- Calendar integration

Shift Templates
- Reusable shift patterns
- Location and skill mapping
- Duration calculations

Timesheet Generation
- Automated time tracking
- Overtime calculations
- Export capabilities

Security & Roles
- Manager vs Staff access
- Secure authentication
- Audit logging

Business Intelligence
- Scheduling analytics
- Performance reporting
- Compliance tracking

---

## Slide 7: System Architecture Overview
Three-Tier Architecture

```
┌─────────────────────┐
│   PRESENTATION      │  🖥 Thymeleaf Views + Spring MVC
│     LAYER           │      Controllers, Forms, Security
└─────────────────────┘
           │
┌─────────────────────┐
│   APPLICATION       │   Business Logic Services  
│     LAYER           │      Workflow Management, Validation
└─────────────────────┘
           │
┌─────────────────────┐
│   DATA ACCESS       │   Spring Data JPA + PostgreSQL
│     LAYER           │      Entities, Repositories, Flyway
└─────────────────────┘
```

Architectural Principles:
- Separation of concerns
- Dependency injection
- Layer isolation
- Single responsibility

---

## Slide 8: Technology Stack
Modern Enterprise Java Stack

Backend Framework:
- Spring Boot 3.4.8 (Java 21)
- Spring Security 6 (Authentication & Authorization)
- Spring Data JPA (ORM & Repository Pattern)

Database & Migration:
- PostgreSQL (Production) / H2 (Demo)
- Flyway Database Migrations
- HikariCP Connection Pooling

Frontend & Presentation:
- Thymeleaf Server-Side Templates
- Bootstrap 5 (Responsive Design)
- Feather Icons (Consistent UI)

Testing & Quality:
- JUnit 5 + Mockito (Unit Tests)
- Testcontainers (Integration Tests)
- Spring Boot Test (Web Layer Tests)

---

## Slide 9: Database Design
Entity Relationship Model (8+ Entities)

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│    USER     │────│ USER_ROLES  │────│    ROLE     │
│ id, email   │    │   (M:M)     │    │ name, desc  │
│ firstName   │    └─────────────┘    └─────────────┘
│ lastName    │
└─────────────┘
       │
       │ (M:M)
┌─────────────┐    ┌─────────────────┐
│USER_SKILLS  │────│     SKILL       │
│   (M:M)     │    │ name, category  │
└─────────────┘    └─────────────────┘

┌─────────────┐    ┌──────────────┐    ┌─────────────┐
│ LOCATION    │────│SHIFT_TEMPLATE│────│SHIFT_INSTANCE│
│ name, type  │    │ name, hours  │    │ date, time  │
└─────────────┘    └──────────────┘    └─────────────┘
                                              │
                   ┌─────────────┐            │
                   │LEAVE_REQUEST│            │
                   │ dates, type │            │
                   └─────────────┘            │
                                              │
                   ┌─────────────┐    ┌─────────────┐
                   │ TIMESHEET   │────│ASSIGNMENT   │
                   │ period, OT  │    │ user, shift │
                   └─────────────┘    └─────────────┘
```

Key Relationships:
- User ↔ Role (Many-to-Many)
- User ↔ Skill (Many-to-Many)
- Location → ShiftTemplate (One-to-Many)
- ShiftTemplate → ShiftInstance (One-to-Many)

---

## Slide 10: Live Demo Introduction
System Demonstration

Demo Scenario: "A Week in ShiftCraft"

What We'll See:
1. Role-based authentication
2.  Manager dashboard & scheduling
3. Shift template creation
4. Leave request workflow (Staff → Manager)
5. Timesheet generation & export

Demo Users:
- Manager: manager@shiftcraft.com
- Staff: nurse1@shiftcraft.com

Ready to see ShiftCraft in action!

---

## Slide 11: Authentication & Security Demo
Role-Based Access Control

Spring Security Implementation:
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login").permitAll()
                .requestMatchers("/approvals/").hasRole("MANAGER")
                .requestMatchers("/templates/").hasAnyRole("STAFF", "MANAGER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form.loginPage("/login"))
            .build();
    }
}
```

Security Features:
- CSRF Protection
- Password Encoding (BCrypt)
- Session Management
- Role-based URL Protection

---

## Slide 12: Scheduling Features Demo
Intelligent Shift Management

Manager View Features:
- Weekly schedule overview (all staff)
- Skill-based assignment suggestions
- Conflict detection & resolution
- Responsive grid layout

Staff View Features:
- Personal schedule ("My Shifts")
- Skill-matched assignments only
- Quick action buttons
- Schedule change notifications

Template System:
- Reusable shift patterns
- Automatic duration calculation
- Location-specific templates
- Skill requirement mapping

---

## Slide 13: Leave Management Workflow
Streamlined Approval Process

Request Submission (Staff):
```java
@Entity
public class LeaveRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveType type; // VACATION, SICK, PERSONAL
    private LeaveStatus status; // PENDING, APPROVED, DENIED
    private String reason;
    
    @ManyToOne
    private User requestedBy;
    
    @ManyToOne  
    private User approvedBy;
}
```

Approval Features:
- Centralized approval dashboard
- One-click approve/deny
- Comments and feedback
- Automatic notifications
- Leave balance tracking

---

## Slide 14: Reporting & Analytics
Data-Driven Insights

Timesheet Generation:
- Flexible period selection (weekly/monthly)
- Automatic overtime calculation (>40 hours)
- Summary statistics
- Export to CSV/Excel

Key Metrics:
```java
@Service
public class TimesheetService {
    
    public TimesheetSummary generateTimesheet(User user, LocalDate start, LocalDate end) {
        List<Assignment> assignments = assignmentRepository
            .findByUserAndDateRange(user, start, end);
        
        double regularHours = calculateRegularHours(assignments);
        double overtimeHours = Math.max(0, totalHours - 40.0);
        
        return new TimesheetSummary(regularHours, overtimeHours);
    }
}
```

---

## Slide 15: Code Architecture Deep Dive
Clean Code & Best Practices

Entity Design with JPA:
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
    
    @ManyToMany
    @JoinTable(name = "user_skills",
        joinColumns = @JoinColumn(name = "user_id"), 
        inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<Skill> skills = new HashSet<>();
}
```

Layer Isolation:
- Controllers → Services only
- Services → Repositories only  
- Repositories → Entities only

---

## Slide 16: Testing Strategy
Comprehensive Test Coverage

Unit Testing with Mockito:
```java
@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {
    
    @Mock
    private LeaveRequestRepository leaveRequestRepository;
    
    @InjectMocks
    private LeaveService leaveService;
    
    @Test
    void shouldApproveValidLeaveRequest() {
        // Given
        LeaveRequest request = createPendingRequest();
        when(leaveRequestRepository.save(any())).thenReturn(request);
        
        // When
        LeaveRequest approved = leaveService.approve(request.getId());
        
        // Then
        assertEquals(LeaveStatus.APPROVED, approved.getStatus());
        verify(leaveRequestRepository).save(request);
    }
}
```

Integration Testing:
- Testcontainers for database
- WebMvcTest for controllers
- Repository layer testing

Test Coverage:85%+ on business logic

---

## Slide 17: Future Enhancements
Roadmap & Scalability

Phase 2 Features:
- Mobile app for on-the-go access
- Real-time notifications (WebSocket)
- Advanced analytics dashboard
- AI-powered scheduling optimization

Technical Improvements:
- Redis caching for performance
- Horizontal scaling with load balancers
- OAuth2/SAML integration
- REST API for third-party integrations

Business Expansion:
- Multi-facility support
- Budget planning integration
- Compliance reporting
- Shift trading marketplace

---

## Slide 18: Questions & Thank You
Q&A Session

Key Achievements:
- Complete 3-tier architecture implementation
- 8+ entity database with complex relationships
- Role-based security with Spring Security
- Full CRUD operations on all entities
- 85%+ test coverage with unit & integration tests
- 6 responsive Thymeleaf views

Technical Depth:
- Spring Boot 3.4.8 with Java 21
- PostgreSQL with Flyway migrations
- Mockito & Testcontainers testing
- Bootstrap 5 responsive design

Ready for Questions!

Thank you for your attention.

---

## Presenter Notes & Timing

Total Time: 13 minutes (2-minute buffer)

1.Introduction(1 min): Slides 1-2
2.Problem & Solution(2 min): Slides 3-5  
3.Live Demo(6 min): Slides 10-14 with screen share
4.Architecture(3 min): Slides 7-9, 15-16
5.Conclusion(1 min): Slides 17-18

Demo Flow Notes:
- Start with login screen
- Show manager dashboard first
- Create a shift template
- Submit leave request as staff
- Switch back to manager to approve
- Generate and show timesheet
- Highlight security (403 errors for staff on manager pages)

Backup Plans:
- Screenshots ready if demo fails
- Code snippets prepared offline
- Alternative demo scenarios available

Q&A Preparation:
- Technology choice rationale ready
- Scalability discussion points prepared
- Security implementation details ready
- Business logic explanation ready