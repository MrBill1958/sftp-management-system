#!/bin/bash

echo "Diagnosing syntax errors in configuration files..."
echo "================================================"

# Function to show lines around error
show_error_context() {
    local file=$1
    local line=$2
    local context=3
    
    if [ -f "$file" ]; then
        echo ""
        echo "File: $file"
        echo "Error around line $line:"
        echo "------------------------"
        
        # Show lines before and after the error
        start=$((line - context))
        end=$((line + context))
        
        # Use sed to show line numbers and content
        sed -n "${start},${end}p" "$file" | nl -v $start
        echo "------------------------"
    fi
}

# Check each problematic file
echo ""
echo "1. DatabaseConfig.java - Errors at lines 106, 113, 115, 127..."
show_error_context "src/main/java/com/nearstar/sftpmanager/config/DatabaseConfig.java" 106
show_error_context "src/main/java/com/nearstar/sftpmanager/config/DatabaseConfig.java" 115
show_error_context "src/main/java/com/nearstar/sftpmanager/config/DatabaseConfig.java" 127

echo ""
echo "2. SchedulerConfig.java - Errors at lines 62, 76, 82..."
show_error_context "src/main/java/com/nearstar/sftpmanager/config/SchedulerConfig.java" 62
show_error_context "src/main/java/com/nearstar/sftpmanager/config/SchedulerConfig.java" 76

echo ""
echo "3. SecurityConfig.java - Error at line 39..."
show_error_context "src/main/java/com/nearstar/sftpmanager/config/SecurityConfig.java" 39

echo ""
echo "4. WebConfig.java - Errors at lines 68, 84, 96..."
show_error_context "src/main/java/com/nearstar/sftpmanager/config/WebConfig.java" 68
show_error_context "src/main/java/com/nearstar/sftpmanager/config/WebConfig.java" 96

echo ""
echo "================================================"
echo "Common causes of these errors:"
echo "1. Missing closing braces }"
echo "2. Missing closing parentheses )"
echo "3. Incomplete lambda expressions"
echo "4. Missing semicolons"
echo "5. Unclosed string literals"
echo ""
echo "To fix manually:"
echo "1. Open each file in your IDE"
echo "2. Look for red underlines at the specified line numbers"
echo "3. Use IDE's auto-fix suggestions (Alt+Enter in IntelliJ)"
echo ""

# Quick check for unbalanced braces
echo "Brace Balance Check:"
echo "-------------------"
for file in src/main/java/com/nearstar/sftpmanager/config/*.java; do
    if [ -f "$file" ]; then
        filename=$(basename "$file")
        open=$(grep -o '{' "$file" | wc -l | tr -d ' ')
        close=$(grep -o '}' "$file" | wc -l | tr -d ' ')
        
        if [ "$open" -ne "$close" ]; then
            echo "❌ $filename: $open open, $close close (UNBALANCED)"
        else
            echo "✅ $filename: $open open, $close close (balanced)"
        fi
    fi
done

echo ""
echo "To see the full file structure, run:"
echo "cat -n src/main/java/com/nearstar/sftpmanager/config/DatabaseConfig.java | sed -n '100,135p'"