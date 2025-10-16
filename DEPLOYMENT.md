# 🚀 Deployment Guide

This guide will help you deploy your Fitness Application to the internet.

## 📋 Table of Contents
- [Database Setup](#-database-setup)
- [Railway Deployment (Recommended)](#-railway-deployment-recommended)
- [Render Deployment](#-render-deployment-alternative)
- [Heroku Deployment](#-heroku-deployment-alternative)
- [Environment Variables](#-environment-variables)
- [Post-Deployment](#-post-deployment)

---

## 🗄️ Database Setup

### Development vs Production

**Development (Local)**
- Uses **H2 in-memory database**
- Data is reset when you restart the app
- No setup required - works out of the box
- Access H2 console at: `http://localhost:8080/h2-console`

**Production (Deployed)**
- Uses **PostgreSQL** (persistent database)
- Data is saved permanently
- Automatically provided by hosting platforms
- No manual setup needed!

### How It Works

Your app automatically switches between databases:
- **Local development**: H2 (in-memory)
- **Production**: PostgreSQL (when `SPRING_PROFILES_ACTIVE=prod`)

---

## 🚂 Railway Deployment (Recommended)

Railway is the easiest way to deploy. It's free to start and handles everything automatically.

### Step 1: Create Railway Account
1. Go to [railway.app](https://railway.app)
2. Click "Login" and sign in with GitHub
3. Authorize Railway to access your repositories

### Step 2: Create New Project
1. Click **"New Project"**
2. Select **"Deploy from GitHub repo"**
3. Choose **`fitness-application`** from the list
4. Railway will automatically detect it's a Spring Boot app

### Step 3: Add PostgreSQL Database
1. In your project, click **"+ New"**
2. Select **"Database"** → **"Add PostgreSQL"**
3. Railway will:
   - Create a PostgreSQL database
   - Generate a `DATABASE_URL` environment variable
   - Automatically link it to your app

### Step 4: Configure Environment Variables
1. Click on your **web service** (not the database)
2. Go to **"Variables"** tab
3. Add this variable:
   ```
   SPRING_PROFILES_ACTIVE=prod
   ```
4. Railway auto-provides `DATABASE_URL`, `PORT`, etc.

### Step 5: Deploy!
1. Railway automatically builds and deploys your app
2. Click **"Deploy"** if it doesn't start automatically
3. Once deployed, click **"Settings"** → **"Generate Domain"**
4. Your app will be live at: `https://your-app.up.railway.app`

### Step 6: Access Your App
1. Click on the generated domain URL
2. You should see your fitness application!
3. Create an account and start using it

**That's it! 🎉** Railway will auto-deploy whenever you push to GitHub.

---

## 🎨 Render Deployment (Alternative)

Render offers a generous free tier with persistent PostgreSQL.

### Step 1: Create Render Account
1. Go to [render.com](https://render.com)
2. Sign up with GitHub

### Step 2: Create PostgreSQL Database
1. Click **"New +"** → **"PostgreSQL"**
2. Name it: `fitness-db`
3. Select **Free** tier
4. Click **"Create Database"**
5. Copy the **"Internal Database URL"** (you'll need it)

### Step 3: Create Web Service
1. Click **"New +"** → **"Web Service"**
2. Connect your **`fitness-application`** repository
3. Configure:
   - **Name**: `fitness-app`
   - **Environment**: `Java`
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -Dspring.profiles.active=prod -jar target/FitnessApplication-*.jar`
   - **Instance Type**: `Free`

### Step 4: Add Environment Variables
1. Scroll to **"Environment Variables"**
2. Add these:
   ```
   SPRING_PROFILES_ACTIVE=prod
   DATABASE_URL=<paste your database URL from Step 2>
   ```

### Step 5: Deploy
1. Click **"Create Web Service"**
2. Render will build and deploy your app (takes 5-10 minutes)
3. Your app will be live at: `https://fitness-app.onrender.com`

**Note**: Free tier apps sleep after 15 minutes of inactivity and take ~30 seconds to wake up.

---

## 🟣 Heroku Deployment (Alternative)

Heroku is a classic platform but no longer offers a free tier. Minimum cost: ~$7/month.

### Prerequisites
Install Heroku CLI:
```bash
# macOS
brew install heroku/brew/heroku

# Windows
# Download from: https://devcenter.heroku.com/articles/heroku-cli
```

### Step 1: Login to Heroku
```bash
heroku login
```

### Step 2: Create Heroku App
```bash
heroku create your-fitness-app
```

### Step 3: Add PostgreSQL
```bash
heroku addons:create heroku-postgresql:mini
```
This creates a PostgreSQL database and sets `DATABASE_URL` automatically.

### Step 4: Set Environment Variables
```bash
heroku config:set SPRING_PROFILES_ACTIVE=prod
```

### Step 5: Deploy
```bash
git push heroku main
```

### Step 6: Open Your App
```bash
heroku open
```

---

## 🔧 Environment Variables

These are the environment variables your app uses:

### Required (Production)
| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Activate production config | `prod` |
| `DATABASE_URL` | PostgreSQL connection URL | Auto-provided by platform |

### Optional
| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Server port | `8080` |
| `DB_USERNAME` | Database username | From `DATABASE_URL` |
| `DB_PASSWORD` | Database password | From `DATABASE_URL` |

**Note**: Railway, Render, and Heroku automatically provide `DATABASE_URL` when you add PostgreSQL.

---

## ✅ Post-Deployment

### 1. Verify Deployment
- Visit your app URL
- You should see the landing page
- Try registering a new account
- Test creating a workout

### 2. Monitor Your App

**Railway:**
- View logs in the "Deployments" tab
- Monitor metrics in the dashboard

**Render:**
- Check "Logs" tab for errors
- View events in the dashboard

**Heroku:**
```bash
heroku logs --tail
```

### 3. Common Issues

**App won't start:**
- Check logs for errors
- Verify `SPRING_PROFILES_ACTIVE=prod` is set
- Ensure `DATABASE_URL` is present

**Database connection error:**
- Verify PostgreSQL database is created
- Check `DATABASE_URL` format
- Ensure database is in the same region

**App is slow:**
- Free tiers have limited resources
- Consider upgrading to paid tier for better performance

### 4. Update Your App

Whenever you make changes:
```bash
git add .
git commit -m "Your update message"
git push
```

The platform will automatically rebuild and redeploy!

---

## 🎯 Recommended: Railway

**Why Railway?**
- ✅ Easiest setup (5 minutes)
- ✅ Free $5/month credit
- ✅ Auto-deploys from GitHub
- ✅ PostgreSQL included
- ✅ Great performance
- ✅ No sleep/wake delays

**Cost:**
- Free tier: $5 credit/month (usually enough for small apps)
- After free credit: Pay only for what you use (~$5-10/month)

---

## 📞 Need Help?

If you run into issues:
1. Check the platform's logs
2. Verify all environment variables are set
3. Ensure PostgreSQL database is running
4. Check that `application-prod.properties` is correct

---

**Happy Deploying! 🚀**

