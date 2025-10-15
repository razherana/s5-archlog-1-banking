# Banking Depot API Test Structure Documentation

## Overview

This document describes the comprehensive test structure for the Banking Depot RESTful API. The test suite is organized into logical categories to ensure thorough coverage of all API functionalities and integration scenarios for the ASP.NET Core deposit module.

## Project Structure

```txt
banking-depot/
├── tests/
│   ├── 1-test-type-management/
│   │   └── type-crud-tests.sh
│   ├── 2-test-account-management/
│   │   └── account-crud-tests.sh
│   ├── 3-test-withdrawal-management/
│   │   └── withdrawal-tests.sh
│   ├── 4-test-integration-scenarios/
│   │   └── full-workflow-test.sh
│   ├── 5-test-interest-calculation/
│   │   └── interest-calculation-tests.sh
│   ├── run-all-tests.sh
│   ├── setup_test_data.sql
│   ├── clear_data.sql
│   ├── README.md (this file)
│   └── TESTING-GUIDE.md
└── [other project files...]
```

## Test Categories

### 1. Type Management Tests (`1-test-type-management/`)

**Purpose**: Test all deposit account type-related CRUD operations and edge cases.

**Test Cases**:

- Create new deposit account types with various interest rates
- Get all account types
- Get account type by ID
- Update existing account types
- Delete account types
- Error handling for invalid data

**Prerequisites**: None

### 2. Account Management Tests (`2-test-account-management/`)

**Purpose**: Test deposit account CRUD operations and business rules.

**Test Cases**:

- Create deposit accounts for existing users
- Get all deposit accounts
- Get account by ID
- Get accounts by user ID
- Validate user existence through Java service integration
- Test business rule validations (maturity date, amounts)
- Error handling for invalid users, types, dates

**Prerequisites**:

- Java current account service running on `127.0.0.2:8080`
- Valid user IDs in the Java service
- Account types must exist

### 3. Withdrawal Management Tests (`3-test-withdrawal-management/`)

**Purpose**: Test withdrawal operations and maturity validations.

**Test Cases**:

- Successful withdrawals at maturity
- Blocked withdrawals before maturity
- Interest calculation on withdrawal
- Multiple withdrawal attempts (should fail)
- Withdrawal with backtracking dates
- Error handling for non-existent accounts

**Prerequisites**:

- Deposit accounts must exist
- Mix of matured and non-matured accounts

### 4. Integration Scenarios Tests (`4-test-integration-scenarios/`)

**Purpose**: Test end-to-end workflows combining multiple operations.

**Test Cases**:

- Complete deposit workflow: type creation → account creation → withdrawal
- User validation integration with Java service
- Multiple accounts per user scenario
- Historical operations with backtracking
- Error recovery scenarios

**Prerequisites**:

- Java current account service running
- Clean database state

### 5. Interest Calculation Tests (`5-test-interest-calculation/`)

**Purpose**: Test interest calculation accuracy and edge cases.

**Test Cases**:

- Simple interest calculation verification
- Different time periods (days, months, years)
- Various interest rates
- Interest preview without withdrawal
- Backtracking interest calculations
- Edge cases (same day maturity, leap years)

**Prerequisites**:

- Accounts with known parameters for calculation verification

## Test Execution

### Prerequisites

1. **ASP.NET Core Service**: The banking-depot service must be running on `http://localhost:5000` or `https://localhost:5001`
2. **Java Current Account Service**: Must be running on `http://127.0.0.2:8080` for user validation
3. **Database**: MySQL database `s5_archlog_1_banking_depot` must be accessible
4. **Dependencies**: `curl` and `jq` must be installed for JSON processing

### Setup

1. **Start Services**:

   ```bash
   # Start ASP.NET Core service
   cd banking-depot
   dotnet run

   # In another terminal, verify Java service is running
   curl http://127.0.0.2:8080/api/users/1
   ```

2. **Prepare Test Data**:

   ```bash
   cd tests
   mysql -u root -p s5_archlog_1_banking_depot < setup_test_data.sql
   ```

### Running Tests

#### Run All Tests

```bash
cd tests
chmod +x run-all-tests.sh
./run-all-tests.sh
```

#### Run Individual Test Suites

```bash
# Type management tests
cd 1-test-type-management
chmod +x type-crud-tests.sh
./type-crud-tests.sh

# Account management tests
cd 2-test-account-management
chmod +x account-crud-tests.sh
./account-crud-tests.sh

# Withdrawal tests
cd 3-test-withdrawal-management
chmod +x withdrawal-tests.sh
./withdrawal-tests.sh

# Integration tests
cd 4-test-integration-scenarios
chmod +x full-workflow-test.sh
./full-workflow-test.sh

# Interest calculation tests
cd 5-test-interest-calculation
chmod +x interest-calculation-tests.sh
./interest-calculation-tests.sh
```

### Cleanup

```bash
cd tests
mysql -u root -p s5_archlog_1_banking_depot < clear_data.sql
```

## API Endpoints Tested

### TypeComptesDepots Controller

- `GET /api/typecomptesdepots` - Get all account types
- `GET /api/typecomptesdepots/{id}` - Get account type by ID
- `POST /api/typecomptesdepots` - Create new account type
- `PUT /api/typecomptesdepots/{id}` - Update account type
- `DELETE /api/typecomptesdepots/{id}` - Delete account type

### ComptesDepots Controller

- `GET /api/comptesdepots` - Get all deposit accounts
- `GET /api/comptesdepots/{id}` - Get account by ID
- `GET /api/comptesdepots/user/{userId}` - Get accounts by user ID
- `POST /api/comptesdepots` - Create new deposit account
- `POST /api/comptesdepots/{id}/withdraw` - Withdraw from account
- `GET /api/comptesdepots/{id}/interest` - Calculate interest preview

## Expected Test Results

### Success Criteria

- All CRUD operations return appropriate HTTP status codes
- JSON responses match expected DTOs structure
- Business rules are properly enforced
- Interest calculations are mathematically correct
- Integration with Java service works seamlessly
- Error handling provides meaningful messages

### Common Issues

- **503 Service Unavailable**: Java service not running
- **400 Bad Request**: Invalid user ID (user doesn't exist in Java service)
- **400 Bad Request**: Withdrawal before maturity date
- **404 Not Found**: Non-existent account or type IDs
- **500 Internal Server Error**: Database connection issues

## Test Data

The test suite uses predefined test data including:

- Account types with various interest rates (1.5% to 5%)
- Test users (IDs 1, 2, 3) that must exist in Java service
- Sample deposit accounts with different maturity dates
- Historical withdrawn accounts for testing

## Integration Dependencies

This test suite validates integration with:

- **Java Current Account Service**: User validation at `127.0.0.2:8080/api/users/{id}`
- **MySQL Database**: Data persistence and retrieval
- **ASP.NET Core**: API endpoints and business logic

## Maintenance

- Update test data when business rules change
- Modify API endpoints when new features are added
- Ensure test user IDs remain valid in Java service
- Review interest calculation tests when formulas change
