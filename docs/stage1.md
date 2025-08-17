# ShiftCraft - Staff Scheduling and Time Management

## EPAM Capstone Stage 1 Project Overview

## One paragraph overview

ShiftCraft is a staff management system for small healthcare teams that solves chaotic roster planning, overtime disputes, and last minute swaps. Managers define roles, skills, and compliance rules. Staff see their upcoming shifts, request time off, and propose swaps. The system enforces constraints like minimum rest windows and skill coverage, and exports clean reports for payroll.

## Target users and pains

• Clinic manager needs coverage without violating rules
• Staff need an easy way to offer and accept swaps
• Finance wants auditable hours for payroll

## Core value

• Constraint aware scheduling that prevents illegal rosters
• Self service swaps with manager approval
• Clean timesheets with overtime and allowance calculations

## MVP scope

• Users and roles
• Shift template builder
• Calendar with assign, swap request, approve flow
• Leave management with accrual rules
• Timesheet export CSV
• Audit log on approvals

## Stretch goals

• Demand based auto scheduling
• Web push and email notifications
• Payroll integrations

## Non functional

• Reliability and auditability first
• Clear access control per role
• Testable business rules

## Candidate data model

User, Role, Skill, ShiftTemplate, ShiftInstance, LeaveRequest, SwapRequest, Timesheet, AuditEvent

---

# Idea B. TrailBuddy Store • Specialized online store for hiking routes, rescue add ons, and permits with in app credits

## One paragraph overview

TrailBuddy is a specialized store that sells curated offline route packs, hut passes, and rescue add ons for hikers. Users top up Trail Credits and spend them on routes and season packs. Purchases include auto generated GPX files, printable permits, and safety checklists. The store manages license terms and issues refunds if a trail is closed.

## Target users and pains

• Hikers want trusted route packs and permits in one place
• Rangers need clear license terms and closure notices
• Platform needs smooth credit top ups and refunds

## Core value

• Route and permit bundles with one click checkout
• App specific credits to simplify refunds and promos
• Closure aware digital delivery

## MVP scope

• Catalog, product variants, inventory for digital assets
• Basket and checkout with credit wallet
• Order history with download links
• Simple closure flag to pause sales
• Basic content pages for safety and FAQs

## Stretch goals

• Gift credits and promo codes
• Partners dashboard for rangers

## Non functional

• Strong transactional integrity
• File delivery security with signed URLs

## Candidate data model

User, Wallet, CreditTransaction, Product, Variant, Order, License, DownloadToken, ClosureNotice

---

# Selected idea to build now

ShiftCraft • Staff Scheduling and Time Management

Below is the concrete plan to start building. When your instructor gives feedback, use the template section near the end to record exactly what changed.

## Architecture at a glance

• Java 21
• Spring Boot 3, Spring Web, Spring Data JPA, Spring Security
• PostgreSQL with Flyway for migrations
• JUnit 5, Testcontainers, MockMvc
• Docker Compose for local infra
• Optional Keycloak for OAuth2 if required later

## Modules

• api
• core domain
• persistence
• security
• web admin

A simple monorepo works well. If you prefer classic single module, keep clear package boundaries.

## Key flows

• Manager creates shift templates and publishes a weekly schedule
• Staff request swap, target staff accepts, manager approves
• Leave request blocks assignments
• Timesheet is generated for the pay period and exported

## Initial endpoints MVP

• POST /api/v1/auth/register
• POST /api/v1/auth/login
• CRUD for skills and roles
• CRUD for shift templates
• POST shifts\:publish
• POST swaps\:request, accept, approve
• POST leaves\:request, approve
• GET timesheets\:period
• GET audit events

## Entity sketch

• User{id, name, email, roleIds, skillIds}
• Skill{id, name}
• ShiftTemplate{id, roleId, skillIds, startTime, endTime, location}
• ShiftInstance{id, templateId, date, assigneeId, status}
• LeaveRequest{id, userId, start, end, type, status}
• SwapRequest{id, fromShiftId, toUserId, status, approvals\[]}
• Timesheet{id, userId, periodStart, periodEnd, hoursRegular, hoursOvertime}
• AuditEvent{id, actorId, action, entityType, entityId, timestamp}

---

# Git repository setup

## Create repo and protect main

1. Create a new empty repo on GitHub or GitLab named shiftcraft
2. Locally

