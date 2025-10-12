# Quick Setup Guide for Banking Interface

## Prerequisites

1. **MySQL Server** running on localhost:3306
2. **Java 17** installed
3. **Maven 3** installed

## Setup Steps

### 1. Create Database

```sql
-- Connect to MySQL
mysql -u root -p

-- Create database
CREATE DATABASE s5_banking_interface;

-- Run the interface.sql script
USE s5_banking_interface;
SOURCE /path/to/interface.sql;
```

### 2. Start the Application

```bash
# Navigate to project
cd projects/java-interface/banking-interface

# Compile and run
mvn clean compile
mvn tomee:run
```

### 3. Access the Application

- **Web Interface**: [http://localhost:8081](http://localhost:8081)
- **REST API**: [http://localhost:8081/api/users](http://localhost:8081/api/users)

### 4. Test Registration

1. Go to [http://localhost:8081](http://localhost:8081)
2. Click "Register here"
3. Create a test account
4. Login and access the dashboard

## Integration with java-courant

Once both services are running:

- **java-interface**: Port 8081 (User management)
- **java-courant**: Port 8080 (Current accounts)

The java-courant service will call java-interface via EJB remote interface to get user information.

## Troubleshooting

- **Database Connection**: Check MySQL is running and credentials are correct
- **Port Conflicts**: Ensure ports 8080 and 8081 are available
- **Build Issues**: Run `mvn clean` if compilation fails
