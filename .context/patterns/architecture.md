# Architecture rules (II.3, IV.2, VI.7)

AR-4…AR-8 are enforced by `ArchitectureTest` (ArchUnit) in `brasil4j-core`; AR-1…AR-3 by the module POMs and review. Human overview: [docs/arquitetura.md](../../docs/arquitetura.md).

## Modules

- **AR-1** `brasil4j-core`: Java only, no runtime dependencies besides the JDK.
- **AR-2** `brasil4j-kotlin`: depends only on `brasil4j-core` and the Kotlin stdlib; adapters only, no rule logic (KA-1).
- **AR-3** `brasil4j-exemplos`: test-only, never published.

## Packages (`io.github.brasil4j`)

| Package | Role | May depend on |
| --- | --- | --- |
| `verificacao` | Check-digit algorithms | JDK only |
| `mascara` | Declarative masks | JDK only |
| `identidade` | Document contracts and parsers | `verificacao`, `mascara` |
| `identidade.cpf`, `.cnpj`, `.nis` | Document types | `identidade`, `verificacao`, `mascara` |
| `identidade.composicao` | `DocumentCheck` | `identidade` and its document packages |
| `geografia` | IBGE codes and UFs | JDK only |
| `financas` | Money, rounding, exchange | JDK only |

- **AR-4** `verificacao` and `mascara` depend on no bounded context.
- **AR-5** `identidade`, `geografia` and `financas` do not depend on each other.
- **AR-6** `identidade.cpf`, `.cnpj` and `.nis` do not depend on each other; only `composicao` sees all of them.
- **AR-7** No package cycles.
- **AR-8** Core production code does not use `java.util.logging`, `System.out`/`System.err` or `java.lang.reflect`.

A new package or context updates this table, the ArchUnit test and `docs/arquitetura.md` in the same change.
