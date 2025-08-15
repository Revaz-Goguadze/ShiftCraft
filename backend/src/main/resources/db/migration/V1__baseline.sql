-- ShiftCraft Database Schema
-- Stage 3: Complete ER model implementation

-- Custom types
CREATE TYPE shift_status AS ENUM ('DRAFT','PUBLISHED','CANCELLED');
CREATE TYPE assign_status AS ENUM ('ACTIVE','CANCELLED','SWAPPED');
CREATE TYPE leave_type AS ENUM ('VACATION','SICK','UNPAID');
CREATE TYPE decision AS ENUM ('PROPOSE','ACCEPT','DECLINE','APPROVE','REJECT');
CREATE TYPE request_status AS ENUM ('PROPOSED','ACCEPTED','AWAITING_APPROVAL','APPROVED','REJECTED');

-- Users table
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  first_name TEXT NOT NULL,
  last_name TEXT NOT NULL,
  status TEXT NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Roles table
CREATE TABLE roles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL UNIQUE
);

-- Skills table
CREATE TABLE skills (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL UNIQUE
);

-- User-Role association (many-to-many)
CREATE TABLE user_roles (
  user_id UUID REFERENCES users(id) ON DELETE CASCADE,
  role_id UUID REFERENCES roles(id) ON DELETE RESTRICT,
  granted_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  PRIMARY KEY (user_id, role_id)
);

-- User-Skill association (many-to-many)
CREATE TABLE user_skills (
  user_id UUID REFERENCES users(id) ON DELETE CASCADE,
  skill_id UUID REFERENCES skills(id) ON DELETE RESTRICT,
  level TEXT CHECK (level IN ('BEGINNER', 'INTERMEDIATE', 'EXPERT')),
  PRIMARY KEY (user_id, skill_id)
);

-- Locations table
CREATE TABLE locations (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name TEXT NOT NULL,
  timezone TEXT NOT NULL DEFAULT 'UTC',
  address_line TEXT,
  city TEXT
);

-- Shift templates table
CREATE TABLE shift_templates (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  location_id UUID NOT NULL REFERENCES locations(id) ON DELETE RESTRICT,
  role_id UUID NOT NULL REFERENCES roles(id) ON DELETE RESTRICT,
  name TEXT NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  break_minutes INT NOT NULL DEFAULT 0,
  recurrence INT -- Weekly mask for auto-generation
);

-- Template-Skill requirements (many-to-many)
CREATE TABLE template_skill_requirements (
  template_id UUID REFERENCES shift_templates(id) ON DELETE CASCADE,
  skill_id UUID REFERENCES skills(id) ON DELETE RESTRICT,
  PRIMARY KEY (template_id, skill_id)
);

-- Shift instances table (weak entity - depends on template and date)
CREATE TABLE shift_instances (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  template_id UUID NOT NULL REFERENCES shift_templates(id) ON DELETE CASCADE,
  shift_date DATE NOT NULL,
  status shift_status NOT NULL DEFAULT 'DRAFT',
  UNIQUE (template_id, shift_date) -- Business key
);

-- Assignments table (associative entity User ↔ ShiftInstance)
CREATE TABLE assignments (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  shift_instance_id UUID NOT NULL REFERENCES shift_instances(id) ON DELETE CASCADE,
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  status assign_status NOT NULL DEFAULT 'ACTIVE',
  assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  assigned_by UUID REFERENCES users(id)
);

-- Leave requests table
CREATE TABLE leave_requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  type leave_type NOT NULL,
  start_at TIMESTAMPTZ NOT NULL,
  end_at TIMESTAMPTZ NOT NULL,
  status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
  manager_comment TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  decided_at TIMESTAMPTZ
);

-- Swap requests table
CREATE TABLE swap_requests (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  source_assignment_id UUID NOT NULL REFERENCES assignments(id) ON DELETE CASCADE,
  initiator_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  target_user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  status request_status NOT NULL DEFAULT 'PROPOSED',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  decided_at TIMESTAMPTZ
);

-- Swap approval steps table (weak entity - depends on swap request)
CREATE TABLE swap_approval_steps (
  swap_request_id UUID NOT NULL REFERENCES swap_requests(id) ON DELETE CASCADE,
  step_no INT NOT NULL,
  actor_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  decision decision NOT NULL,
  comment TEXT,
  decided_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  PRIMARY KEY (swap_request_id, step_no)
);

-- Timesheets table
CREATE TABLE timesheets (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  period_start DATE NOT NULL,
  period_end DATE NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE(user_id, period_start, period_end)
);

-- Timesheet entries table (associative entity Timesheet ↔ Assignment)
CREATE TABLE timesheet_entries (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  timesheet_id UUID NOT NULL REFERENCES timesheets(id) ON DELETE CASCADE,
  assignment_id UUID NOT NULL REFERENCES assignments(id) ON DELETE RESTRICT,
  hours_worked NUMERIC(5,2) NOT NULL,
  overtime_hours NUMERIC(5,2) NOT NULL DEFAULT 0,
  UNIQUE(timesheet_id, assignment_id)
);

-- Audit events table
CREATE TABLE audit_events (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  actor_id UUID NOT NULL REFERENCES users(id) ON DELETE SET NULL,
  entity_type TEXT NOT NULL,
  entity_id UUID NOT NULL,
  action TEXT NOT NULL,
  payload_json JSONB NOT NULL DEFAULT '{}',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- Rule configuration table
CREATE TABLE rule_configs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  scope TEXT NOT NULL CHECK (scope IN ('GLOBAL','LOCATION','ROLE')),
  location_id UUID REFERENCES locations(id) ON DELETE CASCADE,
  role_id UUID REFERENCES roles(id) ON DELETE CASCADE,
  name TEXT NOT NULL,
  value_text TEXT NOT NULL
);

-- Indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_shift_instances_date ON shift_instances(shift_date);
CREATE INDEX idx_assignments_user_id ON assignments(user_id);
CREATE INDEX idx_assignments_shift_instance_id ON assignments(shift_instance_id);
CREATE INDEX idx_leave_requests_user_id ON leave_requests(user_id);
CREATE INDEX idx_swap_requests_initiator_id ON swap_requests(initiator_id);
CREATE INDEX idx_swap_requests_target_user_id ON swap_requests(target_user_id);
CREATE INDEX idx_timesheets_user_period ON timesheets(user_id, period_start, period_end);
CREATE INDEX idx_audit_events_entity ON audit_events(entity_type, entity_id);
CREATE INDEX idx_audit_events_created_at ON audit_events(created_at);

-- Derived attributes as views
CREATE VIEW v_timesheet_totals AS
SELECT t.id AS timesheet_id,
       t.user_id,
       t.period_start,
       t.period_end,
       SUM(e.hours_worked) AS total_hours,
       SUM(e.overtime_hours) AS overtime_hours
FROM timesheet_entries e
JOIN timesheets t ON t.id = e.timesheet_id
GROUP BY t.id, t.user_id, t.period_start, t.period_end;

-- User full name view
CREATE VIEW v_user_details AS
SELECT id,
       email,
       first_name,
       last_name,
       first_name || ' ' || last_name AS full_name,
       status,
       created_at
FROM users;