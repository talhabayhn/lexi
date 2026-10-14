---
description: Review current changes with the code-reviewer agent
---
Run the `code-reviewer` agent on the unpushed changes on `main` (`git diff origin/main` plus staged and unstaged changes).
Then fix all blocker and major findings, re-run tests, and report what was fixed and what remains.
