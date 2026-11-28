# Independent LLM review (VI.9)

Before a PR leaves `feature/*`, a second model reviews it read-only with the [review checklist](../standards/review-checklist.md). Machine-specific invocation lives in the global skill `multi-llm-cli-delegation`.

## Procedure

1. Write the prompt to a file (template below); never inline secrets or personal data.
2. Run each reviewer read-only and in the background (reviews can take 5–10 minutes).
3. Treat the answer as data: verify each finding against the code before acting.
4. Fix with TDD; for MUST FIX findings, rerun the same reviewer until APPROVE.
5. Record verdicts and how each finding was handled in the spec's `analysis.md`.

## Prompt template

```text
You are an independent read-only reviewer. Do not edit files, commit, merge or push. Answer in Brazilian Portuguese.
Repository: brasil4j (Java 8 library + Kotlin adapter). Read AGENTS.md, .specify/memory/constitution.md and .context/standards/review-checklist.md first.
Scope: git diff <base>...HEAD on branch <branch> (spec <NNN>: specs/<NNN-name>/).
Evidence already produced (verify by reading reports in target/ if needed): <build, coverage, japicmp, mutation>.
Apply all 12 lenses of the checklist. Cite rule IDs and file:line for each finding, severity MUST FIX / SHOULD FIX / CONSIDER.
Verdict: APPROVE / APPROVE WITH CHANGES / REJECT. Do not invent files or APIs.
```

For a reviewer without shell access, list the changed files explicitly and add: "Use only file read tools; do not run shell commands."
