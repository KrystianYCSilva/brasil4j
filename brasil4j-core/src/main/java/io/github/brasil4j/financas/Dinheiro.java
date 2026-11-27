package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Hierarquia selada e imutável de valores monetários ({@link Real}, {@link Dolar},
 * {@link Euro} e {@link QuantiaMonetaria}).
 *
 * <p>A igualdade ({@link #equals(Object)}) considera exclusivamente a moeda contábil
 * ({@code codigo} e {@code casasDecimais}) e o valor decimal escalado. A política
 * {@link #getArredondamento()} é retida na instância para governar operações inexatas
 * subsequentes que não recebam outra política explícita.
 */
public abstract class Dinheiro {
    final MoedaPadrao moeda;
    final BigDecimal valor;
    final MetodoArredondamento arredondamento;

    Dinheiro(MoedaPadrao moeda, BigDecimal valor, MetodoArredondamento arredondamento) {
        this.moeda = moeda;
        this.valor = valor;
        this.arredondamento = arredondamento;
    }

    static Dinheiro instanciarCanonico(
            MoedaPadrao moedaNormalizada, BigDecimal valorEscalado, MetodoArredondamento arredondamento) {
        if (moedaNormalizada.getCasasDecimais() == 2) {
            String cod = moedaNormalizada.getCodigo();
            if ("BRL".equals(cod)) {
                return new Real(valorEscalado, arredondamento);
            }
            if ("USD".equals(cod)) {
                return new Dolar(valorEscalado, arredondamento);
            }
            if ("EUR".equals(cod)) {
                return new Euro(valorEscalado, arredondamento);
            }
        }
        return new QuantiaMonetaria(moedaNormalizada, valorEscalado, arredondamento);
    }

    /**
     * Interpreta texto decimal canônico na moeda informada aplicando {@link MetodoArredondamento#ABNT_NBR_5891}.
     *
     * @param valor texto decimal ({@code [+-]?[0-9]+(\.[0-9]+)?}, até 64 caracteres)
     * @param moeda moeda alvo
     * @return instância canônica imutável ({@link Real}, {@link Dolar}, {@link Euro} ou {@link QuantiaMonetaria})
     * @throws IllegalArgumentException se qualquer argumento for inválido
     */
    public static Dinheiro de(String valor, Moeda moeda) {
        return de(valor, moeda, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Interpreta texto decimal canônico na moeda informada aplicando e retendo {@code arredondamento}.
     *
     * @param valor texto decimal ({@code [+-]?[0-9]+(\.[0-9]+)?}, até 64 caracteres)
     * @param moeda moeda alvo
     * @param arredondamento política de arredondamento
     * @return instância canônica imutável
     * @throws IllegalArgumentException se qualquer argumento for inválido
     */
    public static Dinheiro de(String valor, Moeda moeda, MetodoArredondamento arredondamento) {
        MoedaPadrao m = ValidacaoMoeda.normalizarMoedaCanonica(moeda);
        BigDecimal parsed = AritmeticaMonetaria.parseDecimalCanonico(valor);
        BigDecimal escalado =
                AritmeticaMonetaria.aplicarArredondamentoSeguro(parsed, m.getCasasDecimais(), arredondamento);
        return instanciarCanonico(m, escalado, arredondamento);
    }

    /**
     * Constrói um valor monetário a partir de {@link BigDecimal} aplicando {@link MetodoArredondamento#ABNT_NBR_5891}.
     *
     * @param valor valor decimal (precisão {@code <= 38}, escala {@code -18..38})
     * @param moeda moeda alvo
     * @return instância canônica imutável
     * @throws IllegalArgumentException se qualquer argumento for inválido
     */
    public static Dinheiro de(BigDecimal valor, Moeda moeda) {
        return de(valor, moeda, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Constrói um valor monetário a partir de {@link BigDecimal} aplicando e retendo {@code arredondamento}.
     *
     * @param valor valor decimal (precisão {@code <= 38}, escala {@code -18..38})
     * @param moeda moeda alvo
     * @param arredondamento política de arredondamento
     * @return instância canônica imutável
     * @throws IllegalArgumentException se qualquer argumento for inválido
     */
    public static Dinheiro de(BigDecimal valor, Moeda moeda, MetodoArredondamento arredondamento) {
        MoedaPadrao m = ValidacaoMoeda.normalizarMoedaCanonica(moeda);
        BigDecimal escalado =
                AritmeticaMonetaria.aplicarArredondamentoSeguro(valor, m.getCasasDecimais(), arredondamento);
        return instanciarCanonico(m, escalado, arredondamento);
    }

    /**
     * Interpreta texto decimal rejeitando entradas com mais casas decimais que a moeda.
     *
     * @param valor texto decimal sem casas excedentes
     * @param moeda moeda alvo
     * @return instância canônica imutável com {@link MetodoArredondamento#ABNT_NBR_5891}
     * @throws IllegalArgumentException se o texto for inválido ou exceder as casas da moeda
     */
    public static Dinheiro exato(String valor, Moeda moeda) {
        return exato(valor, moeda, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Interpreta texto decimal rejeitando casas excedentes e retendo {@code arredondamento}.
     *
     * @param valor texto decimal sem casas excedentes
     * @param moeda moeda alvo
     * @param arredondamento política retida para operações futuras
     * @return instância canônica imutável
     * @throws IllegalArgumentException se qualquer argumento for inválido ou exceder as casas da moeda
     */
    public static Dinheiro exato(String valor, Moeda moeda, MetodoArredondamento arredondamento) {
        MoedaPadrao m = ValidacaoMoeda.normalizarMoedaCanonica(moeda);
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        BigDecimal parsed = AritmeticaMonetaria.parseDecimalCanonico(valor);
        BigDecimal escalado = AritmeticaMonetaria.escalarExato(parsed, m.getCasasDecimais());
        return instanciarCanonico(m, escalado, arredondamento);
    }

    /**
     * Constrói a partir de {@link BigDecimal} rejeitando escala superior às casas da moeda.
     *
     * @param valor valor decimal sem casas excedentes
     * @param moeda moeda alvo
     * @return instância canônica imutável com {@link MetodoArredondamento#ABNT_NBR_5891}
     * @throws IllegalArgumentException se o valor for inválido ou exceder as casas da moeda
     */
    public static Dinheiro exato(BigDecimal valor, Moeda moeda) {
        return exato(valor, moeda, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Constrói a partir de {@link BigDecimal} rejeitando escala excedente e retendo {@code arredondamento}.
     *
     * @param valor valor decimal sem casas excedentes
     * @param moeda moeda alvo
     * @param arredondamento política retida para operações futuras
     * @return instância canônica imutável
     * @throws IllegalArgumentException se qualquer argumento for inválido ou exceder as casas da moeda
     */
    public static Dinheiro exato(BigDecimal valor, Moeda moeda, MetodoArredondamento arredondamento) {
        MoedaPadrao m = ValidacaoMoeda.normalizarMoedaCanonica(moeda);
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        BigDecimal escalado = AritmeticaMonetaria.escalarExato(valor, m.getCasasDecimais());
        return instanciarCanonico(m, escalado, arredondamento);
    }

    /**
     * Constrói a partir de unidades inteiras principais da moeda (ex.: {@code 10} em {@code BRL} = {@code BRL 10.00}).
     *
     * @param unidadesPrincipais quantidade inteira de unidades principais
     * @param moeda moeda alvo
     * @return instância canônica imutável
     * @throws IllegalArgumentException se a moeda for inválida ou o valor exceder os limites
     */
    public static Dinheiro deInteiro(long unidadesPrincipais, Moeda moeda) {
        return deInteiro(unidadesPrincipais, moeda, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Constrói a partir de unidades inteiras principais retendo {@code arredondamento}.
     *
     * @param unidadesPrincipais quantidade inteira de unidades principais
     * @param moeda moeda alvo
     * @param arredondamento política retida
     * @return instância canônica imutável
     * @throws IllegalArgumentException se qualquer argumento for inválido
     */
    public static Dinheiro deInteiro(
            long unidadesPrincipais, Moeda moeda, MetodoArredondamento arredondamento) {
        MoedaPadrao m = ValidacaoMoeda.normalizarMoedaCanonica(moeda);
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        BigDecimal escalado = AritmeticaMonetaria.deInteiroEscalado(unidadesPrincipais, m.getCasasDecimais());
        return instanciarCanonico(m, escalado, arredondamento);
    }

    /**
     * Constrói a partir de unidades mínimas inteiras ({@code unidadesMinimas * 10^(-casasDecimais)}).
     *
     * @param unidadesMinimas quantidade inteira na menor subunidade da moeda
     * @param moeda moeda alvo
     * @return instância canônica imutável
     * @throws IllegalArgumentException se a moeda for inválida
     */
    public static Dinheiro deUnidadesMinimas(long unidadesMinimas, Moeda moeda) {
        return deUnidadesMinimas(unidadesMinimas, moeda, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Constrói a partir de unidades mínimas inteiras retendo {@code arredondamento}.
     *
     * @param unidadesMinimas quantidade inteira na menor subunidade da moeda
     * @param moeda moeda alvo
     * @param arredondamento política retida
     * @return instância canônica imutável
     * @throws IllegalArgumentException se qualquer argumento for inválido
     */
    public static Dinheiro deUnidadesMinimas(
            long unidadesMinimas, Moeda moeda, MetodoArredondamento arredondamento) {
        MoedaPadrao m = ValidacaoMoeda.normalizarMoedaCanonica(moeda);
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        BigDecimal escalado = AritmeticaMonetaria.deUnidadesMinimasEscalado(unidadesMinimas, m.getCasasDecimais());
        return instanciarCanonico(m, escalado, arredondamento);
    }

    /**
     * Retorna o valor zero na escala da moeda informada com {@link MetodoArredondamento#ABNT_NBR_5891}.
     *
     * @param moeda moeda alvo
     * @return instância canônica representando zero
     * @throws IllegalArgumentException se a moeda for inválida
     */
    public static Dinheiro zero(Moeda moeda) {
        MoedaPadrao m = ValidacaoMoeda.normalizarMoedaCanonica(moeda);
        if (m.getCasasDecimais() == 2) {
            if ("BRL".equals(m.getCodigo())) {
                return Real.ZERO;
            }
            if ("USD".equals(m.getCodigo())) {
                return Dolar.ZERO;
            }
            if ("EUR".equals(m.getCodigo())) {
                return Euro.ZERO;
            }
        }
        return new QuantiaMonetaria(
                m, BigDecimal.ZERO.setScale(m.getCasasDecimais()), MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Retorna o descritor imutável da moeda deste valor.
     *
     * @return moeda associada
     */
    public final Moeda getMoeda() {
        return moeda;
    }

    /**
     * Retorna o valor decimal exato com escala igual a {@code getMoeda().getCasasDecimais()}.
     *
     * @return valor decimal escalado
     */
    public final BigDecimal getValor() {
        return valor;
    }

    /**
     * Retorna a política de arredondamento retida para operações inexatas.
     *
     * @return política de arredondamento
     */
    public final MetodoArredondamento getArredondamento() {
        return arredondamento;
    }

    /**
     * Converte o montante para unidades mínimas inteiras de 64 bits ({@code valor * 10^casasDecimais}).
     *
     * @return quantidade exata de unidades mínimas
     * @throws ArithmeticException se o valor ultrapassar {@link Long#MAX_VALUE} ou {@link Long#MIN_VALUE}
     */
    public final long emUnidadesMinimas() {
        return valor.unscaledValue().longValueExact();
    }

    /**
     * Indica se o montante é igual a zero.
     *
     * @return {@code true} quando zero
     */
    public final boolean isZero() {
        return valor.signum() == 0;
    }

    /**
     * Indica se o montante é estritamente maior que zero.
     *
     * @return {@code true} quando positivo
     */
    public final boolean isPositivo() {
        return valor.signum() > 0;
    }

    /**
     * Indica se o montante é estritamente menor que zero.
     *
     * @return {@code true} quando negativo
     */
    public final boolean isNegativo() {
        return valor.signum() < 0;
    }

    /**
     * Retorna o valor absoluto preservando moeda e política de arredondamento.
     *
     * @return valor não negativo
     */
    public abstract Dinheiro abs();

    /**
     * Retorna o valor com sinal invertido preservando moeda e política de arredondamento.
     *
     * @return valor negado (ou zero sem sinal negativo)
     */
    public abstract Dinheiro negar();

    /**
     * Retorna nova instância com mesmo valor e moeda, substituindo a política retida.
     *
     * @param novoArredondamento nova política não nula
     * @return instância com a nova política
     * @throws IllegalArgumentException se {@code novoArredondamento} for nulo
     */
    public abstract Dinheiro comArredondamento(MetodoArredondamento novoArredondamento);

    /**
     * Soma este valor a outro de mesma moeda, preservando a política deste receptor.
     *
     * @param outro operando de mesma moeda
     * @return soma monetária
     * @throws IllegalArgumentException se {@code outro} for nulo, de moeda divergente ou estourar precisão
     */
    public final Dinheiro somarMesmaMoeda(Dinheiro outro) {
        exigirMesmaMoeda(outro);
        BigDecimal soma = AritmeticaMonetaria.somarEscalado(valor, outro.valor, moeda.getCasasDecimais());
        return instanciarCanonico(moeda, soma, arredondamento);
    }

    /**
     * Subtrai {@code outro} deste valor exigindo mesma moeda e preservando a política deste receptor.
     *
     * @param outro subtraendo de mesma moeda
     * @return diferença monetária
     * @throws IllegalArgumentException se {@code outro} for nulo, de moeda divergente ou estourar precisão
     */
    public final Dinheiro subtrairMesmaMoeda(Dinheiro outro) {
        exigirMesmaMoeda(outro);
        BigDecimal dif = AritmeticaMonetaria.subtrairEscalado(valor, outro.valor, moeda.getCasasDecimais());
        return instanciarCanonico(moeda, dif, arredondamento);
    }

    /**
     * Multiplica de forma exata por um fator inteiro.
     *
     * @param fator multiplicador inteiro
     * @return produto monetário exato
     * @throws IllegalArgumentException se o resultado exceder a precisão máxima
     */
    public abstract Dinheiro multiplicar(long fator);

    /**
     * Multiplica por fator decimal e arredonda na escala da moeda usando a política retida.
     *
     * @param fator fator decimal não nulo
     * @return produto arredondado
     * @throws IllegalArgumentException se o fator ou o resultado violar os limites
     */
    public abstract Dinheiro multiplicar(BigDecimal fator);

    /**
     * Multiplica por fator decimal aplicando e retendo {@code novoArredondamento}.
     *
     * @param fator fator decimal não nulo
     * @param novoArredondamento política a aplicar e reter
     * @return produto arredondado
     * @throws IllegalArgumentException se qualquer argumento for inválido
     */
    public abstract Dinheiro multiplicar(BigDecimal fator, MetodoArredondamento novoArredondamento);

    /**
     * Divide por inteiro não zero em passo único usando a política retida.
     *
     * @param divisor divisor diferente de zero
     * @return quociente arredondado
     * @throws IllegalArgumentException se {@code divisor == 0}
     */
    public abstract Dinheiro dividir(long divisor);

    /**
     * Divide por inteiro não zero em passo único aplicando e retendo {@code novoArredondamento}.
     *
     * @param divisor divisor diferente de zero
     * @param novoArredondamento política a aplicar e reter
     * @return quociente arredondado
     * @throws IllegalArgumentException se {@code divisor == 0} ou política nula/inválida
     */
    public abstract Dinheiro dividir(long divisor, MetodoArredondamento novoArredondamento);

    /**
     * Compara este montante a outro de mesma moeda.
     *
     * @param outro valor de mesma moeda
     * @return inteiro negativo, zero ou positivo conforme este valor seja menor, igual ou maior
     * @throws IllegalArgumentException se {@code outro} for nulo ou de moeda diferente
     */
    public final int compararMesmaMoeda(Dinheiro outro) {
        exigirMesmaMoeda(outro);
        return valor.compareTo(outro.valor);
    }

    /**
     * Divide o valor em {@code parcelas} cotas alocando eventuais unidades mínimas residuais nas primeiras parcelas.
     *
     * @param parcelas quantidade de parcelas ({@code >= 1})
     * @return lista imutável cuja soma é idêntica a este valor
     * @throws IllegalArgumentException se {@code parcelas < 1}
     */
    public abstract List<? extends Dinheiro> ratear(int parcelas);

    /**
     * Divide o valor em {@code parcelas} cotas segundo a {@link PoliticaResiduo} informada.
     *
     * @param parcelas quantidade de parcelas ({@code >= 1})
     * @param politica regra de distribuição das unidades residuais
     * @return lista imutável cuja soma é idêntica a este valor
     * @throws IllegalArgumentException se {@code parcelas < 1} ou {@code politica == null}
     */
    public abstract List<? extends Dinheiro> ratear(int parcelas, PoliticaResiduo politica);

    /**
     * Aloca o valor proporcionalmente aos {@code pesos} pelo método exato dos maiores restos (Hamilton).
     *
     * @param pesos lista não vazia de pesos {@code >= 0} com soma {@code > 0} (até 10.000 itens)
     * @return lista imutável cuja soma é idêntica a este valor
     * @throws IllegalArgumentException se a lista ou qualquer peso for inválido
     */
    public abstract List<? extends Dinheiro> alocar(List<BigDecimal> pesos);

    /**
     * Converte este valor usando {@code taxa} e preservando a política de arredondamento atual.
     *
     * @param taxa taxa de câmbio cuja moeda de origem coincide com {@link #getMoeda()}
     * @return valor convertido na moeda de destino
     * @throws IllegalArgumentException se {@code taxa} for nula ou de origem incompatível
     */
    public final Dinheiro converter(TaxaCambio taxa) {
        return converter(taxa, arredondamento);
    }

    /**
     * Converte este valor usando {@code taxa}, aplicando e retendo {@code novoArredondamento}.
     *
     * @param taxa taxa de câmbio cuja moeda de origem coincide com {@link #getMoeda()}
     * @param novoArredondamento política a aplicar e reter
     * @return valor convertido na moeda de destino
     * @throws IllegalArgumentException se qualquer argumento for inválido ou origem incompatível
     */
    public final Dinheiro converter(TaxaCambio taxa, MetodoArredondamento novoArredondamento) {
        if (taxa == null) {
            throw new IllegalArgumentException("A taxa de câmbio não pode ser nula.");
        }
        return taxa.converter(this, novoArredondamento);
    }

    static <T extends Dinheiro> List<T> mapearFatias(BigDecimal[] fatias, Function<BigDecimal, T> construtor) {
        List<T> lista = new ArrayList<>(fatias.length);
        for (BigDecimal fatia : fatias) {
            lista.add(construtor.apply(fatia));
        }
        return Collections.unmodifiableList(lista);
    }

    final void exigirMesmaMoeda(Dinheiro outro) {
        if (outro == null) {
            throw new IllegalArgumentException("O operando monetário não pode ser nulo.");
        }
        if (!moeda.equals(outro.moeda)) {
            throw new IllegalArgumentException(
                    "Operação inválida entre moedas distintas: " + moeda + " e " + outro.moeda + ".");
        }
    }

    /**
     * Compara igualdade contábil por {@code (moeda.codigo, moeda.casasDecimais, valor)}.
     * Nota: {@link #getArredondamento()} não integra a igualdade contábil.
     */
    @Override
    public final boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        if (!(outro instanceof Dinheiro)) {
            return false;
        }
        Dinheiro d = (Dinheiro) outro;
        return moeda.equals(d.moeda) && valor.equals(d.valor);
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return Objects.hash(moeda, valor);
    }

    /**
     * Retorna a representação textual canônica independente de localidade: {@code "<CODIGO> <VALOR>"}.
     *
     * @return texto canônico sem notação científica
     */
    @Override
    public final String toString() {
        return moeda.getCodigo() + " " + valor.toPlainString();
    }
}
