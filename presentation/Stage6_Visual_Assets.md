# ShiftCraft - Stage 6 Visual Assets
## Diagrams and Screenshots for Presentation

---

## 1. Three-Tier Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   LOGIN     │  │  SCHEDULE   │  │     REQUESTS        │  │
│  │ Controller  │  │ Controller  │  │    Controller       │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │  APPROVALS  │  │ TEMPLATES   │  │    TIMESHEETS       │  │
│  │ Controller  │  │ Controller  │  │    Controller       │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│                                                             │
│  Spring Security    Thymeleaf Views    Bootstrap UI       │
└─────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                   APPLICATION LAYER                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │    USER     │  │   SHIFT     │  │       LEAVE         │  │
│  │   Service   │  │  Service    │  │      Service        │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ TIMESHEET   │  │  TEMPLATE   │  │    ASSIGNMENT       │  │
│  │  Service    │  │  Service    │  │      Service        │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│                                                             │
│  Business Logic     Workflows         Validation Rules    │
└─────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                    DATA ACCESS LAYER                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │    USER     │  │   SHIFT     │  │       LEAVE         │  │
│  │ Repository  │  │Repository   │  │    Repository       │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │ TIMESHEET   │  │  TEMPLATE   │  │    ASSIGNMENT       │  │
│  │Repository   │  │Repository   │  │    Repository       │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│                                                             │
│  Spring Data JPA    PostgreSQL        Flyway Migrations  │
└─────────────────────────────────────────────────────────────┘
```

**Key Principles:**
- **Separation of Concerns**: Each layer has distinct responsibility
- **Dependency Direction**: Top-down dependencies only
- **Layer Isolation**: No layer bypassing (Controller → Service → Repository)
- **Single Responsibility**: One purpose per component

---

## 2. Entity Relationship Diagram

```
                    USER MANAGEMENT
    ┌─────────────────┐    Many-to-Many    ┌─────────────────┐
    │      USER       │◄──────────────────►│      ROLE       │
    │─────────────────│                    │─────────────────│
    │ • id (PK)       │                    │ • id (PK)       │
    │ • email (UQ)    │                    │ • name          │
    │ • firstName     │                    │ • description   │
    │ • lastName      │                    └─────────────────┘
    │ • password      │
    │ • enabled       │
    └─────────────────┘
           │
           │ Many-to-Many
           ▼
    ┌─────────────────┐
    │      SKILL      │
    │─────────────────│
    │ • id (PK)       │
    │ • name          │
    │ • category      │
    │ • description   │
    └─────────────────┘

                  SCHEDULING CORE
    ┌─────────────────┐    One-to-Many     ┌──────────────────┐
    │    LOCATION     │◄───────────────────│  SHIFT_TEMPLATE  │
    │─────────────────│                    │──────────────────│
    │ • id (PK)       │                    │ • id (PK)        │
    │ • name          │                    │ • name           │
    │ • type          │                    │ • startTime      │
    │ • address       │                    │ • endTime        │
    └─────────────────┘                    │ • breakMinutes   │
                                           │ • location_id(FK)│
                                           └──────────────────┘
                                                     │
                                                     │ One-to-Many
                                                     ▼
                                           ┌──────────────────┐
                                           │ SHIFT_INSTANCE   │
                                           │──────────────────│
                                           │ • id (PK)        │
                                           │ • date           │
                                           │ • startTime      │
                                           │ • endTime        │
                                           │ • template_id(FK)│
                                           └──────────────────┘
                                                     │
                                                     │ One-to-Many
                                                     ▼
    ┌─────────────────┐    Many-to-One     ┌──────────────────┐
    │      USER       │◄───────────────────│   ASSIGNMENT     │
    │    (Reference)  │                    │──────────────────│
    └─────────────────┘                    │ • id (PK)        │
                                           │ • user_id (FK)   │
                                           │ • shift_id (FK)  │
                                           │ • assignedAt     │
                                           └──────────────────┘

                  LEAVE MANAGEMENT
    ┌─────────────────┐    One-to-Many     ┌──────────────────┐
    │      USER       │◄───────────────────│  LEAVE_REQUEST   │
    │   (Reference)   │                    │──────────────────│
    └─────────────────┘                    │ • id (PK)        │
                                           │ • startDate      │
                                           │ • endDate        │
                                           │ • type           │
                                           │ • status         │
                                           │ • reason         │
                                           │ • requestedBy(FK)│
                                           │ • approvedBy(FK) │
                                           │ • requestedAt    │
                                           │ • processedAt    │
                                           └──────────────────┘

                   TIME TRACKING
    ┌─────────────────┐    One-to-Many     ┌──────────────────┐
    │   TIMESHEET     │◄───────────────────│ TIMESHEET_ENTRY  │
    │─────────────────│                    │──────────────────│
    │ • id (PK)       │                    │ • id (PK)        │
    │ • user_id (FK)  │                    │ • timesheet_id(FK)│
    │ • startDate     │                    │ • date           │
    │ • endDate       │                    │ • hoursWorked    │
    │ • totalHours    │                    │ • overtimeHours  │
    │ • overtimeHours │                    │ • shiftType      │
    │ • generatedAt   │                    └──────────────────┘
    └─────────────────┘
