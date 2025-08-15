-- PostgreSQL initialization script for ShiftCraft
-- This script runs when the container is first created

-- Create the database (already created by POSTGRES_DB env var)
-- Just ensuring proper permissions
GRANT ALL PRIVILEGES ON DATABASE shiftcraft TO shift;