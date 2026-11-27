# Checklist de qualidade da especificação · Composição de CPF, CNPJ e NIS

- **Finalidade** Revisar os requisitos antes do planejamento.
- **Data** 2026-11-27
- **Spec** [Composição de validação e formatação](../spec.md)

## Qualidade do conteúdo

- [x] A spec define os comportamentos e deixa assinaturas e estruturas para o plano.
- [x] O primeiro cenário trata de uma lista de campos de tipos distintos com decisão e saída por item.
- [x] As histórias descrevem os resultados úteis ao integrador e a quem acompanha a conferência.
- [x] Todas as seções obrigatórias do template estão preenchidas.

## Completude dos requisitos

- [x] Escopo e pressupostos estão explícitos e sem marcadores de esclarecimento.
- [x] FR-001 a FR-010 têm desfechos ou condições observáveis.
- [x] SC-001 a SC-006 têm medidas verificáveis e independentes da implementação.
- [x] Cada história contém um ensaio independente e cenários de aceite.
- [x] As bordas cobrem lista vazia, repetição de item, nulos, espaços, máscara, falhas e saída vazia.
- [x] A travessia pertence ao consumidor e o limite com a Spec 006 e a futura CTPS está explícito.
- [x] As evidências de descoberta e as bases de compatibilidade estão identificadas.

## Prontidão para planejamento

- [x] US1 e SC-001/SC-002 cobrem o contrato comum, os modos explícitos e a formatação após a conferência.
- [x] US2 e FR-009 cobrem a reutilização da regra do consumidor no validador direto e na lista. SC-003 usa os vetores da UEM para aferir zeros iniciais e bloqueio de callbacks para NIS mascarado.
- [x] US3 e SC-004/SC-005 cobrem reexecução, falhas e diagnósticos. SC-006 cobre preservação dos contratos existentes.
- [x] A especificação descreve a ordem de processamento e distingue os desfechos funcionais de erros de configuração e execução.
- [x] Os exemplos de consumo, o desenho da API e a estratégia de verificação ficam para o plano do Spec Kit.

## Evidência da revisão

Os quatro vetores de NIS foram conferidos com a matriz C1. A lista heterogênea mantém o mesmo texto como CPF e NIS, com decisões diferentes. Os exemplos de CPF e CNPJ com DV divergente foram conferidos com os testes existentes. Este checklist avalia a qualidade dos requisitos.
