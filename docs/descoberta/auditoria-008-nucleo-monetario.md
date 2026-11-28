# Verificação do núcleo monetário · candidato à Spec 008

**Data** 2026-11-27 · **Resultado** Integração bloqueada por divergência na divisão customizada.

A entrega do worktree tem base `5fd9dae`. As alterações estavam locais no início da auditoria e foram registradas durante o trabalho no commit `517280f` da branch `008-nucleo-monetario`. Conferi que os 21 arquivos de código e testes desse commit são idênticos aos que executei em uma cópia isolada da `main` em `18bfe9c`. O `clean verify` passou com 520 testes, sem falhas, erros ou pulos, em Corretto 8u512 e 21.0.13. Os dois artefatos passaram na checagem de bytecode Java 8 e na comparação binária e de fonte com a `main` por japicmp, com as adições do núcleo monetário. A integração não foi aplicada à `main`.

## Contraexemplo reproduzido nos dois JDKs

O [contrato do candidato](../../work/008-nucleo-monetario/specs/008-nucleo-monetario/contracts/api.md) prevê divisão exata sem arredondamento duplo para `dividir(long, MetodoArredondamento)`. `AritmeticaMonetaria.dividirExato` limita a expansão para políticas customizadas e acrescenta um dígito de sinalização sem separar resultados terminantes que precisem de mais casas.

Para `Real.de("1.00").dividir(1099511627776L, politica)` (`2^40`), a política escolhe `CEILING` se o valor for maior que o quociente exato e `FLOOR` caso contrário. Ambas as escolhas respeitam os vizinhos imediatos exigidos pelo contrato.

| Sondagem | Valor |
| --- | --- |
| Quociente decimal exato | `0.0000000000009094947017729282379150390625` |
| Argumento entregue ao callback | `0.0000000000009094947017729282379150391` |
| Saída esperada / observada | `BRL 0.00` / `BRL 0.01` |

O contraexemplo roda contra o JAR do núcleo em `.tmp/audit008/MonetaryAudit.java`. O aceite deve distinguir divisões terminantes e não terminantes e explicitar o que a política customizada recebe nos casos sem expansão decimal finita. A prova atual exige, no mínimo, preservar o quociente terminante exato.

## Pontos de integração a reconciliar

- `docs/fontes.md` no worktree reutiliza `F-12`, que a `main` já atribui à composição da Spec 007. O próprio candidato declara pendente a conferência direta da seção da ABNT NBR 5891:2014. A revisão da referência precisa acompanhar eventual integração.
- O README do worktree descreve a Spec 007 como em andamento. O roteiro monetário também reserva 009 para formatação e leiautes financeiros. A futura adoção de 009 para CTPS exige reconciliar essa previsão no roteiro financeiro.
