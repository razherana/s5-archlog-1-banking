#!/bin/bash

# Payment Management Tests
echo "=== Payment Management Tests ==="

BASE_URL="http://127.0.0.3:8080/api"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
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
echo "Testing Payment Management..."
echo "============================"

# Try to get loan ID from previous test
TEST_LOAN_ID=""
echo "No loan ID found from previous test, creating a new loan..."

# Create a test loan
response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/comptes-pret" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "typeComptePretId": 1,
    "montant": 12000.00,
    "dateDebut": "2025-01-01T00:00:00",
    "dateFin": "2026-01-01T00:00:00"
  }')

status_code="${response: -3}"
response_body="${response%???}"

if [ "$status_code" -eq 201 ]; then
    TEST_LOAN_ID=$(echo "$response_body" | jq -r '.id' 2>/dev/null)
    echo "Created test loan with ID: $TEST_LOAN_ID"
else
    echo -e "${RED}❌ FAIL${NC}: Could not create test loan"
    exit 1
fi

echo ""
echo "1. Get Initial Payment Status"
response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/comptes-pret/$TEST_LOAN_ID/payment-status" \
  -H "Content-Type: application/json")

status_code="${response: -3}"
response_body="${response%???}"

check_status 200 "$status_code" "Get initial payment status"

if [ "$status_code" -eq 200 ]; then
    echo "Initial payment status:"
    echo "$response_body" | jq '.' 2>/dev/null || echo "Invalid JSON response"
    
    # Validate payment status structure
    if echo "$response_body" | jq -e 'has("totalPaid") and has("totalExpected") and has("amountDue") and has("monthlyPayment")' >/dev/null 2>&1; then
        echo -e "${GREEN}✅ PASS${NC}: Payment status has required fields"
        ((TESTS_PASSED++))
        
        # Check initial values
        total_paid=$(echo "$response_body" | jq -r '.totalPaid' 2>/dev/null)
        
        # amount_due=$(echo "$response_body" | jq -r '.amountDue' 2>/dev/null)
        
        if [ "$total_paid" = "0" ] || [ "$total_paid" = "0.00" ]; then
            echo -e "${GREEN}✅ PASS${NC}: Initial total paid is zero"
            ((TESTS_PASSED++))
        else
            echo -e "${RED}❌ FAIL${NC}: Initial total paid is not zero ($total_paid)"
            ((TESTS_FAILED++))
        fi
    else
        echo -e "${RED}❌ FAIL${NC}: Payment status missing required fields"
        ((TESTS_FAILED++))
    fi
fi

echo ""
echo "2. Get Initial Payment History"
response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/comptes-pret/$TEST_LOAN_ID/payment-history" \
  -H "Content-Type: application/json")

status_code="${response: -3}"
response_body="${response%???}"

check_status 200 "$status_code" "Get initial payment history"

if [ "$status_code" -eq 200 ]; then
    echo "Initial payment history:"
    echo "$response_body" | jq '.' 2>/dev/null || echo "Invalid JSON response"
    
    # Check if response is an empty array
    if echo "$response_body" | jq -e 'type == "array" and length == 0' >/dev/null 2>&1; then
        echo -e "${GREEN}✅ PASS${NC}: Initial payment history is empty array"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ FAIL${NC}: Initial payment history is not empty"
        ((TESTS_FAILED++))
    fi
fi

echo ""
echo "3. Make First Payment"
response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/comptes-pret/make-payment" \
  -H "Content-Type: application/json" \
  -d "{
    \"compteId\": $TEST_LOAN_ID,
    \"montant\": 500.00,
    \"actionDateTime\": \"2025-02-01T10:00:00\"
  }")

status_code="${response: -3}"
response_body="${response%???}"

check_status 201 "$status_code" "Make first payment"

PAYMENT_ID=""
if [ "$status_code" -eq 201 ]; then
    echo "Payment response:"
    echo "$response_body" | jq '.' 2>/dev/null || echo "Invalid JSON response"
    
    # Extract payment ID
    PAYMENT_ID=$(echo "$response_body" | jq -r '.id' 2>/dev/null)
    if [ "$PAYMENT_ID" != "null" ] && [ -n "$PAYMENT_ID" ]; then
        echo -e "${GREEN}✅ PASS${NC}: Payment created with ID: $PAYMENT_ID"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ FAIL${NC}: No payment ID returned"
        ((TESTS_FAILED++))
    fi
    
    # Validate payment structure
    if echo "$response_body" | jq -e 'has("id") and has("compteId") and has("montant") and has("dateEcheance")' >/dev/null 2>&1; then
        echo -e "${GREEN}✅ PASS${NC}: Payment response has required fields"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ FAIL${NC}: Payment response missing required fields"
        ((TESTS_FAILED++))
    fi
