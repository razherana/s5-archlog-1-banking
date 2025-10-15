#!/bin/bash

# Loan Management CRUD Tests
echo "=== Loan Management Tests ==="

BASE_URL="http://localhost:8080/api"

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
echo "Testing Loan Management CRUD..."
echo "==============================="

echo ""
echo "1. Create New Loan Account"
response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/comptes-pret" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "typeComptePretId": 1,
    "montant": 10000.00,
    "dateDebut": "2025-01-01T00:00:00",
    "dateFin": "2026-01-01T00:00:00"
  }')

status_code="${response: -3}"
response_body="${response%???}"

check_status 201 "$status_code" "Create new loan account"

CREATED_LOAN_ID=""
if [ "$status_code" -eq 201 ]; then
    echo "Response body:"
    echo "$response_body" | jq '.' 2>/dev/null || echo "Invalid JSON response"
    
    # Extract loan ID for subsequent tests
    CREATED_LOAN_ID=$(echo "$response_body" | jq -r '.id' 2>/dev/null)
    if [ "$CREATED_LOAN_ID" != "null" ] && [ -n "$CREATED_LOAN_ID" ]; then
        echo -e "${GREEN}✅ PASS${NC}: Loan created with ID: $CREATED_LOAN_ID"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ FAIL${NC}: No loan ID returned"
        ((TESTS_FAILED++))
    fi
    
    # Validate response structure
    if echo "$response_body" | jq -e 'has("id") and has("userId") and has("montant")' >/dev/null 2>&1; then
        echo -e "${GREEN}✅ PASS${NC}: Response has required fields"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ FAIL${NC}: Response missing required fields"
        ((TESTS_FAILED++))
    fi
fi

echo ""
echo "2. Test Invalid Loan Creation (Negative Amount)"
response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/comptes-pret" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "typeComptePretId": 1,
    "montant": -5000.00,
    "dateDebut": "2025-01-01T00:00:00",
    "dateFin": "2026-01-01T00:00:00"
  }')

status_code="${response: -3}"
check_status 400 "$status_code" "Create loan with negative amount"

echo ""
echo "3. Test Invalid Loan Creation (End Date Before Start Date)"
response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/comptes-pret" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "typeComptePretId": 1,
    "montant": 10000.00,
    "dateDebut": "2026-01-01T00:00:00",
    "dateFin": "2025-01-01T00:00:00"
  }')

status_code="${response: -3}"
check_status 400 "$status_code" "Create loan with invalid date range"

echo ""
echo "4. Get Loan by ID"
if [ -n "$CREATED_LOAN_ID" ]; then
    response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/comptes-pret/$CREATED_LOAN_ID" \
      -H "Content-Type: application/json")
    
    status_code="${response: -3}"
    response_body="${response%???}"
    
    check_status 200 "$status_code" "Get loan by ID"
    
    if [ "$status_code" -eq 200 ]; then
        echo "Response body:"
        echo "$response_body" | jq '.' 2>/dev/null || echo "Invalid JSON response"
        
        # Verify loan ID matches
        returned_id=$(echo "$response_body" | jq -r '.id' 2>/dev/null)
        if [ "$returned_id" = "$CREATED_LOAN_ID" ]; then
            echo -e "${GREEN}✅ PASS${NC}: Returned loan ID matches created ID"
            ((TESTS_PASSED++))
        else
            echo -e "${RED}❌ FAIL${NC}: Loan ID mismatch (Expected: $CREATED_LOAN_ID, Got: $returned_id)"
            ((TESTS_FAILED++))
        fi
    fi
else
    echo -e "${RED}⚠️  SKIP${NC}: Get loan by ID (No loan ID available)"
fi

echo ""
echo "5. Test Get Non-existent Loan"
response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/comptes-pret/99999" \
  -H "Content-Type: application/json")

status_code="${response: -3}"
check_status 404 "$status_code" "Get non-existent loan"

echo ""
echo "6. Get Loans by User ID"
response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/comptes-pret/user/1" \
  -H "Content-Type: application/json")

status_code="${response: -3}"
response_body="${response%???}"

check_status 200 "$status_code" "Get loans by user ID"

if [ "$status_code" -eq 200 ]; then
    echo "Response body:"
    echo "$response_body" | jq '.' 2>/dev/null || echo "Invalid JSON response"
    
    # Check if response is an array
    if echo "$response_body" | jq -e 'type == "array"' >/dev/null 2>&1; then
        echo -e "${GREEN}✅ PASS${NC}: Response is an array"
        ((TESTS_PASSED++))
        
        count=$(echo "$response_body" | jq 'length' 2>/dev/null || echo 0)
        echo -e "${GREEN}✅ PASS${NC}: Found $count loans for user"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}❌ FAIL${NC}: Response is not an array"
        ((TESTS_FAILED++))
    fi
fi

echo ""
echo "7. Test Invalid User ID"
response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/comptes-pret/user/invalid" \
  -H "Content-Type: application/json")

status_code="${response: -3}"
check_status 400 "$status_code" "Get loans with invalid user ID"

echo ""
echo "8. Test Missing Request Body"
response=$(curl -s -w "%{http_code}" -X POST "$BASE_URL/comptes-pret" \
  -H "Content-Type: application/json")

status_code="${response: -3}"
check_status 400 "$status_code" "Create loan with missing request body"

echo ""
echo "=== Loan Management Test Summary ==="
echo -e "Tests Passed: ${GREEN}$TESTS_PASSED${NC}"
echo -e "Tests Failed: ${RED}$TESTS_FAILED${NC}"
echo -e "Total Tests: $((TESTS_PASSED + TESTS_FAILED))"

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "\n${GREEN}🎉 All loan management tests passed!${NC}"
    
    # Save created loan ID for other tests
    if [ -n "$CREATED_LOAN_ID" ]; then
        echo "$CREATED_LOAN_ID" > /tmp/test_loan_id.txt
        echo "Saved loan ID $CREATED_LOAN_ID for subsequent tests"
    fi
    
    exit 0
else
    echo -e "\n${RED}💥 Some tests failed!${NC}"
    exit 1
fi