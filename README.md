# Farm2Future Backend

Farm2Future Backend is the backend service for the Farm2Future agricultural digital management platform.

This project provides RESTful APIs for the Farm2Future frontend, including user authentication, dashboard statistics, farm data management, ESG score records, token reward records, and transaction records.

---

## 1. Project Overview

Farm2Future is an agricultural digital platform designed to support farm management, ESG performance tracking, and token-based reward records.

The backend system is responsible for:

- Providing REST API services for the frontend
- Managing user login and role-based access
- Managing farm information
- Managing farm production and ESG-related data
- Providing dashboard overview statistics
- Recording token rewards
- Recording transaction history
- Connecting the frontend with the MySQL database
- Supporting future blockchain / smart contract integration

---

## 2. Technology Stack

| Technology | Description |
|---|---|
| Java 21 | Backend programming language |
| Spring Boot 3 | Main backend framework |
| Spring Web | REST API development |
| Spring Security | Authentication and authorization |
| JWT | Token-based login authentication |
| MyBatis-Plus | ORM framework |
| MySQL | Relational database |
| Lombok | Reduces boilerplate Java code |
| Spring Boot Actuator | Health check and monitoring |
| Springdoc OpenAPI | Swagger API documentation |
| Maven | Dependency management and project build tool |

---

## 3. Project Structure

```text
Fram2Future-Backend
├── sql
│   └── database scripts
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.farm2future.farm2future_backend
│   │   │       ├── common
│   │   │       │   ├── exception
│   │   │       │   ├── result
│   │   │       │   └── security
│   │   │       ├── config
│   │   │       ├── model
│   │   │       │   ├── dashboard
│   │   │       │   ├── fram
│   │   │       │   └── user
│   │   │       └── Farm2futureBackendApplication.java
│   │   └── resources
│   │       └── application.yml
│   └── test
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

---

## 4. Main Features

### 4.1 User Authentication

The backend supports user login through JWT authentication.

Example endpoint:

```http
POST /api/auth/login
```

Example request:

```json
{
  "email": "demo@farmer.com",
  "password": "123456",
  "role": "farmer"
}
```

Supported test users:

| Role | Email | Password |
|---|---|---|
| Farmer | demo@farmer.com | 123456 |
| Buyer | demo@buyer.com | 123456 |
| Regulator | demo@regulator.com | 123456 |

---

### 4.2 Dashboard Overview

The dashboard API provides summary data for the frontend home page.

Example endpoint:

```http
GET /api/dashboard/overview
```

Example response:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "stats": {
      "totalFarms": 3,
      "totalTokens": 1200,
      "totalTransactions": 15,
      "averageEsgScore": 86.5
    },
    "monthlyComparison": {
      "farmsChange": 0,
      "tokensChange": 120,
      "transactionsChange": 3,
      "esgScoreChange": 2.5
    }
  }
}
```

---

### 4.3 Farm Data Management

The backend is designed to support farm data submission from the frontend.

Example endpoint:

```http
POST /api/farms/{farmId}/data
```

Example request:

```json
{
  "period": "2026-Q2",
  "yield_kg": 1200,
  "water_usage_liters": 5000,
  "fertilizer_usage_kg": 120,
  "pesticide_usage_kg": 20,
  "labor_hours": 300,
  "fair_wage_flag": true,
  "compliance_pass": true
}
```

This data can be used later for ESG score calculation and token reward generation.

---

### 4.4 ESG Score Records

The backend stores ESG score records for farms.

ESG scoring can include:

- Environmental score
- Social score
- Governance score
- Overall ESG score
- Score period
- Related farm ID

---

### 4.5 Token Reward Records

The backend stores token reward records for farms.

Possible token reward logic:

- Higher ESG score gives more token rewards
- Compliant farm data receives reward tokens
- Token records can later be connected with blockchain smart contracts

---

### 4.6 Transaction Records

The backend stores transaction history, such as:

- Token transfers
- Farm product transactions
- Buyer-related purchase records
- Reward distribution history

---

## 5. API Response Format

All backend APIs should follow a unified response format.

Success response:

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

Failure response:

```json
{
  "code": 400,
  "message": "error message",
  "data": null
}
```

---

## 6. Environment Requirements

Before running this project, make sure the following tools are installed:

- Java 21 or above
- Maven 3.8 or above
- MySQL 8 or above
- Git

Check Java version:

```bash
java -version
```

Check Maven version:

```bash
mvn -version
```

---

## 7. Database Setup

### 7.1 Create Database

Login to MySQL and run:

```sql
CREATE DATABASE farm2future
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

### 7.2 Import SQL Scripts

If SQL files are provided in the `sql` directory, import them into MySQL:

```bash
mysql -u root -p farm2future < sql/your_sql_file.sql
```

---

## 8. Configuration

The main configuration file is:

```text
src/main/resources/application.yml
```

Recommended database configuration:

```yml
server:
  port: 7020

