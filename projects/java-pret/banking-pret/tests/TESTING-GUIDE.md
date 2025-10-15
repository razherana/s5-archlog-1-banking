# Loan System Testing Guide

This document provides comprehensive testing instructions for the Banking Loan System (banking-pret).

## Test Structure Overview

The loan system tests are organized into 4 main categories:

### 1. Loan Type Management (`1-test-loan-types/`)

- **Purpose**: Test loan type CRUD operations and validation
- **Coverage**: Create, read, update, delete loan types with different interest rates
- **Key Tests**: Input validation, duplicate handling, interest rate calculations

### 2. Loan Account Management (`2-test-loan-management/`)

- **Purpose**: Test loan account creation and lifecycle management
- **Coverage**: Loan creation, validation, status checking, account queries
- **Key Tests**: Amount validation, date handling, user association, loan calculations

### 3. Payment Management (`3-test-payment-management/`)

- **Purpose**: Test payment processing and amortization logic
- **Coverage**: Payment processing, overpayment handling, payment history, status queries
- **Key Tests**: Monthly payment calculations, rolling credit system, backdated payments

### 4. Integration Scenarios (`4-test-integration-scenarios/`)

- **Purpose**: End-to-end workflow testing with realistic scenarios
- **Coverage**: Complete loan lifecycles, multiple payment strategies, error handling
- **Key Tests**: Student loan workflow, business loan scenarios, edge cases

## Prerequisites

### 1. Database Setup

Ensure your MySQL database has the required tables:

```sql
-- Load the schema
mysql -u root -p your_database < /path/to/database/s5-banking-prog.sql
```

### 2. Application Server

Start the TomEE server:

```bash
cd projects/java-pret/banking-pret
mvn clean compile
mvn tomee:run
```

The server should be running on `http://localhost:8081`

### 3. Test Data Cleanup

Before running tests, clean existing data:

```bash
mysql -u root -p your_database < tests/clear_loans.sql
```

## Running Tests

### Run All Tests

```bash
cd tests/
./run-all-tests.sh
```

### Run Specific Test Categories

```bash
# Loan type tests
./1-test-loan-types/loan-type-tests.sh

# Loan management tests
./2-test-loan-management/loan-crud-tests.sh

# Payment management tests
./3-test-payment-management/payment-tests.sh

# Integration scenarios
./4-test-integration-scenarios/full-loan-workflow-test.sh
```

## Test Data Requirements

### Default Loan Types

The system expects these loan types to be available:

- **Personal Loan** (5.5% annual interest)
- **Student Loan** (3.2% annual interest)
- **Business Loan** (7.8% annual interest)
- **Home Loan** (4.1% annual interest)

### Test Users

Tests create temporary users with predictable data:

- Email patterns: `test-user-{scenario}@example.com`
- Names: `Test User {Number}`

## Understanding Test Results

### Success Indicators

- ✅ HTTP 200/201 responses for valid operations
- ✅ Correct calculation results (payments, balances)
- ✅ Proper error handling for invalid inputs
- ✅ Expected JSON response structures

### Common Issues

- **404 Errors**: Check if application server is running
- **Database Errors**: Verify database connection and schema
- **Calculation Mismatches**: Review amortization formula implementation
- **JSON Parse Errors**: Check API response format

## Amortization Formula Validation

The system uses the standard loan payment formula:

```txt
M = [C × i] / [1 - (1 + i)^(-n)]
```

Where:

- M = Monthly payment
- C = Principal amount
- i = Monthly interest rate (annual rate / 12)
- n = Total number of payments

### Example Calculation

For a €10,000 loan at 5.5% annual interest over 2 years:

- Monthly rate: 5.5% / 12 = 0.0045833
- Number of payments: 24
- Monthly payment: €448.07

## Rolling Credit System

The payment system follows these rules:

1. **Regular Payments**: Applied to current month's obligation
2. **Overpayments**: Credited toward future months
3. **Underpayments**: Create payment arrears
4. **Payment History**: All payments tracked with dates and amounts

## API Endpoint Testing

### Base URL

All endpoints use base: `http://localhost:8081/banking-pret/api`

### Key Endpoints

- `POST /loan-types` - Create loan type
- `GET /loan-types` - List all loan types
- `POST /comptes-pret` - Create loan account
- `GET /comptes-pret/{id}` - Get loan details
- `POST /comptes-pret/make-payment` - Process payment
- `GET /comptes-pret/{id}/payment-status` - Check payment status

## Debugging Test Failures

### Check Server Logs

```bash
tail -f target/apache-tomee/logs/catalina.out
```

### Verify Database State

```sql
-- Check loan accounts
SELECT * FROM compte_prets ORDER BY id DESC LIMIT 10;

-- Check recent payments
SELECT * FROM echeance ORDER BY dateEcheance DESC LIMIT 10;

-- Check loan types
SELECT * FROM type_compte_pret;
```

### Manual API Testing

Use curl to manually test endpoints:

```bash
# Test server health
curl -X GET http://localhost:8081/banking-pret/api/loan-types

# Create test loan type
curl -X POST http://localhost:8081/banking-pret/api/loan-types \
  -H "Content-Type: application/json" \
  -d '{"nom":"Test Loan","interet":0.055}'
```

## Best Practices

1. **Always clean test data** before running tests
2. **Run tests in order** (types → loans → payments → integration)
3. **Check server logs** for detailed error information
4. **Validate calculations** against external tools when debugging
5. **Use realistic test data** that matches production scenarios

## Troubleshooting Common Issues

### Port Conflicts

If port 8081 is in use, update TomEE configuration or stop conflicting services.

### Database Connection Issues

Verify MySQL is running and connection details in `resources.xml` are correct.

### Test Script Permissions

Ensure all test scripts are executable:

```bash
find tests/ -name "*.sh" -exec chmod +x {} \;
```

## Contact & Support

For issues with the loan system or test framework:

1. Check server logs for detailed error messages
2. Verify database schema matches expectations
3. Review API documentation in source code comments
4. Compare with working java-courant implementation patterns