```
git init
git branch -m main
echo "# ShiftCraft" > README.md
git add .
git commit -m "chore: bootstrap repo"
git remote add origin <your-remote-url>
git push -u origin main
```

3. Enable branch protection on main and require pull requests

## Suggested repo structure

```
shiftcraft/
  README.md
  backend/
    api/
    core/
    persistence/
    security/
    web/
  ops/
    docker-compose.yml
    postgres/
      init.sql
  .github/
    ISSUE_TEMPLATE/
      bug_report.md
      feature_request.md
    PULL_REQUEST_TEMPLATE.md
  docs/
    stage1-capstone-overview.md
    architecture.md
```

## Add basic CI

• GitHub Actions or GitLab CI to run mvn verify with Testcontainers
• Cache Maven dependencies

---

# Task tracking setup

Choose one
• GitHub Projects new experience
• GitLab Issues and Boards
• Jira Free if your team prefers

Board columns
• Backlog
• Ready
• In progress
• In review
• Done

Initial issues to open
• Project skeleton
• Database schema and Flyway baseline
• Auth and roles
• Shift templates CRUD
• Publish schedule
• Swap request flow
• Leave management
• Timesheet export
• Audit log
• Docker Compose dev environment
• CI pipeline

Milestone 1 MVP two weeks
• Close issues up to swap flow and timesheet CSV

---

# README starter you can paste

## ShiftCraft

Staff scheduling and time management for small clinics and labs

### Why

Prevent illegal rosters and make swaps auditable

### Stack

Java 21, Spring Boot 3, PostgreSQL, Flyway, JUnit 5, Testcontainers

### Run locally

```
docker compose up -d postgres
./mvnw -q -DskipTests package
java -jar backend/web/target/web.jar
```

### Configuration

Set env

```
DB_URL=jdbc:postgresql://localhost:5432/shiftcraft
DB_USER=shift
DB_PASS=shift
JWT_SECRET=replace-me
```

### Modules

api, core, persistence, security, web

### Docs

See docs/architecture.md and docs/stage1-capstone-overview\.md

---

# Submission doc text block

Copy the block below into your doc or pdf.

Title
Stage 1. Capstone Project Creation and Setup

High Level Overviews

1. ShiftCraft • staff scheduling and time management for small clinics and labs
   Summary
   Managers define rules. Staff request swaps. System enforces constraints and produces payroll ready timesheets
   Key features for MVP
   Users and roles, shift templates, schedule publish, swap request with approval, leave management, timesheet export, audit log
   Non functional
   Reliability and auditability, role based access, tests for business rules

2. TrailBuddy Store • specialized online store for hiking routes, permits, and rescue add ons with in app credits
   Summary
   Users top up credits and purchase curated route packs and permits with closure aware delivery
   Key features for MVP
   Catalog, wallet credits, checkout, order history, closure flag, secure download links
   Non functional
   Transactional integrity and secured file delivery

Selected idea
ShiftCraft

Instructor feedback implementation
Record the feedback you receive here and list the changes you made
• Feedback
• Changes applied
• Commit or merge request link

Git repository
• Repo URL
• Instructor invited as collaborator

Task tracking
• Board URL
• Columns and initial issues created

Merge request link
• Paste your first MR link here. You can use a trivial change like adding the docs folder to demonstrate process

---

# First commit plan you can follow today

1. Generate Spring Boot starter with dependencies
   • Spring Web, Spring Security, Spring Data JPA, Validation, Flyway, Lombok, Testcontainers

2. Add Flyway baseline V1\_\_baseline.sql with tables for users, roles, skills

3. Implement auth endpoints with JWT
   • POST /auth/register
   • POST /auth/login

4. CRUD for Skill and ShiftTemplate behind role manager

5. Seed data through Flyway callbacks for two roles manager and staff

6. Docker Compose with Postgres and Adminer for quick checks

7. Write two happy path tests with Testcontainers to validate login and create skill

8. Open a merge request from feature/bootstrap to main called stage 1 bootstrap and link it in LMS

---

# Short checklist mapped to rubric

• Git repository created and collaborator invited
• Task tracking board ready for use
• Two High Level Overviews written
• One idea selected
• Placeholder section to record instructor feedback and resulting changes

If you want, I can turn the Submission doc text into a nicely formatted PDF and drop it here, along with a docs folder scaffold and a ready to push README.

