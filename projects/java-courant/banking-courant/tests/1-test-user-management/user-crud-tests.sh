#!/bin/bash

# User Management API Tests
BASE_URL="http://localhost:8080/api"
TEST_NAME="User Management Tests"

echo "=== $TEST_NAME ==="
echo "Base URL: $BASE_URL"
echo ""

# Test 1: Create a user
echo "1. Creating a new user..."
echo "POST $BASE_URL/users"
USER_RESPONSE=$(curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "password": "securepassword"
  }' \
  "$BASE_URL/users")

echo "Response: $USER_RESPONSE"
echo ""

# Extract user ID for next tests (assuming jq is available)
USER_ID=$(echo "$USER_RESPONSE" | jq -r '.id // empty' 2>/dev/null)
if [ -z "$USER_ID" ]; then
    USER_ID=1  # Fallback ID
fi

# Test 2: Get all users
echo "2. Getting all users..."
echo "GET $BASE_URL/users"
curl -s -X GET "$BASE_URL/users" | jq '.' 2>/dev/null || curl -s -X GET "$BASE_URL/users"
echo ""
echo ""

# Test 3: Get user by ID
echo "3. Getting user by ID ($USER_ID)..."
echo "GET $BASE_URL/users/$USER_ID"
curl -s -X GET "$BASE_URL/users/$USER_ID" | jq '.' 2>/dev/null || curl -s -X GET "$BASE_URL/users/$USER_ID"
echo ""
echo ""

# Test 4: Get user by email
echo "4. Getting user by email..."
echo "GET $BASE_URL/users/email/john.doe@example.com"
curl -s -X GET "$BASE_URL/users/email/john.doe@example.com" | jq '.' 2>/dev/null || curl -s -X GET "$BASE_URL/users/email/john.doe@example.com"
echo ""
echo ""

# Test 5: Update user
echo "5. Updating user..."
echo "PUT $BASE_URL/users/$USER_ID"
curl -s -X PUT \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Smith",
    "email": "john.smith@example.com",
    "password": "newsecurepassword"
  }' \
  "$BASE_URL/users/$USER_ID" | jq '.' 2>/dev/null || curl -s -X PUT \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Smith",
    "email": "john.smith@example.com",
    "password": "newsecurepassword"
  }' \
  "$BASE_URL/users/$USER_ID"
echo ""
echo ""

# Test 6: Try to get non-existent user (404 test)
echo "6. Testing 404 - Getting non-existent user..."
echo "GET $BASE_URL/users/99999"
curl -s -X GET "$BASE_URL/users/99999" | jq '.' 2>/dev/null || curl -s -X GET "$BASE_URL/users/99999"
echo ""
echo ""

# Test 7: Try to create user with invalid data (400 test)
echo "7. Testing 400 - Creating user with invalid data..."
echo "POST $BASE_URL/users (missing required fields)"
curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "name": ""
  }' \
  "$BASE_URL/users" | jq '.' 2>/dev/null || curl -s -X POST \
  -H "Content-Type: application/json" \
  -d '{
    "name": ""
  }' \
  "$BASE_URL/users"
echo ""
echo ""

echo "=== User Management Tests Completed ==="
echo "Note: User ID $USER_ID was created and can be used for subsequent tests"
