# Research: CNPJ

## Decision

O Anexo XV da IN RFB 2.119/2022 e o manual do Serpro descrevem o módulo 11 e a conversão ASCII − 48. A matriz C1 distingue regra de apresentação da biblioteca e tolerância do exemplo oficial. O cálculo é compartilhado pelos dois modos de DV.

## Rationale

Implementar a apresentação estrita e três modos de validação Java, com uma value class Kotlin sobre o mesmo objeto de valor.

## Alternatives Considered

A descoberta comparou correção local, uso de artefato de terceiros e implementação independente. A decisão desta entrega é usar a estrutura própria, com regras rastreáveis e compatibilidade JVM. [Evidências do C1](../../docs/descoberta/c1/README.md) · [Fontes](../../docs/fontes.md).

## Cálculo e conferência da conversão ASCII

A validação de apresentação restringe a base aos caracteres ASCII 0–9 e A–Z antes do cálculo. Em Java, subtrair o caractere '0' subtrai o valor 48. Assim 0 vira 0, 9 vira 9, A vira 17 e Z vira 42. A expressão aplica diretamente a conversão descrita no manual.

O primeiro DV usa os 12 caracteres da base. Os pesos percorrem a base da direita para a esquerda, de 2 a 9, e reiniciam em 2. Resto 0 ou 1 produz DV 0. Nos demais casos, o DV é 11 menos o resto. Depois de conferir o primeiro DV informado, o cálculo do segundo usa os 13 caracteres formados pela base e por esse primeiro DV, com a mesma sequência de pesos a partir da direita. No exemplo do manual, a base 12ABC34501DE dá somas 459 e 424 e DVs 35.

A conferência diferencial em Corretto 8u512 comparou 2.313 bases com o código Java do ZIP oficial de 05/11/2024. O conjunto inclui as 26 letras em cada uma das 12 posições, o exemplo do manual e amostras sintéticas de ambos os formatos. Foram comparadas ainda 4.626 alterações de DV. As decisões coincidiram em todos os casos. A suíte do projeto conserva 26 vetores sintéticos calculados com o código de referência, um para cada letra.

[Manual de Cálculo do DV e ZIP oficial](https://www.gov.br/receitafederal/pt-br/centrais-de-conteudo/publicacoes/documentos-tecnicos/cnpj).
