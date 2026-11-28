# Design quality (VI.1–VI.3)

Checks a planner or reviewer can verify. Cite the ID in findings.

## Clean code (VI.1)

- **DQ-1** Names reveal intent and one word per concept (`parse` everywhere, not `parse`/`read`/`of` for the same idea).
- **DQ-2** One responsibility per type and per method; cyclomatic complexity ≤ 10 per method (11–15 should fix, > 15 must fix).
- **DQ-3** Guard clauses over nesting; happy path last and flat.
- **DQ-4** No boolean mode parameters; use an enum of modes, a separate method or a strategy.
- **DQ-5** No `null` as a signal and no exceptions for expected outcomes; return a result type (`ValidationResult`) or `Optional`.
- **DQ-6** Comments explain why (rule, source, trade-off), never what.

## SOLID (VI.1)

- **DQ-7 SRP** Mask, check digit, rule, parser, value, presentation and orchestration live in separate types.
- **DQ-8 OCP** A new document, mode or rule is a new class or configuration, not an edited `switch`/`if` chain.
- **DQ-9 LSP** Every implementation honors its interface contract; enforced by a shared contract test (TQ-3).
- **DQ-10 ISP** Small capability interfaces (`MaskedDocument`, `CheckDigitDocument`); a type implements only what it can honor.
- **DQ-11 DIP** Consumers depend on interfaces (`DocumentParser<D>`); concrete types are created by static factories or builders.

## DRY, KISS, YAGNI, reuse (VI.2)

- **DQ-12** DRY removes duplicated knowledge, not lookalike code; extract on the third real occurrence.
- **DQ-13** Composition over inheritance; inheritance only for a skeletal base (`AbstractDocument`) with no state beyond the value.
- **DQ-14** No abstraction without a current second use or a stated consumer extension point.
- **DQ-15** Reuse existing blocks (`DocumentMask`, `Modulus11`, `ValidationRule`, `DocumentParsers`) before writing new parsing, masking or check-digit code.

## Contracts and extensibility (VI.3)

- **DQ-16** Library types are built with the same public blocks a consumer would use (dogfooding); a test proves a consumer-defined type works end to end.
- **DQ-17** Injectable strategies over static utilities; static helpers are private or package-private.
- **DQ-18** Value objects over primitives for domain concepts (primitive obsession is a must-fix in public API).
