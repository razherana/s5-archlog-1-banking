# Banking API Test Structure - Quick Reference

## 🏗️ Directory Structure

```
tests/
├── 1-test-user-management/          # User CRUD operations
│   └── user-crud-tests.sh
├── 2-test-account-management/       # Account CRUD operations  
│   └── account-crud-tests.sh
├── 3-test-transaction-management/   # Transaction operations (deposit/withdrawal/transfer)
│   └── transaction-tests.sh
├── 4-test-integration-scenarios/    # End-to-end workflow tests
│   └── full-workflow-test.sh
├── run-all-tests.sh                 # Master test runner
└── README.md                        # Comprehensive documentation
```

## 🚀 Quick Start

### Prerequisites
- TomEE server running on port 8080
- `curl` command available
- Optional: `jq` for JSON formatting

### Run All Tests
```bash
cd banking-courant/tests
./run-all-tests.sh
```

### Run Individual Test Suites
```bash
./1-test-user-management/user-crud-tests.sh
./2-test-account-management/account-crud-tests.sh  
./3-test-transaction-management/transaction-tests.sh
./4-test-integration-scenarios/full-workflow-test.sh
```

## 📊 Test Coverage

| Test Suite | Purpose | Key Features |
|------------|---------|--------------|
| **User Management** | Test user CRUD operations | Create, read, update, delete users + error handling |
| **Account Management** | Test account operations | Create accounts, link to users, retrieve by various criteria |
| **Transaction Management** | Test banking transactions | Deposits, withdrawals, transfers, balance verification |
| **Integration Scenarios** | End-to-end workflows | Complete family banking scenario with realistic transactions |

## 🔧 How Tests Work

1. **Self-Contained**: Each test creates its own data
2. **RESTful**: Uses curl to call API endpoints
3. **Error-Aware**: Tests both success and failure scenarios
4. **Realistic**: Uses meaningful test data and scenarios
5. **Independent**: Tests can run in any order

## 📋 Test Patterns

### Standard Test Flow
1. **Setup**: Create required test data (users, accounts)
2. **Execute**: Perform API operations
3. **Verify**: Check responses and data integrity
4. **Report**: Show results with ✅/❌ indicators

### Error Testing
- 400 Bad Request (invalid data)
- 404 Not Found (non-existent resources)  
- Business rule violations (insufficient funds, etc.)

## 🎯 For Agent Handoff

### Understanding the Tests
- Each numbered directory represents a test category
- Scripts are bash files using curl for HTTP requests
- Tests output structured results with clear success/failure indicators
- Full documentation is in `tests/README.md`

### Modifying Tests
- Follow existing naming patterns: `N-test-category-name/`
- Include comprehensive error handling
- Use descriptive test data names
- Update `run-all-tests.sh` for new test suites

### Expected Outputs
- ✅ Success indicators for passing tests
- ❌ Error indicators for failing tests  
- Clear step-by-step execution logs
- Summary reports with pass/fail counts

## 🔍 Key Files to Review

1. **`README.md`** - Complete documentation
2. **`run-all-tests.sh`** - Master test runner with example execution flow
3. **`4-test-integration-scenarios/full-workflow-test.sh`** - Most comprehensive test example
4. **`1-test-user-management/user-crud-tests.sh`** - Simple CRUD test pattern

## 🚨 Common Issues

- **Server not running**: Start with `mvn tomee:run`
- **Permission denied**: Run `chmod +x *.sh` 
- **JSON formatting**: Install `jq` or ignore formatting errors
- **Test failures**: Check server logs and API responses

---

This test structure provides comprehensive API validation for the Banking Courant application. All tests are documented, executable, and designed for easy maintenance and extension.
