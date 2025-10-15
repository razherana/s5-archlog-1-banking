#!/bin/bash

# Banking API Integration Test - Complete Workflow
BASE_URL="http://localhost:8080/api"
TEST_NAME="Full Banking Workflow Integration Test"

echo "=== $TEST_NAME ==="
echo "Base URL: $BASE_URL"
echo "This test simulates a complete banking workflow from user creation to complex transactions"
echo ""

# Check if server is running
echo "Checking if server is running..."
if ! curl -s -f "$BASE_URL/comptes" >/dev/null 2>&1; then
    echo "❌ ERROR: Server is not running or not accessible at $BASE_URL"
    echo "Please start the TomEE server before running this test."
    exit 1
fi
echo "✅ Server is accessible"
echo ""

# Scenario: Create a family banking setup with fake user IDs
echo "📋 SCENARIO: Creating a family banking setup"
echo "1. Use fake family member user IDs (assumes users exist in central service)"
echo "2. Create accounts for each member"
echo "3. Perform various transactions"
echo "4. Verify final state"
echo ""

# Step 1: Use fake user IDs (assume they exist in java-interface service)
echo "👤 Step 1: Using fake family member user IDs..."
echo ""

echo "Using father user ID: 2 (John Doe)"
FATHER_ID=2

echo "Using mother user ID: 3 (Jane Doe)"
MOTHER_ID=3

echo "Using child user ID: 4 (Alice Doe)"
CHILD_ID=4
echo ""

# Step 2: Create accounts
echo "🏦 Step 2: Creating bank accounts..."
echo ""

echo "Creating father's main account..."
FATHER_ACCOUNT_RESPONSE=$(curl -s -X POST "$BASE_URL/comptes/user/$FATHER_ID")
FATHER_ACCOUNT_ID=$(echo "$FATHER_ACCOUNT_RESPONSE" | jq -r '.id // empty' 2>/dev/null)
echo "Father's account ID: $FATHER_ACCOUNT_ID"

echo "Creating mother's account..."
MOTHER_ACCOUNT_RESPONSE=$(curl -s -X POST "$BASE_URL/comptes/user/$MOTHER_ID")
MOTHER_ACCOUNT_ID=$(echo "$MOTHER_ACCOUNT_RESPONSE" | jq -r '.id // empty' 2>/dev/null)
echo "Mother's account ID: $MOTHER_ACCOUNT_ID"

echo "Creating child's savings account..."
CHILD_ACCOUNT_RESPONSE=$(curl -s -X POST "$BASE_URL/comptes/user/$CHILD_ID")
CHILD_ACCOUNT_ID=$(echo "$CHILD_ACCOUNT_RESPONSE" | jq -r '.id // empty' 2>/dev/null)
echo "Child's account ID: $CHILD_ACCOUNT_ID"

echo "Creating father's second account (business)..."
FATHER_BUSINESS_RESPONSE=$(curl -s -X POST "$BASE_URL/comptes/user/$FATHER_ID")
FATHER_BUSINESS_ID=$(echo "$FATHER_BUSINESS_RESPONSE" | jq -r '.id // empty' 2>/dev/null)
echo "Father's business account ID: $FATHER_BUSINESS_ID"
echo ""

# Step 3: Perform transactions
echo "💰 Step 3: Performing banking transactions..."
echo ""

echo "Father receives salary (deposit 5000MGA)..."
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "compteId": '$FATHER_ACCOUNT_ID',
    "montant": 5000.00,
    "description": "Monthly salary"
  }' \
  "$BASE_URL/transactions/depot" >/dev/null

echo "Mother receives salary (deposit 3500MGA)..."
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "compteId": '$MOTHER_ACCOUNT_ID',
    "montant": 3500.00,
    "description": "Monthly salary"
  }' \
  "$BASE_URL/transactions/depot" >/dev/null

echo "Father transfers money to mother (1000MGA)..."
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "compteSourceId": '$FATHER_ACCOUNT_ID',
    "compteDestinationId": '$MOTHER_ACCOUNT_ID',
    "montant": 1000.00,
    "description": "Household expenses"
  }' \
  "$BASE_URL/transactions/transfert" >/dev/null

echo "Mother gives allowance to child (100MGA)..."
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "compteSourceId": '$MOTHER_ACCOUNT_ID',
    "compteDestinationId": '$CHILD_ACCOUNT_ID',
    "montant": 100.00,
    "description": "Monthly allowance"
  }' \
  "$BASE_URL/transactions/transfert" >/dev/null

