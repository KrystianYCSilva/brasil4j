# brasil4j context index

Read the [constitution](../.specify/memory/constitution.md) first, then open only the files the task needs. Rule IDs (`VI.4`) refer to the constitution.

| Task | Load |
| --- | --- |
| Plan or review any change | [review-checklist](standards/review-checklist.md) |
| Design classes, contracts, refactors | [design-quality](standards/design-quality.md), [design-patterns](patterns/design-patterns.md) |
| Change public Java API | [java-library-api](standards/java-library-api.md) |
| Change the Kotlin adapter | [kotlin-library-api](standards/kotlin-library-api.md) |
| Add a package, module or dependency | [architecture](patterns/architecture.md), [docs/arquitetura.md](../docs/arquitetura.md) |
| Write or review tests | [testing](standards/testing.md) |
| Branch, PR, release, CI | [git-flow](workflows/git-flow.md) |
| Ask another LLM for a review | [llm-review](workflows/llm-review.md) |
| Add a Brazilian rule or vector | [docs/fontes.md](../docs/fontes.md) and the spec in `specs/` |

Delivery workflow: Spec Kit (`speckit-specify` → `clarify` → `plan` → `tasks` → `analyze` → `implement`). Global guides in `~/.agents` are optional and language-agnostic; this folder is authoritative for brasil4j.
