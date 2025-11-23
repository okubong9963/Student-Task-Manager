#!/bin/bash

# compile-and-run.sh
# Script to compile and run the Student Task Manager application
# Make sure to set JAVAFX_PATH to your JavaFX SDK location

# IMPORTANT: Update this path to your JavaFX SDK location
JAVAFX_PATH="/Users/goddaffi/javafx-sdk-17.0.17/lib"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Student Task Manager - Compile and Run Script${NC}"
echo "================================================"

# Check if JAVAFX_PATH is set correctly
if [ "$JAVAFX_PATH" = "/path/to/javafx-sdk-17/lib" ]; then
    echo -e "${RED}ERROR: Please update JAVAFX_PATH in this script to point to your JavaFX SDK!${NC}"
    echo "Download JavaFX SDK from: https://openjfx.io/"
    exit 1
fi

# Check if JavaFX path exists
if [ ! -d "$JAVAFX_PATH" ]; then
    echo -e "${RED}ERROR: JavaFX path does not exist: $JAVAFX_PATH${NC}"
    exit 1
fi

# Navigate to src directory
cd src

echo -e "${GREEN}Compiling Java files...${NC}"
javac --module-path "$JAVAFX_PATH" --add-modules javafx.controls Task.java FileHelper.java Main.java

if [ $? -eq 0 ]; then
    echo -e "${GREEN}Compilation successful!${NC}"
    echo ""
    echo -e "${GREEN}Running application...${NC}"
    java --module-path "$JAVAFX_PATH" --add-modules javafx.controls Main
else
    echo -e "${RED}Compilation failed!${NC}"
    exit 1
fi

