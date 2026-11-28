# brasil4j

Biblioteca de domínio brasileiro para Java e Kotlin, com Java 8 como versão mínima.

| Artefato | Coordenadas | API |
| --- | --- | --- |
| Núcleo | `io.github.brasil4j:brasil4j-core` | Objetos de valor e regras de CNPJ, CPF, NIS, CTPS histórica, geografia e finanças (moedas, rateio, câmbio, calculadora, percentuais e tabela progressiva), com contratos de documento, verificação de DV e máscaras |
| Kotlin | `io.github.brasil4j:brasil4j-kotlin` | Value classes, operadores e extensões sobre o núcleo |
| Exemplos *(somente repositório, sem deploy)* | [brasil4j-exemplos](brasil4j-exemplos/README.md) | Suítes de teste comentadas em Java e Kotlin demonstrando o uso prático da biblioteca |

Execute `sh ./mvnw clean verify` ou `.\mvnw.cmd clean verify` no Windows. O build verifica testes, cobertura e JavaDoc. Em JDK 21, o verify verifica KDoc com Dokka e gera a referência Kotlin em target/dokka. A versão 0.2.0 é a última lançada, como tag e GitHub Release. A 0.3.0 está em preparação (`0.3.0-SNAPSHOT` em `develop`; veja o [CHANGELOG](CHANGELOG.md)). A publicação no Maven Central depende da verificação do namespace e ainda não ocorreu. [Guia do bundle local e do namespace](specs/004-publicacao-central/quickstart.md).

## Especificações

- **Spec 001** · [Fundação JVM](specs/001-fundacao/spec.md)
- **Spec 002** · [CNPJ](specs/002-cnpj/spec.md)
- **Spec 003** · [CPF](specs/003-cpf/spec.md)
- **Spec 004** · [Publicação no Central](specs/004-publicacao-central/spec.md)
- **Spec 005** · [Código IBGE de município e UF](specs/005-municipio-ibge-uf/spec.md)
- **Spec 006** · [Conferência estrutural de NIS, PIS, Pasep e NIT](specs/006-nis-estrutural/spec.md)
- **Spec 007** · [Composição de identidade](specs/007-composicao-identidade/spec.md)
- **Spec 008** · [Núcleo monetário, rateio sem perda e conversão cambial](specs/008-nucleo-monetario/spec.md)
- **Spec 009** · [Módulo vitrine de exemplos de utilização](specs/009-exemplos-utilizacao/spec.md)
- **Spec 010** · [Contratos, polimorfismo e composição para documentos](specs/010-contratos-documentos/spec.md)
- **Spec 011** · [Calculadora financeira funcional, percentuais com gross-up e tabela progressiva](specs/011-calculadora-percentual-faixas/spec.md)
- **Spec 014** · [Conferência histórica de CTPS](specs/014-ctps-historica/spec.md)

[Contexto de engenharia](.context/README.md) · [Requisitos](docs/requisitos.md) · [Arquitetura](docs/arquitetura.md) · [Constituição](.specify/memory/constitution.md) · [Fontes](docs/fontes.md) · [Descoberta do C1](docs/descoberta/c1/README.md)

## Uso dos documentos

