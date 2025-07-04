#!/bin/bash

echo "Starting MCP Alarm Center..."
echo

echo "Checking Java version..."
java -version
if [ $? -ne 0 ]; then
    echo "Error: Java is not installed or not in PATH"
    exit 1
fi

echo
echo "Building and starting the application..."
mvn spring-boot:run 