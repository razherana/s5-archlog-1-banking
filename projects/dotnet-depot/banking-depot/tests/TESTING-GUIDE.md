# Banking Depot API Testing Guide

## Overview

This guide provides detailed instructions for running the Banking Depot API test suite. The tests are designed to validate all functionality of the ASP.NET Core deposit account module and its integration with the Java current account service.

## Prerequisites

### 1. System Requirements

- **Operating System**: Linux/macOS/Windows with bash support
- **Tools**: `curl`, `jq` (for JSON processing)
- **Database**: MySQL server with `s5_archlog_1_banking_depot` database

### 2. Services Required

1. **ASP.NET Core Deposit Service**: Must be running on `http://localhost:5000`
2. **Java Current Account Service**: Must be running on `http://127.0.0.2:8080`
3. **MySQL Database**: Accessible with configured credentials

### 3. Test Users

The following users must exist in the Java current account service:

- User ID: 1
- User ID: 2
- User ID: 3

## Installation & Setup

### 1. Install Dependencies

**Ubuntu/Debian:**

```bash
sudo apt update
sudo apt install curl jq mysql-client
```

**macOS:**

```bash
brew install curl jq mysql-client
```

**Windows (WSL):**

```bash
sudo apt update && sudo apt install curl jq mysql-client
```

### 2. Start Required Services

**Start ASP.NET Core Service:**

```bash
cd banking-depot
dotnet run
# Service should be available at http://localhost:5000
```

**Verify Java Service is Running:**

```bash
curl http://127.0.0.2:8080/api/users/1
# Should return user information
```

### 3. Database Setup

**Create and setup database:**

```bash
mysql -u root -p
CREATE DATABASE s5_archlog_1_banking_depot;
exit

# Run schema creation
cd banking-depot
mysql -u root -p s5_archlog_1_banking_depot < db/depot.sql
```

### 4. Verify Setup

**Test API accessibility:**

```bash
curl http://localhost:5000/api/typecomptesdepots
# Should return JSON array (possibly empty)
```

**Test Java integration:**

```bash
curl http://127.0.0.2:8080/api/users/1
# Should return user information
```

## Running Tests

### Quick Start

**Run all tests:**

```bash
cd tests
chmod +x run-all-tests.sh
./run-all-tests.sh
```

### Individual Test Suites

#### 1. Type Management Tests

```bash
cd tests/1-test-type-management
chmod +x type-crud-tests.sh
./type-crud-tests.sh
```

**What it tests:**

- Create/Read/Update/Delete account types
- Interest rate validation
- Error handling for invalid data

#### 2. Account Management Tests

```bash
cd tests/2-test-account-management
chmod +x account-crud-tests.sh
./account-crud-tests.sh
```

**What it tests:**

- Deposit account creation
- User validation through Java service
- Business rule enforcement
- Multiple accounts per user

#### 3. Withdrawal Management Tests

```bash
cd tests/3-test-withdrawal-management
chmod +x withdrawal-tests.sh
./withdrawal-tests.sh
```

**What it tests:**

- Successful withdrawals at maturity
- Prevention of early withdrawals
- Interest calculation on withdrawal
- Duplicate withdrawal prevention

#### 4. Interest Calculation Tests

```bash
cd tests/5-test-interest-calculation
chmod +x interest-calculation-tests.sh
./interest-calculation-tests.sh
```

**What it tests:**

- Mathematical accuracy of interest calculations
- Different time periods and rates
- Edge cases (leap years, same-day maturity)
- Backtracking calculations

#### 5. Integration Scenarios Tests

```bash
cd tests/4-test-integration-scenarios
chmod +x full-workflow-test.sh
./full-workflow-test.sh
```

**What it tests:**

- End-to-end workflows
- Multi-user scenarios
- Historical operations
- Error recovery
- Performance under load

## Test Data Management

### Setup Test Data

```bash
cd tests
mysql -u root -p s5_archlog_1_banking_depot < sample_data.sql
```

### Clear Test Data

```bash
cd tests
mysql -u root -p s5_archlog_1_banking_depot < clear_data.sql
```

### Custom Test Data

You can modify `sample_data.sql` to include additional test scenarios:

- Different account types with various interest rates
- Sample deposit accounts with different maturity dates
- Historical withdrawn accounts

## Understanding Test Results

### Success Indicators

- ✅ **HTTP 200/201**: Successful operations
- ✅ **HTTP 204**: Successful deletions
- ✅ **JSON responses**: Properly formatted data
- ✅ **Mathematical accuracy**: Interest calculations match expected values