```

**Relationship Summary:**
-  **User ↔ Role**: Many-to-Many (user_roles join table)
-  **User ↔ Skill**: Many-to-Many (user_skills join table)  
- ➡ **Location → ShiftTemplate**: One-to-Many
- ➡ **ShiftTemplate → ShiftInstance**: One-to-Many
- ➡ **ShiftInstance → Assignment**: One-to-Many
- ➡ **User → LeaveRequest**: One-to-Many
- ➡ **User → Assignment**: One-to-Many
- ➡ **Timesheet → TimesheetEntry**: One-to-Many

---

## 3. Technology Stack Visualization

```
┌─────────────────────────────────────────────────────────────┐
│                    TECHNOLOGY STACK                         │
│                                                             │
│  FRONTEND LAYER                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │  Thymeleaf  │  │ Bootstrap 5 │  │   Feather Icons     │  │
│  │   3.1.2     │  │   5.3.2     │  │      4.29.0         │  │
│  │Server-Side  │  │ Responsive  │  │   Icon Library      │  │
│  │ Templates   │  │    CSS      │  │                     │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│                                                             │
│  BACKEND LAYER                                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │Spring Boot  │  │   Spring    │  │   Spring Data       │  │
│  │   3.4.8     │  │ Security 6  │  │      JPA 3          │  │
│  │   Java 21   │  │ Auth + ACL  │  │   ORM + Repos       │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│                                                             │
│  DATABASE LAYER                                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │PostgreSQL   │  │   Flyway    │  │     HikariCP        │  │
│  │    16+      │  │     10      │  │       5.1.0         │  │
│  │ Production  │  │ Migrations  │  │ Connection Pool     │  │
│  │     +       │  │             │  │                     │  │
│  │  H2 Demo    │  │             │  │                     │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│                                                             │
│  TESTING LAYER                                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   JUnit 5   │  │   Mockito   │  │   Testcontainers    │  │
│  │   5.10.2    │  │    5.11     │  │       1.19.7        │  │
│  │ Unit Tests  │  │ Mock Objects│  │ Integration Tests   │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## 4. Security Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    SECURITY LAYERS                          │
│                                                             │
│  ┌─────────────────────────────────────────────────────────┐│
│  │              HTTP REQUEST FLOW                          ││
│  │                                                         ││
│  │  Browser ──► SecurityFilterChain ──► Controller        ││
│  │              │                                          ││
│  │              ├─ CSRF Protection                         ││
│  │              ├─ Authentication Filter                   ││
│  │              ├─ Authorization Filter                    ││
│  │              └─ Session Management                      ││
│  └─────────────────────────────────────────────────────────┘│
│                                                             │
│  AUTHENTICATION & AUTHORIZATION                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   USERS     │  │    ROLES    │  │     PERMISSIONS     │  │
│  │─────────────│  │─────────────│  │─────────────────────│  │
│  │manager@...  │  │  MANAGER    │  │ /approvals/**       │  │
│  │nurse1@...   │  │   STAFF     │  │ /templates/**       │  │
│  │nurse2@...   │  │   ADMIN     │  │ /timesheets/export  │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│                                                             │
│  SECURITY CONTROLS                                          │
│   BCrypt Password Encoding                                │
│   CSRF Token Validation                                   │
│   Session Timeout (30 minutes)                            │
│   Role-Based URL Protection                               │
│   Remember Me Functionality                               │
│   Logout with Session Invalidation                        │
└─────────────────────────────────────────────────────────────┘
```

---

## 5. Application Screenshots (To Be Captured)

**Required Screenshots:**
1. **Login Page** (login.html)
2. **Manager Dashboard** (schedule.html - manager view)
3. **Staff Personal Schedule** (schedule.html - staff view)
4. **Leave Request Form** (requests.html)
5. **Manager Approval Page** (approvals.html)
6. **Shift Templates** (templates.html)
7. **Timesheet Generation** (timesheets.html)
8. **403 Forbidden Error** (security demonstration)

**Screenshot Standards:**
- Clean browser window (no bookmarks bar)
- Consistent zoom level (100%)
- Real data (not Lorem Ipsum)
- Highlight key features with annotations
- Responsive design examples (desktop + mobile views)

---

## 6. Code Snippet Formatting

**Entity Example (Slide 15):**
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
    
    // Constructors, getters, setters...
}
```

**Controller Example:**
```java
@Controller
@RequestMapping("/schedule")
@PreAuthorize("isAuthenticated()")
public class ScheduleController {
    
    @Autowired
    private ShiftService shiftService;
    
    @GetMapping
    public String viewSchedule(Model model, Authentication auth) {
        User currentUser = getCurrentUser(auth);
        
        if (hasRole(currentUser, "MANAGER")) {
            model.addAttribute("shifts", shiftService.getAllShifts());
            return "schedule/manager-view";
        } else {
            model.addAttribute("shifts", shiftService.getUserShifts(currentUser));
            return "schedule/staff-view";
        }
    }
}
```

**Test Example (Slide 16):**
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

---

## Conversion Instructions

**For PowerPoint/Google Slides:**
1. Convert ASCII diagrams to visual diagrams using draw.io or similar
2. Use consistent color scheme: Blue (#007bff), Green (#28a745), Red (#dc3545)
3. Add ShiftCraft branding and logos
4. Ensure readable font sizes (minimum 18pt)
5. Use animations sparingly (fade in/out only)

**For PDF Export:**
- High resolution (300 DPI minimum)
- Landscape orientation for better readability
- Embedded fonts to prevent display issues
- Optimized file size for email sharing