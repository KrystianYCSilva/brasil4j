# Design patterns (VI.4)

Apply a pattern only for a named problem; record considered/applied/rejected patterns in the plan's Design Decisions. Catalog reference: refactoring.guru/design-patterns.

## In use

| Pattern | Where | Problem solved |
| --- | --- | --- |
| Strategy | `CpfValidationMode`, `CnpjValidationMode`, `NisValidationMode`, `CheckDigitAlgorithm`, `DocumentFormatter` | Vary validation, check digit and presentation without conditionals |
| Builder | `DocumentParsers.Builder`, `Modulus11.Builder` | Multi-step configuration of parsers and algorithms |
| Decorator (flattened) | `DocumentParser.withRule`/`withPolicy` | Add consumer rules without subclassing; origin parsed once |
| Chain of Responsibility + Null Object | `ValidationRule.and`, `ValidationRule.accept` | Ordered rules, first rejection wins, no `null` rule |
| Skeletal implementation | `AbstractDocument` | Shared value equality and hidden `toString` |
| Static factory | `Cpf.parser(mode)`, `Rejection.of`, `ValidationResult.valid` | Named creation, cached immutable instances |
| Sealed-like result | `ValidationResult` (`Valid`/`Invalid`) | Total outcome without exceptions on Java 8 |
| Command | `DocumentCheck` | Reusable executable check item |
| Facade (deprecated) | `CpfValidator`, `CnpjFormatter`, … | Keep the published static API working |
| Adapter | `brasil4j-kotlin` value classes | Idiomatic Kotlin over the Java core |

## Opportunity checklist (plan and review)

- **DP-1** Growing `switch`/`if` on a type or mode → Strategy or polymorphism.
- **DP-2** Many optional parameters or telescoping constructors → Builder or static factories.
- **DP-3** Behavior added around a call without changing it → Decorator.
- **DP-4** Ordered checks with early exit → Chain of rules.
- **DP-5** Same skeleton with varying steps → template method only if inheritance is otherwise justified (DQ-13); prefer Strategy.
- **DP-6** Repeated algorithm with different constants → configurable algorithm (as `Modulus11`).

## Reject when

- **DP-7** Only one implementation exists and no consumer extension point is planned (YAGNI).
- **DP-8** The pattern hides a God type or adds a layer that only forwards calls.
- **DP-9** It forces a public API change that breaks compatibility (JA-9) without a release plan.
