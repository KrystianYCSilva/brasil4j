package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.util.List;

/**
 * Quantia imutável em Real brasileiro ({@code BRL}, escala fixa {@code 2}).
 */
public final class Real extends Dinheiro implements Comparable<Real> {
    private static final int ESCALA = 2;

    /** Zero reais ({@code BRL 0.00}) com política {@link MetodoArredondamento#ABNT_NBR_5891}. */
    public static final Real ZERO =
            new Real(BigDecimal.ZERO.setScale(ESCALA), MetodoArredondamento.ABNT_NBR_5891);

    Real(BigDecimal valorEscalado, MetodoArredondamento arredondamento) {
        super((MoedaPadrao) Moeda.BRL, valorEscalado, arredondamento);
    }

    /**
     * Interpreta texto decimal em Reais com {@link MetodoArredondamento#ABNT_NBR_5891}.
     *
     * @param valor texto decimal canônico
     * @return quantia em Reais
     * @throws IllegalArgumentException se o texto for inválido
     */
    public static Real de(String valor) {
        return de(valor, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Interpreta texto decimal em Reais aplicando e retendo {@code arredondamento}.
     *
     * @param valor texto decimal canônico
     * @param arredondamento política de arredondamento
     * @return quantia em Reais
     * @throws IllegalArgumentException se qualquer argumento for inválido
     */
    public static Real de(String valor, MetodoArredondamento arredondamento) {
        BigDecimal parsed = AritmeticaMonetaria.parseDecimalCanonico(valor);
        return de(parsed, arredondamento);
    }

    /**
     * Constrói quantia em Reais a partir de {@link BigDecimal} com {@link MetodoArredondamento#ABNT_NBR_5891}.
     *
     * @param valor valor decimal
     * @return quantia em Reais
     * @throws IllegalArgumentException se o valor for inválido
     */
    public static Real de(BigDecimal valor) {
        return de(valor, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Constrói quantia em Reais a partir de {@link BigDecimal} aplicando e retendo {@code arredondamento}.
     *
     * @param valor valor decimal
     * @param arredondamento política de arredondamento
     * @return quantia em Reais
     * @throws IllegalArgumentException se qualquer argumento for inválido
     */
    public static Real de(BigDecimal valor, MetodoArredondamento arredondamento) {
        BigDecimal escalado = AritmeticaMonetaria.aplicarArredondamentoSeguro(valor, ESCALA, arredondamento);
        return new Real(escalado, arredondamento);
    }

    /**
     * Interpreta texto decimal em Reais rejeitando entradas com mais de 2 casas decimais.
     *
     * @param valor texto decimal sem casas excedentes
     * @return quantia em Reais
     * @throws IllegalArgumentException se inválido ou com mais de 2 casas decimais
     */
    public static Real exato(String valor) {
        return exato(valor, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Interpreta texto decimal em Reais rejeitando mais de 2 casas decimais e retendo {@code arredondamento}.
     *
     * @param valor texto decimal sem casas excedentes
     * @param arredondamento política retida
     * @return quantia em Reais
     * @throws IllegalArgumentException se qualquer argumento for inválido
     */
    public static Real exato(String valor, MetodoArredondamento arredondamento) {
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        BigDecimal parsed = AritmeticaMonetaria.parseDecimalCanonico(valor);
        return new Real(AritmeticaMonetaria.escalarExato(parsed, ESCALA), arredondamento);
    }

    /**
     * Constrói quantia em Reais rejeitando {@link BigDecimal} com escala superior a 2.
     *
     * @param valor valor decimal sem casas excedentes
     * @return quantia em Reais
     * @throws IllegalArgumentException se inválido ou com escala superior a 2
     */
    public static Real exato(BigDecimal valor) {
        return exato(valor, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Constrói quantia em Reais rejeitando escala superior a 2 e retendo {@code arredondamento}.
     *
     * @param valor valor decimal sem casas excedentes
     * @param arredondamento política retida
     * @return quantia em Reais
     * @throws IllegalArgumentException se qualquer argumento for inválido
     */
    public static Real exato(BigDecimal valor, MetodoArredondamento arredondamento) {
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        return new Real(AritmeticaMonetaria.escalarExato(valor, ESCALA), arredondamento);
    }

    /**
     * Constrói quantia a partir de unidades inteiras de Real (ex.: {@code 1050} = {@code BRL 1050.00}).
     *
     * @param reaisInteiros valor inteiro em Reais
     * @return quantia em Reais
     */
    public static Real deInteiro(long reaisInteiros) {
        return deInteiro(reaisInteiros, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Constrói quantia a partir de unidades inteiras de Real retendo {@code arredondamento}.
     *
     * @param reaisInteiros valor inteiro em Reais
     * @param arredondamento política retida
     * @return quantia em Reais
     * @throws IllegalArgumentException se {@code arredondamento} for nulo
     */
    public static Real deInteiro(long reaisInteiros, MetodoArredondamento arredondamento) {
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        return new Real(AritmeticaMonetaria.deInteiroEscalado(reaisInteiros, ESCALA), arredondamento);
    }

    /**
     * Constrói quantia em Reais a partir do total de centavos (ex.: {@code 1050} = {@code BRL 10.50}).
     *
     * @param centavos quantidade inteira de centavos
     * @return quantia em Reais
     */
    public static Real deCentavos(long centavos) {
        return deCentavos(centavos, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Constrói quantia em Reais a partir do total de centavos retendo {@code arredondamento}.
     *
     * @param centavos quantidade inteira de centavos
     * @param arredondamento política retida
     * @return quantia em Reais
     * @throws IllegalArgumentException se {@code arredondamento} for nulo
     */
    public static Real deCentavos(long centavos, MetodoArredondamento arredondamento) {
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        return new Real(AritmeticaMonetaria.deUnidadesMinimasEscalado(centavos, ESCALA), arredondamento);
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
     * Soma este valor em Reais a outro valor em Reais, preservando a política deste receptor.
     *
     * @param outro parcela em Reais não nula
     * @return soma em Reais
     * @throws IllegalArgumentException se {@code outro} for nulo ou exceder a precisão máxima
     */
    public Real somar(Real outro) {
        if (outro == null) {
            throw new IllegalArgumentException("O operando em Real não pode ser nulo.");
        }
        return new Real(AritmeticaMonetaria.somarEscalado(valor, outro.valor, ESCALA), arredondamento);
    }

    /**
     * Subtrai {@code outro} deste valor em Reais, preservando a política deste receptor.
     *
     * @param outro subtraendo em Reais não nulo
     * @return diferença em Reais
     * @throws IllegalArgumentException se {@code outro} for nulo ou exceder a precisão máxima
     */
    public Real subtrair(Real outro) {
        if (outro == null) {
            throw new IllegalArgumentException("O operando em Real não pode ser nulo.");
        }
        return new Real(AritmeticaMonetaria.subtrairEscalado(valor, outro.valor, ESCALA), arredondamento);
    }

    @Override
    public Real abs() {
        return valor.signum() < 0 ? negar() : this;
    }

    @Override
    public Real negar() {
        return new Real(AritmeticaMonetaria.normalizarZero(valor.negate(), ESCALA), arredondamento);
    }

    @Override
    public Real comArredondamento(MetodoArredondamento novoArredondamento) {
        AritmeticaMonetaria.exigirArredondamento(novoArredondamento);
        return new Real(valor, novoArredondamento);
    }

    @Override
    public Real multiplicar(long fator) {
        return new Real(AritmeticaMonetaria.multiplicarPorLong(valor, fator, ESCALA), arredondamento);
    }

    @Override
    public Real multiplicar(BigDecimal fator) {
        return multiplicar(fator, arredondamento);
    }

    @Override
    public Real multiplicar(BigDecimal fator, MetodoArredondamento novoArredondamento) {
        return new Real(
                AritmeticaMonetaria.multiplicarPorDecimal(valor, fator, ESCALA, novoArredondamento),
                novoArredondamento);
    }

    @Override
    public Real dividir(long divisor) {
        return dividir(divisor, arredondamento);
    }

    @Override
    public Real dividir(long divisor, MetodoArredondamento novoArredondamento) {
        return new Real(
                AritmeticaMonetaria.dividirPorLong(valor, divisor, ESCALA, novoArredondamento), novoArredondamento);
    }

    @Override
    public List<Real> ratear(int parcelas) {
        return ratear(parcelas, PoliticaResiduo.PRIMEIRAS_PARCELAS);
    }

    @Override
    public List<Real> ratear(int parcelas, PoliticaResiduo politica) {
        BigDecimal[] fatias = AritmeticaMonetaria.ratearEscalado(valor, ESCALA, parcelas, politica);
        return mapearFatias(fatias, f -> new Real(f, arredondamento));
    }

    @Override
    public List<Real> alocar(List<BigDecimal> pesos) {
        BigDecimal[] fatias = AritmeticaMonetaria.alocarHamiltonEscalado(valor, ESCALA, pesos);
        return mapearFatias(fatias, f -> new Real(f, arredondamento));
    }

    /**
     * Converte para {@link Dolar} aceitando taxa {@code BRL -> USD} ou {@code USD -> BRL}.
     *
     * @param taxa taxa do par {@code BRL/USD}
     * @return valor convertido em Dólar
     * @throws IllegalArgumentException se {@code taxa} for nula ou de outro par de moedas
     */
    public Dolar paraDolar(TaxaCambio taxa) {
        return paraDolar(taxa, arredondamento);
    }

    /**
     * Converte para {@link Dolar} aplicando e retendo {@code novoArredondamento}.
     *
     * @param taxa taxa do par {@code BRL/USD}
     * @param novoArredondamento política a aplicar e reter
     * @return valor convertido em Dólar
     * @throws IllegalArgumentException se qualquer argumento for inválido ou de outro par
     */
    public Dolar paraDolar(TaxaCambio taxa, MetodoArredondamento novoArredondamento) {
        return (Dolar) TaxaCambio.converterParBidirecional(
                this, taxa, (MoedaPadrao) Moeda.USD, novoArredondamento);
    }

    /**
     * Converte para {@link Euro} aceitando taxa {@code BRL -> EUR} ou {@code EUR -> BRL}.
     *
     * @param taxa taxa do par {@code BRL/EUR}
     * @return valor convertido em Euro
     * @throws IllegalArgumentException se {@code taxa} for nula ou de outro par de moedas
     */
    public Euro paraEuro(TaxaCambio taxa) {
        return paraEuro(taxa, arredondamento);
    }

    /**
     * Converte para {@link Euro} aplicando e retendo {@code novoArredondamento}.
     *
     * @param taxa taxa do par {@code BRL/EUR}
     * @param novoArredondamento política a aplicar e reter
     * @return valor convertido em Euro
     * @throws IllegalArgumentException se qualquer argumento for inválido ou de outro par
     */
    public Euro paraEuro(TaxaCambio taxa, MetodoArredondamento novoArredondamento) {
        return (Euro) TaxaCambio.converterParBidirecional(
                this, taxa, (MoedaPadrao) Moeda.EUR, novoArredondamento);
    }

    @Override
    public int compareTo(Real outro) {
        if (outro == null) {
            throw new IllegalArgumentException("O operando em Real não pode ser nulo.");
        }
        return valor.compareTo(outro.valor);
    }
}
