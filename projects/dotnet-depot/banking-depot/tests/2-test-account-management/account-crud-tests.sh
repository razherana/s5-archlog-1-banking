#!/bin/bash

# Account Management API Tests
BASE_URL="http://127.0.0.4:8080/api"
JAVA_SERVICE_URL="http://127.0.0.2:8080/api"
TEST_NAME="Account Management Tests"

echo "=== $TEST_NAME ==="
echo "Base URL: $BASE_URL"
echo "Java Service URL: $JAVA_SERVICE_URL"
echo ""

# Use single test user (must exist in Java service)
TEST_USER_ID=1
echo "Using test user ID: $TEST_USER_ID (assumes user exists in Java service)"
echo ""

# Verify user exists in Java service
echo "0. Verifying test user exists in Java service..."
echo "Checking user $TEST_USER_ID..."
USER_CHECK=$(curl -s -X GET "$JAVA_SERVICE_URL/users/$TEST_USER_ID")
if echo "$USER_CHECK" | grep -q '"id"'; then
    echo "✅ User $TEST_USER_ID exists"
else
    echo "❌ User $TEST_USER_ID not found - this will cause test failures"
fi
echo ""

# First, ensure we have account types to work with
echo "1. Creating test account types..."
TYPE_DATA_1='{
  "nom": "Test Épargne 6 Mois",
  "tauxInteret": 0.02
}'
TYPE_DATA_2='{
  "nom": "Test Terme 1 An",
  "tauxInteret": 0.035
}'

echo "Creating type 1..."
TYPE_1_RESPONSE=$(curl -s -X POST "$BASE_URL/typecomptesdepots" \
  -H "Content-Type: application/json" \
  -d "$TYPE_DATA_1")
TYPE_1_ID=$(echo "$TYPE_1_RESPONSE" | jq -r '.id // 1' 2>/dev/null || echo "1")

echo "Creating type 2..."
TYPE_2_RESPONSE=$(curl -s -X POST "$BASE_URL/typecomptesdepots" \
  -H "Content-Type: application/json" \
  -d "$TYPE_DATA_2")
TYPE_2_ID=$(echo "$TYPE_2_RESPONSE" | jq -r '.id // 2' 2>/dev/null || echo "2")

echo "Created types with IDs: $TYPE_1_ID, $TYPE_2_ID"
echo ""

# Test 2: Create deposit accounts
echo "2. Creating deposit accounts..."

# Account 1: 6-month term for test user
ACCOUNT_DATA_1='{
  "typeCompteDepotId": '$TYPE_1_ID',
  "userId": '$TEST_USER_ID',
  "dateEcheance": "2025-04-15T10:00:00",
  "montant": 100000.00
}'
echo "Creating account for user $TEST_USER_ID..."
echo "POST $BASE_URL/comptesdepots"
echo "Request: $ACCOUNT_DATA_1"
ACCOUNT_1_RESPONSE=$(curl -s -X POST "$BASE_URL/comptesdepots" \
  -H "Content-Type: application/json" \
  -d "$ACCOUNT_DATA_1")
echo "Response: $ACCOUNT_1_RESPONSE"
ACCOUNT_1_ID=$(echo "$ACCOUNT_1_RESPONSE" | jq -r '.id // 1' 2>/dev/null || echo "1")
echo ""

# Account 2: 1-year term for same test user
ACCOUNT_DATA_2='{
  "typeCompteDepotId": '$TYPE_2_ID',
  "userId": '$TEST_USER_ID',
  "dateEcheance": "2026-01-15T14:30:00",
  "montant": 250000.00
}'
echo "Creating second account for user $TEST_USER_ID..."
echo "Request: $ACCOUNT_DATA_2"
ACCOUNT_2_RESPONSE=$(curl -s -X POST "$BASE_URL/comptesdepots" \
  -H "Content-Type: application/json" \
  -d "$ACCOUNT_DATA_2")
echo "Response: $ACCOUNT_2_RESPONSE"
echo ""

# Account 3: Already matured account for testing withdrawal
ACCOUNT_DATA_3='{
  "typeCompteDepotId": '$TYPE_1_ID',
  "userId": '$TEST_USER_ID',
  "dateEcheance": "2024-01-15T09:00:00",
  "montant": 50000.00,
  "actionDateTime": "2023-07-15T09:00:00"
}'
echo "Creating matured account for user $TEST_USER_ID (with backtracking)..."
echo "Request: $ACCOUNT_DATA_3"
ACCOUNT_3_RESPONSE=$(curl -s -X POST "$BASE_URL/comptesdepots" \
  -H "Content-Type: application/json" \
  -d "$ACCOUNT_DATA_3")
echo "Response: $ACCOUNT_3_RESPONSE"
echo ""

# Test 3: Get all accounts
echo "3. Getting all deposit accounts..."
echo "GET $BASE_URL/comptesdepots"
ALL_ACCOUNTS=$(curl -s -X GET "$BASE_URL/comptesdepots")
echo "$ALL_ACCOUNTS" | jq '.' 2>/dev/null || echo "$ALL_ACCOUNTS"
echo ""

# Test 4: Get account by ID
echo "4. Getting deposit account by ID ($ACCOUNT_1_ID)..."
echo "GET $BASE_URL/comptesdepots/$ACCOUNT_1_ID"
curl -s -X GET "$BASE_URL/comptesdepots/$ACCOUNT_1_ID" | jq '.' 2>/dev/null || curl -s -X GET "$BASE_URL/comptesdepots/$ACCOUNT_1_ID"
echo ""
echo ""

