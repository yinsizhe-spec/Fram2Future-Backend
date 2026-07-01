# Farm2Future Backend

Farm2Future Backend is the backend service for the Farm2Future agricultural digital management platform.

This backend provides RESTful APIs for the Farm2Future frontend, including user authentication, dashboard statistics, farm data submission, ESG report data, ESG report export, token records, token transfer records, and system health monitoring.

---

## 1. Project Overview

Farm2Future is an agricultural digital platform designed to support:

- Farm data management
- ESG performance tracking
- Dashboard data statistics
- Token reward records
- Smart contract transfer records
- AI ESG report support
- CSV / PDF ESG report export
- Frontend and backend integration

The backend system is responsible for:

- Providing REST APIs for the frontend
- Managing user login and JWT authentication
- Managing farm and farm-related data
- Receiving farm data submitted by users
- Calculating or storing ESG-related data
- Providing dashboard overview statistics
- Providing ESG report export APIs
- Recording token rewards and token transfer history
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
| Maven | Dependency management and build tool |

---

## 3. Project Structure

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

---

## 4. Main Features

### 4.1 User Authentication

The backend supports user login through JWT authentication.

Endpoint:

POST /api/auth/login

Example request:

{
  "email": "demo@farmer.com",
  "password": "123456",
  "role": "farmer"
}

Example response:

{
  "code": 200,
  "message": "success",
  "data": {
    "token": "jwt-token",
    "user": {
      "id": "u1",
      "name": "Joseph",
      "email": "demo@farmer.com",
      "role": "farmer",
      "entityName": "Green Valley Farm"
    }
  }
}

Supported test users:

| Role | Email | Password |
|---|---|---|
| Farmer | demo@farmer.com | 123456 |
| Buyer | demo@buyer.com | 123456 |
| Regulator | demo@regulator.com | 123456 |

---

### 4.2 Dashboard Overview

The dashboard API provides summary data for the frontend home page.

Endpoint:

GET /api/dashboard/overview

The dashboard data focuses on current-month statistics and comparison with the previous month.

---

### 4.3 Farm Data Submission

The backend supports farm data submission from the frontend.

Endpoint:

POST /api/farms/{farmId}/data

Example request:

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

This submitted data can be used for:

- ESG score calculation
- Dashboard statistics
- Token reward calculation
- AI ESG report generation
- ESG report export

---

### 4.4 ESG Report Data

The backend supports ESG-related data records for farms.

ESG data may include:

- Environmental score
- Social score
- Governance score
- Overall ESG score
- Reporting period
- Farm ID
- Farm / entity information
- Water usage
- Fertilizer usage
- Pesticide usage
- Yield data
- Labor and compliance data

The frontend should request farm / entity data from the backend instead of using hard-coded values.

---

### 4.5 ESG Report Export

The backend supports ESG report export.

Endpoint:

GET /api/reports/esg/export?format=csv|pdf&farmId={farmId}&from={from}

Example:

GET /api/reports/esg/export?format=csv&farmId=farm_001&from=2026-06

If farmId or from is not provided, the backend can return all available matching data depending on the service logic.

Download endpoint:

GET /api/reports/esg/export/download/{filename}

Supported formats:

| Format | Description |
|---|---|
| csv | Export ESG report as CSV |
| pdf | Export ESG report as PDF |

---

### 4.6 Token Records

The backend stores token reward records for farms.

Token reward records may include:

- Token ID
- Farm ID
- Batch ID
- Crop type
- Quantity
- Token amount
- Owner
- Owner address
- Transaction hash
- Token status

Possible reward logic:

- Higher ESG score gives more token rewards
- Compliant farm data receives reward tokens
- Token records can later be connected with blockchain smart contracts

---

### 4.7 Smart Contract Transfer Records

The backend supports token transfer records for the Smart Contract Transfer page.

Transfer-related APIs may include:

POST /api/tokens/{tokenId}/transfer

GET /api/tokens/transfers

GET /api/tokens/transfers?farmId={farmId}

The transfer history should come from the backend API instead of frontend mock data.

Transfer records may include:

- Transfer ID
- Token ID
- Farm ID
- Old owner
- Old owner address
- New owner
- New owner address
- Transaction hash
- Transfer time

---

### 4.8 Transaction Records

The backend stores transaction history, such as:

- Token transfers
- Farm product transactions
- Buyer-related purchase records
- Reward distribution records
- Smart contract transfer history

---

## 5. API Response Format

All backend APIs should follow a unified response format.

Success response:

{
  "code": 200,
  "message": "success",
  "data": {}
}

Failure response:

{
  "code": 400,
  "message": "error message",
  "data": null
}

---

