# Java library API (VI.5, IV.4)

Sources: JLS 8 ch. 13 (binary compatibility); Eclipse "Evolving Java-based APIs" parts 1–2; project japicmp gate.

## Shape

- **JA-1** Minimal visibility: public only what is designed, documented and tested as API; helpers package-private.
- **JA-2** Immutable values: `final` classes, `private final` fields, defensive copies of arrays; `equals`/`hashCode` by value; `toString` never exposes sensitive data (V.4).
- **JA-3** Static factories (`Cpf.parser(mode)`) and builders for multi-step configuration; no public constructors on value types.
- **JA-4** Interfaces are small and `@FunctionalInterface` when they have one abstract method; Java 8 interface static methods are public, so shared helpers go to package-private classes.
- **JA-5** Null policy: parameters reject `null` with `NullPointerException` unless documented as accepted (parse input may be `null`); collections and `Optional` are never `null`.
- **JA-6** Exceptions: `IllegalArgumentException` for invalid arguments, `IllegalStateException` for invalid configuration; messages never contain input. Existing exception types and messages are part of the contract.
- **JA-7** Java 8 only: no APIs from 9+ (`Optional.isEmpty`, `List.of`, `String.isBlank`); verified by bytecode and JDK 8 build.

## Evolution

- **JA-8** Treat binary, source and behavioral compatibility separately; japicmp checks the first two, tests the third.
- **JA-9** Breaking (never in a minor release): removing or renaming a public member; making a class or method `final` or `abstract` (japicmp `METHOD_NOW_FINAL`); narrowing visibility; adding an abstract method to a type clients implement; changing generic parameters; removing or reordering enum constants clients rely on.
- **JA-10** Compatible: adding classes, overloads, static methods and enum constants; widening visibility; inserting a superclass that loses no members.
- **JA-11** `default` methods on published interfaces need a stated reason; they can conflict with another inherited default.
- **JA-12** Generics are designed once: bound type parameters only where needed (`DocumentCheck<T>` stays unbounded for compatibility).
- **JA-13** Deprecation: `@Deprecated` plus `@deprecated` Javadoc naming the replacement; behavior unchanged; removal only in a major release (1.0 for current facades).
- **JA-14** Module name: `Automatic-Module-Name` is fixed per artifact (`io.github.brasil4j.core`, `io.github.brasil4j.kotlin`) and never changes.

## Documentation

- **JA-15** Every public type and member has Javadoc with parameters, return, thrown exceptions, defaults and limits; DocLint `all` with no warnings.
