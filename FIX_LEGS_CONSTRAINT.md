# Fix LEGS to QUADS Migration for PostgreSQL

The PostgreSQL database has a check constraint that prevents the QUADS value. Here's how to fix it:

## Option 1: Via Railway PostgreSQL Console (Recommended)

1. Go to your Railway project dashboard
2. Click on your PostgreSQL database service
3. Click on the "Query" tab or "Connect" to open a database console
4. Run these commands in order:

```sql
-- First, update all LEGS values to QUADS
UPDATE exercises SET primary_muscle = 'QUADS' WHERE primary_muscle = 'LEGS';

-- Drop the old check constraint
ALTER TABLE exercises DROP CONSTRAINT IF EXISTS exercises_primary_muscle_check;

-- Hibernate's ddl-auto=update will recreate the constraint with the correct values on next startup
```

## Option 2: Via psql Command Line

If you have PostgreSQL client installed:

1. Connect to your Railway PostgreSQL database
2. Run the same SQL commands as above

## After the Fix

1. The app will automatically restart
2. The seeder will run and create/update all exercises with correct QUADS values
3. Everything should work normally

## Why This Happened

The database had a check constraint with LEGS in the allowed enum values. When we changed the Java enum to QUADS, Hibernate couldn't automatically update the database constraint because `ddl-auto=update` doesn't modify existing constraints.

