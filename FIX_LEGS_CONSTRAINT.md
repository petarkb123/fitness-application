# Fix LEGS to QUADS Migration for PostgreSQL

The PostgreSQL database has a check constraint that prevents the QUADS value. Here's how to fix it:

## Option 1: Via Railway PostgreSQL Console (Recommended - KEEPS YOUR DATA!)

1. Go to https://railway.app and log in
2. Click on your **Fitness Application project**
3. You'll see two services: one for your app, one for **PostgreSQL**
4. Click on the **PostgreSQL** service (not your app service)
5. Click on the **"Data"** tab at the top
6. You'll see a SQL query editor on the right side
7. Copy and paste these commands one at a time, clicking "Run" after each:

```sql
-- First, update all LEGS values to QUADS
UPDATE exercises SET primary_muscle = 'QUADS' WHERE primary_muscle = 'LEGS';
```

Then run this second command:

```sql
-- Drop the old check constraint
ALTER TABLE exercises DROP CONSTRAINT IF EXISTS exercises_primary_muscle_check;
```

## Option 2: Redeploy Database (LOSES ALL DATA!)

**⚠️ WARNING: This will delete ALL your data!**

If you don't care about losing user data, workouts, templates, etc.:

1. In Railway dashboard, click on your PostgreSQL service
2. Click **"Settings"** → **"Delete"**
3. Confirm deletion
4. Click **"+ New"** → **"Database"** → **"Add PostgreSQL"** again
5. The app will restart and create a fresh database

## After the Fix

1. Go back to your **app service** (not database)
2. Click **"Deployments"** → **"Redeploy"**
3. The app will restart
4. The seeder will run and create/update all exercises with correct QUADS values
5. Everything should work normally

## Why This Happened

The database had a check constraint with LEGS in the allowed enum values. When we changed the Java enum to QUADS, Hibernate couldn't automatically update the database constraint because `ddl-auto=update` doesn't modify existing constraints.

