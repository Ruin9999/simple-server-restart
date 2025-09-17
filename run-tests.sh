#!/bin/bash

# Test runner script for Simple Server Restart Plugin
# This script can be used to run tests when the proper Minecraft development environment is set up

echo "Simple Server Restart Plugin - Test Runner"
echo "==========================================="

# Check if gradle wrapper exists
if [ ! -f "./gradlew" ]; then
    echo "Error: gradlew not found. Please run this script from the project root directory."
    exit 1
fi

# Check if test directory exists
if [ ! -d "src/test" ]; then
    echo "Error: Test directory not found."
    exit 1
fi

echo "Found test files:"
find src/test -name "*.java" | wc -l | xargs echo "- Java test files:"

echo ""
echo "Test Coverage:"
echo "- RestartServiceTest.java       - Tests the restart scheduling service"
echo "- RestartHelperTest.java        - Tests restart script execution logic"
echo "- SimpleServerRestartCommandTest.java - Tests command permissions and execution"
echo "- IntegrationTest.java          - Tests end-to-end restart flow"

echo ""
echo "Running tests..."
echo "Note: This requires a proper Minecraft Fabric development environment with:"
echo "- Minecraft mappings"
echo "- Fabric API"
echo "- Fabric Loader"
echo "- JUnit 5 and Mockito (automatically downloaded)"

# Try to run tests
./gradlew test

# Check exit code
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ All tests passed!"
else
    echo ""
    echo "❌ Some tests failed or build environment needs setup."
    echo "Make sure you have the proper Minecraft development environment configured."
fi