# Banking Core API & Event-Driven Distributed Banking Service

A Spring Boot 3 enterprise banking microservice built with **Java 21**, **Redisson (Redis Distributed Locks)**, **MySQL & Spring Data JPA**, **Double-entry style Ledgering**, and **Apache Kafka Event-Driven Architecture**.

---

## 🏛 Architecture & Transaction Pipeline

```text
Client
  │
  ▼
Spring Boot API (Controller / DTO / Validation)
  │
  ▼
Distributed Lock (Redis / Redisson)
  │
  ▼
Update Database (MySQL / Spring Data JPA)
  │
  ▼
Create Ledger Entry (Audit Ledger Record)
  │
  ▼
Publish Kafka Event (Banking Event Producer)
  │
  ▼
Audit Consumer (Asynchronous Compliance & Audit Logging)
```

---

## 📂 Project Structure

```text
Banking/
├── docker-compose.yml
├── backend/
│    └── banking-core/
│         ├── pom.xml
│         └── src/
│              └── main/
│                   ├── java/
│                   │    └── com/bank/banking_core/
│                   │         ├── BankingCoreApplication.java
│                   │         ├── config/
│                   │         │    ├── KafkaConfig.java
│                   │         │    ├── OpenApiConfig.java
│                   │         │    └── RedissonConfig.java
│                   │         ├── constants/
│                   │         │    └── AppConstants.java
│                   │         ├── controller/
│                   │         │    ├── AccountController.java
│                   │         │    └── TransferController.java
│                   │         ├── dto/
│                   │         │    ├── AccountResponseDto.java
│                   │         │    ├── CreateAccountRequestDto.java
│                   │         │    ├── TransactionRequestDto.java
│                   │         │    ├── TransactionResponseDto.java
│                   │         │    └── TransferRequestDto.java
│                   │         ├── entity/
│                   │         │    ├── AccountEntity.java
│                   │         │    └── LedgerEntryEntity.java
│                   │         ├── enums/
│                   │         │    ├── AccountStatus.java
│                   │         │    └── TransactionType.java
│                   │         ├── event/
│                   │         │    ├── consumer/
│                   │         │    │    └── AuditConsumer.java
│                   │         │    ├── mapper/
│                   │         │    │    └── TransactionEventMapper.java
│                   │         │    ├── model/
│                   │         │    │    └── BankingTransactionEvent.java
│                   │         │    └── producer/
│                   │         │         └── BankingEventProducer.java
│                   │         ├── exception/
│                   │         │    ├── AccountInactiveException.java
│                   │         │    ├── DuplicateResourceException.java
│                   │         │    ├── GlobalExceptionHandler.java
│                   │         │    ├── InsufficientBalanceException.java
│                   │         │    └── ResourceNotFoundException.java
│                   │         ├── ledger/
│                   │         │    └── LedgerService.java
│                   │         ├── mapper/
│                   │         │    └── AccountMapper.java
│                   │         ├── repository/
│                   │         │    ├── AccountRepository.java
│                   │         │    └── LedgerRepository.java
│                   │         ├── response/
│                   │         │    └── ApiResponse.java
│                   │         ├── service/
│                   │         │    ├── BankingService.java
│                   │         │    └── BankingServiceImpl.java
│                   │         └── util/
│                   │              └── DistributedLockUtil.java
│                   └── resources/
│                        └── application.yml
```

---

## 🛠 Tech Stack

| Technology | Purpose |
|---|---|
| **Java 21** | Modern Java LTS |
| **Spring Boot 3.3.4** | Core backend framework |
| **Spring Data JPA & Hibernate** | Database persistence & ORM |
| **MySQL 8.0** | Relational Database |
| **Redis 7 & Redisson 3.35.0** | Distributed Locking for concurrency control |
| **Apache Kafka & Zookeeper** | Event streaming & asynchronous audit trail |
| **Docker Compose** | Local infrastructure services orchestrator |
| **Swagger / Springdoc OpenAPI 2.6.0** | Interactive API Documentation |
| **Maven** | Dependency management & build tool |

---

## 🚀 Getting Started

### 1. Start Infrastructure (Docker Compose)
In the root directory, start MySQL, Redis, Zookeeper, and Kafka:
```bash
docker compose up -d
```

### 2. Run the Application
Navigate to `backend/banking-core`:
```bash
cd backend/banking-core
mvn clean spring-boot:run
```

### 3. API Documentation
Access Swagger UI in your browser:
- [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## 🔌 API Endpoints

### Account Endpoints
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/accounts` | Create an account with initial deposit |
| `GET` | `/api/accounts` | Fetch all accounts |
| `GET` | `/api/accounts/{id}` | Fetch account details by ID |

### Deposit Endpoint
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/accounts/{id}/deposit` | Deposit funds (Protected by Distributed Lock, logged in Ledger, emits Kafka event) |

### Withdraw Endpoint
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/accounts/{id}/withdraw` | Withdraw funds (Protected by Distributed Lock, validated balance, logged in Ledger, emits Kafka event) |

### Transfer Endpoint
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/transfers` | Transfer funds atomically between two accounts with deadlock-free dual lock ordering |