## 6. Useful API List

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/login` | User login |
| GET | `/api/dashboard/overview` | Dashboard overview statistics |
| POST | `/api/farms/{farmId}/data` | Submit farm data |
| GET | `/api/reports/esg/export` | Export ESG report |
| GET | `/api/reports/esg/export/download/{filename}` | Download exported ESG report |
| POST | `/api/tokens/{tokenId}/transfer` | Transfer token ownership |
| GET | `/api/tokens/transfers` | Query all token transfer records |
| GET | `/api/tokens/transfers?farmId={farmId}` | Query transfer records by farm |
| GET | `/actuator/health` | Backend health check |
| GET | `/v3/api-docs` | OpenAPI JSON |
| GET | `/swagger-ui/index.html` | Swagger UI |

---

## 7. Environment Requirements

Before running this project, make sure the following tools are installed:

- Java 21 or above
- Maven 3.8 or above
- MySQL 8 or above
- Git

Check Java version:

java -version

Check Maven version:

mvn -version

---

## 8. Database Setup

### 8.1 Create Database

Login to MySQL and run:

CREATE DATABASE farm2future
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

### 8.2 Import SQL Scripts

If SQL files are provided in the `sql` directory, import them into MySQL:

mysql -u root -p farm2future < sql/your_sql_file.sql

---

## 9. Configuration

The main configuration file is:

src/main/resources/application.yml

Recommended database configuration:

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

Important:

Do not commit real database passwords or JWT secrets to GitHub.

Use environment variables instead.

Linux / macOS:

export DB_USERNAME=root
export DB_PASSWORD=your_password
export JWT_SECRET=farm2future-enterprise-secret-key-change-this-to-a-long-random-string

Windows CMD:

set DB_USERNAME=root
set DB_PASSWORD=your_password
set JWT_SECRET=farm2future-enterprise-secret-key-change-this-to-a-long-random-string

Windows PowerShell:

$env:DB_USERNAME="root"
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="farm2future-enterprise-secret-key-change-this-to-a-long-random-string"

---

## 10. Run Locally

### 10.1 Clone Repository

git clone https://github.com/yinsizhe-spec/Fram2Future-Backend.git
cd Fram2Future-Backend

### 10.2 Start Project

Using Maven:

mvn spring-boot:run

Using Maven Wrapper on Linux / macOS:

./mvnw spring-boot:run

Using Maven Wrapper on Windows:

mvnw.cmd spring-boot:run

After startup, the backend should run at:

http://localhost:7020

---

## 11. Build Project

Package the project:

mvn clean package

Skip tests when packaging:

mvn clean package -DskipTests

Run the JAR file:

java -jar target/farm2future-backend-0.0.1-SNAPSHOT.jar

---

## 12. Server Deployment

### 12.1 Build JAR

mvn clean package -DskipTests

### 12.2 Upload JAR to Server

Example server directory:

/root/farm2future/

### 12.3 Start Backend Service

nohup java -jar farm2future-backend.jar > app.log 2>&1 &

### 12.4 Check Running Process

ps -ef | grep farm2future-backend

### 12.5 View Logs

tail -f app.log

### 12.6 Check Port

ss -tulpn | grep 7020

---

## 13. API Documentation

This project uses Springdoc OpenAPI.

After starting the backend, visit:

http://localhost:7020/swagger-ui/index.html

OpenAPI JSON:

http://localhost:7020/v3/api-docs

---

## 14. Health Check

Spring Boot Actuator can be used to check the backend status.

Health check endpoint:

GET /actuator/health

Example:

http://localhost:7020/actuator/health

Example response:

{
  "status": "UP"
}

---

## 15. Frontend Integration Notes

The frontend should connect to this backend by setting the API base URL correctly.

Example frontend `.env`:

VITE_USE_MOCK=false
VITE_API_BASE_URL=http://64.176.57.254:7020

Important frontend integration requirements:

- Do not use mock data when real backend APIs are available.
- Dashboard statistics should come from `/api/dashboard/overview`.
- Farm data submission should call `/api/farms/{farmId}/data`.
- ESG report export should call `/api/reports/esg/export`.
- Smart Contract Transfer history should call backend transfer record APIs.
- Farm / Entity values in AI ESG Report should come from backend data.
- Frontend should handle loading, success, and error states clearly.

---

## 16. Common Issues

### 16.1 MySQL Connection Failed

Check the following:

- MySQL service is running
- Database name is correct
- Username and password are correct
- MySQL port is open
- Server firewall allows database connection
- JDBC URL is correct

---

### 16.2 Public Key Retrieval is not allowed

If this error appears:

Public Key Retrieval is not allowed

Add this parameter to the JDBC URL:

allowPublicKeyRetrieval=true

Example:

url: jdbc:mysql://localhost:3306/farm2future?allowPublicKeyRetrieval=true&useSSL=false

---

### 16.3 Backend Started but Frontend Cannot Access API

Check the following:

- Backend service is running
- Backend port is correct
- Server firewall has opened the backend port
- Cloud server security group allows the port
- Frontend `.env` API base URL is correct
- Frontend mock mode is disabled
- CORS configuration allows the frontend domain
- Spring Security configuration allows the target endpoint

---

### 16.4 Port Already in Use

Check port usage:

lsof -i:7020

Or:

ss -tulpn | grep 7020

Kill the process:

kill -9 PID

Or change the port in `application.yml`:

server:
  port: 7021

---

### 16.5 403 Forbidden

If the frontend receives `403 Forbidden`, check:

- Whether Spring Security permits the requested endpoint
- Whether JWT token is required
- Whether the frontend sends the token correctly
- Whether CORS configuration is correct
- Whether preflight `OPTIONS` requests are allowed

---

### 16.6 No Acceptable Representation

If this error appears:

HttpMediaTypeNotAcceptableException: No acceptable representation

Possible solutions:

- Make sure response DTO has getters and setters
- Make sure Lombok is working correctly
- Make sure the returned object can be serialized to JSON
- Restart the backend project after modifying DTO or response classes

---

## 17. Git Commit Example

After updating the README:

git add README.md
git commit -m "docs: update backend README documentation"
git push origin main

---

## 18. Project Status

Current backend supports the core structure for Farm2Future frontend integration, including authentication, dashboard data, farm data submission, ESG-related data, token records, transfer records, and export support.

Future improvements may include:

- More complete ESG score calculation logic
- Scheduled daily statistics calculation
- More detailed API documentation
- Real blockchain smart contract integration
- More complete frontend-backend integration testing
- Deployment automation with CI/CD