### Expected Errors

- ❌ **HTTP 400**: Invalid data (expected for validation tests)
- ❌ **HTTP 404**: Non-existent resources (expected for negative tests)
- ❌ **User validation failures**: Non-existent users in Java service

### Common Issues

#### Connection Issues

```bash
# Error: "Connection refused"
# Solution: Ensure services are running
curl http://localhost:5000/api/typecomptesdepots
curl http://127.0.0.2:8080/api/users/1
```

#### Database Issues

```bash
# Error: "Database connection failed"
# Solution: Check MySQL service and credentials
systemctl status mysql
mysql -u root -p s5_archlog_1_banking_depot -e "SHOW TABLES;"
```

#### User Validation Issues

```bash
# Error: "User does not exist"
# Solution: Verify test users exist in Java service
curl http://127.0.0.2:8080/api/users/1
curl http://127.0.0.2:8080/api/users/2
curl http://127.0.0.2:8080/api/users/3
```

## Test Customization

### Adding New Test Cases

**1. Create new test script:**

```bash
cp tests/1-test-type-management/type-crud-tests.sh tests/my-custom-test.sh
# Edit the script to add your test cases
```

**2. Add to main runner:**

```bash
# Edit run-all-tests.sh and add:
if run_test "$TESTS_DIR/my-custom-test.sh" "My Custom Tests"; then
    ((TESTS_PASSED++))
else
    ((TESTS_FAILED++))
fi
```

### Modifying Test Parameters

**Change API base URL:**

```bash
# Edit test scripts and modify:
BASE_URL="http://localhost:5001/api"  # For HTTPS
```

**Use different test users:**

```bash
# Edit account management tests:
USER_ID_1=5
USER_ID_2=6
USER_ID_3=7
```

**Adjust test amounts:**

```bash
# Modify account creation requests:
"montant": 500000.00  # Larger test amounts
```

## Automated Testing

### Continuous Integration

**Create CI script:**

```bash
#!/bin/bash
# ci-test.sh

# Start services
dotnet run --project banking-depot &
DOTNET_PID=$!

# Wait for service to start
sleep 10

# Run tests
cd tests
./run-all-tests.sh
TEST_RESULT=$?

# Cleanup
kill $DOTNET_PID

exit $TEST_RESULT
```

### Scheduled Testing

**Add to crontab for daily testing:**

```bash
crontab -e
# Add line:
0 2 * * * cd /path/to/banking-depot/tests && ./run-all-tests.sh > /tmp/banking-tests.log 2>&1
```

## Performance Testing

### Load Testing

```bash
# Test concurrent account creation
for i in {1..10}; do
  (curl -s -X POST "http://localhost:5000/api/comptesdepots" \
    -H "Content-Type: application/json" \
    -d '{"typeCompteDepotId":1,"userId":1,"dateEcheance":"2025-12-31T23:59:59","montant":10000.00}' &)
done
wait
```

### Response Time Testing

```bash
# Time API responses
time curl -s -X GET "http://localhost:5000/api/comptesdepots"
```

## Troubleshooting

### Debug Mode

```bash
# Run tests with verbose output
bash -x tests/run-all-tests.sh
```

### Manual API Testing

```bash
# Test individual endpoints
curl -v -X GET "http://localhost:5000/api/typecomptesdepots"
curl -v -X POST "http://localhost:5000/api/typecomptesdepots" \
  -H "Content-Type: application/json" \
  -d '{"nom":"Test Type","tauxInteret":0.02}'
```

### Log Analysis

```bash
# Check ASP.NET Core logs
dotnet run --project banking-depot > app.log 2>&1 &
tail -f app.log
```

## Best Practices

### Before Running Tests

1. ✅ Verify all services are running
2. ✅ Clear previous test data
3. ✅ Ensure test users exist in Java service
4. ✅ Check database connectivity

### During Testing

1. 📊 Monitor test output for errors
2. 📊 Verify mathematical calculations manually
3. 📊 Check database state changes
4. 📊 Validate integration with Java service

### After Testing

1. 🧹 Clean up test data
2. 🧹 Review any failed tests
3. 🧹 Document any issues found
4. 🧹 Update test scripts if needed

## Support

For issues with the test suite:

1. Check this guide for common solutions
2. Verify system prerequisites
3. Review test script comments
4. Check API documentation in README.md
5. Validate business logic in source code

The test suite is designed to be comprehensive and self-documenting. Each test script includes detailed output explaining what is being tested and what the expected results should be.
