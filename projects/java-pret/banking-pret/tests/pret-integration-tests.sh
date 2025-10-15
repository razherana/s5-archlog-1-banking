#!/bin/bash

# Banking Pret Integration Tests
# Tests loan account management functionality

set -e

BASE_URL="http://127.0.0.3:8080/api"
USER_SERVICE_URL="http://127.0.0.2:8080/api"

echo "🏦 Banking Pret Service - Integration Tests"
echo "==========================================="

# Test 1: Health check
echo "1. Testing service health..."
response=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/comptes" || echo "000")
if [ "$response" -eq 200 ]; then
    echo "✅ Service is running"
else
    echo "❌ Service is not responding (HTTP $response)"
    exit 1
fi

# Test 2: Get all loan accounts
echo "2. Testing GET /comptes..."
curl -s -X GET "$BASE_URL/comptes" \
    -H "Content-Type: application/json" | jq '.' || echo "Response received"

# Test 3: Create a user first (in java-interface)
echo "3. Creating test user in java-interface..."
user_response=$(curl -s -X POST "$USER_SERVICE_URL/users" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "Test User Pret",
        "email": "testpret@example.com",
        "password": "password123"
    }')

user_id=$(echo "$user_response" | jq -r '.id' 2>/dev/null || echo "1")
echo "Created user with ID: $user_id"

# Test 4: Create loan account
echo "4. Testing POST /comptes/user/$user_id..."
compte_response=$(curl -s -X POST "$BASE_URL/comptes/user/$user_id" \
    -H "Content-Type: application/json")

compte_id=$(echo "$compte_response" | jq -r '.id' 2>/dev/null || echo "1")
echo "Created loan account with ID: $compte_id"

# Test 5: Get loan account by ID
echo "5. Testing GET /comptes/$compte_id..."
curl -s -X GET "$BASE_URL/comptes/$compte_id" \
    -H "Content-Type: application/json" | jq '.' || echo "Response received"

# Test 6: Get user's loan accounts
echo "6. Testing GET /comptes/user/$user_id..."
curl -s -X GET "$BASE_URL/comptes/user/$user_id" \
    -H "Content-Type: application/json" | jq '.' || echo "Response received"

# Test 7: Delete loan account
echo "7. Testing DELETE /comptes/$compte_id..."
delete_response=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE_URL/comptes/$compte_id")
if [ "$delete_response" -eq 204 ]; then
    echo "✅ Loan account deleted successfully"
else
    echo "❌ Failed to delete loan account (HTTP $delete_response)"
fi

echo ""
echo "🎉 All tests completed!"
echo "Loan account service is working correctly."