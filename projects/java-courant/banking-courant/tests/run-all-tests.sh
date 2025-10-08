#!/bin/bash

# Banking API Test Runner
# This script runs all test suites in order

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TESTS_DIR="$BASE_DIR"
BASE_URL="http://localhost:8080/api"

echo "🏦 Banking API Test Suite Runner"
echo "================================="
echo "Base Directory: $BASE_DIR"
echo "API Base URL: $BASE_URL"
echo ""

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

# Function to run a test script
run_test() {
    local test_script="$1"
    local test_name="$2"
    
    echo "🧪 Running: $test_name"
    echo "   Script: $test_script"
    echo "   Time: $(date)"
    echo ""
    
    if [ -f "$test_script" ]; then
        chmod +x "$test_script"
        if bash "$test_script"; then
            echo "✅ $test_name completed successfully"
        else
            echo "❌ $test_name failed"
            return 1
        fi
    else
        echo "❌ Test script not found: $test_script"
        return 1
    fi
    
    echo ""
    echo "⏱️  Waiting 2 seconds before next test..."
    sleep 2
    echo ""
}

# Track test results
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to track test results
track_result() {
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    if [ $1 -eq 0 ]; then
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

echo "📋 Test Execution Plan:"
echo "1. User Management Tests"
echo "2. Account Management Tests"  
echo "3. Transaction Management Tests"
echo "4. Integration Scenario Tests"
echo ""
echo "🚀 Starting test execution..."
echo ""

# Run Test Suite 1: User Management
run_test "$TESTS_DIR/1-test-user-management/user-crud-tests.sh" "User Management Tests"
track_result $?

# Run Test Suite 2: Account Management
run_test "$TESTS_DIR/2-test-account-management/account-crud-tests.sh" "Account Management Tests"
track_result $?

# Run Test Suite 3: Transaction Management
run_test "$TESTS_DIR/3-test-transaction-management/transaction-tests.sh" "Transaction Management Tests"
track_result $?

# Run Test Suite 4: Integration Scenarios
run_test "$TESTS_DIR/4-test-integration-scenarios/full-workflow-test.sh" "Integration Scenario Tests"
track_result $?

# Run Test Suite 5: Tax Functionality
run_test "$TESTS_DIR/5-test-tax-functionality/tax-tests.sh" "Tax Functionality Tests"
track_result $?

# Test Results Summary
echo "📊 TEST EXECUTION SUMMARY"
echo "========================="
echo "Total Tests Run: $TOTAL_TESTS"
echo "Passed: $PASSED_TESTS"
echo "Failed: $FAILED_TESTS"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo "🎉 ALL TESTS PASSED! Banking API is working correctly."
    exit 0
else
    echo "⚠️  $FAILED_TESTS test(s) failed. Please check the output above for details."
    exit 1
fi
