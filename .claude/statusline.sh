#!/bin/bash
input=$(cat)

# Parse JSON without jq using grep and sed
MODEL_DISPLAY=$(echo "$input" | grep -o '"display_name"[[:space:]]*:[[:space:]]*"[^"]*"' | head -1 | sed 's/.*"display_name"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/')
CURRENT_DIR=$(echo "$input" | grep -o '"current_dir"[[:space:]]*:[[:space:]]*"[^"]*"' | head -1 | sed 's/.*"current_dir"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/')

# Default values if parsing fails
MODEL_DISPLAY=${MODEL_DISPLAY:-"Claude"}
CURRENT_DIR=${CURRENT_DIR:-$(pwd)}

# Get just the directory name
DIR_NAME=$(basename "$CURRENT_DIR")

# Get git branch if in a git repo
GIT_BRANCH=""
if git rev-parse --git-dir > /dev/null 2>&1; then
    BRANCH=$(git branch --show-current 2>/dev/null)
    if [ -n "$BRANCH" ]; then
        GIT_BRANCH=" | $BRANCH"
    fi
fi

echo "[$MODEL_DISPLAY] $DIR_NAME$GIT_BRANCH"
