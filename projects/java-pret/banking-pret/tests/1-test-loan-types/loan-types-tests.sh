#!/bin/bash

# Loan Types Management Tests
echo "=== Loan Types Tests ==="

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
echo "Testing Loan Types Management..."
echo "================================"

echo ""
echo "1. Get All Loan Types"
response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/comptes-pret/types" \
  -H "Content-Type: application/json")

status_code="${response: -3}"
response_body="${response%???}"

check_status 200 "$status_code" "Get all loan types"

if [ "$status_code" -eq 200 ]; then
    echo "Response body:"
    echo "$response_body" | jq '.' 2>/dev/null || echo "Invalid JSON response"
    
    # Check if response is an array
    if echo "$response_body" | jq -e 'type == "array"' >/dev/null 2>&1; then
        echo -e "${GREEN}✅ PASS${NC}: Response is an array"
        ((TESTS_PASSED++))
        
        # Check if array has elements
        count=$(echo "$response_body" | jq 'length' 2>/dev/null || echo 0)
        if [ "$count" -gt 0 ]; then
            echo -e "${GREEN}✅ PASS${NC}: Found $count loan types"
            ((TESTS_PASSED++))
            
            # Check structure of first loan type
            first_type=$(echo "$response_body" | jq '.[0]' 2>/dev/null)
            if echo "$first_type" | jq -e 'has("id") and has("nom") and has("interet")' >/dev/null 2>&1; then
                echo -e "${GREEN}✅ PASS${NC}: Loan type has required fields (id, nom, interet)"
                ((TESTS_PASSED++))
                
                # Validate interest rate format
                interet=$(echo "$first_type" | jq -r '.interet' 2>/dev/null)
                if [[ $interet =~ ^0\.[0-9]+$ ]]; then
                    echo -e "${GREEN}✅ PASS${NC}: Interest rate format is valid ($interet)"
                    ((TESTS_PASSED++))
                else
                    echo -e "${RED}❌ FAIL${NC}: Interest rate format is invalid ($interet)"
                    ((TESTS_FAILED++))
                fi
            else
                echo -e "${RED}❌ FAIL${NC}: Loan type missing required fields"
                ((TESTS_FAILED++))
            fi
        else
            echo -e "${RED}❌ FAIL${NC}: No loan types found"
            ((TESTS_FAILED++))
        fi
    else
        echo -e "${RED}❌ FAIL${NC}: Response is not an array"
        ((TESTS_FAILED++))
    fi
else
    echo "Failed to retrieve loan types"
fi

echo ""
echo "2. Test Non-existent Endpoint"
response=$(curl -s -w "%{http_code}" -X GET "$BASE_URL/comptes-pret/types/999" \
  -H "Content-Type: application/json")

status_code="${response: -3}"
check_status 404 "$status_code" "Non-existent loan type endpoint"

echo ""
echo "=== Loan Types Test Summary ==="
echo -e "Tests Passed: ${GREEN}$TESTS_PASSED${NC}"
echo -e "Tests Failed: ${RED}$TESTS_FAILED${NC}"
echo -e "Total Tests: $((TESTS_PASSED + TESTS_FAILED))"

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "\n${GREEN}🎉 All loan types tests passed!${NC}"
    exit 0
else
    echo -e "\n${RED}💥 Some tests failed!${NC}"
    exit 1
fi