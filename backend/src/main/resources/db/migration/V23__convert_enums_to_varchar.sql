-- Convert PostgreSQL custom enum types to VARCHAR for better Hibernate compatibility
-- Hibernate with @Enumerated(EnumType.STRING) sends enum values as VARCHAR
-- PostgreSQL custom enum types require explicit casting which Hibernate doesn't provide

-- Convert users.role from user_role enum to VARCHAR
ALTER TABLE users 
    ALTER COLUMN role TYPE VARCHAR(20) USING role::text;

-- Convert users.status from user_status enum to VARCHAR  
ALTER TABLE users
    ALTER COLUMN status TYPE VARCHAR(30) USING status::text;

-- Drop the now-unused enum types with CASCADE to remove dependent defaults
DROP TYPE IF EXISTS user_role CASCADE;
DROP TYPE IF EXISTS user_status CASCADE;
