#!/usr/bin/env python3
"""
Fix Java configuration files with syntax errors
Run: python3 fix_java_syntax.py
"""

import os
import re

def count_braces(content):
    """Count opening and closing braces"""
    open_braces = content.count('{')
    close_braces = content.count('}')
    return open_braces, close_braces

def count_parentheses(content):
    """Count opening and closing parentheses"""
    open_parens = content.count('(')
    close_parens = content.count(')')
    return open_parens, close_parens

def fix_file(filepath):
    """Fix common syntax issues in Java file"""
    if not os.path.exists(filepath):
        print(f"File not found: {filepath}")
        return False
    
    print(f"\nChecking {os.path.basename(filepath)}...")
    
    with open(filepath, 'r') as f:
        content = f.read()
    
    original_content = content
    fixed = False
    
    # Count braces
    open_b, close_b = count_braces(content)
    print(f"  Braces: {open_b} open, {close_b} close")
    
    # Count parentheses
    open_p, close_p = count_parentheses(content)
    print(f"  Parentheses: {open_p} open, {close_p} close")
    
    # Fix missing closing braces
    if open_b > close_b:
        diff = open_b - close_b
        print(f"  Adding {diff} closing brace(s)")
        content += '\n' + '}\n' * diff
        fixed = True
    
    # Fix missing closing parentheses (be careful with this)
    if open_p > close_p:
        diff = open_p - close_p
        print(f"  WARNING: Missing {diff} closing parenthesis/es")
        # Don't auto-fix parentheses as they need context
    
    # Check for common patterns that cause syntax errors
    lines = content.split('\n')
    fixed_lines = []
    in_comment = False
    
    for i, line in enumerate(lines, 1):
        # Skip comment blocks
        if '/*' in line:
            in_comment = True
        if '*/' in line:
            in_comment = False
            
        if not in_comment and not line.strip().startswith('//'):
            # Check for incomplete method chains (line ends with .)
            if line.rstrip().endswith('.') and not line.strip().startswith('*'):
                print(f"  Line {i}: Possible incomplete method chain")
            
            # Check for missing semicolons after statements
            stripped = line.strip()
            if stripped and not stripped.endswith((';', '{', '}', ',', '//', '*/')) and \
               any(stripped.startswith(kw) for kw in ['return ', 'throw ', 'break', 'continue']):
                print(f"  Line {i}: Possibly missing semicolon")
                line = line.rstrip() + ';'
                fixed = True
        
        fixed_lines.append(line)
    
    if fixed:
        content = '\n'.join(fixed_lines)
        
        # Backup original file
        backup_path = filepath + '.bak'
        with open(backup_path, 'w') as f:
            f.write(original_content)
        print(f"  Backup saved to {backup_path}")
        
        # Write fixed content
        with open(filepath, 'w') as f:
            f.write(content)
        print(f"  Fixed and saved {filepath}")
        return True
    
    return False

def main():
    """Main function to fix all config files"""
    config_dir = "src/main/java/com/nearstar/sftpmanager/config"
    
    # Files with reported errors
    problem_files = [
        "DatabaseConfig.java",
        "SchedulerConfig.java",
        "SecurityConfig.java",
        "SessionInterceptor.java",
        "SpringDataSourceConnectionProvider.java",
        "WebConfig.java"
    ]
    
    print("Java Syntax Fixer")
    print("=" * 50)
    
    for filename in problem_files:
        filepath = os.path.join(config_dir, filename)
        fix_file(filepath)
    
    print("\n" + "=" * 50)
    print("Fixes complete. Now run: mvn clean compile")
    print("\nIf errors persist, manually check:")
    print("  - DatabaseConfig.java around line 106")
    print("  - SchedulerConfig.java around line 62")
    print("  - SecurityConfig.java around line 39")
    print("  - WebConfig.java around line 68")
    print("\nLook for:")
    print("  - Incomplete lambda expressions")
    print("  - Unclosed method chains")
    print("  - Missing return statements")
    print("  - Incorrect annotations")

if __name__ == "__main__":
    main()