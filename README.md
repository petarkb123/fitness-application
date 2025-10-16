# 💪 Fitness Application

A comprehensive fitness tracking web application built with Spring Boot and modern web technologies. Track your workouts, analyze your progress, and achieve your fitness goals with advanced analytics and personalized insights.

![Java](https://img.shields.io/badge/Java-17+-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)
![License](https://img.shields.io/badge/license-MIT-blue)

## ✨ Features

### 🏋️ Workout Management
- **Custom Workout Templates**: Create and save reusable workout templates
- **Live Workout Sessions**: Track exercises, sets, reps, and weights in real-time
- **Exercise Library**: Comprehensive built-in exercise database with custom exercise support
- **Workout History**: View and analyze past workout sessions

### 📊 Advanced Analytics
- **Weekly Stats**: Track volume, frequency, and progress over time
- **Progressive Overload**: Monitor strength gains and training progression
- **Personal Records**: Automatically track your PRs for each exercise
- **Volume Trends**: Visualize training volume across muscle groups
- **Training Frequency**: Analyze workout patterns and consistency

### 📅 Smart Scheduling
- **Workout Calendar**: Schedule workouts and rest days
- **Dashboard Overview**: See upcoming workouts and recent activity at a glance
- **Rest Day Tracking**: Log recovery days with notes and reasons

### 🎨 Modern UI/UX
- **Responsive Design**: Fully optimized for mobile (iPhone) and desktop
- **Dark Purple Theme**: Beautiful glassmorphic design with smooth animations
- **Mobile-First Navigation**: Bottom navigation bar for easy mobile access
- **Intuitive Interface**: Clean, modern interface with excellent user experience

### 👤 User Features
- **User Authentication**: Secure login and registration
- **Profile Management**: Customize your profile with avatars
- **Settings**: Personalize your experience

## 🛠️ Tech Stack

### Backend
- **Java 17+**
- **Spring Boot 3.x**
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - Thymeleaf Template Engine
- **H2 Database** (Development)
- **PostgreSQL** (Production-ready)
- **Maven** for dependency management

### Frontend
- **HTML5/CSS3** with modern features
- **JavaScript** for interactivity
- **Thymeleaf** for server-side rendering
- **Font Awesome** for icons
- **Responsive Design** (Mobile & Desktop)

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Git

### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/petarkb123/fitness-application.git
   cd fitness-application
   ```

2. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```
   
   Or on Windows:
   ```bash
   mvnw.cmd spring-boot:run
   ```

3. **Access the application**
   
   Open your browser and navigate to: `http://localhost:8080`

4. **Default Development Database**
   
   The app uses H2 in-memory database for development. Data is reset on each restart.

### Building for Production

```bash
./mvnw clean package -DskipTests
java -jar target/fitness-application-*.jar
```

## 📦 Deployment

### Recommended: Railway (Easiest)

1. **Push your code to GitHub** ✅ (Already done!)

2. **Sign up for Railway**: [railway.app](https://railway.app)

3. **Create a new project**:
   - Click "New Project"
   - Select "Deploy from GitHub repo"
   - Choose `fitness-application`

4. **Add PostgreSQL Database**:
   - Click "New" → "Database" → "Add PostgreSQL"
   - Railway automatically creates and links the database

5. **Set Environment Variables** (Railway detects these automatically):
   ```
   SPRING_PROFILES_ACTIVE=prod
   ```

6. **Deploy**: Railway auto-deploys on every push to `main`

### Alternative: Render

1. Sign up at [render.com](https://render.com)
2. Create a new Web Service
3. Connect your GitHub repository
4. Add PostgreSQL database
5. Set build command: `./mvnw clean package -DskipTests`
6. Set start command: `java -jar target/fitness-application-*.jar`

### Alternative: Heroku

1. Install Heroku CLI
2. Login: `heroku login`
3. Create app: `heroku create your-app-name`
4. Add PostgreSQL: `heroku addons:create heroku-postgresql:mini`
5. Deploy: `git push heroku main`

## 🗄️ Database Configuration

### Development (H2 - In-Memory)
The app uses H2 by default. Configuration in `application.properties`:
```properties
spring.datasource.url=jdbc:h2:mem:fitnessdb
spring.jpa.hibernate.ddl-auto=create-drop
```

### Production (PostgreSQL)
For production deployment, create an `application-prod.properties` file:

```properties
# PostgreSQL Configuration
spring.datasource.url=${DATABASE_URL}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update

# Production settings
spring.jpa.show-sql=false
logging.level.org.hibernate.SQL=WARN
```

**Note**: Most cloud platforms (Railway, Render, Heroku) provide `DATABASE_URL` environment variable automatically when you add PostgreSQL.

## 📱 Mobile Support

The application is fully responsive and optimized for mobile devices:
- **Bottom Navigation**: Easy thumb access on mobile
- **Touch-Optimized**: All buttons and controls are mobile-friendly
- **Responsive Charts**: Analytics adjust to screen size
- **Fast Loading**: Optimized for mobile networks

## 🔐 Security

- Spring Security integration
- Password hashing with BCrypt
- Session-based authentication
- CSRF protection
- Secure password reset flow

## 📸 Screenshots

*(Add screenshots of your app here after deployment)*

## 🗺️ Roadmap

- [ ] Social features (share workouts, follow friends)
- [ ] Workout recommendations based on AI/ML
- [ ] Nutrition tracking
- [ ] Mobile app (React Native/Flutter)
- [ ] Export data to CSV/PDF
- [ ] Integration with fitness wearables

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👨‍💻 Author

**Petar Bozhkov**
- GitHub: [@petarkb123](https://github.com/petarkb123)

## 🙏 Acknowledgments

- Built-in exercise database seeded with common exercises
- UI inspired by modern fitness tracking applications
- Icons by Font Awesome

---

**Made with ❤️ and Spring Boot**

