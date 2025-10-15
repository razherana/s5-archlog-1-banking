# Banking Pret Service

A Java EE loan account management service built with TomEE 10.1.0 and Jakarta EE 10.

## Overview

The Banking Pret service is the third module in our banking system architecture, specifically designed for loan account management. It follows the same architectural patterns as the other banking services (java-courant and java-interface).

## Architecture

- **Framework**: Jakarta EE 10.0 with TomEE 10.1.0
- **Database**: MySQL 8.0 (`s5_banking_pret`)
- **Host**: `127.0.0.3:8080`
- **API Base Path**: `/api`

## Features

- **Loan Account Management**: Create, read, and delete loan accounts
- **User Integration**: Integrates with java-interface service for user management via REST
- **RESTful API**: Complete REST API with JSON responses
- **Error Handling**: Comprehensive error handling with consistent ErrorDTO responses
- **Database**: Dedicated MySQL database for loan data

## Project Structure

```txt
banking-pret/
├── pom.xml                          # Maven configuration
├── src/main/
│   ├── db/pret.sql                  # Database schema
│   ├── java/mg/razherana/banking/pret/
│   │   ├── BankingApplication.java  # JAX-RS Application config
│   │   ├── api/                     # REST endpoints
│   │   │   └── ComptePretResource.java
│   │   ├── application/             # Business logic (EJB services)
│   │   │   └── ComptePretService.java
│   │   ├── entities/                # JPA entities
│   │   │   ├── ComptePret.java      # Loan account entity
│   │   │   └── User.java            # User representation
│   │   └── dto/                     # Data Transfer Objects
│   │       ├── ComptePretDTO.java
│   │       └── ErrorDTO.java
│   ├── resources/META-INF/
│   │   └── persistence.xml          # JPA configuration
│   ├── tomee/conf/                  # TomEE configuration
│   │   ├── server.xml               # Server config (127.0.0.3:8080)
│   │   └── resources.xml            # Database configuration
│   └── webapp/
│       └── index.html               # Service information page
└── tests/
    └── pret-integration-tests.sh    # Integration tests
```

## API Endpoints

### Loan Account Management

- `GET /api/comptes` - List all loan accounts
- `GET /api/comptes/{id}` - Get loan account by ID
- `GET /api/comptes/user/{userId}` - Get user's loan accounts
- `POST /api/comptes/user/{userId}` - Create loan account for user
- `DELETE /api/comptes/{id}` - Delete loan account

## Database Schema

The service uses a dedicated MySQL database `s5_banking_pret` with:

- **compte_prets**: Loan account table
  - `id` (PRIMARY KEY, AUTO_INCREMENT)
  - `user_id` (INT, NOT NULL) - Reference to user in java-interface
  - `created_at` (TIMESTAMP)
  - `updated_at` (TIMESTAMP)

## Integration

- **User Management**: Integrates with java-interface service at `127.0.0.2:8080/api`
- **REST Communication**: Uses Jakarta REST Client for inter-service communication
- **UserDTO Mapping**: Handles UserDTO responses from java-interface and maps to local User entity

## Getting Started

### Prerequisites

- Java 17
- Maven 3.8+
- MySQL 8.0
- TomEE 10.1.0 (automatically downloaded by Maven plugin)

### Database Setup

- Create the database:

```sql
mysql -u root -p < src/main/db/pret.sql
```

- Update database credentials in `src/main/tomee/conf/resources.xml` if needed.

### Build and Run

```bash
# Build the project
mvn clean compile

# Run with TomEE
mvn tomee:run

# Build and run
mvn clean install tomee:run
```

The service will be available at: `http://127.0.0.3:8080`

### Testing

Run integration tests:

```bash
chmod +x tests/pret-integration-tests.sh
./tests/pret-integration-tests.sh
```

## Service Dependencies

This service requires the following services to be running:

1. **java-interface** (`127.0.0.2:8080`) - For user management
2. **MySQL Server** - For data persistence

## Architecture Notes

- **EJB Services**: Business logic implemented as stateless EJBs
- **JPA Entities**: Hibernate-based persistence with MySQL
- **REST Integration**: Uses Jakarta REST Client for service-to-service communication
- **Error Handling**: Consistent error responses following banking system patterns
- **Transaction Management**: JTA-managed transactions for data consistency

## Entity Design

### ComptePret

Basic loan account entity with:

- `id`: Unique identifier
- `userId`: Reference to user (via java-interface service)
- `createdAt`: Creation timestamp

_Additional loan-specific fields will be added based on business requirements._

## Development

The project follows the established banking system conventions:

- Package structure: `mg.razherana.banking.pret.*`
- Service patterns matching java-courant and java-interface
- REST-based inter-service communication
- Comprehensive error handling and logging

## Version

- **Version**: 1.0-SNAPSHOT
- **Java**: 17
- **Jakarta EE**: 10.0
- **TomEE**: 10.1.0
- **Maven**: 3.8+
