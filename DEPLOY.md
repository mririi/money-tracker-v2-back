# Deploy to Render

## Prerequisites
- PostgreSQL database created on Render (manual or via blueprint)
- Render account

## Environment Variables
Set these in your Render Web Service dashboard:

| Variable | Value | Example |
|----------|-------|---------|
| `SPRING_PROFILES_ACTIVE` | `prod` | |
| `JWT_SECRET` | Generate a strong random string | |
| `CORS_ORIGINS` | Your frontend URL | `https://money-tracker-frontend.onrender.com` |
| `DB_URL` | JDBC URL to your Render Postgres | `jdbc:postgresql://dpg-xxx.frankfurt-postgres.render.com:5432/moneytracker_egdd?sslmode=require` |
| `DB_USER` | Database username | `moneytracker` |
| `DB_PASSWORD` | Database password | From Render dashboard |

> **Important:** Render PostgreSQL requires `?sslmode=require` at the end of the JDBC URL.

## Deploy via Docker
1. Go to [dashboard.render.com](https://dashboard.render.com)
2. **New +** → **Web Service**
3. Connect GitHub → select `mririi/money-tracker-v2-back`
4. **Runtime:** Docker
5. Set the environment variables above
6. Click **Create Web Service**

## Database Connection
The backend uses the external database URL from Render. Make sure your JDBC URL follows this format:
```
jdbc:postgresql://YOUR_HOST.frankfurt-postgres.render.com:5432/YOUR_DB_NAME?sslmode=require
```
