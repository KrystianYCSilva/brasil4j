# Research: Fundação JVM

## Decision

O Maven 3.9 executa em Java 8. O Compiler Plugin converte release 8 para source/target quando a JVM de build já é 8. A mesma versão do Wrapper é usada em ambos os JDKs. Dokka usa o JDK 21 da matriz para verificar KDoc, preservando a verificação do runtime 8.

## Rationale

Preparar o reator Java/Kotlin e as verificações repetíveis de build, empacotamento e documentação.

## Alternatives Considered

A descoberta comparou correção local, uso de artefato de terceiros e implementação independente. A decisão desta entrega é usar a estrutura própria, com regras rastreáveis e compatibilidade JVM. [Evidências do C1](../../docs/descoberta/c1/README.md) · [Fontes](../../docs/fontes.md).
