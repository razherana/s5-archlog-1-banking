# Banking API Test Structure Documentation

## Overview

This document describes the comprehensive test structure for the Banking Courant RESTful API. The test suite is organized into logical categories to ensure thorough coverage of all API functionalities and integration scenarios.

## Project Structure

```
banking-courant/
├── tests/
│   ├── 1-test-user-management/
│   │   └── user-crud-tests.sh
│   ├── 2-test-account-management/
│   │   └── account-crud-tests.sh
│   ├── 3-test-transaction-management/
│   │   └── transaction-tests.sh
│   ├── 4-test-integration-scenarios/
│   │   └── full-workflow-test.sh
│   ├── run-all-tests.sh
│   └── README.md (this file)
└── [other project files...]
```

## Test Categories

### 1. User Management Tests (`1-test-user-management/`)

**Purpose**: Test all user-related CRUD operations and edge cases.

**Test Script**: `user-crud-tests.sh`

**Test Cases Covered**:
- ✅ Create new user (POST /api/users)
- ✅ Get all users (GET /api/users)
- ✅ Get user by ID (GET /api/users/{id})
- ✅ Get user by email (GET /api/users/email/{email})
- ✅ Update user (PUT /api/users/{id})
- ✅ Test 404 error for non-existent user
- ✅ Test 400 error for invalid user data

**Expected Outcomes**:
- Valid user creation returns 201 status with user data
- User retrieval operations return correct user information
- Invalid operations return appropriate error codes (400, 404)
- User updates modify existing records correctly

### 2. Account Management Tests (`2-test-account-management/`)

**Purpose**: Test compte courant (current account) operations and relationships with users.

**Test Script**: `account-crud-tests.sh`

**Test Cases Covered**:
- ✅ Create account for existing user (POST /api/comptes/user/{userId})
- ✅ Get all accounts (GET /api/comptes)
- ✅ Get account by ID (GET /api/comptes/{id})
- ✅ Get account by account number (GET /api/comptes/numero/{numeroCompte})
- ✅ Get all accounts for specific user (GET /api/comptes/user/{userId})
- ✅ Create multiple accounts for same user
- ✅ Test 404 error for non-existent user
- ✅ Test 404 error for non-existent account

**Expected Outcomes**:
- Account creation generates unique account numbers
- Initial account balance is 0.00
- Accounts are properly linked to users
- Multiple accounts per user are supported
- Proper error handling for invalid requests

### 3. Transaction Management Tests (`3-test-transaction-management/`)

**Purpose**: Test all transaction types (deposit, withdrawal, transfer) and business logic.

**Test Script**: `transaction-tests.sh`

**Test Cases Covered**:
- ✅ Make deposit (POST /api/transactions/depot)
- ✅ Make withdrawal (POST /api/transactions/retrait)
- ✅ Make transfer between accounts (POST /api/transactions/transfert)
- ✅ Get all transactions (GET /api/transactions)
- ✅ Get transactions for specific account (GET /api/transactions/compte/{compteId})
- ✅ Verify account balance updates after transactions
- ✅ Test insufficient funds error (400)
- ✅ Test transaction on non-existent account (404)

**Expected Outcomes**:
- Deposits increase account balance correctly
- Withdrawals decrease account balance correctly
- Transfers move money between accounts atomically
- Insufficient funds are properly rejected
- Transaction history is maintained accurately
- Account balances reflect all completed transactions

### 4. Integration Scenarios Tests (`4-test-integration-scenarios/`)

**Purpose**: Test complete end-to-end workflows that simulate real-world banking scenarios.

**Test Script**: `full-workflow-test.sh`

**Test Scenario**: Family Banking Setup
- 👤 Create family members (father, mother, child)
- 🏦 Create multiple accounts (personal, business, savings)
- 💰 Perform realistic transaction sequence:
  - Salary deposits
  - Inter-family transfers
  - Business transactions
  - Cash withdrawals
  - Bill payments
- 📊 Verify final balances and transaction integrity

**Expected Outcomes**:
- Complete workflow executes without errors
- All balances sum correctly (conservation of money)
- Transaction history is complete and accurate
- Business rules are enforced throughout the workflow

## Test Execution

### Prerequisites

1. **Server Running**: Ensure TomEE server is running with the banking application deployed
   ```bash
   cd banking-courant
   mvn tomee:run
   ```

2. **Dependencies**: Tests require `curl` and optionally `jq` for JSON formatting
   ```bash
   # Install jq for better JSON output (optional)
   sudo apt-get install jq
   ```

### Running Individual Test Suites

