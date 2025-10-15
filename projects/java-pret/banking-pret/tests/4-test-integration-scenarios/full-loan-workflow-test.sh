#!/bin/bash

# Full Loan Workflow Integration Tests
echo "=== Full Loan Workflow Integration Tests ==="

BASE_URL="http://localhost:8080/api"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counter
TESTS_PASSED=0
TESTS_FAILED=0

# Helper function to check HTTP status
check_status() {
    local expected=$1
    local actual=$2
    local test_name=$3
    
    if [ "$actual" -eq "$expected" ]; then
        echo -e "${GREEN}✅ PASS${NC}: $test_name (Status: $actual)"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ FAIL${NC}: $test_name (Expected: $expected, Got: $actual)"
        ((TESTS_FAILED++))
    fi
}

echo ""
echo -e "${BLUE}=== Scenario 1: Complete Student Loan Lifecycle ===${NC}"
echo "Creating student loan, making regular payments, and completing early..."

# 1. Create student loan (12 months, 3% interest)
echo ""
echo "1.1 Creating student loan..."
response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/comptes-pret" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "typeComptePretId": 1,
    "montant": 6000.00,
    "dateDebut": "2025-01-01T00:00:00",
    "dateFin": "2025-12-31T23:59:59"
  }')

status_code="${response: -3}"
response_body="${response%???}"

check_status 201 "$status_code" "Create student loan"

STUDENT_LOAN_ID=""
if [ "$status_code" -eq 201 ]; then
    STUDENT_LOAN_ID=$(echo "$response_body" | jq -r '.id' 2>/dev/null)
    echo "Student loan created with ID: $STUDENT_LOAN_ID"
fi

# 1.2 Check initial payment status
echo ""
echo "1.2 Checking initial payment status..."
response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/comptes-pret/$STUDENT_LOAN_ID/payment-status" \
  -H "Content-Type: application/json")

status_code="${response: -3}"
response_body="${response%???}"

check_status 200 "$status_code" "Get initial payment status"

if [ "$status_code" -eq 200 ]; then
    monthly_payment=$(echo "$response_body" | jq -r '.monthlyPayment' 2>/dev/null)
    echo "Monthly payment calculated: $monthly_payment"
fi

# 1.3 Make regular monthly payments
echo ""
echo "1.3 Making regular monthly payments..."

for month in {1..6}; do
    date="2025-0${month}-15T10:00:00"
    if [ $month -gt 9 ]; then
        date="2025-${month}-15T10:00:00"
    fi
    
    response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/comptes-pret/make-payment" \
      -H "Content-Type: application/json" \
      -d "{
        \"compteId\": $STUDENT_LOAN_ID,
        \"montant\": 520.00,
        \"actionDateTime\": \"$date\"
      }")
    
    status_code="${response: -3}"
    check_status 201 "$status_code" "Monthly payment $month"
done

# 1.4 Check payment status after 6 months
echo ""
echo "1.4 Checking payment status after 6 months..."
response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/comptes-pret/$STUDENT_LOAN_ID/payment-status?actionDateTime=2025-06-30T23:59:59" \
  -H "Content-Type: application/json")

status_code="${response: -3}"
response_body="${response%???}"

check_status 200 "$status_code" "Payment status after 6 months"

if [ "$status_code" -eq 200 ]; then
    echo "Payment status after 6 months:"
    echo "$response_body" | jq '.' 2>/dev/null
    
    total_paid=$(echo "$response_body" | jq -r '.totalPaid' 2>/dev/null)
    echo "Total paid so far: $total_paid"
fi

# 1.5 Make large payment to complete loan early
echo ""
echo "1.5 Making large payment to complete loan early..."
response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/comptes-pret/make-payment" \
  -H "Content-Type: application/json" \
  -d "{
    \"compteId\": $STUDENT_LOAN_ID,
    \"montant\": 3000.00,
    \"actionDateTime\": \"2025-07-01T10:00:00\"
  }")

status_code="${response: -3}"
check_status 201 "$status_code" "Large completion payment"

# 1.6 Verify loan is fully paid
echo ""
echo "1.6 Verifying loan completion..."
response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/comptes-pret/$STUDENT_LOAN_ID/payment-status" \
  -H "Content-Type: application/json")

status_code="${response: -3}"
response_body="${response%???}"

check_status 200 "$status_code" "Final payment status"

if [ "$status_code" -eq 200 ]; then
    is_fully_paid=$(echo "$response_body" | jq -r '.isFullyPaid' 2>/dev/null)
    if [ "$is_fully_paid" = "true" ]; then
        echo -e "${GREEN}✅ PASS${NC}: Loan is fully paid"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ FAIL${NC}: Loan is not marked as fully paid"
        ((TESTS_FAILED++))
    fi
fi

echo ""
echo -e "${BLUE}=== Scenario 2: Multiple Payment Strategy ===${NC}"
echo "Testing multiple small payments within the same month..."

# 2.1 Create another loan
echo ""
echo "2.1 Creating test loan for multiple payments..."
response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/comptes-pret" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "typeComptePretId": 2,
    "montant": 5000.00,
    "dateDebut": "2025-01-01T00:00:00",
    "dateFin": "2025-06-30T23:59:59"
  }')

status_code="${response: -3}"
response_body="${response%???}"

check_status 201 "$status_code" "Create test loan for multiple payments"