fi

echo ""
echo "4. Check Payment Status After First Payment"
response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/comptes-pret/$TEST_LOAN_ID/payment-status" \
  -H "Content-Type: application/json")

status_code="${response: -3}"
response_body="${response%???}"

check_status 200 "$status_code" "Get payment status after first payment"

if [ "$status_code" -eq 200 ]; then
    echo "Payment status after first payment:"
    echo "$response_body" | jq '.' 2>/dev/null || echo "Invalid JSON response"
    
    total_paid=$(echo "$response_body" | jq -r '.totalPaid' 2>/dev/null)
    if [ "$total_paid" = "500.00" ] || [ "$total_paid" = "500" ]; then
        echo -e "${GREEN}✅ PASS${NC}: Total paid updated to $total_paid"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ FAIL${NC}: Total paid not updated correctly (Expected: 500.00, Got: $total_paid)"
        ((TESTS_FAILED++))
    fi
fi

echo ""
echo "5. Make Second Payment (Same Month)"
response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/comptes-pret/make-payment" \
  -H "Content-Type: application/json" \
  -d "{
    \"compteId\": $TEST_LOAN_ID,
    \"montant\": 300.00,
    \"actionDateTime\": \"2025-02-15T14:30:00\"
  }")

status_code="${response: -3}"
check_status 201 "$status_code" "Make second payment (same month)"

echo ""
echo "6. Check Payment History After Multiple Payments"
response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/comptes-pret/$TEST_LOAN_ID/payment-history" \
  -H "Content-Type: application/json")

status_code="${response: -3}"
response_body="${response%???}"

check_status 200 "$status_code" "Get payment history after multiple payments"

if [ "$status_code" -eq 200 ]; then
    echo "Payment history:"
    echo "$response_body" | jq '.' 2>/dev/null || echo "Invalid JSON response"
    
    # Check if we have 2 payments
    count=$(echo "$response_body" | jq 'length' 2>/dev/null || echo 0)
    if [ "$count" -eq 2 ]; then
        echo -e "${GREEN}✅ PASS${NC}: Payment history shows 2 payments"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ FAIL${NC}: Payment history count incorrect (Expected: 2, Got: $count)"
        ((TESTS_FAILED++))
    fi
fi

echo ""
echo "7. Test Payment Status with Custom Date"
response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/comptes-pret/$TEST_LOAN_ID/payment-status?actionDateTime=2025-03-01T00:00:00" \
  -H "Content-Type: application/json")

status_code="${response: -3}"
response_body="${response%???}"

check_status 200 "$status_code" "Get payment status with custom date"

if [ "$status_code" -eq 200 ]; then
    echo "Payment status for March 1st:"
    echo "$response_body" | jq '.' 2>/dev/null || echo "Invalid JSON response"
fi

echo ""
echo "8. Test Invalid Payment (Negative Amount)"
response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/comptes-pret/make-payment" \
  -H "Content-Type: application/json" \
  -d "{
    \"compteId\": $TEST_LOAN_ID,
    \"montant\": -100.00,
    \"actionDateTime\": \"2025-02-01T10:00:00\"
  }")

status_code="${response: -3}"
check_status 400 "$status_code" "Make payment with negative amount"

echo ""
echo "9. Test Invalid Payment (Non-existent Loan)"
response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/comptes-pret/make-payment" \
  -H "Content-Type: application/json" \
  -d '{
    "compteId": 99999,
    "montant": 100.00,
    "actionDateTime": "2025-02-01T10:00:00"
  }')

status_code="${response: -3}"
check_status 400 "$status_code" "Make payment for non-existent loan"

echo ""
echo "10. Test Missing Payment Request Body"
response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/comptes-pret/make-payment" \
  -H "Content-Type: application/json")

status_code="${response: -3}"
check_status 400 "$status_code" "Make payment with missing request body"

echo ""
echo "=== Payment Management Test Summary ==="
echo -e "Tests Passed: ${GREEN}$TESTS_PASSED${NC}"
echo -e "Tests Failed: ${RED}$TESTS_FAILED${NC}"
echo -e "Total Tests: $((TESTS_PASSED + TESTS_FAILED))"

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "\n${GREEN}🎉 All payment management tests passed!${NC}"
    exit 0
else
    echo -e "\n${RED}💥 Some tests failed!${NC}"
    exit 1
fi