# Checklist de qualidade da especificação · NIS, PIS, Pasep e NIT

- **Finalidade** Revisar os requisitos antes do planejamento.
- **Data** 2026-11-26
- **Spec** [Conferência estrutural de NIS, PIS, Pasep e NIT](../spec.md)

## Qualidade do conteúdo

- [x] A spec descreve o comportamento esperado sem escolhas de linguagem, framework ou assinatura.
- [x] O primeiro cenário atende à conferência de campos em arquivos trabalhistas históricos.
- [x] As histórias descrevem o valor para quem confere arquivos e registra diagnósticos.
- [x] As seções obrigatórias do template estão preenchidas.

## Completude dos requisitos

- [x] As decisões de escopo estão explícitas e não há marcadores de esclarecimento.
- [x] FR-001 a FR-005 definem condições verificáveis de apresentação, preservação, igualdade e ocultação.
- [x] SC-001 a SC-004 têm resultados mensuráveis e independentes da implementação.
- [x] As histórias têm cenários de aceite e podem ser verificadas separadamente.
- [x] Os limites cobrem ausência, tamanho, zeros iniciais, repetições, DV divergente, máscara, sinais, espaços, pontuação e dígitos Unicode.
- [x] A conferência opera sobre o campo textual extraído. O consumidor aplica as regras de preenchimento, cadastro e interpretação do registro.
- [x] As fontes, a matriz de vetores sintéticos e a ausência de compromisso de adoção estão registradas.

## Prontidão para planejamento

- [x] FR-001 a FR-003 têm cobertura nos casos 1 a 4 da história 1 e em SC-001 e SC-002.
- [x] FR-004 tem cobertura no caso 5 da história 1 e em SC-003.
- [x] FR-005 tem cobertura na história 2 e em SC-004.
- [x] O fluxo principal e os diagnósticos têm resultados observáveis para o consumidor.
- [x] Escolhas de implementação ficam para o plano do Spec Kit.

## Evidência da revisão

O resolvedor do Spec Kit localizou a Spec 006. Os links internos foram conferidos. A classificação de SC-002 coincide com a gramática de 11 dígitos ASCII em todos os 22 casos da matriz do C1. Os itens deste checklist avaliam a qualidade dos requisitos.

O aditamento FR-006/FR-007 tem cenários de aceite na US3, critério mensurável em SC-005 e cobertura planejada em T018–T022. A invariante de forma e a representação padrão permanecem no objeto de valor.
