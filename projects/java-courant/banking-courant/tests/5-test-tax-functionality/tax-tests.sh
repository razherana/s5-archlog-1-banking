#!/bin/bash

# Test script for tax functionality
BASE_URL="http://localhost:8080/api"

echo "🏦 Testing Tax Functionality"
echo "=============================="

# Check if server is running
echo "🔍 Checking if server is running..."
if ! curl -s -f "$BASE_URL/users" >/dev/null 2>&1; then
    echo "❌ ERROR: Server is not running or not accessible at $BASE_URL"
    echo "Please start the TomEE server before running tests:"
    echo "   cd banking-courant && mvn tomee:run"
    exit 1
fi
echo "✅ Server is accessible"

echo ""
echo "📋 Testing Tax-related endpoints..."
echo ""

# Test account creation for tax testing
echo "1. Creating test user..."
USER_ID=$(curl -s -X POST "$BASE_URL/users" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tax Test User", 
    "email": "tax.test@example.com", 
    "password": "password123"
  }' | jq -r '.id' 2>/dev/null || echo "1")

echo "   User ID: $USER_ID"

echo "2. Creating test account..."
ACCOUNT_ID=$(curl -s -X POST "$BASE_URL/comptes/user/$USER_ID?taxe=20.00" \
  -H "Content-Type: application/json" | jq -r '.id' 2>/dev/null || echo "1")

echo "   Account ID: $ACCOUNT_ID"

echo "3. Making initial deposit..."
curl -s -X POST "$BASE_URL/transactions/depot" \
  -H "Content-Type: application/json" \
  -d '{
    "compteId": '"$ACCOUNT_ID"',
    "montant": 1000.00,
    "description": "Initial deposit for tax testing"
  }' >/dev/null

echo "   ✅ Deposit completed"

echo "4. Testing tax status endpoint..."
TAX_STATUS=$(curl -s "$BASE_URL/comptes/$ACCOUNT_ID/tax-status")
echo "   Tax Status: $TAX_STATUS"

echo "5. Testing tax to pay endpoint..."
TAX_TO_PAY=$(curl -s "$BASE_URL/comptes/$ACCOUNT_ID/tax-to-pay")
echo "   Tax To Pay: $TAX_TO_PAY"

echo "6. Testing pay tax endpoint..."
PAY_TAX_RESULT=$(curl -s -X POST "$BASE_URL/transactions/pay-tax" \
  -H "Content-Type: application/json" \
  -d '{
    "compteId": '"$ACCOUNT_ID"',
    "description": "Monthly tax payment",
    "actionDateTime": "2025-10-08T20:00:00"
  }')
echo "   Pay Tax Result: $PAY_TAX_RESULT"

echo "7. Testing withdrawal with actionDateTime..."
WITHDRAWAL_RESULT=$(curl -s -X POST "$BASE_URL/transactions/retrait" \
  -H "Content-Type: application/json" \
  -d '{
    "compteId": '"$ACCOUNT_ID"',
    "montant": 100.00,
    "description": "Test withdrawal with tax check",
    "actionDateTime": "2025-10-08T20:00:00"
  }')
echo "   Withdrawal Result: $WITHDRAWAL_RESULT"

echo ""
echo "📋 Testing Additional Tax Scenarios..."
echo ""

# Test 8: Not enough money but pay tax
echo "8. Testing insufficient funds for tax payment..."
echo "   Creating new user for insufficient funds test..."
USER_ID_2=$(curl -s -X POST "$BASE_URL/users" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Poor Tax User", 
    "email": "poor.tax@example.com", 
    "password": "password123"
  }' | jq -r '.id' 2>/dev/null || echo "2")

echo "   Creating account with taxe..."
ACCOUNT_ID_2=$(curl -s -X POST "$BASE_URL/comptes/user/$USER_ID_2?taxe=50.00" \
  -H "Content-Type: application/json" | jq -r '.id' 2>/dev/null || echo "2")

echo "   Making small deposit (less than tax amount)..."
curl -s -X POST "$BASE_URL/transactions/depot" \
  -H "Content-Type: application/json" \
  -d '{
    "compteId": '"$ACCOUNT_ID_2"',
    "montant": 30.00,
    "description": "Small deposit - insufficient for tax"
  }' >/dev/null

echo "   Trying to pay tax with insufficient funds..."
INSUFFICIENT_TAX_RESULT=$(curl -s -X POST "$BASE_URL/transactions/pay-tax" \
  -H "Content-Type: application/json" \
  -d '{
    "compteId": '"$ACCOUNT_ID_2"',
    "description": "Tax payment attempt with insufficient funds",
    "actionDateTime": "2025-10-08T20:00:00"
  }')
