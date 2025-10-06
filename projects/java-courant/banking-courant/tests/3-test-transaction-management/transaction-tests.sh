#!/bin/bash

# Transaction Management API Tests
BASE_URL="http://localhost:8080/api"
TEST_NAME="Transaction Management Tests"

echo "=== $TEST_NAME ==="
echo "Base URL: $BASE_URL"
echo ""

# Setup: Create test user and accounts
echo "Setup: Creating test user and accounts..."
USER_RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Transaction Test User",
    "email": "transaction.test@example.com",
    "password": "testpassword"
  }' \
  "$BASE_URL/users")

USER_ID=$(echo "$USER_RESPONSE" | jq -r '.id // empty' 2>/dev/null)
if [ -z "$USER_ID" ]; then
    USER_ID=1  # Fallback ID
fi

# Create first account
ACCOUNT1_RESPONSE=$(curl -s -X POST "$BASE_URL/comptes/user/$USER_ID")
ACCOUNT1_ID=$(echo "$ACCOUNT1_RESPONSE" | jq -r '.id // empty' 2>/dev/null)

# Create second account for transfer tests
ACCOUNT2_RESPONSE=$(curl -s -X POST "$BASE_URL/comptes/user/$USER_ID")
ACCOUNT2_ID=$(echo "$ACCOUNT2_RESPONSE" | jq -r '.id // empty' 2>/dev/null)

if [ -z "$ACCOUNT1_ID" ]; then
    ACCOUNT1_ID=1  # Fallback ID
fi
if [ -z "$ACCOUNT2_ID" ]; then
    ACCOUNT2_ID=2  # Fallback ID
fi

echo "Created accounts with IDs: $ACCOUNT1_ID and $ACCOUNT2_ID"
echo ""

# Test 1: Make a deposit
echo "1. Making a deposit to account ID $ACCOUNT1_ID..."
echo "POST $BASE_URL/transactions/depot"
DEPOSIT_RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "compteId": '$ACCOUNT1_ID',
    "montant": 1000.00,
    "description": "Initial deposit"
  }' \
  "$BASE_URL/transactions/depot")

echo "Response: $DEPOSIT_RESPONSE"
echo ""

# Test 2: Make a withdrawal
echo "2. Making a withdrawal from account ID $ACCOUNT1_ID..."
echo "POST $BASE_URL/transactions/retrait"
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "compteId": '$ACCOUNT1_ID',
    "montant": 200.00,
    "description": "ATM withdrawal"
  }' \
  "$BASE_URL/transactions/retrait" | jq '.' 2>/dev/null || curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "compteId": '$ACCOUNT1_ID',
    "montant": 200.00,
    "description": "ATM withdrawal"
  }' \
  "$BASE_URL/transactions/retrait"
echo ""
echo ""

# Test 3: Make a transfer
echo "3. Making a transfer from account ID $ACCOUNT1_ID to $ACCOUNT2_ID..."
echo "POST $BASE_URL/transactions/transfert"
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "compteSourceId": '$ACCOUNT1_ID',
    "compteDestinationId": '$ACCOUNT2_ID',
    "montant": 150.00,
    "description": "Transfer between accounts"
  }' \
  "$BASE_URL/transactions/transfert" | jq '.' 2>/dev/null || curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "compteSourceId": '$ACCOUNT1_ID',
    "compteDestinationId": '$ACCOUNT2_ID',
    "montant": 150.00,
    "description": "Transfer between accounts"
  }' \
  "$BASE_URL/transactions/transfert"
echo ""
echo ""

# Test 4: Get all transactions
echo "4. Getting all transactions..."
echo "GET $BASE_URL/transactions"
curl -s -X GET "$BASE_URL/transactions" | jq '.' 2>/dev/null || curl -s -X GET "$BASE_URL/transactions"
echo ""
echo ""

# Test 5: Get transactions for specific account
echo "5. Getting transactions for account ID $ACCOUNT1_ID..."
echo "GET $BASE_URL/transactions/compte/$ACCOUNT1_ID"
curl -s -X GET "$BASE_URL/transactions/compte/$ACCOUNT1_ID" | jq '.' 2>/dev/null || curl -s -X GET "$BASE_URL/transactions/compte/$ACCOUNT1_ID"
echo ""
echo ""

# Test 6: Check account balances after transactions
echo "6. Checking account balances after transactions..."
echo "Account 1 (ID: $ACCOUNT1_ID) details and calculated balance:"
curl -s -X GET "$BASE_URL/comptes/$ACCOUNT1_ID" | jq '.' 2>/dev/null || curl -s -X GET "$BASE_URL/comptes/$ACCOUNT1_ID"
echo ""
echo "Account 2 (ID: $ACCOUNT2_ID) details and calculated balance:"
curl -s -X GET "$BASE_URL/comptes/$ACCOUNT2_ID" | jq '.' 2>/dev/null || curl -s -X GET "$BASE_URL/comptes/$ACCOUNT2_ID"
echo ""
echo ""

# Test 7: Try withdrawal with insufficient funds (400 test)
echo "7. Testing 400 - Withdrawal with insufficient funds..."
echo "POST $BASE_URL/transactions/retrait (amount exceeding balance)"
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "compteId": '$ACCOUNT1_ID',
    "montant": 10000.00,
    "description": "Excessive withdrawal"
  }' \
  "$BASE_URL/transactions/retrait" | jq '.' 2>/dev/null || curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "compteId": '$ACCOUNT1_ID',
    "montant": 10000.00,
    "description": "Excessive withdrawal"
  }' \
  "$BASE_URL/transactions/retrait"
echo ""
echo ""

# Test 8: Try transaction on non-existent account (404 test)
echo "8. Testing 404 - Transaction on non-existent account..."
echo "POST $BASE_URL/transactions/depot (non-existent account)"
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "compteId": 9999,
    "montant": 100.00,
    "description": "Test on non-existent account"
  }' \
  "$BASE_URL/transactions/depot" | jq '.' 2>/dev/null || curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "compteId": 9999,
    "montant": 100.00,
    "description": "Test on non-existent account"
  }' \
  "$BASE_URL/transactions/depot"
echo ""
echo ""
echo ""
echo ""

echo "=== Transaction Management Tests Completed ==="
echo "Final account balances should reflect all transactions performed"