- [CNPJ em Java e Kotlin](specs/002-cnpj/quickstart.md). O modo padrão valida o DV de CNPJs numéricos e alfanuméricos.
- [CPF em Java e Kotlin](specs/003-cpf/quickstart.md). O modo padrão verifica o formato. Escolha NUMERIC para conferir o DV.
- [NIS em Java e Kotlin](specs/006-nis-estrutural/quickstart.md). Use `Nis.parse(campo)` em Java ou `campo.toNis()` em Kotlin para conferir os 11 dígitos ASCII e preservar zeros à esquerda. A saída padrão oculta todo o número. Por padrão o NIS confere só a forma. Os validadores de DV são opcionais e nomeados pela fonte: `NisValidationMode.CAIXA_FGTS_V016` e `LEGACY_MOD11`, mais a guarda `NisRules.repeatedDigits()`. [Fontes e alcance](docs/fontes.md).
- [Composição de documentos em Java e Kotlin](specs/007-composicao-identidade/quickstart.md). Use `DocumentCheck` para configurar validação nativa, política adicional e apresentação opcional em listas heterogêneas de CPF, CNPJ e NIS. `DocumentCheck.of(campo, parser)` aceita também documentos do consumidor.
- [Contratos de documento](specs/010-contratos-documentos/quickstart.md). `Cpf.parser(mode)`, `Cnpj.parser(mode)` e `Nis.parser(mode)` devolvem um `DocumentParser` injetável. `parse(campo)` devolve `ValidationResult` com o motivo da rejeição, sem exceção. `withPolicy(...)` acrescenta regras do negócio. `applyMask()` devolve a máscara completa e `applySecurityMask()` a máscara de segurança (`***.345.678-**`). Os utilitários `*Validator` e `*Formatter` continuam funcionando, mas estão deprecados.
- [CTPS histórica em Java e Kotlin](specs/014-ctps-historica/quickstart.md). Confira os campos extraídos do S-2200 v2.5 com `CtpsProfile.ESOCIAL_S2200_V2_5`. Para a CTPS Digital, confira o CPF pelo modo adequado ao seu fluxo.
- [Código IBGE de município e UF em Java e Kotlin](specs/005-municipio-ibge-uf/quickstart.md). Consulte o prefixo e compare a UF. Use `Uf.fromName("sao paulo")` para obter a UF e consultar nome, sigla, capital, código IBGE e DDDs.
- [Dinheiro, Moedas, Rateio e Câmbio em Java e Kotlin](specs/008-nucleo-monetario/quickstart.md). Use `Real`, `Dolar`, `Euro` ou `Dinheiro` com política explícita (`ABNT_NBR_5891`, `FINANCEIRO`, `TRUNCAMENTO`), `ratear`/`alocar` sem perda de centavos e `TaxaCambio` reversível.
- [Exemplos executáveis (módulo `brasil4j-exemplos`)](brasil4j-exemplos/README.md). Suítes comentadas em Java e Kotlin para todos os domínios, executadas em `verify` e não publicadas.
- [Calculadora Financeira, Percentuais e Tabela Progressiva em Java e Kotlin](specs/011-calculadora-percentual-faixas/quickstart.md). Use `CalculadoraFinanceira` para fórmulas intermediárias em `DECIMAL128`, `Percentual` para descontos/acréscimos conciliáveis e *gross-up* (`calcularBasePorDentro`) e `TabelaProgressiva` para apuração marginal parametrizada.

## Criando seu próprio documento

Os tipos da biblioteca são construídos com os mesmos blocos públicos que você pode usar: `AbstractDocument` (igualdade e `toString` oculto), `DocumentMask` (apresentação), `Modulus11` (DV) e `DocumentParsers` (interpretação). O documento resultante entra nos mesmos fluxos de CPF, CNPJ e NIS, inclusive em `DocumentCheck.of(...)`.

```java
final class Matricula extends AbstractDocument implements MaskedDocument {
    private static final DocumentMask MASCARA = DocumentMask.of("999.999-999");
    static final DocumentParser<Matricula> PARSER = DocumentParsers.builder(Matricula::new)
            .mask(MASCARA)
            .checkDigits(Modulus11.builder().weightsFromRight(2, 9).build())
            .errorMessage("Matrícula inválida.")
            .build();

    private Matricula(String canonico) { super(canonico); }

    @Override public String applyMask() { return MASCARA.apply(getCanonicalValue()); }
}

Matricula.PARSER.parse("123.456-780").getRejection();   // Optional[Rejection[INVALID_CHECK_DIGITS]]
```

[Exemplos executáveis](brasil4j-exemplos/src/test/java/io/github/brasil4j/exemplos/java/ContratosExemplosTest.java) · [Contrato da API](specs/010-contratos-documentos/contracts/api.md)
