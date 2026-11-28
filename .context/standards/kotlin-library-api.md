# Kotlin library API and functional style (VI.6)

Source: Kotlin "Library authors' guidelines" (kotlinlang.org/docs/api-guidelines-introduction.html and linked pages).

## Adapter role

- **KA-1** `brasil4j-kotlin` delegates every rule to the Java core; it never reimplements validation, masks or check digits.
- **KA-2** Value classes (`@JvmInline value class`) wrap core values; `toJava()` exposes the core type explicitly.
- **KA-3** Core behavior lives in the core types; Kotlin adds extensions (`String.toCpfOrNull()`), not parallel class hierarchies.

## API surface

- **KA-4** Explicit API mode (`-Xexplicit-api=strict`): every public declaration states visibility and type.
- **KA-5** No `data class` in public API (constructor, `copy` and `componentN` break compatibility on change).
- **KA-6** Adding a parameter, even with a default, breaks binary compatibility; `@JvmOverloads` does not protect Kotlin callers. Add an overload instead.
- **KA-7** Never widen or narrow a published return type.
- **KA-8** Accept and return read-only collections; no arrays in public API.
- **KA-9** Validate with `require` (argument) and `check` (state); messages never contain sensitive input (V.4).
- **KA-10** Absence is `null` (`toCpfOrNull`), failure with reason is the core `ValidationResult`; no exceptions for control flow.
- **KA-11** Do not shadow members of Java types with same-named extensions (no `fold` extension on `ValidationResult`).
- **KA-12** Deprecate with `@Deprecated(message, ReplaceWith(...))`; escalate warning → error → hidden across minor releases; remove only in a major release.
- **KA-13** KDoc for every public declaration: what it does, inputs, invalid-input behavior, exceptions; Dokka with no warnings.

## Functional style

- **KF-1** Prefer `val`, immutable data and pure functions; side effects only at the edges.
- **KF-2** Expression style (`when`, `?.let`, `?:`) when it is clearer than branching; loops when they are clearer than chains.
- **KF-3** Higher-order functions take Java SAM types where the core expects them (`Predicate`, `Function`), keeping one contract for both languages.
- **KF-4** No global mutable state and no stateful top-level functions.
