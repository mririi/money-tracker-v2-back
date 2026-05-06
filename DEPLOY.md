# Deploy to Render

## Prerequisites
- Render account (free tier works)

## Step 1: Deploy via Blueprint
1. Go to [Render Dashboard](https://dashboard.render.com)
2. Click **New +** → **Blueprint**
3. Connect your GitHub account and select `mririi/money-tracker-v2-back`
4. Render will read `render.yaml` and create:
   - A **Web Service** (`money-tracker-backend`) running the Docker container
   - A **PostgreSQL database** (`moneytracker-db`) on the free plan
5. Click **Apply**

## Step 2: Note the Backend URL
After deployment, copy the backend service URL (e.g. `https://money-tracker-backend.onrender.com`). You will need it for the frontend's `API_URL`.

## Step 3: Update CORS (after frontend deploys)
Once the frontend is deployed, update the `CORS_ORIGINS` environment variable in the backend service settings with the frontend URL, then redeploy.

## Environment Variables
| Variable | Source | Description |
|----------|--------|-------------|
| `SPRING_PROFILES_ACTIVE` | `render.yaml` | Set to `prod` |
| `JWT_SECRET` | Auto-generated | Used for signing JWT tokens |
| `CORS_ORIGINS` | Manual | Frontend URL (update after frontend deploy) |
| `DB_URL` | From database | PostgreSQL connection string |
| `DB_USER` | From database | PostgreSQL username |
| `DB_PASSWORD` | From database | PostgreSQL password |
