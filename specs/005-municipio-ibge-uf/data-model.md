# Data Model: Código IBGE de município e UF

| Tipo | Estado | Invariante |
| --- | --- | --- |
| `CodigoMunicipio` | Valor canônico `String` | Sete caracteres ASCII 0 a 9. O prefixo é derivado dos dois primeiros caracteres. Igualdade por valor. |
| `Uf` | Sigla em constante, código, nome, capital e lista imutável de DDDs | 27 UFs com sigla e código unívocos. Nome oficial e busca normalizada. DDDs distintos por UF, com possível compartilhamento entre UFs. |
| `CodigoMunicipio` Kotlin | Objeto Java delegado | Propriedades e validação delegadas ao núcleo. Igualdade por valor dentro do tipo Kotlin. Representação textual igual ao valor canônico Java. |
