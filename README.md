# SkillExchange API

**SkillExchange** is a Spring Boot–based platform designed to facilitate skill sharing between users.
It features built-in **MongoDB integration**, **JWT-based security**, and **automated Email (OTP) services**.

---

## 📁 Folder Structure

The project follows a standard **Spring Boot layered architecture** for scalability and maintainability:

```text
src/main/java/com/skillexchange/
├── config/             # Configuration classes (Security, Swagger, MongoDB)
├── controller/         # REST API Endpoints
├── dto/                # Data Transfer Objects for Requests & Responses
├── model/              # MongoDB Entities (Collections)
├── repository/         # Data Access Layer (MongoRepository)
├── service/            # Business Logic Interfaces
│   └── impl/           # Business Logic Implementations
└── security/           # JWT Filters, Auth Providers, and Security Logic
```

---

## 🛠 Tech Stack

* **Backend Framework:** Spring Boot 3.x
* **Database:** MongoDB (NoSQL)
* **Security:** Spring Security & JWT (JSON Web Token)
* **Documentation:** Swagger UI / SpringDoc OpenAPI
* **Messaging:** Java Mail Sender (SMTP for OTP & Notifications)

---

## ⚙️ Setup & Installation

### 1️⃣ Prerequisites

* JDK **17 or higher**
* MongoDB (Local instance or MongoDB Atlas URI)
* Maven (Dependency Management)

---

### 2️⃣ Environment Variables

To keep your credentials secure, configure the following environment variables in your system or IDE:

| Variable Name   | Description               | Default Value                             |
| --------------- | ------------------------- | ----------------------------------------- |
| `MONGODB_URI`   | MongoDB Connection String | `mongodb://localhost:27017/skillexchange` |
| `MAIL_USERNAME` | SMTP Email Address        | `your mail`                   |
| `MAIL_PASSWORD` | SMTP App Password         | `your app password`                     |
| `JWT_SECRET`    | Base64 Encoded Secret Key | `any secret key`                    |

---

### 3️⃣ Running the Application

```bash
# Clone the repository
git clone https://github.com/aakashsharma003/skillExchange.git

# Navigate to the project directory
cd skillExchange

# Build and run the application
mvn spring-boot:run
```

---

## 📖 API Documentation

Once the application is running on `localhost:8080`, access the interactive API documentation:

* **Swagger UI:**
  👉 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

* **OpenAPI Specification:**
  👉 [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

---

## 🛡️ Key Features

* **Secure Authentication**
  JWT-based stateless authentication with **60-minute expiration**

* **OTP Verification**
  Built-in email OTP service with **5-minute validity**

* **Performance Optimization**
  Response compression enabled for faster data transfer

* **Logging**
  Comprehensive **DEBUG-level logging** for development & monitoring

