# Testing Guide - Updated Architecture

## Overview

The tests have been updated to work with the new architecture where user management is handled by the `java-interface` service.

## Key Changes

### ✅ **What Changed:**

- **No User Creation**: Tests no longer create users via API calls
- **Fake User IDs**: Tests use hardcoded user IDs (1, 2, 3, etc.)
- **Assumption**: Users with these IDs exist in the central user service
- **Simplified Setup**: No need to manage user data locally

### 🔧 **How Tests Work Now:**

#### User ID Assignments

- **User ID 1**: General account testing
- **User ID 2**: Father (John Doe) - Integration tests
- **User ID 3**: Mother (Jane Doe) - Integration tests
- **User ID 4**: Child (Alice Doe) - Integration tests
- **User ID 5**: Transaction testing
- **User ID 6**: Tax functionality testing
- **User ID 99999**: Edge case testing

#### Database Setup

```sql
-- Only clear account-related data
DELETE FROM transaction_courants;
DELETE FROM compte_courants;
-- Users are managed by java-interface service
```

## Running Tests

### 1. **Start the Server:**

```bash
cd banking-courant
mvn tomee:run
```

### 2. **Clear Data (Optional):**

```bash
# Execute clear_comptes.sql if needed
mysql -u root -p banking_db < tests/clear_comptes.sql
```

### 3. **Run All Tests:**

```bash
cd tests
./run-all-tests.sh
```

### 4. **Run Individual Test Suites:**

```bash
# Account management
./2-test-account-management/account-crud-tests.sh

# Transactions
./3-test-transaction-management/transaction-tests.sh

# Integration scenarios
./4-test-integration-scenarios/full-workflow-test.sh

# Tax functionality
./5-test-tax-functionality/tax-tests.sh
```

## Test Suite Summary

| Test Suite             | User IDs Used | Purpose                |
| ---------------------- | ------------- | ---------------------- |
| Account Management     | 1, 99999      | Basic CRUD operations  |
| Transaction Management | 5             | Transaction operations |
| Integration Scenarios  | 2, 3, 4       | Full workflow testing  |
| Tax Functionality      | 6             | Tax calculations       |

## Future Integration

When the `java-interface` service is implemented:

1. **No test changes needed** - user IDs remain the same
2. **User validation** will happen automatically via `findUser()` method
3. **Real user data** will be fetched from central service
4. **Tests continue to work** without modification

## Notes

- **Server Health Check**: Changed from `/users` to `/comptes` endpoint
- **Error Handling**: Account creation assumes users exist (no 404 for missing users)
- **Cleanup**: Only compte and transaction data is cleared between test runs
