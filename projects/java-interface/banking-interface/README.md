# Banking Interface - Java EE Application

## Overview

The **Banking Interface** is the central user management system for the banking ecosystem. It provides both REST API endpoints and EJB remote interfaces for user management, plus a clean web interface for user authentication and service navigation.

## Architecture

### 🏗️ **3-Tier Architecture**

- **API Layer**: JAX-RS REST endpoints (`/api/*`)
- **Service Layer**: EJB stateless services with remote interfaces
- **Persistence Layer**: JPA entities using Hibernate + MySQL

### 🌐 **Dual Access Pattern**

- **REST API**: For external services and web applications
- **EJB Remote**: For Java-to-Java communication (java-courant, java-depot, java-pret)

## Technical Stack

- **Java EE**: Jakarta EE 10
- **Application Server**: Apache TomEE 10.1.0
- **Database**: MySQL with JPA/Hibernate
- **Web UI**: Simple HTML + CSS (no JavaScript frameworks)
- **Build Tool**: Maven 3

## Database Schema

```sql
-- Users table (interface.sql)
CREATE TABLE users (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(id)
);
```

## API Endpoints

### 🔌 **REST API** (`http://localhost:8081/api`)

| Method | Endpoint               | Description       |
| ------ | ---------------------- | ----------------- |
| GET    | `/users`               | Get all users     |
| GET    | `/users/{id}`          | Get user by ID    |
| GET    | `/users/email/{email}` | Get user by email |
| POST   | `/users`               | Create new user   |
| PUT    | `/users/{id}`          | Update user       |
| DELETE | `/users/{id}`          | Delete user       |

### 🔗 **EJB Remote Interface**

```java
@Remote
public interface UserServiceRemote {
    User findUserById(Integer userId);
    User findUserByEmail(String email);
    List<User> getAllUsers();
    User createUser(String name, String email, String password);
    User updateUser(Integer userId, String name, String email, String password);
    void deleteUser(Integer userId);
    User authenticateUser(String email, String password);
}
```

## Web Interface

### 🎨 **Design System**

- **Theme**: Grayscale (black, white, shades of gray)
- **Colors**: Only green/red for success/error messages
- **Style**: Clean, minimal, professional
- **No JavaScript**: Pure HTML + CSS

### 📱 **Pages**

- **`/`** → Redirects to login
- **`/login.html`** → User login form
- **`/register.html`** → User registration form
- **`/menu.html`** → Dashboard with service links
- **`/logout`** → Session cleanup

### 🔐 **Authentication Flow**

1. **Register**: Create account → Auto-login → Dashboard
2. **Login**: Authenticate → Create session → Dashboard
3. **Session**: HttpSession with user data
4. **Logout**: Invalidate session → Redirect to login

## Integration Points

### 🔄 **With java-courant**

The java-courant service will use the `findUser(userId)` method which will be updated to call:

```java
// Future integration in CompteCourantService
@EJB
UserServiceRemote userService; // Remote EJB call

public User findUser(Integer userId) {
    return userService.findUserById(userId);
}
```

### 🔄 **Service Links**

- **Comptes Courants**: `http://localhost:8080` (java-courant)
- **Comptes Dépôts**: Coming soon (java-depot)
- **Comptes Prêts**: Coming soon (java-pret)

## Configuration

### 🗄️ **Database**

- **Name**: `s5_banking_interface`
- **Port**: `3306` (MySQL default)
- **Auto-creation**: Enabled
- **Connection Pool**: 5-20 connections

### 🚀 **Server**

- **Port**: `8081` (TomEE)
- **Context**: ROOT (`/`)
- **API Base**: `/api`

## Development

### 📁 **Project Structure**

```txt
banking-interface/
├── src/main/
│   ├── java/mg/razherana/banking/interfaces/
│   │   ├── api/          # REST resources
│   │   ├── application/  # EJB services
│   │   ├── entities/     # JPA entities
│   │   ├── dto/          # Data transfer objects
│   │   └── web/          # Web controllers
│   ├── resources/META-INF/
│   │   └── persistence.xml
│   ├── webapp/
│   │   ├── WEB-INF/resources.xml
│   │   ├── *.html        # Web pages
│   │   └── styles.css    # Grayscale theme
│   └── db/interface.sql  # Database schema
└── pom.xml
```

### 🛠️ **Build & Run**

```bash
# Compile
mvn clean compile

# Start server
mvn tomee:run

# Access
http://localhost:8081
```

### 🧪 **Testing**

```bash
# Test REST API
curl http://localhost:8081/api/users

# Test registration
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","password":"password"}'
```

## Future Enhancements

### 🔒 **Security**

- Password hashing (BCrypt)
- JWT tokens for API authentication
- HTTPS configuration
- Input validation & sanitization

### 🎯 **Features**

- User roles and permissions
- Password reset functionality
- Email verification
- Audit logging
- Account lockout policies

### 🌐 **Integration**

- Update java-courant to use EJB remote calls
- Implement java-depot service integration
- Implement java-pret service integration
- Single sign-on (SSO) across services

## Notes

- **Simple Authentication**: Currently uses plain text password comparison
- **Session Management**: Basic HttpSession storage
- **Error Handling**: Comprehensive with user-friendly messages
- **Responsive Design**: Works on desktop and mobile
- **Cross-Service**: Ready for multi-service architecture
