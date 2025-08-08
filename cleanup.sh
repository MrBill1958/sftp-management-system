#!/bin/bash

# SFTP Management System Repository Cleanup Script
# This script will help clean up unnecessary files from your Git repository
# Run this from the root of your repository

echo "=========================================="
echo "SFTP Management System - Repository Cleanup"
echo "=========================================="
echo ""

# Function to remove files/directories and report
remove_if_exists() {
    if [ -e "$1" ]; then
        echo "✓ Removing: $1"
        rm -rf "$1"
    else
        echo "✗ Not found: $1"
    fi
}

# Function to find and remove files by pattern
remove_pattern() {
    local pattern="$1"
    local found=0
    while IFS= read -r -d '' file; do
        echo "✓ Removing: $file"
        rm -rf "$file"
        found=1
    done < <(find . -name "$pattern" -print0 2>/dev/null)
    
    if [ $found -eq 0 ]; then
        echo "✗ No files found matching: $pattern"
    fi
}

echo "Step 1: Removing IntelliJ IDEA files..."
echo "-----------------------------------------"
remove_if_exists ".idea"
remove_pattern "*.iml"
remove_pattern "*.iws"
remove_pattern "*.ipr"
remove_if_exists "out"

echo ""
echo "Step 2: Removing build artifacts..."
echo "-----------------------------------------"
remove_if_exists "target"
remove_if_exists "build"
remove_pattern "*.class"
remove_pattern "*.jar"
remove_pattern "*.war"
remove_pattern "*.ear"

echo ""
echo "Step 3: Removing Tomcat runtime directories..."
echo "-----------------------------------------"
remove_if_exists "work"
remove_if_exists "Catalina"
remove_if_exists "tomcat"
remove_pattern "tomcat.*"

echo ""
echo "Step 4: Removing log files..."
echo "-----------------------------------------"
remove_pattern "*.log"
remove_if_exists "logs"
remove_if_exists "log"

echo ""
echo "Step 5: Removing OS-specific files..."
echo "-----------------------------------------"
remove_pattern ".DS_Store"
remove_pattern "Thumbs.db"
remove_pattern "*~"
remove_pattern "*.swp"
remove_pattern "*.swo"
remove_pattern "*.bak"
remove_pattern "*.tmp"
remove_pattern "*.temp"
remove_pattern "*.orig"

echo ""
echo "Step 6: Removing Eclipse files (if any)..."
echo "-----------------------------------------"
remove_if_exists ".settings"
remove_if_exists ".project"
remove_if_exists ".classpath"
remove_if_exists "bin"

echo ""
echo "Step 7: Removing VS Code files (if any)..."
echo "-----------------------------------------"
remove_if_exists ".vscode"

echo ""
echo "Step 8: Checking for sensitive files..."
echo "-----------------------------------------"
# Just warn about these, don't auto-remove
check_sensitive() {
    if [ -e "$1" ]; then
        echo "⚠️  WARNING: Found sensitive file: $1 - Consider removing manually"
    fi
}

check_sensitive ".env"
check_sensitive "*.pem"
check_sensitive "*.ppk"
check_sensitive "*.key"
check_sensitive "id_rsa"
check_sensitive "id_rsa.pub"

echo ""
echo "=========================================="
echo "Cleanup Summary"
echo "=========================================="

# Check if .gitignore exists
if [ ! -f ".gitignore" ]; then
    echo "⚠️  No .gitignore file found! You should add one."
    echo "   A template has been provided - copy it to .gitignore"
else
    echo "✓ .gitignore file exists"
fi

echo ""
echo "Next steps:"
echo "1. Review the cleanup results above"
echo "2. Make sure you have a proper .gitignore file"
echo "3. Commit the .gitignore file: git add .gitignore && git commit -m 'Add comprehensive .gitignore'"
echo "4. If files were already tracked, remove them from Git:"
echo "   git rm -r --cached ."
echo "   git add ."
echo "   git commit -m 'Clean up repository and apply .gitignore'"
echo "5. Push changes: git push origin main"
echo ""
echo "Cleanup complete!"