spring:
  application:
    name: farm2future-backend

  datasource:
    url: jdbc:mysql://localhost:3306/farm2future?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Kuala_Lumpur&allowPublicKeyRetrieval=true&useSSL=false
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

app:
  jwt:
    secret: ${JWT_SECRET}
    expire-hours: 24
```

Important:

Do not commit real database passwords or JWT secrets to GitHub.

Use environment variables instead.

Linux / macOS:

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
export JWT_SECRET=farm2future-enterprise-secret-key-change-this-to-a-long-random-string
```

Windows CMD:

```cmd
set DB_USERNAME=root
set DB_PASSWORD=your_password
set JWT_SECRET=farm2future-enterprise-secret-key-change-this-to-a-long-random-string
```

Windows PowerShell:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="farm2future-enterprise-secret-key-change-this-to-a-long-random-string"
```

---

## 9. Run Locally

### 9.1 Clone Repository

```bash
git clone https://github.com/yinsizhe-spec/Fram2Future-Backend.git
cd Fram2Future-Backend
```

### 9.2 Start Project

Using Maven:

```bash
mvn spring-boot:run
```

Using Maven Wrapper on Linux / macOS:

```bash
./mvnw spring-boot:run
```

Using Maven Wrapper on Windows:

```cmd
mvnw.cmd spring-boot:run
```

After startup, the backend should run at:

```text
http://localhost:7020
```

---

## 10. Build Project

Package the project:

```bash
mvn clean package
```

Skip tests when packaging:

```bash
mvn clean package -DskipTests
```

Run the JAR file:

```bash
java -jar target/farm2future-backend-0.0.1-SNAPSHOT.jar
```

---

## 11. Server Deployment

### 11.1 Build JAR

```bash
mvn clean package -DskipTests
```

### 11.2 Upload JAR to Server

Example server directory:

```text
/root/farm2future/
```

### 11.3 Start Backend Service

```bash
nohup java -jar farm2future-backend.jar > app.log 2>&1 &
```

### 11.4 Check Running Process

```bash
ps -ef | grep farm2future-backend
```

### 11.5 View Logs

```bash
tail -f app.log
```

---

## 12. API Documentation

This project uses Springdoc OpenAPI.

After starting the backend, visit:

```text
http://localhost:7020/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:7020/v3/api-docs
```

---

## 13. Health Check

Spring Boot Actuator can be used to check the backend status.

Health check endpoint:

```http
GET /actuator/health
```

Example:

```text
http://localhost:7020/actuator/health
```

---

## 14. Common Issues

### 14.1 MySQL Connection Failed

Check the following:

- MySQL service is running
- Database name is correct
- Username and password are correct
- MySQL port is open
- Server firewall allows database connection
- JDBC URL is correct

---

### 14.2 Public Key Retrieval is not allowed

If this error appears:

```text
Public Key Retrieval is not allowed
```

Add this parameter to the JDBC URL:

```text
allowPublicKeyRetrieval=true
```

Example:

```yml
url: jdbc:mysql://localhost:3306/farm2future?allowPublicKeyRetrieval=true&useSSL=false
```

---

### 14.3 Backend Started but Frontend Cannot Access API

Check the following:

- Backend service is running
- Backend port is correct
- Server firewall has opened the backend port
- Cloud server security group allows the port
- Frontend `.env` API base URL is correct
- CORS configuration allows the frontend domain
- Spring Security configuration allows the target endpoint

---

### 14.4 Port Already in Use

Check port usage:

```bash
lsof -i:7020
```

Kill the process:

```bash
kill -9 PID
```

Or change the port in `application.yml`:

```yml
server:
  port: 7021
```

---

## 15. Useful API List

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/auth/login | User login |
| GET | /api/dashboard/overview | Dashboard overview statistics |
| POST | /api/farms/{farmId}/data | Submit farm data |
| GET | /actuator/health | Backend health check |
| GET | /v3/api-docs | OpenAPI JSON |
| GET | /swagger-ui/index.html | Swagger UI page |

---

## 16. Git Commit Guide

After updating the README:

```bash
git add README.md
git commit -m "docs: update backend README"
git push origin main
```

---

## 17. Future Development Plan

Planned improvements:

- Complete all frontend API contract endpoints
- Improve farm data submission API
- Add ESG score calculation logic
- Add token reward calculation logic
- Add blockchain smart contract integration
- Improve role-based permission control
- Add more unit tests and integration tests
- Add Docker deployment support
- Add Jenkins or GitHub Actions CI/CD
- Improve production security configuration

---

## 18. Repository

Backend repository:

```text
https://github.com/yinsizhe-spec/Fram2Future-Backend
```

Frontend repository:

```text
https://github.com/Joseph9807/Farm2Future-Frontend-V4.1
```

---

## 19. Team

This project is developed by the Farm2Future team.
