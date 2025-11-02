# Remove PostgreSQL CHECK Constraint

The deployed PostgreSQL database has a CHECK constraint on the `reason` column that needs to be removed.

## Steps to Remove the Constraint

1. Go to your Railway project dashboard at https://railway.app
2. Navigate to your PostgreSQL database service
3. Click on the **"Query"** tab or **"Data"** tab
4. Run this SQL command:

```sql
ALTER TABLE rest_days
DROP CONSTRAINT IF EXISTS rest_days_reason_check;
```

## Verify the Constraint is Removed

Run this query to confirm:

```sql
SELECT conname
FROM pg_constraint
WHERE conrelid = 'rest_days'::regclass
  AND contype = 'c';
```

This should return no rows (or no row with `rest_days_reason_check`).

## Alternative: Find All CHECK Constraints

If the above doesn't work, find all CHECK constraints on the table:

```sql
SELECT conname
FROM pg_constraint
WHERE conrelid = 'rest_days'::regclass;
```

Then drop the appropriate one using its exact name.

