# Review checklist (single source for plans and reviews)

Use for the plan's Design Quality Gate (I.3) and for every code review or LLM review (VI.9). Report each finding as `[ID] file:line — what — why — fix`, with severity MUST FIX / SHOULD FIX / CONSIDER. Distinguish regressions from pre-existing debt.

| Lens | Check | Rules |
| --- | --- | --- |
| 1. Correctness | Logic, edge cases (null, empty, blank, masks, zeros), exact exception types and messages | III, JA-5, JA-6 |
| 2. Clean code | Names, size, complexity ≤ 10, guard clauses, no boolean modes, no null/exception signaling | DQ-1…DQ-6 |
| 3. SOLID | SRP, OCP, LSP (contract test exists), ISP, DIP | DQ-7…DQ-11 |
| 4. DRY / KISS / YAGNI / reuse | Duplicated knowledge, speculative abstraction, existing blocks reused | DQ-12…DQ-15 |
| 5. Contracts and extensibility | Built from public blocks; consumer can extend without editing the library | DQ-16…DQ-18 |
| 6. Design patterns | Opportunities taken or consciously rejected; no pattern without a problem | DP-1…DP-9 |
| 7. Java library API | Visibility, immutability, compatibility (binary/source/behavioral), deprecation, Javadoc, Java 8 | JA-1…JA-15 |
| 8. Kotlin API and functional style | Delegation, explicit API, no public data class, default-argument compatibility, read-only collections, KDoc | KA-1…KA-13, KF-1…KF-4 |
| 9. Architecture | Module and package dependencies, no cycles, new package documented | AR-1…AR-8 |
| 10. Test quality | TDD evidence, contract tests, behavior assertions, characterization/equivalence, JarIT, examples unchanged, mutation score | TQ-1…TQ-11 |
| 11. Provenance and privacy | "Official" only with confirmed source; scope stated; no sensitive data in diagnostics | V.2…V.6 |
| 12. Delivery | Spec/plan/tasks/analysis in sync; CHANGELOG; PR into the right branch; all checks green | I, VII |

## Gates (evidence, not opinion)

`clean verify` on JDK 8 and 21 · JaCoCo ≥90/85 · japicmp vs latest release tag · bytecode 52 · mutation threshold · DocLint and Dokka clean.

## Verdict

APPROVE / APPROVE WITH CHANGES / REJECT. Any MUST FIX or failed gate means not approved.
