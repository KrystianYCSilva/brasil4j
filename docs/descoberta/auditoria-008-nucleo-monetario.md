# Auditoria do núcleo monetário · Spec 008

**Auditoria inicial** 2026-11-27 · **Revalidação independente** 2026-11-28 · **Resultado** Correção verificada e integrada na `main` local em `c0a91af`.

## Defeito reproduzido e correção

O candidato `517280f` tinha base em `5fd9dae`. Os 21 arquivos de código e teste do candidato foram conferidos em uma cópia isolada da `main` em `18bfe9c`. A suíte passou com 520 testes nos JDKs 8 e 21. Uma sondagem adicional pelos JARs reproduziu um erro que a suíte ainda não cobria.

Em `Real.de("1.00").dividir(1099511627776L, politica)`, a política escolhia `CEILING` quando recebia um valor maior que o quociente exato e `FLOOR` nos demais casos.

| Sondagem | Valor |
| --- | --- |
| Quociente decimal exato | `0.0000000000009094947017729282379150390625` |
| Argumento entregue pelo candidato | `0.0000000000009094947017729282379150391` |
| Saída esperada / observada no candidato | `BRL 0.00` / `BRL 0.01` |

O Agy escreveu o teste de regressão, que falhou no Corretto 8u512 com a divergência acima. A correção em `c0a91af` entrega o quociente exato ao callback sempre que a expansão decimal termina. O teste cobre o resultado e o argumento do callback nos dois sinais, além de uma cotação extrema. [Teste de regressão](../../brasil4j-core/src/test/java/io/github/brasil4j/financas/DinheiroNucleoTest.java)

## Limite em divisões não terminantes

O callback recebe o quociente truncado em direção a zero na escala da moeda de destino mais 34 casas, com um dígito de sinalização assinado na casa seguinte. Em uma sondagem com `1 ÷ 3` em BRL, a política escolhia `CEILING` acima do limiar `0.3333333333333333333333333333333333332`. O quociente matemático está acima desse limiar. O argumento entregue, `0.3333333333333333333333333333333333331`, levou a política a escolher `FLOOR` e retornar `BRL 0.33`. O resultado corresponde à precisão do contrato. [Contrato da Spec 008](../../specs/008-nucleo-monetario/contracts/api.md)

## Verificação e integração

- `clean verify` de `c0a91af` em Corretto 8u512 e 21.0.13 passou com 521 testes em cada JDK, zero falhas, erros ou pulos. Os JARs Java e Kotlin passaram na checagem de bytecode 52 e na comparação binária e de fonte com os artefatos da Spec 007 via japicmp.
- A integração manteve `F-12` para a Spec 007 e atribuiu `F-13` e `F-14` às fontes da 008. A ficha `F-14` registra a pendência de referência direta à ABNT NBR 5891:2014. [Catálogo de fontes](../fontes.md)
- A `main` local contém a correção da 008 e reserva a Spec 009 para Exemplos.
