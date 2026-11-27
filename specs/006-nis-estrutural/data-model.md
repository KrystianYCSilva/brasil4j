# Data Model · Inscrição estrutural

| Entidade | Dados | Invariante e fronteira |
| --- | --- | --- |
| `Nis` Java | Uma string canônica privada e final | Exatamente 11 caracteres ASCII entre `0` e `9`, na mesma ordem recebida. Só existe após a conferência. |
| `Nis` Kotlin | Referência privada ao valor Java imutável | Compartilha o mesmo valor e a mesma regra. A recuperação explícita e a representação textual delegam ao núcleo. |
| Campo de origem | Texto extraído de um leiaute, ou campo ausente | O consumidor escolhe o campo, decide se ausência é permitida e entrega o texto literal. Um elemento ausente pode chegar como nulo. Um elemento presente sem conteúdo chega como vazio. |

A extração de arquivo ocorre antes da conferência. Uma entrada inválida produz a falha descrita no [contrato](contracts/api.md). Uma entrada válida gera valor imutável sem estados posteriores. A igualdade, dentro de cada tipo, usa o texto canônico. Rótulo do campo, origem, DV e dados cadastrais não compõem a inscrição.