echo "   ❌ Expected error - Insufficient Funds for Tax: $INSUFFICIENT_TAX_RESULT"

# Test 9: Deposit enough and pay tax
echo ""
echo "9. Testing deposit enough money and pay tax..."
echo "   Adding more funds to cover tax..."
curl -s -X POST "$BASE_URL/transactions/depot" \
  -H "Content-Type: application/json" \
  -d '{
    "compteId": '"$ACCOUNT_ID_2"',
    "montant": 100.00,
    "description": "Additional deposit to cover tax"
  }' >/dev/null

echo "   Now paying tax with sufficient funds..."
SUCCESSFUL_TAX_RESULT=$(curl -s -X POST "$BASE_URL/transactions/pay-tax" \
  -H "Content-Type: application/json" \
  -d '{
    "compteId": '"$ACCOUNT_ID_2"',
    "description": "Successful tax payment",
    "actionDateTime": "2025-10-08T20:00:00"
  }')
echo "   ✅ Tax Payment Result: $SUCCESSFUL_TAX_RESULT"

# Test 10: Deposit enough but don't pay tax and try operations
echo ""
echo "10. Testing transactions without paying tax..."
echo "    Creating third user for unpaid tax scenario..."
USER_ID_3=$(curl -s -X POST "$BASE_URL/users" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Tax Evader User", 
    "email": "evader.tax@example.com", 
    "password": "password123"
  }' | jq -r '.id' 2>/dev/null || echo "3")

echo "    Creating account with monthly tax..."
ACCOUNT_ID_3=$(curl -s -X POST "$BASE_URL/comptes/user/$USER_ID_3?taxe=25.00" \
  -H "Content-Type: application/json" | jq -r '.id' 2>/dev/null || echo "3")

echo "    Making sufficient deposit..."
curl -s -X POST "$BASE_URL/transactions/depot" \
  -H "Content-Type: application/json" \
  -d '{
    "compteId": '"$ACCOUNT_ID_3"',
    "montant": 500.00,
    "description": "Large deposit but will not pay tax"
  }' >/dev/null

echo "    Checking tax status before operations..."
TAX_STATUS_UNPAID=$(curl -s "$BASE_URL/comptes/$ACCOUNT_ID_3/tax-status")
echo "    Tax Status (should show unpaid): $TAX_STATUS_UNPAID"

echo "    Attempting withdrawal without paying tax first..."
BLOCKED_WITHDRAWAL=$(curl -s -X POST "$BASE_URL/transactions/retrait" \
  -H "Content-Type: application/json" \
  -d '{
    "compteId": '"$ACCOUNT_ID_3"',
    "montant": 100.00,
    "description": "Withdrawal attempt without tax payment",
    "actionDateTime": "2025-10-08T20:00:00"
  }')
echo "    ❌ Expected error - Tax Must Be Paid: $BLOCKED_WITHDRAWAL"

echo "    Attempting transfer without paying tax first..."
BLOCKED_TRANSFER=$(curl -s -X POST "$BASE_URL/transactions/transfert" \
  -H "Content-Type: application/json" \
  -d '{
    "compteSourceId": '"$ACCOUNT_ID_3"',
    "compteDestinationId": '"$ACCOUNT_ID"',
    "montant": 50.00,
    "description": "Transfer attempt without tax payment",
    "actionDateTime": "2025-10-08T20:00:00"
  }')
echo "    ❌ Expected error - Tax Must Be Paid: $BLOCKED_TRANSFER"

echo ""
echo "11. Testing tax update endpoint..."
echo "    Updating tax amount using PUT endpoint..."
UPDATE_TAX_RESULT=$(curl -s -X PUT "$BASE_URL/comptes/$ACCOUNT_ID_3/taxe" \
  -H "Content-Type: application/json" \
  -d '{
    "taxe": 35.00
  }')
echo "    ✅ Tax Update Result: $UPDATE_TAX_RESULT"

echo "    Checking new tax status after update..."
NEW_TAX_STATUS=$(curl -s "$BASE_URL/comptes/$ACCOUNT_ID_3/tax-status")
echo "    New Tax Status: $NEW_TAX_STATUS"

echo ""
echo "🎉 All tax functionality tests completed!"
echo "✅ Basic tax operations"
echo "❌ Insufficient funds for tax payment"
echo "✅ Sufficient funds for tax payment" 
echo "❌ Blocked operations when tax unpaid"
echo "✅ Tax amount update via PUT endpoint"
echo "Check the results above to verify proper tax handling."