package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Quantia imutável em Euro ({@code EUR}, escala fixa {@code 2}).
 */
public final class Euro extends Dinheiro implements Comparable<Euro> {
    private static final int ESCALA = 2;

    /** Zero euros ({@code EUR 0.00}) com política {@link MetodoArredondamento#ABNT_NBR_5891}. */
    public static final Euro ZERO =
            new Euro(BigDecimal.ZERO.setScale(ESCALA, RoundingMode.UNNECESSARY), MetodoArredondamento.ABNT_NBR_5891);

    Euro(BigDecimal valorEscalado, MetodoArredondamento arredondamento) {
        super((MoedaPadrao) Moeda.EUR, valorEscalado, arredondamento);
    }

    /**
     * Interpreta texto decimal em Euro com {@link MetodoArredondamento#ABNT_NBR_5891}.
     *
     * @param valor texto decimal canônico
     * @return quantia em Euro
     * @throws IllegalArgumentException se o texto for inválido
     */
    public static Euro de(String valor) {
        return de(valor, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Interpreta texto decimal em Euro aplicando e retendo {@code arredondamento}.
     *
     * @param valor texto decimal canônico
     * @param arredondamento política de arredondamento
     * @return quantia em Euro
     * @throws IllegalArgumentException se qualquer argumento for inválido
     */
    public static Euro de(String valor, MetodoArredondamento arredondamento) {
        BigDecimal parsed = AritmeticaMonetaria.parseDecimalCanonico(valor);
        return de(parsed, arredondamento);
    }

    /**
     * Constrói quantia em Euro a partir de {@link BigDecimal} com {@link MetodoArredondamento#ABNT_NBR_5891}.
     *
     * @param valor valor decimal
     * @return quantia em Euro
     * @throws IllegalArgumentException se o valor for inválido
     */
    public static Euro de(BigDecimal valor) {
        return de(valor, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Constrói quantia em Euro a partir de {@link BigDecimal} aplicando e retendo {@code arredondamento}.
     *
     * @param valor valor decimal
     * @param arredondamento política de arredondamento
     * @return quantia em Euro
     * @throws IllegalArgumentException se qualquer argumento for inválido
     */
    public static Euro de(BigDecimal valor, MetodoArredondamento arredondamento) {
        BigDecimal escalado = AritmeticaMonetaria.aplicarArredondamentoSeguro(valor, ESCALA, arredondamento);
        return new Euro(escalado, arredondamento);
    }

    /**
     * Interpreta texto decimal em Euro rejeitando entradas com mais de 2 casas decimais.
     *
     * @param valor texto decimal sem casas excedentes
     * @return quantia em Euro
     * @throws IllegalArgumentException se inválido ou com mais de 2 casas decimais
     */
    public static Euro exato(String valor) {
        return exato(valor, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Interpreta texto decimal em Euro rejeitando mais de 2 casas e retendo {@code arredondamento}.
     *
     * @param valor texto decimal sem casas excedentes
     * @param arredondamento política retida
     * @return quantia em Euro
     * @throws IllegalArgumentException se qualquer argumento for inválido
     */
    public static Euro exato(String valor, MetodoArredondamento arredondamento) {
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        BigDecimal parsed = AritmeticaMonetaria.parseDecimalCanonico(valor);
        return new Euro(AritmeticaMonetaria.escalarExato(parsed, ESCALA), arredondamento);
    }

    /**
     * Constrói quantia em Euro rejeitando {@link BigDecimal} com escala superior a 2.
     *
     * @param valor valor decimal sem casas excedentes
     * @return quantia em Euro
     * @throws IllegalArgumentException se inválido ou com escala superior a 2
     */
    public static Euro exato(BigDecimal valor) {
        return exato(valor, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Constrói quantia em Euro rejeitando escala superior a 2 e retendo {@code arredondamento}.
     *
     * @param valor valor decimal sem casas excedentes
     * @param arredondamento política retida
     * @return quantia em Euro
     * @throws IllegalArgumentException se qualquer argumento for inválido
     */
    public static Euro exato(BigDecimal valor, MetodoArredondamento arredondamento) {
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        return new Euro(AritmeticaMonetaria.escalarExato(valor, ESCALA), arredondamento);
    }

    /**
     * Constrói quantia a partir de unidades inteiras de Euro.
     *
     * @param eurosInteiros valor inteiro em Euro
     * @return quantia em Euro
     */
    public static Euro deInteiro(long eurosInteiros) {
        return deInteiro(eurosInteiros, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Constrói quantia a partir de unidades inteiras de Euro retendo {@code arredondamento}.
     *
     * @param eurosInteiros valor inteiro em Euro
     * @param arredondamento política retida
     * @return quantia em Euro
     * @throws IllegalArgumentException se {@code arredondamento} for nulo
     */
    public static Euro deInteiro(long eurosInteiros, MetodoArredondamento arredondamento) {
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        return new Euro(AritmeticaMonetaria.deInteiroEscalado(eurosInteiros, ESCALA), arredondamento);
    }

    /**
     * Constrói quantia em Euro a partir do total de centavos (euro cents).
     *
     * @param centavos quantidade inteira de centavos
     * @return quantia em Euro
     */
    public static Euro deCentavos(long centavos) {
        return deCentavos(centavos, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Constrói quantia em Euro a partir do total de centavos retendo {@code arredondamento}.
     *
     * @param centavos quantidade inteira de centavos
     * @param arredondamento política retida
     * @return quantia em Euro
     * @throws IllegalArgumentException se {@code arredondamento} for nulo
     */
    public static Euro deCentavos(long centavos, MetodoArredondamento arredondamento) {
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        return new Euro(AritmeticaMonetaria.deUnidadesMinimasEscalado(centavos, ESCALA), arredondamento);
    }

    /**
     * Retorna o montante total em centavos inteiros.
     *
     * @return total de centavos
     * @throws ArithmeticException se ultrapassar a capacidade de {@code long}
     */
    public long getCentavos() {
        return emUnidadesMinimas();
    }

    /**
     * Soma este valor em Euro a outro em Euro.
     *
     * @param outro operando em Euro não nulo
     * @return soma em Euro
     * @throws IllegalArgumentException se {@code outro} for nulo ou estourar precisão
     */
    public Euro somar(Euro outro) {
        if (outro == null) {
            throw new IllegalArgumentException("O operando em Euro não pode ser nulo.");
        }
        return new Euro(AritmeticaMonetaria.somarEscalado(valor, outro.valor, ESCALA), arredondamento);
    }

    /**
     * Subtrai {@code outro} deste valor em Euro.
     *
     * @param outro subtraendo em Euro não nulo
     * @return diferença em Euro
     * @throws IllegalArgumentException se {@code outro} for nulo ou estourar precisão
     */
    public Euro subtrair(Euro outro) {
        if (outro == null) {
            throw new IllegalArgumentException("O operando em Euro não pode ser nulo.");
        }
        return new Euro(AritmeticaMonetaria.subtrairEscalado(valor, outro.valor, ESCALA), arredondamento);
    }

    @Override
    public Euro abs() {
        return valor.signum() < 0 ? negar() : this;
    }

    @Override
    public Euro negar() {
        return new Euro(AritmeticaMonetaria.normalizarZero(valor.negate(), ESCALA), arredondamento);
    }

    @Override
    public Euro comArredondamento(MetodoArredondamento novoArredondamento) {
        AritmeticaMonetaria.exigirArredondamento(novoArredondamento);
        return new Euro(valor, novoArredondamento);
    }

    @Override
    public Euro multiplicar(long fator) {
        return new Euro(AritmeticaMonetaria.multiplicarPorLong(valor, fator, ESCALA), arredondamento);
    }

    @Override
    public Euro multiplicar(BigDecimal fator) {
        return multiplicar(fator, arredondamento);
    }

    @Override
    public Euro multiplicar(BigDecimal fator, MetodoArredondamento novoArredondamento) {
        return new Euro(
                AritmeticaMonetaria.multiplicarPorDecimal(valor, fator, ESCALA, novoArredondamento),
                novoArredondamento);
    }

    @Override
    public Euro dividir(long divisor) {
        return dividir(divisor, arredondamento);
    }

    @Override
    public Euro dividir(long divisor, MetodoArredondamento novoArredondamento) {
        return new Euro(
                AritmeticaMonetaria.dividirPorLong(valor, divisor, ESCALA, novoArredondamento), novoArredondamento);
    }

    @Override
    public List<Euro> ratear(int parcelas) {
        return ratear(parcelas, PoliticaResiduo.PRIMEIRAS_PARCELAS);
    }

    @Override
    public List<Euro> ratear(int parcelas, PoliticaResiduo politica) {
        BigDecimal[] fatias = AritmeticaMonetaria.ratearEscalado(valor, ESCALA, parcelas, politica);
        return mapearFatias(fatias, f -> new Euro(f, arredondamento));
    }

    @Override
    public List<Euro> alocar(List<BigDecimal> pesos) {
        BigDecimal[] fatias = AritmeticaMonetaria.alocarHamiltonEscalado(valor, ESCALA, pesos);
        return mapearFatias(fatias, f -> new Euro(f, arredondamento));
    }

    /**
     * Converte para {@link Real} aceitando taxa {@code EUR -> BRL} ou {@code BRL -> EUR}.
     *
     * @param taxa taxa do par {@code EUR/BRL}
     * @return valor convertido em Real
     * @throws IllegalArgumentException se {@code taxa} for nula ou de outro par
     */
    public Real paraReal(TaxaCambio taxa) {
        return paraReal(taxa, arredondamento);
    }

    /**
     * Converte para {@link Real} aplicando e retendo {@code novoArredondamento}.
     *
     * @param taxa taxa do par {@code EUR/BRL}
     * @param novoArredondamento política a aplicar e reter
     * @return valor convertido em Real
     * @throws IllegalArgumentException se qualquer argumento for inválido ou de outro par
     */
    public Real paraReal(TaxaCambio taxa, MetodoArredondamento novoArredondamento) {
        return (Real) TaxaCambio.converterParBidirecional(
                this, taxa, (MoedaPadrao) Moeda.BRL, novoArredondamento);
    }

    /**
     * Converte para {@link Dolar} aceitando taxa {@code EUR -> USD} ou {@code USD -> EUR}.
     *
     * @param taxa taxa do par {@code EUR/USD}
     * @return valor convertido em Dólar
     * @throws IllegalArgumentException se {@code taxa} for nula ou de outro par
     */
    public Dolar paraDolar(TaxaCambio taxa) {
        return paraDolar(taxa, arredondamento);
    }

    /**
     * Converte para {@link Dolar} aplicando e retendo {@code novoArredondamento}.
     *
     * @param taxa taxa do par {@code EUR/USD}
     * @param novoArredondamento política a aplicar e reter
     * @return valor convertido em Dólar
     * @throws IllegalArgumentException se qualquer argumento for inválido ou de outro par
     */
    public Dolar paraDolar(TaxaCambio taxa, MetodoArredondamento novoArredondamento) {
        return (Dolar) TaxaCambio.converterParBidirecional(
                this, taxa, (MoedaPadrao) Moeda.USD, novoArredondamento);
    }

    @Override
    Euro criarMesmoSubtipo(BigDecimal valorEscalado, MetodoArredondamento novoArredondamento) {
        return new Euro(valorEscalado, novoArredondamento);
    }

    @Override
    public int compareTo(Euro outro) {
        if (outro == null) {
            throw new IllegalArgumentException("O operando em Euro não pode ser nulo.");
        }
        return valor.compareTo(outro.valor);
    }
}
