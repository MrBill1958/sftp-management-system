#!/bin/bash

# Git Repository Diagnosis and Fix Script
# Run these commands one by one to diagnose and fix the branch issue

echo "=========================================="
echo "Git Repository Diagnosis"
echo "=========================================="

# Step 1: Check current branch
echo "1. Current branch:"
git branch --show-current

# Step 2: List all branches (local and remote)
echo -e "\n2. All local branches:"
git branch -a

# Step 3: Check git status
echo -e "\n3. Git status:"
git status

# Step 4: Check remote configuration
echo -e "\n4. Remote configuration:"
git remote -v

# Step 5: Check if there are any commits
echo -e "\n5. Checking for commits:"
git log --oneline -1 2>/dev/null || echo "No commits found"

echo -e "\n=========================================="
echo "Fix Options (choose the appropriate one):"
echo "=========================================="

echo -e "\nOption A: If the branch is 'master' and you want to rename it to 'main':"
echo "  git branch -M main"
echo "  git push -u origin main"

echo -e "\nOption B: If the branch is 'master' and you want to keep it:"
echo "  git push origin master"

echo -e "\nOption C: If there are no commits yet:"
echo "  git add ."
echo "  git commit -m 'Initial commit'"
echo "  git branch -M main"
echo "  git push -u origin main"

echo -e "\nOption D: If you need to set up the remote:"
echo "  git remote add origin https://github.com/MrBill1958/sftp-management-system.git"
echo "  git branch -M main"
echo "  git push -u origin main"

echo -e "\n=========================================="
echo "Quick Fix Commands to Run:"
echo "=========================================="
echo "# Run these commands in sequence:"
echo ""
echo "# 1. First, check what branch you're on:"
echo "git branch"
echo ""
echo "# 2. If you see 'master', rename it to 'main':"
echo "git branch -M main"
echo ""
echo "# 3. Push to the remote repository:"
echo "git push -u origin main"
echo ""
echo "# If that doesn't work, try:"
echo "git push -u origin master"