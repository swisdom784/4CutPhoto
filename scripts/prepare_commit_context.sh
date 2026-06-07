#!/usr/bin/env bash
set -euo pipefail

# This script prepares a local text context for commit-message drafting.
# It does not call any AI API.
# Review staged changes before running this script, and do not include secrets,
# internal URLs, real user data, account information, or private logs.

OUTPUT_FILE="build/commit-message-context.txt"

if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  echo "This script must be run inside a git repository."
  exit 1
fi

if git diff --cached --quiet; then
  echo "No staged diff found."
  echo "Stage files first, then run: scripts/prepare_commit_context.sh"
  echo "Example: git add <files>"
  exit 0
fi

mkdir -p build

{
  echo "# Commit Message Context"
  echo
  echo "This file is a local prompt context for drafting a commit message."
  echo "Do not paste secrets, internal URLs, real user data, account information, or private logs into AI tools."
  echo "Generated commit messages are drafts. A human must review the final message."
  echo
  echo "## Developer Inputs"
  echo
  echo "- ticket id: [SECMF-9999]"
  echo "- cell: (D|U|P)"
  echo "- action candidate: add|fix|apply|restore|separate|update|refactor"
  echo "- issue symptom: <describe user-visible or developer-visible issue>"
  echo "- root cause: <describe why the issue happened>"
  echo "- verification: <for example :app:testDebugUnitTest, :app:assembleDebug>"
  echo "- impact scope: <describe changed area and unaffected area>"
  echo
  echo "## Commit Title Format"
  echo
  echo '"[action] [subject] ([cell]) [[ticket id]] - [optional detail]"'
  echo
  echo "## Body Format"
  echo
  echo "1. 이슈 현상"
  echo "2. 원인 분석"
  echo "3. 해결 방법"
  echo "4. 검증 방법"
  echo "5. 영향 범위"
  echo
  echo "## Staged Files"
  echo
  git diff --cached --name-only
  echo
  echo "## Staged Diff"
  echo
  git diff --cached --no-ext-diff
} > "$OUTPUT_FILE"

echo "Commit message context written to $OUTPUT_FILE"
echo "Review the file before sharing it with any AI tool."
