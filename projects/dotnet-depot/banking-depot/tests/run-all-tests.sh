#!/bin/bash

# Banking Depot API Test Runner
# This script runs all test suites in order

BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TESTS_DIR="$BASE_DIR"
BASE_URL="http://127.0.0.4:8080/api"
JAVA_SERVICE_URL="http://127.0.0.2:8080/api"

echo "🏦 Banking Depot API Test Suite Runner"
echo "======================================"
echo "Base Directory: $BASE_DIR"
echo "API Base URL: $BASE_URL"
echo "Java Service URL: $JAVA_SERVICE_URL"
echo ""

# Check if ASP.NET Core server is running
echo "🔍 Checking if ASP.NET Core server is running..."
if ! curl -s -f "$BASE_URL/typecomptesdepots" >/dev/null 2>&1; then
    echo "❌ ERROR: ASP.NET Core server is not running or not accessible at $BASE_URL"
    echo "Please start the ASP.NET Core server before running tests:"
    echo "   cd banking-depot && dotnet run"
    exit 1
fi
echo "✅ ASP.NET Core server is accessible"

# Check if Java service is running
echo "🔍 Checking if Java service is running..."
if ! curl -s -f "$JAVA_SERVICE_URL/users/1" >/dev/null 2>&1; then
    echo "❌ ERROR: Java service is not running or not accessible at $JAVA_SERVICE_URL"
    echo "Please start the Java current account service before running tests:"
    echo "   cd ../java-courant/banking-courant && mvn tomee:run"
    exit 1
fi
echo "✅ Java service is accessible"
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
    echo "=================================================="
    echo ""
}

# Track test results
TESTS_PASSED=0
TESTS_FAILED=0

# Run test suites in order
echo "🚀 Starting test execution..."
echo ""

# 1. Type Management Tests
if run_test "$TESTS_DIR/1-test-type-management/type-crud-tests.sh" "Type Management Tests"; then
    ((TESTS_PASSED++))
else
    ((TESTS_FAILED++))
fi

# 2. Account Management Tests
if run_test "$TESTS_DIR/2-test-account-management/account-crud-tests.sh" "Account Management Tests"; then
    ((TESTS_PASSED++))
else
    ((TESTS_FAILED++))
fi

# 3. Withdrawal Management Tests
if run_test "$TESTS_DIR/3-test-withdrawal-management/withdrawal-tests.sh" "Withdrawal Management Tests"; then
    ((TESTS_PASSED++))
else
    ((TESTS_FAILED++))
fi

# 4. Interest Calculation Tests
if run_test "$TESTS_DIR/5-test-interest-calculation/interest-calculation-tests.sh" "Interest Calculation Tests"; then
    ((TESTS_PASSED++))
else
    ((TESTS_FAILED++))
fi

# 5. Integration Scenarios (run last as they may depend on previous test data)
if run_test "$TESTS_DIR/4-test-integration-scenarios/full-workflow-test.sh" "Integration Scenarios Tests"; then
    ((TESTS_PASSED++))
else
    ((TESTS_FAILED++))
fi

# Final results
echo "📊 TEST SUMMARY"
echo "==============="
echo "Tests Passed: $TESTS_PASSED"
echo "Tests Failed: $TESTS_FAILED"
echo "Total Tests: $((TESTS_PASSED + TESTS_FAILED))"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo "🎉 ALL TESTS PASSED! 🎉"
    echo "The Banking Depot API is working correctly."
    exit 0
else
    echo "❌ SOME TESTS FAILED"
    echo "Please check the failed tests and fix any issues."
    exit 1
fi