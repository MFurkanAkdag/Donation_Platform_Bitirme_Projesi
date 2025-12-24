-- Fix mismatch between JPA Entity and Database Schema
-- AuditLog.java defines ipAction as String (varchar), but DB uses INET
-- AuditLog.java defines oldValues/newValues as TEXT, but DB uses JSONB

ALTER TABLE audit_logs 
ALTER COLUMN ip_address TYPE VARCHAR(45),
ALTER COLUMN old_values TYPE TEXT,
ALTER COLUMN new_values TYPE TEXT;
