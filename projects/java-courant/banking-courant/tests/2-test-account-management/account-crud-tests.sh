#!/bin/bash

# Account Management API Tests
BASE_URL="http://localhost:8080/api"
TEST_NAME="Account Management Tests"

echo "=== $TEST_NAME ==="
echo "Base URL: $BASE_URL"
echo ""

# First, ensure we have a user to work with
echo "Setup: Creating a test user first..."
USER_RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User For Accounts",
    "email": "test.accounts@example.com",
    "password": "testpassword"
  }' \
  "$BASE_URL/users")

USER_ID=$(echo "$USER_RESPONSE" | jq -r '.id // empty' 2>/dev/null)
if [ -z "$USER_ID" ]; then
    USER_ID=1  # Fallback ID
fi
echo "Test user created with ID: $USER_ID"
echo ""

# Test 1: Create an account for user
echo "1. Creating a compte courant for user ID $USER_ID..."
echo "POST $BASE_URL/comptes/user/$USER_ID"
ACCOUNT_RESPONSE=$(curl -s -X POST "$BASE_URL/comptes/user/$USER_ID")
echo "Response: $ACCOUNT_RESPONSE"
echo ""

# Extract account details
ACCOUNT_ID=$(echo "$ACCOUNT_RESPONSE" | jq -r '.id // empty' 2>/dev/null)
ACCOUNT_TAXE=$(echo "$ACCOUNT_RESPONSE" | jq -r '.taxe // empty' 2>/dev/null)
if [ -z "$ACCOUNT_ID" ]; then
    ACCOUNT_ID=1  # Fallback ID
fi
if [ -z "$ACCOUNT_TAXE" ]; then
    ACCOUNT_TAXE="0.00"  # Fallback taxe
fi

echo "Created account with ID: $ACCOUNT_ID, Taxe: $ACCOUNT_TAXE"

# Test 2: Get all accounts
echo "2. Getting all compte courants..."
echo "GET $BASE_URL/comptes"
curl -s -X GET "$BASE_URL/comptes" | jq '.' 2>/dev/null || curl -s -X GET "$BASE_URL/comptes"
echo ""
echo ""

# Test 3: Get account by ID
echo "3. Getting compte courant by ID ($ACCOUNT_ID)..."
echo "GET $BASE_URL/comptes/$ACCOUNT_ID"
curl -s -X GET "$BASE_URL/comptes/$ACCOUNT_ID" | jq '.' 2>/dev/null || curl -s -X GET "$BASE_URL/comptes/$ACCOUNT_ID"
echo ""
echo ""

# Test 4: Get account by numero (deprecated endpoint test)
echo "4. Testing deprecated endpoint - Getting compte courant by numero..."
echo "GET $BASE_URL/comptes/numero/123456789"
echo "This endpoint should return 410 Gone since numeroCompte field doesn't exist"
curl -s -X GET "$BASE_URL/comptes/numero/123456789" | jq '.' 2>/dev/null || curl -s -X GET "$BASE_URL/comptes/numero/123456789"
echo ""
echo ""

# Test 5: Get accounts by user ID
echo "5. Getting all comptes for user ID $USER_ID..."
echo "GET $BASE_URL/comptes/user/$USER_ID"
curl -s -X GET "$BASE_URL/comptes/user/$USER_ID" | jq '.' 2>/dev/null || curl -s -X GET "$BASE_URL/comptes/user/$USER_ID"
echo ""
echo ""

# Test 6: Create another account for the same user
echo "6. Creating a second compte courant for the same user..."
echo "POST $BASE_URL/comptes/user/$USER_ID"
curl -s -X POST "$BASE_URL/comptes/user/$USER_ID" | jq '.' 2>/dev/null || curl -s -X POST "$BASE_URL/comptes/user/$USER_ID"
echo ""
echo ""

# Test 7: Try to create account for non-existent user (404 test)
echo "7. Testing 404 - Creating account for non-existent user..."
echo "POST $BASE_URL/comptes/user/99999"
curl -s -X POST "$BASE_URL/comptes/user/99999" | jq '.' 2>/dev/null || curl -s -X POST "$BASE_URL/comptes/user/99999"
echo ""
echo ""

# Test 8: Try to get non-existent account (404 test)
echo "8. Testing 404 - Getting non-existent account..."
echo "GET $BASE_URL/comptes/99999"
curl -s -X GET "$BASE_URL/comptes/99999" | jq '.' 2>/dev/null || curl -s -X GET "$BASE_URL/comptes/99999"
echo ""
echo ""

echo "=== Account Management Tests Completed ==="
echo "Note: Account ID $ACCOUNT_ID was created and can be used for transaction tests"
echo "The account has a dynamic balance calculated from transactions and a taxe of $ACCOUNT_TAXE"