```bash
# Navigate to tests directory
cd banking-courant/tests

# Run specific test suite
./1-test-user-management/user-crud-tests.sh
./2-test-account-management/account-crud-tests.sh
./3-test-transaction-management/transaction-tests.sh
./4-test-integration-scenarios/full-workflow-test.sh
```

### Running All Tests

```bash
# Navigate to tests directory
cd banking-courant/tests

# Make test runner executable and run
chmod +x run-all-tests.sh
./run-all-tests.sh
```

### Test Output Format

Each test script provides:
- 📋 Test description and purpose
- 🔍 Server connectivity check
- 📊 Step-by-step execution with clear labeling
- ✅/❌ Success/failure indicators
- 📈 Expected vs actual results comparison
- 🎉 Summary of test results

## Test Data Management

### Data Isolation
- Each test suite creates its own test data
- Tests are designed to be independent and can run in any order
- No cleanup is required between test runs (fresh data each time)

### Test Data Patterns
- **Users**: Named with descriptive prefixes (e.g., "Transaction Test User")
- **Emails**: Use distinct domains (e.g., `family.com`, `example.com`)
- **Amounts**: Use realistic monetary values for better test readability

## Error Handling Testing

### HTTP Status Code Validation
- **200 OK**: Successful GET requests
- **201 Created**: Successful POST requests
- **204 No Content**: Successful DELETE requests
- **400 Bad Request**: Invalid request data, business rule violations
- **404 Not Found**: Non-existent resources
- **500 Internal Server Error**: Server-side errors

### Business Rule Testing
- Insufficient funds for withdrawals
- Negative amounts rejection
- Invalid account numbers
- Non-existent user references

## Extending the Test Suite

### Adding New Test Categories

1. Create new directory with naming pattern: `N-test-category-name/`
2. Create test script following existing patterns
3. Update `run-all-tests.sh` to include new test
4. Document new tests in this README

### Test Script Best Practices

1. **Consistent Structure**:
   - Header with test purpose
   - Server connectivity check
   - Clear step numbering
   - Error handling
   - Results summary

2. **Data Management**:
   - Create fresh test data
   - Use descriptive names
   - Extract IDs/numbers for reuse
   - Provide fallback values

3. **Output Formatting**:
   - Use emojis for visual clarity
   - Include expected vs actual comparisons
   - Format JSON output when possible
   - Provide meaningful error messages

4. **Error Resilience**:
   - Handle missing `jq` gracefully
   - Provide fallback values for extracted data
   - Check server availability before testing
   - Continue testing even if one test fails

## API Endpoints Reference

### User Management
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/email/{email}` - Get user by email
- `POST /api/users` - Create new user
- `PUT /api/users/{id}` - Update user
- `DELETE /api/users/{id}` - Delete user

### Account Management
- `GET /api/comptes` - Get all accounts
- `GET /api/comptes/{id}` - Get account by ID
- `GET /api/comptes/numero/{numeroCompte}` - Get account by number
- `GET /api/comptes/user/{userId}` - Get accounts for user
- `POST /api/comptes/user/{userId}` - Create account for user
- `DELETE /api/comptes/{id}` - Delete account

### Transaction Management
- `GET /api/transactions` - Get all transactions
- `GET /api/transactions/{id}` - Get transaction by ID
- `GET /api/transactions/compte/{compteId}` - Get transactions for account
- `POST /api/transactions/depot` - Make deposit
- `POST /api/transactions/retrait` - Make withdrawal
- `POST /api/transactions/transfert` - Transfer between accounts

## Troubleshooting

### Common Issues

1. **Server Not Running**
   - Error: "Server is not running or not accessible"
   - Solution: Start TomEE server with `mvn tomee:run`

2. **Permission Denied**
   - Error: "Permission denied" when running scripts
   - Solution: Make scripts executable with `chmod +x script-name.sh`

3. **JSON Parsing Errors**
   - Error: "jq: command not found"
   - Solution: Install jq or tests will fall back to raw JSON output

4. **Port Conflicts**
   - Error: Address already in use
   - Solution: Check if another service is using port 8080, or modify server configuration

### Debugging Tips

1. **Verbose Output**: Add `-v` flag to curl commands for detailed HTTP information
2. **Server Logs**: Check TomEE logs for server-side errors
3. **Step-by-Step**: Run individual test scripts to isolate issues
4. **Manual Testing**: Use tools like Postman or curl directly to verify API behavior

## Contributing

When adding new tests or modifying existing ones:

1. Follow the established naming conventions
2. Include comprehensive error handling
3. Add documentation for new test scenarios
4. Test your changes with both success and failure cases
5. Update this README with any new test categories or significant changes

---

This test structure provides comprehensive coverage of the Banking Courant API, ensuring reliability and correctness of all banking operations. The modular design allows for easy maintenance and extension as the API evolves.
