#!/bin/bash

# Banking Loan System - Master Test Runner
# This script runs all loan system tests in the correct order

set -e  # Exit on any error

# Configuration
BASE_URL="http://localhost:8081/banking-pret/api"
TEST_DIR="$(dirname "$0")"
PASSED=0
FAILED=0

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Helper functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
    ((PASSED++))
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
    ((FAILED++))
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Test server connectivity
check_server() {
    log_info "Checking server connectivity..."
    if curl -s -f "$BASE_URL/loan-types" > /dev/null 2>&1; then
        log_success "Server is running and accessible"
        return 0
    else
        log_error "Server is not accessible at $BASE_URL"
        log_info "Please start the server with: mvn tomee:run"
        return 1
    fi
}

# Run a test script and capture results
run_test_script() {
    local script_path="$1"
    local script_name
    script_name="$(basename "$script_path")"
    
    log_info "Running $script_name..."
    
    if [ ! -f "$script_path" ]; then
        log_error "Test script not found: $script_path"
        return 1
    fi
    
    if [ ! -x "$script_path" ]; then
        log_warning "Making $script_name executable..."
        chmod +x "$script_path"
    fi
    
    # Run the test script and capture output
    if "$script_path" > /tmp/test_output_$$.log 2>&1; then
        log_success "$script_name completed successfully"
        # Show summary of results
        if grep -q "PASSED\|SUCCESS" /tmp/test_output_$$.log; then
            local passed_count
            passed_count=$(grep -c "PASSED\|SUCCESS" /tmp/test_output_$$.log || echo "0")
            log_info "  → $passed_count tests passed"
        fi
        rm -f /tmp/test_output_$$.log
        return 0
    else
        log_error "$script_name failed"
        log_info "Error details:"
        cat /tmp/test_output_$$.log | head -20
        rm -f /tmp/test_output_$$.log
        return 1
    fi
}

# Main test execution
main() {
    echo "============================================"
    echo "Banking Loan System - Test Suite Runner"
    echo "============================================"
    echo
    
    # Check prerequisites
    if ! check_server; then
        exit 1
    fi
    
    echo
    log_info "Starting comprehensive test suite..."
    echo
    
    # Test order is important - dependencies between test categories
    
    # 1. Loan Type Management Tests
    echo "1. LOAN TYPE MANAGEMENT TESTS"
    echo "=============================="
    run_test_script "$TEST_DIR/1-test-loan-types/loan-type-tests.sh"
    echo
    
    # 2. Loan Account Management Tests  
    echo "2. LOAN ACCOUNT MANAGEMENT TESTS"
    echo "================================="
    run_test_script "$TEST_DIR/2-test-loan-management/loan-crud-tests.sh"
    echo
    
    # 3. Payment Management Tests
    echo "3. PAYMENT MANAGEMENT TESTS"
    echo "==========================="
    run_test_script "$TEST_DIR/3-test-payment-management/payment-tests.sh"
    echo
    
    # 4. Integration Scenario Tests
    echo "4. INTEGRATION SCENARIO TESTS"
    echo "============================="
    run_test_script "$TEST_DIR/4-test-integration-scenarios/full-loan-workflow-test.sh"
    echo
    
    # Test Summary
    echo "============================================"
    echo "TEST SUITE SUMMARY"
    echo "============================================"
    echo -e "Test Categories Passed: ${GREEN}$PASSED${NC}"
    echo -e "Test Categories Failed: ${RED}$FAILED${NC}"
    echo -e "Total Categories: $((PASSED + FAILED))"
    echo
    
    if [ $FAILED -eq 0 ]; then
        echo -e "${GREEN}🎉 ALL TEST CATEGORIES COMPLETED SUCCESSFULLY!${NC}"
        echo
        log_info "The loan system is ready for production use."
        log_info "All core functionality has been validated:"
        echo "  ✅ Loan type management"
        echo "  ✅ Loan account creation and management"
        echo "  ✅ Payment processing and amortization"
        echo "  ✅ End-to-end integration scenarios"
        exit 0
    else
        echo -e "${RED}❌ SOME TEST CATEGORIES FAILED${NC}"
        echo
        log_error "Please review the failed tests and fix any issues."
        log_info "Check the server logs for detailed error information:"
        log_info "  tail -f target/apache-tomee/logs/catalina.out"
        exit 1
    fi
}

# Cleanup function for interrupted tests
cleanup() {
    log_info "Test execution interrupted. Cleaning up..."
    rm -f /tmp/test_output_$$.log
}

# Set up signal handlers
trap cleanup INT TERM

# Run main function
main "$@"