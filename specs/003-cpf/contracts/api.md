# Contract: CPF

Java fornece `Cpf.parse(input[, mode])`, `CpfValidator.isValid(input[, mode])` e `CpfFormatter.format/unformat(input)`. O objeto expõe `getCanonicalValue/getBase/getCheckDigits/applyMask/hasValidCheckDigits`. Kotlin fornece `Cpf(texto)`, `String.toCpf([mode])` e `toJava()`.

## Validação da apresentação

Cada modo recebe a entrada original e só devolve o valor canônico depois de conferir o texto completo. A remoção de pontuação só ocorre para uma máscara completa. FORMAT_ONLY aplica essas mesmas regras de apresentação e dispensa a conferência matemática do DV.

## Aceite

- A conferência numérica reproduz o exemplo publicado no manual da e-Financeira.
- Java e Kotlin concordam no modo padrão, no modo explícito, no canônico e na representação segura.
