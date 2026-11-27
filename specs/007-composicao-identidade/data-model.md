# Modelo de dados · Spec 007

## Item de conferência

`DocumentCheck<T>` vincula um texto literal, possivelmente nulo, à fábrica nativa de um tipo concreto. CPF e CNPJ vinculam um modo obrigatório. NIS vincula a forma da Spec 006. Uma política opcional decide sobre o valor produzido e uma função de apresentação opcional recebe esse mesmo valor após a aceitação. A configuração do item é imutável. Se uma função do consumidor captura estado, a gestão desse estado pertence ao consumidor.

O consumidor mantém a lista e a correlação por posição ou rótulo. A biblioteca não registra nem persiste esses dados. O objeto de valor construído é local à execução. O texto bruto permanece no item para permitir reavaliação, e a saída fica no resultado devolvido ao consumidor.

## Transições por avaliação

| Etapa | Condição | Efeito |
| --- | --- | --- |
| Conferência e construção nativas | Entrada inválida no modo | Resultado `REJECTED_BY_VALIDATION`, sem saída. |
| Política | Ausente ou retorna `true` | Avança à apresentação. |
| Política | Retorna `false` | Resultado `REJECTED_BY_POLICY`, sem saída. |
| Apresentação | Ausente | Resultado `ACCEPTED`, sem saída. |
| Apresentação | Retorna texto não nulo | Resultado `ACCEPTED`, com saída presente. |
| Política ou apresentação | Lança exceção | A mesma exceção chega ao chamador, sem resultado para a avaliação. |
| Apresentação | Retorna nulo | Erro de execução, sem resultado para a avaliação. |

Cada nova avaliação parte do texto associado ao item e repete as etapas alcançadas na thread de quem chama. Rejeições de itens diferentes são independentes. O resultado contém estado e saída opcional. O texto bruto, os dígitos canônicos e a saída ficam fora das representações de diagnóstico.