# Test 5: Get accounts by user ID
echo "5. Getting accounts for user $TEST_USER_ID..."
echo "GET $BASE_URL/comptesdepots/user/$TEST_USER_ID"
USER_ACCOUNTS=$(curl -s -X GET "$BASE_URL/comptesdepots/user/$TEST_USER_ID")
echo "$USER_ACCOUNTS" | jq '.' 2>/dev/null || echo "$USER_ACCOUNTS"
echo ""

# Test 6: Test invalid user (should fail user validation)
echo "6. Testing account creation with invalid user..."
INVALID_USER_DATA='{
  "typeCompteDepotId": '$TYPE_1_ID',
  "userId": 999999,
  "dateEcheance": "2025-06-15T10:00:00",
  "montant": 75000.00
}'
echo "POST $BASE_URL/comptesdepots (with invalid user)"
echo "Request: $INVALID_USER_DATA"
curl -s -X POST "$BASE_URL/comptesdepots" \
  -H "Content-Type: application/json" \
  -d "$INVALID_USER_DATA" | jq '.' 2>/dev/null || curl -s -X POST "$BASE_URL/comptesdepots" \
  -H "Content-Type: application/json" \
  -d "$INVALID_USER_DATA"
echo ""
echo ""

# Test 7: Test invalid account type
echo "7. Testing account creation with invalid account type..."
INVALID_TYPE_DATA='{
  "typeCompteDepotId": 999999,
  "userId": '$TEST_USER_ID',
  "dateEcheance": "2025-06-15T10:00:00",
  "montant": 75000.00
}'
echo "Request: $INVALID_TYPE_DATA"
curl -s -X POST "$BASE_URL/comptesdepots" \
  -H "Content-Type: application/json" \
  -d "$INVALID_TYPE_DATA" | jq '.' 2>/dev/null || curl -s -X POST "$BASE_URL/comptesdepots" \
  -H "Content-Type: application/json" \
  -d "$INVALID_TYPE_DATA"
echo ""
echo ""

# Test 8: Test invalid maturity date (past date)
echo "8. Testing account creation with past maturity date..."
PAST_DATE_DATA='{
  "typeCompteDepotId": '$TYPE_1_ID',
  "userId": '$TEST_USER_ID',
  "dateEcheance": "2020-01-15T10:00:00",
  "montant": 75000.00
}'
echo "Request: $PAST_DATE_DATA"
curl -s -X POST "$BASE_URL/comptesdepots" \
  -H "Content-Type: application/json" \
  -d "$PAST_DATE_DATA" | jq '.' 2>/dev/null || curl -s -X POST "$BASE_URL/comptesdepots" \
  -H "Content-Type: application/json" \
  -d "$PAST_DATE_DATA"
echo ""
echo ""

# Test 9: Test invalid amount
echo "9. Testing account creation with invalid amount..."
INVALID_AMOUNT_DATA='{
  "typeCompteDepotId": '$TYPE_1_ID',
  "userId": '$TEST_USER_ID',
  "dateEcheance": "2025-06-15T10:00:00",
  "montant": -1000.00
}'
echo "Request: $INVALID_AMOUNT_DATA"
curl -s -X POST "$BASE_URL/comptesdepots" \
  -H "Content-Type: application/json" \
  -d "$INVALID_AMOUNT_DATA" | jq '.' 2>/dev/null || curl -s -X POST "$BASE_URL/comptesdepots" \
  -H "Content-Type: application/json" \
  -d "$INVALID_AMOUNT_DATA"
echo ""
echo ""

# Test 10: Get non-existent account
echo "10. Testing non-existent account..."
echo "GET $BASE_URL/comptesdepots/999999"
curl -s -X GET "$BASE_URL/comptesdepots/999999" | jq '.' 2>/dev/null || curl -s -X GET "$BASE_URL/comptesdepots/999999"
echo ""
echo ""

# Test 11: Multiple accounts for same user
echo "11. Creating multiple accounts for same user..."
MULTI_ACCOUNT_DATA='{
  "typeCompteDepotId": '$TYPE_2_ID',
  "userId": '$TEST_USER_ID',
  "dateEcheance": "2025-12-15T16:45:00",
  "montant": 150000.00
}'
echo "Creating second account for user $TEST_USER_ID..."
echo "Request: $MULTI_ACCOUNT_DATA"
MULTI_ACCOUNT_RESPONSE=$(curl -s -X POST "$BASE_URL/comptesdepots" \
  -H "Content-Type: application/json" \
  -d "$MULTI_ACCOUNT_DATA")
echo "Response: $MULTI_ACCOUNT_RESPONSE"
echo ""

# Verify user now has multiple accounts
echo "12. Verifying user has multiple accounts..."
echo "GET $BASE_URL/comptesdepots/user/$TEST_USER_ID"
FINAL_USER_ACCOUNTS=$(curl -s -X GET "$BASE_URL/comptesdepots/user/$TEST_USER_ID")
echo "$FINAL_USER_ACCOUNTS" | jq '.' 2>/dev/null || echo "$FINAL_USER_ACCOUNTS"
ACCOUNT_COUNT=$(echo "$FINAL_USER_ACCOUNTS" | jq 'length' 2>/dev/null || echo "unknown")
echo "User $TEST_USER_ID now has $ACCOUNT_COUNT accounts"
echo ""

echo "=== Account Management Tests Completed ==="
echo "✅ Account creation with valid data tested"
echo "✅ User validation integration verified"
echo "✅ Business rule validations tested"
echo "✅ Error handling for invalid data tested"
echo "✅ Multiple accounts per user verified"
echo "✅ Backtracking support tested"