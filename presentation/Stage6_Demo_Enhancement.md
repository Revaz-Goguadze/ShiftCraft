# ShiftCraft Stage 6 Demo Environment Setup
## Presentation-Ready Configuration

---

## Demo Environment Status: ✅ READY

Your existing DataLoader.java is already **presentation-perfect** with:

### **Demo Users (All password: password123)**
```
🏥 MANAGER LOGIN
   Email: manager@shiftcraft.com  
   Name: John Manager
   Access: Full system (all pages)

👨‍⚕️ STAFF LOGIN  
   Email: nurse1@shiftcraft.com
   Name: Alice Johnson (Expert Nursing + Emergency)
   Access: Personal schedule, requests only

👩‍⚕️ STAFF LOGIN
   Email: nurse2@shiftcraft.com  
   Name: Bob Wilson (Nursing + Laboratory Expert)
   Access: Personal schedule, requests only

📞 RECEPTION LOGIN
   Email: reception@shiftcraft.com
   Name: Dana Smith (Reception Expert)
   Access: Personal schedule, requests only
```

### **Perfect Demo Flow (6 minutes)**

#### **1. Authentication Demo (30 seconds)**
- Start at: `http://localhost:8080/login`
- Login as **manager@shiftcraft.com** / **password123**
- Show manager dashboard with weekly overview

#### **2. Manager Dashboard (1 minute)**
- Highlight: "Weekly Schedule - All Staff"  
- Point out: Multiple staff assigned across week
- Show: Different shift types (Day Nursing, Night Nursing, Lab, Reception)
- Demonstrate: Current week with realistic assignments

#### **3. Shift Template Creation (1.5 minutes)**
- Navigate to **Templates** page
- Show existing templates:
  - Day Shift Nursing (8:00-16:00, 30 min break)
  - Night Shift Nursing (20:00-04:00, 30 min break)  
  - Laboratory Shift (9:00-17:00, 45 min break)
  - Reception Shift (8:00-17:00, 60 min break)
- Create new template during demo
- Show auto-duration calculation

#### **4. Leave Request Workflow (1.5 minutes)**
- **Switch to staff view**: Logout → Login as **nurse1@shiftcraft.com**
- Navigate to **Requests** page
- Show existing leave requests:
  - Future vacation request (pending)
  - Medical appointment (pending)
- Submit new leave request live
- **Switch back to manager**: Show approval workflow
- Approve the newly submitted request

#### **5. Timesheet Generation (1 minute)**
- As manager, navigate to **Timesheets**
- Select current week period
- Generate timesheet for Alice Johnson (nurse1)
- Show calculated hours and overtime
- Demonstrate export functionality

#### **6. Security Demonstration (30 seconds)**
- Remain logged in as staff (nurse1)
- Try to access **Templates** → Show 403 Forbidden
- Try to access **Approvals** → Show 403 Forbidden
- Summarize role-based security

---

## Database Pre-Population Details

### **Shift Assignments (Current Week)**
```
MONDAY:    Alice(Day) + Bob(Lab) + Dana(Reception) + Alice(Night)
TUESDAY:   Alice(Day) + Bob(Lab) + Dana(Reception) + Bob(Night)  
WEDNESDAY: Alice(Day) + Bob(Lab) + Dana(Reception) + Alice(Night)
THURSDAY:  Alice(Day) + Bob(Lab) + Dana(Reception) + Bob(Night)
FRIDAY:    Alice(Day) + Bob(Lab) + Dana(Reception) + Alice(Night)
SATURDAY:  [Weekend] + Bob(Night)
SUNDAY:    [Weekend] + Alice(Night)
```

### **Leave Requests Pre-Loaded**
```
✅ APPROVED: Dana Smith - Personal leave (3 days ago, already processed)
⏳ PENDING:  Alice Johnson - Vacation (10 days from now)
⏳ PENDING:  Bob Wilson - Medical appointment (5 days from now)
```

### **Skills Matrix**
```
Alice Johnson:  🥇 Expert Nursing + 🥈 Intermediate Emergency
Bob Wilson:     🥈 Intermediate Nursing + 🥇 Expert Laboratory  
Dana Smith:     🥇 Expert Reception
```

---

## Technical Demo Setup Checklist