MULTI_PAYMENT_LOAN_ID=""
if [ "$status_code" -eq 201 ]; then
    MULTI_PAYMENT_LOAN_ID=$(echo "$response_body" | jq -r '.id' 2>/dev/null)
    echo "Multi-payment test loan created with ID: $MULTI_PAYMENT_LOAN_ID"
fi

# 2.2 Make multiple small payments in January
echo ""
echo "2.2 Making multiple payments in January..."

payment_amounts=(200 150 300 250 100)
for i in "${!payment_amounts[@]}"; do
    day=$((5 + i * 5))
    amount=${payment_amounts[$i]}
    
    response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/comptes-pret/make-payment" \
      -H "Content-Type: application/json" \
      -d "{
        \"compteId\": $MULTI_PAYMENT_LOAN_ID,
        \"montant\": $amount.00,
        \"actionDateTime\": \"2025-01-${day}T10:00:00\"
      }")
    
    status_code="${response: -3}"
    check_status 201 "$status_code" "Multiple payment $((i+1)) ($amount)"
done

# 2.3 Check payment history
echo ""
echo "2.3 Checking payment history..."
response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/comptes-pret/$MULTI_PAYMENT_LOAN_ID/payment-history" \
  -H "Content-Type: application/json")

status_code="${response: -3}"
response_body="${response%???}"

check_status 200 "$status_code" "Get payment history for multiple payments"

if [ "$status_code" -eq 200 ]; then
    payment_count=$(echo "$response_body" | jq 'length' 2>/dev/null || echo 0)
    if [ "$payment_count" -eq 5 ]; then
        echo -e "${GREEN}✅ PASS${NC}: All 5 payments recorded"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ FAIL${NC}: Payment count incorrect (Expected: 5, Got: $payment_count)"
        ((TESTS_FAILED++))
    fi
fi

echo ""
echo -e "${BLUE}=== Scenario 3: Different Loan Types Comparison ===${NC}"
echo "Creating loans with different interest rates and comparing calculations..."

# 3.1 Create personal loan (higher interest)
echo ""
echo "3.1 Creating personal loan (higher interest)..."
response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/comptes-pret" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 2,
    "typeComptePretId": 3,
    "montant": 10000.00,
    "dateDebut": "2025-01-01T00:00:00",
    "dateFin": "2026-01-01T00:00:00"
  }')

status_code="${response: -3}"
response_body="${response%???}"

check_status 201 "$status_code" "Create personal loan"

PERSONAL_LOAN_ID=""
if [ "$status_code" -eq 201 ]; then
    PERSONAL_LOAN_ID=$(echo "$response_body" | jq -r '.id' 2>/dev/null)
    echo "Personal loan created with ID: $PERSONAL_LOAN_ID"
fi

# 3.2 Compare monthly payments between loan types
echo ""
echo "3.2 Comparing monthly payments..."

if [ -n "$STUDENT_LOAN_ID" ]; then
    response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/comptes-pret/$STUDENT_LOAN_ID/payment-status" \
      -H "Content-Type: application/json")
    status_code="${response: -3}"
    response_body="${response%???}"
    if [ "$status_code" -eq 200 ]; then
        student_monthly=$(echo "$response_body" | jq -r '.monthlyPayment' 2>/dev/null)
        echo "Student loan monthly payment: $student_monthly"
    fi
fi

if [ -n "$PERSONAL_LOAN_ID" ]; then
    response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/comptes-pret/$PERSONAL_LOAN_ID/payment-status" \
      -H "Content-Type: application/json")
    status_code="${response: -3}"
    response_body="${response%???}"
    if [ "$status_code" -eq 200 ]; then
        personal_monthly=$(echo "$response_body" | jq -r '.monthlyPayment' 2>/dev/null)
        echo "Personal loan monthly payment: $personal_monthly"
    fi
fi

echo ""
echo -e "${BLUE}=== Scenario 4: Error Handling Validation ===${NC}"
echo "Testing various error conditions..."

# 4.1 Try to pay on fully paid loan
echo ""
echo "4.1 Testing payment on fully paid loan..."
if [ -n "$STUDENT_LOAN_ID" ]; then
    response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/comptes-pret/make-payment" \
      -H "Content-Type: application/json" \
      -d "{
        \"compteId\": $STUDENT_LOAN_ID,
        \"montant\": 100.00,
        \"actionDateTime\": \"2025-08-01T10:00:00\"
      }")
    
    status_code="${response: -3}"
    check_status 400 "$status_code" "Payment on fully paid loan"
fi

# 4.2 Test invalid date formats
echo ""
echo "4.2 Testing invalid date format..."
response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/comptes-pret/$PERSONAL_LOAN_ID/payment-status?actionDateTime=invalid-date" \
  -H "Content-Type: application/json")

status_code="${response: -3}"
check_status 400 "$status_code" "Invalid date format"

echo ""
echo "=== Integration Test Summary ==="
echo -e "Tests Passed: ${GREEN}$TESTS_PASSED${NC}"
echo -e "Tests Failed: ${RED}$TESTS_FAILED${NC}"
echo -e "Total Tests: $((TESTS_PASSED + TESTS_FAILED))"

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "\n${GREEN}🎉 All integration tests passed!${NC}"
    echo -e "${BLUE}💡 Loan system is working correctly across all scenarios!${NC}"
    exit 0
else
    echo -e "\n${RED}💥 Some integration tests failed!${NC}"
    exit 1
fi