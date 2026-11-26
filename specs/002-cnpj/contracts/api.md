# Contract: CNPJ

Java fornece `Cnpj.parse(input[, mode])`, `CnpjValidator.isValid(input[, mode])` e `CnpjFormatter.format/unformat(input)`. O valor oferece `getCanonicalValue/getBase/getRoot/getBranch/getCheckDigits/getType/applyMask`. Kotlin fornece `Cnpj(texto)`, `String.toCnpj([mode])` e `toJava()`. As exceções de parsing são `IllegalArgumentException`, sem texto de entrada.

## Validação da apresentação

Cada modo recebe a entrada original e só devolve o valor canônico depois de conferir o texto completo. A remoção de pontuação só ocorre para uma máscara completa. FORMAT_ONLY aplica essas mesmas regras de apresentação e dispensa a conferência matemática do DV.

## Aceite

- A matriz original de 27 casos é reproduzida no modo padrão.
- Java e Kotlin demonstram o valor e os modos em JDK 8 e 21.