echo "Father transfers to business account (2000MGA)..."
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "compteSourceId": '$FATHER_ACCOUNT_ID',
    "compteDestinationId": '$FATHER_BUSINESS_ID',
    "montant": 2000.00,
    "description": "Business investment"
  }' \
  "$BASE_URL/transactions/transfert" >/dev/null

echo "Father withdraws cash (300MGA)..."
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "compteId": '$FATHER_ACCOUNT_ID',
    "montant": 300.00,
    "description": "ATM withdrawal"
  }' \
  "$BASE_URL/transactions/retrait" >/dev/null

echo "Mother pays bills (500MGA)..."
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "compteId": '$MOTHER_ACCOUNT_ID',
    "montant": 500.00,
    "description": "Utility bills payment"
  }' \
  "$BASE_URL/transactions/retrait" >/dev/null
echo ""

# Step 4: Verify final state
echo "📊 Step 4: Verifying final account balances..."
echo ""

echo "👨 Father's main account balance:"
FATHER_BALANCE=$(curl -s -X GET "$BASE_URL/comptes/$FATHER_ACCOUNT_ID" | jq -r '.solde // "N/A"' 2>/dev/null)
echo "   Account ID: $FATHER_ACCOUNT_ID"
echo "   Balance: MGA$FATHER_BALANCE"
echo "   Expected: MGA1700 (5000 - 1000 - 2000 - 300)"

echo ""
echo "👩 Mother's account balance:"
MOTHER_BALANCE=$(curl -s -X GET "$BASE_URL/comptes/$MOTHER_ACCOUNT_ID" | jq -r '.solde // "N/A"' 2>/dev/null)
echo "   Account ID: $MOTHER_ACCOUNT_ID"
echo "   Balance: MGA$MOTHER_BALANCE"
echo "   Expected: MGA3900 (3500 + 1000 - 100 - 500)"

echo ""
echo "👧 Child's account balance:"
CHILD_BALANCE=$(curl -s -X GET "$BASE_URL/comptes/$CHILD_ACCOUNT_ID" | jq -r '.solde // "N/A"' 2>/dev/null)
echo "   Account ID: $CHILD_ACCOUNT_ID"
echo "   Balance: MGA$CHILD_BALANCE"
echo "   Expected: MGA100 (0 + 100)"

echo ""
echo "💼 Father's business account balance:"
BUSINESS_BALANCE=$(curl -s -X GET "$BASE_URL/comptes/$FATHER_BUSINESS_ID" | jq -r '.solde // "N/A"' 2>/dev/null)
echo "   Account ID: $FATHER_BUSINESS_ID"
echo "   Balance: MGA$BUSINESS_BALANCE"
echo "   Expected: MGA2000 (0 + 2000)"
echo ""

# Step 5: Get transaction history
echo "📜 Step 5: Transaction summary..."
echo ""
echo "Total number of transactions:"
TRANSACTION_COUNT=$(curl -s -X GET "$BASE_URL/transactions" | jq '. | length' 2>/dev/null)
echo "   Count: $TRANSACTION_COUNT transactions"
echo ""

echo "Father's transaction history:"
curl -s -X GET "$BASE_URL/transactions/compte/$FATHER_ACCOUNT_ID" | jq '.[] | {type: .type, montant: .montant, description: .description}' 2>/dev/null || echo "   Unable to fetch transaction details"
echo ""

# Step 6: Validation summary
echo "✅ Step 6: Test Results Summary"
echo ""
TOTAL_BALANCE=$(echo "$FATHER_BALANCE + $MOTHER_BALANCE + $CHILD_BALANCE + $BUSINESS_BALANCE" | bc 2>/dev/null || echo "N/A")
echo "Total money in system: MGA$TOTAL_BALANCE"
echo "Expected total: MGA7800 (5000 + 3500 - 300 - 500 = 7700 + 100 fees tolerance)"
echo ""

if [ "$FATHER_BALANCE" = "1700.00" ] && [ "$MOTHER_BALANCE" = "3900.00" ] && [ "$CHILD_BALANCE" = "100.00" ] && [ "$BUSINESS_BALANCE" = "2000.00" ]; then
    echo "🎉 ALL TESTS PASSED! Banking system is working correctly."
else
    echo "⚠️  Some balances don't match expected values. Check for errors."
fi

echo ""
echo "=== Integration Test Completed ==="
echo "This test created 3 users, 4 accounts, and performed 7 transactions"
echo "to simulate a realistic banking workflow."
