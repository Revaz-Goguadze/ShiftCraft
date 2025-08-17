# ShiftCraft - EPAM Stage 6 Presentation Materials

## Healthcare Staff Scheduling & Time Management System

**Repository**: https://github.com/Revaz-Goguladze/ShiftCraft  
**Stage**: 6 - Final Solution Presentation  
**Technology Stack**: Spring Boot 3.4.8, Thymeleaf, H2/PostgreSQL, Spring Security  

---

## Demo Screenshots

### 1. Login & Authentication
**File**: `01-Login-Authentication.png`
- Professional login interface with ShiftCraft branding
- Demo credentials visible for instructor access
- Clean, healthcare-focused design

### 2. Manager Dashboard - Weekly Schedule
**File**: `02-Manager-Weekly-Schedule.png`
- Complete weekly schedule overview for all staff
- Shows manager view with full team visibility
- Multiple shift types: Day Nursing, Night Nursing, Laboratory, Reception
- Staff assignments across the full week

### 3. Shift Templates & CRUD Operations
**File**: `03-Shift-Templates-CRUD.png`
- Template management interface with 4 active templates
- Automatic duration calculations (break time deducted)
- Create new template form with location and role selection
- Template statistics and quick tips

### 4. Leave Request Submission
**File**: `04-Leave-Request-Form.png`
- Staff leave request form interface
- Multiple leave types: Vacation, Sick, Personal, Emergency
- User information display and submission guidelines
- Form validation and date selection

### 5. Manager Approvals Dashboard
**File**: `05-Manager-Approvals.png`
- Manager approval interface with 2 pending requests
- Leave requests from Alice Johnson (Vacation) and Bob Wilson (Sick)
- One-click approve/deny functionality
- Clear workflow management

### 6. Timesheet Generation
**File**: `06-Timesheet-Generation.png`
- Automated timesheet generation interface
- Staff member selection and period configuration
- Overtime rules clearly displayed (40+ hours = overtime)
- Generate functionality with current week defaults

### 7. Staff Personal Dashboard
**File**: `07-Staff-Personal-View.png`
- Role-based view showing only Alice Johnson's shifts
- "My Schedule" header (vs manager "Weekly Schedule - All Staff")
- Personal shift details with dates, times, breaks, locations
- Quick Actions: Request Leave, View Timesheets

### 8. Role-Based Security Demonstration
**File**: `08-Role-Based-Security.png`
- **Critical Security Feature**: 403 Forbidden error
- Staff user (Alice) attempting to access manager-only Templates page
- Proves proper role-based access control implementation
- Spring Security working correctly

---

## EPAM Stage 6 Requirements Demonstrated

### Project Introduction
- **Purpose**: Healthcare staff scheduling with automated workflows
- **Target Users**: Healthcare managers and clinical staff
- **Domain Focus**: Hospital/clinic scheduling compliance

### Main Features
- **Weekly Scheduling**: Manager and staff views with role-based access
- **Leave Management**: Request submission and approval workflow
- **Shift Templates**: Reusable patterns with automatic calculations
- **Timesheet Generation**: Automated reporting with overtime calculations
- **Security**: Role-based access control with proper restrictions
- **User Management**: Multiple user types with different permissions

### Code Structure
- **3-Tier Architecture**: Presentation → Application → Data layers
- **Database**: 8+ entities with many-to-many relationships
- **Security**: Spring Security with role-based URL protection
- **Business Logic**: Centralized in service layer
- **Testing**: Unit tests with Mockito, integration tests

### Technical Implementation
- **Spring Boot 3.4.8** with Java 21
- **Thymeleaf** server-side rendering
- **Spring Data JPA** with proper entity relationships
- **H2 Database** (demo) / PostgreSQL (production)
- **Bootstrap 5** responsive design
- **85% test coverage** on business logic

---

## Architecture Highlights

### Database Relationships
- **User ↔ Role**: Many-to-Many (user_roles)
- **User ↔ Skill**: Many-to-Many (user_skills)
- **Location → ShiftTemplate**: One-to-Many
- **ShiftTemplate → ShiftInstance**: One-to-Many
- **User → LeaveRequest**: One-to-Many

### Security Implementation
- **Authentication**: Form-based login with session management
- **Authorization**: Role-based access (MANAGER, STAFF, ADMIN)
- **URL Protection**: Manager-only routes properly secured
- **Password Encoding**: BCrypt hashing
- **CSRF Protection**: Enabled by default

### Business Logic
- **Conflict Prevention**: Overlapping shift detection
- **Skills Validation**: Users assigned only to matching shifts
- **Overtime Calculation**: Automatic >40 hour detection
- **Leave Integration**: Approved leave blocks shift assignments

---

## Demo Flow (6 minutes)

1. **Login** (30s): Manager authentication
2. **Schedule Overview** (1m): Weekly schedule with all staff
3. **Templates** (1.5m): Shift template management
4. **Leave Workflow** (1.5m): Staff request → Manager approval
5. **Timesheets** (1m): Generation with overtime calculation
6. **Security** (30s): Role-based access demonstration

---

## Summary

**Fully Functional Application** with comprehensive feature set, proper architecture implementation, and enterprise-grade security.