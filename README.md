# 🧪 Students Service

This microservice handles student scores management. It allows uploading and retrieving student scores with pagination and filtering support.

---

## 🚀 Tech Stack

- Java 17
- Spring Boot 3.5.5
- Spring Batch
- Spring Data JPA
- PostgreSQL
- Flyway
- Swagger (OpenAPI)
- Docker

---

## ✅ Prerequisites

Ensure the following are installed and properly configured on your system:

- Java 17
- Maven
- Docker(To spin the database)

---

## 🚀 How to Run the Project

Follow these steps to run the application locally:

### 1. Clone the project
```bash
https://github.com/nathankorir/student-backend.git
```

### 2. Install dependencies
```bash
mvn clean install
```

### 3. Create the directory on your host machine
#### Linux Host Setup
```bash
mkdir -p /var/log/applications/API/dataprocessing
```
```bash
sudo chmod 777 /var/log/applications/API/dataprocessing
```

### 4. Start the postgres database
```bash
docker-compose up
```

### 5. Run the application
```bash
./mvnw spring-boot:run  
```

## Documentation

### Swagger UI
```bash
http://localhost:8080/swagger-ui/index.html
```

### OpenAPI JSON Spec
```bash
http://localhost:8080/v3/api-docs
```