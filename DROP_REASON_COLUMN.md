# SQL Migration: Drop reason column from rest_days table

## For Localhost (MySQL)
Run this SQL command in your MySQL database:
```sql
ALTER TABLE rest_days DROP COLUMN reason;
```

## For Production (PostgreSQL on Railway)
You'll need to connect to your PostgreSQL database and run:
```sql
ALTER TABLE rest_days DROP COLUMN IF EXISTS reason;
```

### How to run on Railway:
1. Go to your Railway project dashboard
2. Navigate to your PostgreSQL database service
3. Click on the "Query" tab or use the Railway CLI:
   ```bash
   railway connect postgres
   ```
4. Run the SQL command above

### Alternative: Using psql
If you have the connection details, you can connect directly:
```bash
psql -h $PGHOST -U $PGUSER -d $PGDATABASE -c "ALTER TABLE rest_days DROP COLUMN IF EXISTS reason;"
```

---

**Note:** Since this migration was already attempted and caused issues, make sure to run the SQL manually on both databases before deploying the updated code.

