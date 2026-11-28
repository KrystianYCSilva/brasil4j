package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Quantia imutável em Dólar americano ({@code USD}, escala fixa {@code 2}).
 */
public final class Dolar extends Dinheiro implements Comparable<Dolar> {
    private static final int ESCALA = 2;

    /** Zero dólares ({@code USD 0.00}) com política {@link MetodoArredondamento#ABNT_NBR_5891}. */
    public static final Dolar ZERO =
            new Dolar(BigDecimal.ZERO.setScale(ESCALA, RoundingMode.UNNECESSARY), MetodoArredondamento.ABNT_NBR_5891);

    Dolar(BigDecimal valorEscalado, MetodoArredondamento arredondamento) {
        super((MoedaPadrao) Moeda.USD, valorEscalado, arredondamento);
    }

    /**
     * Interpreta texto decimal em Dólar com {@link MetodoArredondamento#ABNT_NBR_5891}.
     *
     * @param valor texto decimal canônico
     * @return quantia em Dólar
     * @throws IllegalArgumentException se o texto for inválido
     */
    public static Dolar de(String valor) {
        return de(valor, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Interpreta texto decimal em Dólar aplicando e retendo {@code arredondamento}.
     *
     * @param valor texto decimal canônico
     * @param arredondamento política de arredondamento
     * @return quantia em Dólar
     * @throws IllegalArgumentException se qualquer argumento for inválido
     */
    public static Dolar de(String valor, MetodoArredondamento arredondamento) {
        BigDecimal parsed = AritmeticaMonetaria.parseDecimalCanonico(valor);
        return de(parsed, arredondamento);
    }

    /**
     * Constrói quantia em Dólar a partir de {@link BigDecimal} com {@link MetodoArredondamento#ABNT_NBR_5891}.
     *
     * @param valor valor decimal
     * @return quantia em Dólar
     * @throws IllegalArgumentException se o valor for inválido
     */
    public static Dolar de(BigDecimal valor) {
        return de(valor, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Constrói quantia em Dólar a partir de {@link BigDecimal} aplicando e retendo {@code arredondamento}.
     *
     * @param valor valor decimal
     * @param arredondamento política de arredondamento
     * @return quantia em Dólar
     * @throws IllegalArgumentException se qualquer argumento for inválido
     */
    public static Dolar de(BigDecimal valor, MetodoArredondamento arredondamento) {
        BigDecimal escalado = AritmeticaMonetaria.aplicarArredondamentoSeguro(valor, ESCALA, arredondamento);
        return new Dolar(escalado, arredondamento);
    }

    /**
     * Interpreta texto decimal em Dólar rejeitando entradas com mais de 2 casas decimais.
     *
     * @param valor texto decimal sem casas excedentes
     * @return quantia em Dólar
     * @throws IllegalArgumentException se inválido ou com mais de 2 casas decimais
     */
    public static Dolar exato(String valor) {
        return exato(valor, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Interpreta texto decimal em Dólar rejeitando mais de 2 casas e retendo {@code arredondamento}.
     *
     * @param valor texto decimal sem casas excedentes
     * @param arredondamento política retida
     * @return quantia em Dólar
     * @throws IllegalArgumentException se qualquer argumento for inválido
     */
    public static Dolar exato(String valor, MetodoArredondamento arredondamento) {
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        BigDecimal parsed = AritmeticaMonetaria.parseDecimalCanonico(valor);
        return new Dolar(AritmeticaMonetaria.escalarExato(parsed, ESCALA), arredondamento);
    }

    /**
     * Constrói quantia em Dólar rejeitando {@link BigDecimal} com escala superior a 2.
     *
     * @param valor valor decimal sem casas excedentes
     * @return quantia em Dólar
     * @throws IllegalArgumentException se inválido ou com escala superior a 2
     */
    public static Dolar exato(BigDecimal valor) {
        return exato(valor, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Constrói quantia em Dólar rejeitando escala superior a 2 e retendo {@code arredondamento}.
     *
     * @param valor valor decimal sem casas excedentes
     * @param arredondamento política retida
     * @return quantia em Dólar
     * @throws IllegalArgumentException se qualquer argumento for inválido
     */
    public static Dolar exato(BigDecimal valor, MetodoArredondamento arredondamento) {
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        return new Dolar(AritmeticaMonetaria.escalarExato(valor, ESCALA), arredondamento);
    }

    /**
     * Constrói quantia a partir de unidades inteiras de Dólar.
     *
     * @param dolaresInteiros valor inteiro em Dólar
     * @return quantia em Dólar
     */
    public static Dolar deInteiro(long dolaresInteiros) {
        return deInteiro(dolaresInteiros, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Constrói quantia a partir de unidades inteiras de Dólar retendo {@code arredondamento}.
     *
     * @param dolaresInteiros valor inteiro em Dólar
     * @param arredondamento política retida
     * @return quantia em Dólar
     * @throws IllegalArgumentException se {@code arredondamento} for nulo
     */
    public static Dolar deInteiro(long dolaresInteiros, MetodoArredondamento arredondamento) {
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        return new Dolar(AritmeticaMonetaria.deInteiroEscalado(dolaresInteiros, ESCALA), arredondamento);
    }

    /**
     * Constrói quantia em Dólar a partir do total de centavos (cents).
     *
     * @param centavos quantidade inteira de cents
     * @return quantia em Dólar
     */
    public static Dolar deCentavos(long centavos) {
        return deCentavos(centavos, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Constrói quantia em Dólar a partir do total de centavos retendo {@code arredondamento}.
     *
     * @param centavos quantidade inteira de cents
     * @param arredondamento política retida
     * @return quantia em Dólar
     * @throws IllegalArgumentException se {@code arredondamento} for nulo
     */
    public static Dolar deCentavos(long centavos, MetodoArredondamento arredondamento) {
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        return new Dolar(AritmeticaMonetaria.deUnidadesMinimasEscalado(centavos, ESCALA), arredondamento);
    }

    /**
     * Retorna o montante total em centavos inteiros.
     *
     * @return total de cents
     * @throws ArithmeticException se ultrapassar a capacidade de {@code long}
     */
    public long getCentavos() {
        return emUnidadesMinimas();
    }

    /**
     * Soma este valor em Dólar a outro em Dólar.
     *
     * @param outro operando em Dólar não nulo
     * @return soma em Dólar
     * @throws IllegalArgumentException se {@code outro} for nulo ou estourar precisão
     */
    public Dolar somar(Dolar outro) {
        if (outro == null) {
            throw new IllegalArgumentException("O operando em Dólar não pode ser nulo.");
        }
        return new Dolar(AritmeticaMonetaria.somarEscalado(valor, outro.valor, ESCALA), arredondamento);
    }

    /**
     * Subtrai {@code outro} deste valor em Dólar.
     *
     * @param outro subtraendo em Dólar não nulo
     * @return diferença em Dólar
     * @throws IllegalArgumentException se {@code outro} for nulo ou estourar precisão
     */
    public Dolar subtrair(Dolar outro) {
        if (outro == null) {
            throw new IllegalArgumentException("O operando em Dólar não pode ser nulo.");
        }
        return new Dolar(AritmeticaMonetaria.subtrairEscalado(valor, outro.valor, ESCALA), arredondamento);
    }

    @Override
    public Dolar abs() {
        return valor.signum() < 0 ? negar() : this;
    }

    @Override
    public Dolar negar() {
        return new Dolar(AritmeticaMonetaria.normalizarZero(valor.negate(), ESCALA), arredondamento);
    }

    @Override
    public Dolar comArredondamento(MetodoArredondamento novoArredondamento) {
        AritmeticaMonetaria.exigirArredondamento(novoArredondamento);
        return new Dolar(valor, novoArredondamento);
    }

    @Override
    public Dolar multiplicar(long fator) {
        return new Dolar(AritmeticaMonetaria.multiplicarPorLong(valor, fator, ESCALA), arredondamento);
    }

    @Override
    public Dolar multiplicar(BigDecimal fator) {
        return multiplicar(fator, arredondamento);
    }

    @Override
    public Dolar multiplicar(BigDecimal fator, MetodoArredondamento novoArredondamento) {
        return new Dolar(
                AritmeticaMonetaria.multiplicarPorDecimal(valor, fator, ESCALA, novoArredondamento),
                novoArredondamento);
    }

    @Override
    public Dolar dividir(long divisor) {
        return dividir(divisor, arredondamento);
    }

    @Override
    public Dolar dividir(long divisor, MetodoArredondamento novoArredondamento) {
        return new Dolar(
                AritmeticaMonetaria.dividirPorLong(valor, divisor, ESCALA, novoArredondamento), novoArredondamento);
    }

    @Override
    public List<Dolar> ratear(int parcelas) {
        return ratear(parcelas, PoliticaResiduo.PRIMEIRAS_PARCELAS);
    }

    @Override
    public List<Dolar> ratear(int parcelas, PoliticaResiduo politica) {
        BigDecimal[] fatias = AritmeticaMonetaria.ratearEscalado(valor, ESCALA, parcelas, politica);
        return mapearFatias(fatias, f -> new Dolar(f, arredondamento));
    }

    @Override
    public List<Dolar> alocar(List<BigDecimal> pesos) {
        BigDecimal[] fatias = AritmeticaMonetaria.alocarHamiltonEscalado(valor, ESCALA, pesos);
        return mapearFatias(fatias, f -> new Dolar(f, arredondamento));
    }

    /**
     * Converte para {@link Real} aceitando taxa {@code USD -> BRL} ou {@code BRL -> USD}.
     *
     * @param taxa taxa do par {@code USD/BRL}
     * @return valor convertido em Real
     * @throws IllegalArgumentException se {@code taxa} for nula ou de outro par
     */
    public Real paraReal(TaxaCambio taxa) {
        return paraReal(taxa, arredondamento);
    }

    /**
     * Converte para {@link Real} aplicando e retendo {@code novoArredondamento}.
     *
     * @param taxa taxa do par {@code USD/BRL}
     * @param novoArredondamento política a aplicar e reter
     * @return valor convertido em Real
     * @throws IllegalArgumentException se qualquer argumento for inválido ou de outro par
     */
    public Real paraReal(TaxaCambio taxa, MetodoArredondamento novoArredondamento) {
        return (Real) TaxaCambio.converterParBidirecional(
                this, taxa, (MoedaPadrao) Moeda.BRL, novoArredondamento);
    }

    /**
     * Converte para {@link Euro} aceitando taxa {@code USD -> EUR} ou {@code EUR -> USD}.
     *
     * @param taxa taxa do par {@code USD/EUR}
     * @return valor convertido em Euro
     * @throws IllegalArgumentException se {@code taxa} for nula ou de outro par
     */
    public Euro paraEuro(TaxaCambio taxa) {
        return paraEuro(taxa, arredondamento);
    }

    /**
     * Converte para {@link Euro} aplicando e retendo {@code novoArredondamento}.
     *
     * @param taxa taxa do par {@code USD/EUR}
     * @param novoArredondamento política a aplicar e reter
     * @return valor convertido em Euro
     * @throws IllegalArgumentException se qualquer argumento for inválido ou de outro par
     */
    public Euro paraEuro(TaxaCambio taxa, MetodoArredondamento novoArredondamento) {
        return (Euro) TaxaCambio.converterParBidirecional(
                this, taxa, (MoedaPadrao) Moeda.EUR, novoArredondamento);
    }

    @Override
    Dolar criarMesmoSubtipo(BigDecimal valorEscalado, MetodoArredondamento novoArredondamento) {
        return new Dolar(valorEscalado, novoArredondamento);
    }

    @Override
    public int compareTo(Dolar outro) {
        if (outro == null) {
            throw new IllegalArgumentException("O operando em Dólar não pode ser nulo.");
        }
        return valor.compareTo(outro.valor);
    }
}
