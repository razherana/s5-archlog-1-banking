# Banking Loan System Test Structure Documentation

## Overview

This document describes the comprehensive test structure for the Banking Loan (Prêt) RESTful API. The test suite is organized into logical categories to ensure thorough coverage of all loan management functionalities and integration scenarios.

## Project Structure

```txt
banking-pret/
├── tests/
│   ├── 1-test-loan-types/
│   │   └── loan-types-tests.sh
│   ├── 2-test-loan-management/
│   │   └── loan-crud-tests.sh
│   ├── 3-test-payment-management/
│   │   └── payment-tests.sh
│   ├── 4-test-integration-scenarios/
│   │   └── full-loan-workflow-test.sh
│   ├── run-all-tests.sh
│   ├── clear_loans.sql
│   ├── TESTING-GUIDE.md
│   └── README.md (this file)
└── [other project files...]
```

## Test Categories

### 1. Loan Types Tests (`1-test-loan-types/`)

**Purpose**: Test loan type management and retrieval operations.

**Test Script**: `loan-types-tests.sh`

**Test Cases Covered**:

- ✅ Get all loan types (GET /api/comptes-pret/types)
- ✅ Verify loan type data structure (id, nom, interet)
- ✅ Test loan type availability for loan creation
- ✅ Validate interest rate formats and values

**Expected Outcomes**:

- Loan types retrieval returns all available types
- Each loan type contains proper fields with valid data
- Interest rates are in decimal format (e.g., 0.0500 for 5%)

### 2. Loan Management Tests (`2-test-loan-management/`)

**Purpose**: Test all loan account CRUD operations and validation.

**Test Script**: `loan-crud-tests.sh`

**Test Cases Covered**:

- ✅ Create new loan account (POST /api/comptes-pret)
- ✅ Get loan by ID (GET /api/comptes-pret/{id})
- ✅ Get loans by user ID (GET /api/comptes-pret/user/{userId})
- ✅ Test loan creation validation (amount, dates, user, type)
- ✅ Test 404 error for non-existent loan
- ✅ Test 400 error for invalid loan data
- ✅ Verify loan duration calculations
- ✅ Test monthly payment calculations

**Expected Outcomes**:

- Valid loan creation returns 201 status with loan data
- Loan retrieval operations return correct loan information
- Monthly payments calculated using amortization formula
- Invalid operations return appropriate error codes (400, 404)

### 3. Payment Management Tests (`3-test-payment-management/`)

**Purpose**: Test all payment-related operations and business logic.

**Test Script**: `payment-tests.sh`

**Test Cases Covered**:

- ✅ Make loan payment (POST /api/comptes-pret/make-payment)
- ✅ Get payment status (GET /api/comptes-pret/{id}/payment-status)
- ✅ Get payment history (GET /api/comptes-pret/{id}/payment-history)
- ✅ Test multiple payments in same month
- ✅ Test overpayment credit system
- ✅ Test payment validation (positive amounts, loan exists)
- ✅ Test payment status with actionDateTime parameter
- ✅ Test fully paid loan rejection
- ✅ Test backdated payments

**Expected Outcomes**:

- Payment creation returns 201 status with payment record
- Payment status shows correct total paid vs expected amounts
- Overpayments correctly credit toward future months
- Multiple payments per month are properly tracked
- Payment history shows chronological payment records

### 4. Integration Scenarios Tests (`4-test-integration-scenarios/`)

**Purpose**: Test complete loan lifecycle and complex business scenarios.

**Test Script**: `full-loan-workflow-test.sh`

**Test Cases Covered**:

- ✅ Complete loan lifecycle (create → payments → completion)
- ✅ Multi-user loan scenarios
- ✅ Different loan types with varying interest rates
- ✅ Payment schedule compliance testing
- ✅ Early loan completion scenarios
- ✅ Late payment scenarios
- ✅ Cross-month payment calculations
- ✅ Amortization schedule validation

**Expected Outcomes**:

- Complete loan workflows execute without errors
- Payment calculations remain consistent across scenarios
- Business rules are properly enforced
- System handles edge cases gracefully

## Business Logic Validation

### Amortization Formula Testing

The system uses the standard amortization formula:

```txt
M = [C × i] / [1 - (1 + i)^(-n)]
```

Where:

- M = Monthly payment amount
- C = Principal (loan amount)
- i = Monthly interest rate (annual rate / 12)
- n = Total number of payments (loan duration in months)

### Payment Logic Testing

The system implements a rolling credit system:

- **Expected Amount**: Based on months elapsed since loan start
- **Total Paid**: Sum of all payments made
- **Amount Due**: Max(0, Expected - Paid)
- **Overpayment Credit**: Applied to future months

## Running Tests

### Prerequisites

1. **Database Setup**: Ensure loan database is running with sample data
2. **Server Running**: TomEE server must be running on port 8080
3. **Dependencies**: `curl` and `jq` must be installed

### Individual Test Execution

```bash
# Run specific test category
cd tests/1-test-loan-types/
./loan-types-tests.sh

cd tests/2-test-loan-management/
./loan-crud-tests.sh

cd tests/3-test-payment-management/
./payment-tests.sh

cd tests/4-test-integration-scenarios/
./full-loan-workflow-test.sh
```

### Complete Test Suite

```bash
# Run all tests in sequence
cd tests/
./run-all-tests.sh
```

### Database Reset

```bash
# Clear test data between runs
mysql -u root -p s5_archlog_1_banking_pret < clear_loans.sql
```

## API Endpoints Tested

| Method | Endpoint                                 | Purpose             |
| ------ | ---------------------------------------- | ------------------- |
| GET    | `/api/comptes-pret/types`                | Get all loan types  |
| POST   | `/api/comptes-pret`                      | Create new loan     |
| GET    | `/api/comptes-pret/{id}`                 | Get loan by ID      |
| GET    | `/api/comptes-pret/user/{userId}`        | Get user's loans    |
| GET    | `/api/comptes-pret/{id}/payment-status`  | Get payment status  |
| GET    | `/api/comptes-pret/{id}/payment-history` | Get payment history |
| POST   | `/api/comptes-pret/make-payment`         | Make loan payment   |

## Test Data Requirements

### Loan Types

- At least 2-3 different loan types with varying interest rates
- Types should include common categories (etudiant, immobilier, personnel)

### Test Users

- Valid user IDs that exist in the system
- Multiple users for multi-user testing scenarios

### Sample Loan Scenarios

- Short-term loans (6-12 months) for quick testing
- Medium-term loans (24-36 months) for comprehensive testing
- Various loan amounts to test calculation precision

## Error Scenarios Tested

- **400 Bad Request**: Invalid data, missing fields, negative amounts
- **404 Not Found**: Non-existent loan IDs, invalid user IDs
- **409 Conflict**: Payments on fully paid loans
- **500 Server Error**: Database connection issues, calculation errors

## Success Criteria

All tests pass when:

1. ✅ All HTTP responses return expected status codes
2. ✅ JSON response structures match API specifications
3. ✅ Business calculations are mathematically correct
4. ✅ Payment tracking maintains data consistency
5. ✅ Error handling provides meaningful feedback
6. ✅ Integration scenarios complete successfully

---

_Last Updated: October 2025_
_Banking Loan System v1.0_