### **Pre-Demo Startup (5 minutes before)**
```bash
# 1. Clean startup
cd /home/coder/epam/shiftcraft/backend
export JAVA_HOME=/home/coder/.sdkman/candidates/java/21.0.3-tem

# 2. Start application  
mvn spring-boot:run

# 3. Wait for "Demo data loaded successfully!" message
# 4. Verify at http://localhost:8080/login
```

### **Browser Setup**
- **Clean profile**: No bookmarks, extensions disabled
- **Zoom level**: 100% (consistent visibility)
- **Window size**: 1920x1080 (standard presentation size)
- **Tabs**: Close all other tabs
- **Bookmark**: Save `http://localhost:8080/login` for quick access

### **Demo Flow Bookmarks**
```
🔗 http://localhost:8080/login
🔗 http://localhost:8080/schedule  
🔗 http://localhost:8080/templates
🔗 http://localhost:8080/requests
🔗 http://localhost:8080/approvals  
🔗 http://localhost:8080/timesheets
```

---

## Live Demo Script (6 minutes exact)

### **Opening (30 sec)**
> "Let me show you ShiftCraft in action. I'll demonstrate our complete healthcare scheduling workflow using real demo data representing a typical clinic week."

### **Manager Login (30 sec)**  
> "Starting with manager authentication..." 
- Type: **manager@shiftcraft.com** / **password123**
- Click Sign In
> "This brings us to the manager dashboard with a complete weekly overview."

### **Schedule Overview (1 min)**
> "Here's our weekly schedule showing all staff assignments. Notice we have:"
- Point to different shift types
- Highlight staff names and roles  
- Show responsive design
> "Each staff member has skill-based assignments - Alice handles nursing and emergency, Bob covers lab work, Dana manages reception."

### **Template Creation (1.5 min)**
> "Let's look at our shift template system..."
- Navigate to Templates
- Show existing templates with times and breaks
- Click "Create New Template"
- Fill in: "Weekend Shift" / Saturday-Sunday / 10:00-18:00
> "Notice the automatic duration calculation and skill requirement options."

### **Leave Request Flow (1.5 min)**  
> "Now let's see the leave management workflow from a staff perspective..."
- Logout → Login as **nurse1@shiftcraft.com**
- Navigate to Requests
> "Alice can see her personal requests and submit new ones..."
- Submit new request: Personal / Tomorrow / "Conference attendance"
- Logout → Login back as manager
- Navigate to Approvals
> "The manager sees pending requests and can approve with one click..."
- Approve the new request

### **Timesheet Demo (1 min)**
> "Finally, let's generate timesheets for payroll..."
- Navigate to Timesheets
- Select current week
- Choose Alice Johnson
- Click Generate
> "The system automatically calculates regular hours, overtime, and provides export options for payroll integration."

### **Security Demo (30 sec)**
> "Security is role-based. Watch what happens when staff tries to access manager functions..."
- Login as nurse1
- Try /templates → Show 403 error
- Try /approvals → Show 403 error  
> "Perfect - staff are properly restricted to their authorized functions."

### **Closing (30 sec)**
> "That's our complete ShiftCraft workflow - from scheduling to approvals to payroll, all with enterprise-grade security."

---

## Backup Scenarios

### **If Demo App Fails**
1. **Screenshots ready**: All major screens captured
2. **Video backup**: 3-minute recorded walkthrough  
3. **Code snippets**: Live code review mode
4. **Architecture focus**: Extended time on technical deep dive

### **If Time Runs Over**
1. **Condensed demo**: Skip template creation, focus on workflow
2. **Screenshot mode**: Show pre-captured screens instead of live
3. **Q&A priority**: Cut demo short, maximize question time

### **If Time Runs Under**  
1. **Code walkthrough**: Show entity relationships in IDE
2. **Test demonstration**: Run unit test examples
3. **Architecture detail**: Deeper technical explanation

---

## Post-Demo Cleanup

### **For Portfolio Video Recording**
- Same demo flow but slower pace (10-12 minutes)
- More detailed explanations
- Include code snippets and architecture
- Professional narration for LinkedIn/portfolio use

### **PDF Slide Export**
- Export presentation to PDF
- Include demo screenshots as appendix
- Submit via EPAM platform after live session