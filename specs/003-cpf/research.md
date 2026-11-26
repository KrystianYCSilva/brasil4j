# Research: CPF

## Decision

A IN RFB 2.172/2024 contextualiza o cadastro. O manual e-Financeira v2.0.1 do módulo de repasse publica a regra REGRA_VALIDA_CPF para seu leiaute. A verificação fica explícita e opcional. O catálogo público de dados descreve 11 posições.

## Rationale

Separar interpretação estrutural de CPF e verificação de DV, protegendo sua representação textual padrão.

## Alternatives Considered

A descoberta comparou correção local, uso de artefato de terceiros e implementação independente. A decisão desta entrega é usar a estrutura própria, com regras rastreáveis e compatibilidade JVM. [Evidências do C1](../../docs/descoberta/c1/README.md) · [Fontes](../../docs/fontes.md).
