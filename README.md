# RepoPulse Backend

**Powerful GitHub Repository Analytics Platform**  
Backend for [RepoPulse](https://repopulse01.vercel.app/) — Analyze repositories with beautiful dashboards and AI-powered insights.

---

## ✨ Features

- GitHub repository statistics & analytics
- AI-powered code insights using OpenRouter
- Google OAuth authentication
- Clean REST API architecture
- Docker support
- Centralized exception handling
- DTO-based request/response management

---

## 🛠 Tech Stack

- **Java 17**
- **Spring Boot 3**
- **Spring Data JPA**
- **Maven**
- **MySQL** (or any SQL database)
- **GitHub REST API**
- **OpenRouter API**

---

## 📁 Project Structure

```bash
src/main/java/com/repopulse/
├── RepopulseApplication.java
├── config/
├── controller/
├── dto/
├── entity/
├── repository/
├── service/
└── exception/
```

---

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/Shubham-1068/repopulse-backend.git
cd repopulse-backend
```

### 2. Create `application.yml`

Create the file: `src/main/resources/application.yml`

```yaml
spring:
  application:
    name: repopulse-backend
  datasource:
    url: jdbc:postgresql://localhost:5432/repopulse_db
    username: postgres
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update

github:
  token: ghp_your_github_personal_access_token

openrouter:
  api-key: sk-or-v1-your_openrouter_api_key_here
  base-url: https://openrouter.ai/api/v1
```

### 3. Run the Application

```bash
# Using Maven
./mvnw spring-boot:run

# Or using Docker
docker build -t repopulse-backend .
docker run -p 8080:8080 repopulse-backend
```

---

## 📡 Main API Endpoints

| Method | Endpoint                        | Description                        |
|--------|---------------------------------|------------------------------------|
| POST   | `/api/analyze`                  | Analyze GitHub repository          |
| GET    | `/api/repos/{owner}/{repo}`     | Get repository details             |
| POST   | `/api/ai/insights`              | Get AI analysis                    |
| GET    | `/api/auth/profile`             | Get user profile                   |

---

## 🔗 Links

- **Live Frontend**: [repopulse01.vercel.app](https://repopulse01.vercel.app/)
- **Backend Deployed On**: Render 

---

## 🚢 Deployment

This project is ready to deploy on:
- Render.com
- Railway
- AWS
- Docker

---

## 📄 License

MIT License

---

## 👨‍💻 Author

**Shubham**  
GitHub: [Shubham-1068](https://github.com/Shubham-1068)

---

**Made with ❤️ for the developer community**
