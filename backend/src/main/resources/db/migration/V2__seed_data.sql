-- Seed Data for ShiftCraft Demo Environment
-- Stage 1 requirements: seed sample data for demo environments

-- Insert default roles
INSERT INTO roles (id, name) VALUES 
  ('11111111-1111-1111-1111-111111111111', 'MANAGER'),
  ('22222222-2222-2222-2222-222222222222', 'STAFF'),
  ('33333333-3333-3333-3333-333333333333', 'FINANCE'),
  ('44444444-4444-4444-4444-444444444444', 'ADMIN');

-- Insert default skills
INSERT INTO skills (id, name) VALUES 
  ('55555555-5555-5555-5555-555555555555', 'Nursing'),
  ('66666666-6666-6666-6666-666666666666', 'Emergency Care'),
  ('77777777-7777-7777-7777-777777777777', 'Laboratory'),
  ('88888888-8888-8888-8888-888888888888', 'Reception'),
  ('99999999-9999-9999-9999-999999999999', 'Pharmacy');

-- Insert default location
INSERT INTO locations (id, name, timezone, address_line, city) VALUES 
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Main Clinic', 'America/New_York', '123 Healthcare Ave', 'Medical City');

-- Insert sample users
-- Password hash for 'password123' (in real implementation, use proper bcrypt)
INSERT INTO users (id, email, password_hash, first_name, last_name, status) VALUES 
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'admin@shiftcraft.com', '$2a$10$example_hash_admin', 'Admin', 'User', 'ACTIVE'),
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'manager@shiftcraft.com', '$2a$10$example_hash_manager', 'John', 'Manager', 'ACTIVE'),
  ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'nurse1@shiftcraft.com', '$2a$10$example_hash_nurse1', 'Alice', 'Johnson', 'ACTIVE'),
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'nurse2@shiftcraft.com', '$2a$10$example_hash_nurse2', 'Bob', 'Wilson', 'ACTIVE'),
  ('ffffffff-ffff-ffff-ffff-ffffffffffff', 'finance@shiftcraft.com', '$2a$10$example_hash_finance', 'Carol', 'Finance', 'ACTIVE');

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id) VALUES 
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '44444444-4444-4444-4444-444444444444'), -- admin -> ADMIN
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', '11111111-1111-1111-1111-111111111111'), -- manager -> MANAGER
  ('dddddddd-dddd-dddd-dddd-dddddddddddd', '22222222-2222-2222-2222-222222222222'), -- nurse1 -> STAFF
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '22222222-2222-2222-2222-222222222222'), -- nurse2 -> STAFF
  ('ffffffff-ffff-ffff-ffff-ffffffffffff', '33333333-3333-3333-3333-333333333333'); -- finance -> FINANCE

-- Assign skills to users
INSERT INTO user_skills (user_id, skill_id, level) VALUES 
  ('dddddddd-dddd-dddd-dddd-dddddddddddd', '55555555-5555-5555-5555-555555555555', 'EXPERT'),    -- Alice: Nursing
  ('dddddddd-dddd-dddd-dddd-dddddddddddd', '66666666-6666-6666-6666-666666666666', 'INTERMEDIATE'), -- Alice: Emergency
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '55555555-5555-5555-5555-555555555555', 'INTERMEDIATE'), -- Bob: Nursing
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '77777777-7777-7777-7777-777777777777', 'EXPERT');    -- Bob: Laboratory

-- Create sample shift templates
INSERT INTO shift_templates (id, location_id, role_id, name, start_time, end_time, break_minutes) VALUES 
  ('template1-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222', 'Day Shift Nursing', '08:00:00', '16:00:00', 30),
  ('template2-2222-2222-2222-222222222222', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222', 'Night Shift Nursing', '20:00:00', '04:00:00', 30),
  ('template3-3333-3333-3333-333333333333', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222', 'Lab Shift', '09:00:00', '17:00:00', 45);

-- Link templates to required skills
INSERT INTO template_skill_requirements (template_id, skill_id) VALUES 
  ('template1-1111-1111-1111-111111111111', '55555555-5555-5555-5555-555555555555'), -- Day Nursing -> Nursing skill
  ('template2-2222-2222-2222-222222222222', '55555555-5555-5555-5555-555555555555'), -- Night Nursing -> Nursing skill
  ('template2-2222-2222-2222-222222222222', '66666666-6666-6666-6666-666666666666'), -- Night Nursing -> Emergency skill
  ('template3-3333-3333-3333-333333333333', '77777777-7777-7777-7777-777777777777'); -- Lab Shift -> Laboratory skill

-- Insert default rule configurations
INSERT INTO rule_configs (scope, name, value_text) VALUES 
  ('GLOBAL', 'MIN_REST_HOURS', '12'),
  ('GLOBAL', 'WEEKLY_MAX_HOURS', '48'),
  ('GLOBAL', 'MAX_CONSECUTIVE_DAYS', '5'),
  ('ROLE', 'OVERTIME_THRESHOLD', '40');

-- Update the role-specific rule to reference the STAFF role
UPDATE rule_configs 
SET role_id = '22222222-2222-2222-2222-222222222222' 
WHERE name = 'OVERTIME_THRESHOLD';

-- Create some sample shift instances for the current week
INSERT INTO shift_instances (id, template_id, shift_date, status) VALUES 
  ('shift001-1111-1111-1111-111111111111', 'template1-1111-1111-1111-111111111111', CURRENT_DATE, 'PUBLISHED'),
  ('shift002-2222-2222-2222-222222222222', 'template1-1111-1111-1111-111111111111', CURRENT_DATE + 1, 'PUBLISHED'),
  ('shift003-3333-3333-3333-333333333333', 'template2-2222-2222-2222-222222222222', CURRENT_DATE, 'PUBLISHED'),
  ('shift004-4444-4444-4444-444444444444', 'template3-3333-3333-3333-333333333333', CURRENT_DATE, 'PUBLISHED');

-- Create sample assignments
INSERT INTO assignments (id, shift_instance_id, user_id, status, assigned_by) VALUES 
  ('assign01-1111-1111-1111-111111111111', 'shift001-1111-1111-1111-111111111111', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'ACTIVE', 'cccccccc-cccc-cccc-cccc-cccccccccccc'),
  ('assign02-2222-2222-2222-222222222222', 'shift002-2222-2222-2222-222222222222', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'ACTIVE', 'cccccccc-cccc-cccc-cccc-cccccccccccc'),
  ('assign03-3333-3333-3333-333333333333', 'shift003-3333-3333-3333-333333333333', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 'ACTIVE', 'cccccccc-cccc-cccc-cccc-cccccccccccc'),
  ('assign04-4444-4444-4444-444444444444', 'shift004-4444-4444-4444-444444444444', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'ACTIVE', 'cccccccc-cccc-cccc-cccc-cccccccccccc');