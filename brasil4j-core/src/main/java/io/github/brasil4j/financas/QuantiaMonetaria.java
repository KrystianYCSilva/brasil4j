package io.github.brasil4j.financas;

import java.math.BigDecimal;
import java.util.List;

/**
 * Quantia imutável para qualquer {@link Moeda} não reservada ({@code JPY}, {@code GBP},
 * {@code KWD}, indexadores fiscais, etc.).
 *
 * <p>Nota: {@link #compareTo(QuantiaMonetaria)} exige mesma moeda contábil, lançando
 * {@link IllegalArgumentException} caso coleções ordenadas misturem moedas distintas.
 */
public final class QuantiaMonetaria extends Dinheiro implements Comparable<QuantiaMonetaria> {

    QuantiaMonetaria(MoedaPadrao moeda, BigDecimal valorEscalado, MetodoArredondamento arredondamento) {
        super(moeda, valorEscalado, arredondamento);
    }

    private static MoedaPadrao exigirMoedaNaoReservada(Moeda moeda) {
        MoedaPadrao snap = ValidacaoMoeda.capturarSnapshot(moeda);
        if (snap.getCasasDecimais() == 2) {
            String cod = snap.getCodigo();
            if ("BRL".equals(cod) || "USD".equals(cod) || "EUR".equals(cod)) {
                throw new IllegalArgumentException(
                        "Use Real, Dolar, Euro ou Dinheiro.de para moedas dedicadas (" + cod + "/2).");
            }
        }
        return snap;
    }

    /**
     * Interpreta texto decimal na moeda não reservada com {@link MetodoArredondamento#ABNT_NBR_5891}.
     *
     * @param valor texto decimal canônico
     * @param moeda moeda não reservada
     * @return quantia monetária imutável
     * @throws IllegalArgumentException se inválido ou se a moeda for {@code BRL/2}, {@code USD/2} ou {@code EUR/2}
     */
    public static QuantiaMonetaria de(String valor, Moeda moeda) {
        return de(valor, moeda, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Interpreta texto decimal na moeda não reservada aplicando e retendo {@code arredondamento}.
     *
     * @param valor texto decimal canônico
     * @param moeda moeda não reservada
     * @param arredondamento política de arredondamento
     * @return quantia monetária imutável
     * @throws IllegalArgumentException se qualquer argumento for inválido ou moeda reservada
     */
    public static QuantiaMonetaria de(String valor, Moeda moeda, MetodoArredondamento arredondamento) {
        MoedaPadrao m = exigirMoedaNaoReservada(moeda);
        BigDecimal parsed = AritmeticaMonetaria.parseDecimalCanonico(valor);
        return new QuantiaMonetaria(
                m,
                AritmeticaMonetaria.aplicarArredondamentoSeguro(parsed, m.getCasasDecimais(), arredondamento),
                arredondamento);
    }

    /**
     * Constrói a partir de {@link BigDecimal} com {@link MetodoArredondamento#ABNT_NBR_5891}.
     *
     * @param valor valor decimal
     * @param moeda moeda não reservada
     * @return quantia monetária imutável
     * @throws IllegalArgumentException se inválido ou moeda reservada
     */
    public static QuantiaMonetaria de(BigDecimal valor, Moeda moeda) {
        return de(valor, moeda, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Constrói a partir de {@link BigDecimal} aplicando e retendo {@code arredondamento}.
     *
     * @param valor valor decimal
     * @param moeda moeda não reservada
     * @param arredondamento política de arredondamento
     * @return quantia monetária imutável
     * @throws IllegalArgumentException se qualquer argumento for inválido ou moeda reservada
     */
    public static QuantiaMonetaria de(BigDecimal valor, Moeda moeda, MetodoArredondamento arredondamento) {
        MoedaPadrao m = exigirMoedaNaoReservada(moeda);
        BigDecimal escalado =
                AritmeticaMonetaria.aplicarArredondamentoSeguro(valor, m.getCasasDecimais(), arredondamento);
        return new QuantiaMonetaria(m, escalado, arredondamento);
    }

    /**
     * Interpreta texto decimal rejeitando entradas com mais casas que a moeda.
     *
     * @param valor texto decimal sem casas excedentes
     * @param moeda moeda não reservada
     * @return quantia monetária imutável
     * @throws IllegalArgumentException se inválido, exceder casas ou moeda reservada
     */
    public static QuantiaMonetaria exato(String valor, Moeda moeda) {
        return exato(valor, moeda, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Interpreta texto decimal rejeitando casas excedentes e retendo {@code arredondamento}.
     *
     * @param valor texto decimal sem casas excedentes
     * @param moeda moeda não reservada
     * @param arredondamento política retida
     * @return quantia monetária imutável
     * @throws IllegalArgumentException se qualquer argumento for inválido ou moeda reservada
     */
    public static QuantiaMonetaria exato(String valor, Moeda moeda, MetodoArredondamento arredondamento) {
        MoedaPadrao m = exigirMoedaNaoReservada(moeda);
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        BigDecimal parsed = AritmeticaMonetaria.parseDecimalCanonico(valor);
        return new QuantiaMonetaria(m, AritmeticaMonetaria.escalarExato(parsed, m.getCasasDecimais()), arredondamento);
    }

    /**
     * Constrói rejeitando {@link BigDecimal} com escala superior às casas da moeda.
     *
     * @param valor valor decimal sem casas excedentes
     * @param moeda moeda não reservada
     * @return quantia monetária imutável
     * @throws IllegalArgumentException se inválido, exceder casas ou moeda reservada
     */
    public static QuantiaMonetaria exato(BigDecimal valor, Moeda moeda) {
        return exato(valor, moeda, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Constrói rejeitando escala excedente e retendo {@code arredondamento}.
     *
     * @param valor valor decimal sem casas excedentes
     * @param moeda moeda não reservada
     * @param arredondamento política retida
     * @return quantia monetária imutável
     * @throws IllegalArgumentException se qualquer argumento for inválido ou moeda reservada
     */
    public static QuantiaMonetaria exato(BigDecimal valor, Moeda moeda, MetodoArredondamento arredondamento) {
        MoedaPadrao m = exigirMoedaNaoReservada(moeda);
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        return new QuantiaMonetaria(m, AritmeticaMonetaria.escalarExato(valor, m.getCasasDecimais()), arredondamento);
    }

    /**
     * Constrói a partir de unidades principais inteiras da moeda não reservada.
     *
     * @param unidadesPrincipais unidades inteiras
     * @param moeda moeda não reservada
     * @return quantia monetária imutável
     * @throws IllegalArgumentException se moeda inválida/reservada
     */
    public static QuantiaMonetaria deInteiro(long unidadesPrincipais, Moeda moeda) {
        return deInteiro(unidadesPrincipais, moeda, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Constrói a partir de unidades principais inteiras retendo {@code arredondamento}.
     *
     * @param unidadesPrincipais unidades inteiras
     * @param moeda moeda não reservada
     * @param arredondamento política retida
     * @return quantia monetária imutável
     * @throws IllegalArgumentException se qualquer argumento for inválido ou moeda reservada
     */
    public static QuantiaMonetaria deInteiro(
            long unidadesPrincipais, Moeda moeda, MetodoArredondamento arredondamento) {
        MoedaPadrao m = exigirMoedaNaoReservada(moeda);
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        return new QuantiaMonetaria(
                m, AritmeticaMonetaria.deInteiroEscalado(unidadesPrincipais, m.getCasasDecimais()), arredondamento);
    }

    /**
     * Constrói a partir de unidades mínimas inteiras da moeda não reservada.
     *
     * @param unidadesMinimas unidades mínimas
     * @param moeda moeda não reservada
     * @return quantia monetária imutável
     * @throws IllegalArgumentException se moeda inválida/reservada
     */
    public static QuantiaMonetaria deUnidadesMinimas(long unidadesMinimas, Moeda moeda) {
        return deUnidadesMinimas(unidadesMinimas, moeda, MetodoArredondamento.ABNT_NBR_5891);
    }

    /**
     * Constrói a partir de unidades mínimas inteiras retendo {@code arredondamento}.
     *
     * @param unidadesMinimas unidades mínimas
     * @param moeda moeda não reservada
     * @param arredondamento política retida
     * @return quantia monetária imutável
     * @throws IllegalArgumentException se qualquer argumento for inválido ou moeda reservada
     */
    public static QuantiaMonetaria deUnidadesMinimas(
            long unidadesMinimas, Moeda moeda, MetodoArredondamento arredondamento) {
        MoedaPadrao m = exigirMoedaNaoReservada(moeda);
        AritmeticaMonetaria.exigirArredondamento(arredondamento);
        return new QuantiaMonetaria(
                m, AritmeticaMonetaria.deUnidadesMinimasEscalado(unidadesMinimas, m.getCasasDecimais()), arredondamento);
    }

    /**
     * Soma este valor a outra {@link QuantiaMonetaria} de mesma moeda.
     *
     * @param outro operando de mesma moeda
     * @return soma monetária
     * @throws IllegalArgumentException se {@code outro} for nulo ou de moeda diferente
     */
    public QuantiaMonetaria somar(QuantiaMonetaria outro) {
        exigirMesmaMoeda(outro);
        return new QuantiaMonetaria(
                moeda, AritmeticaMonetaria.somarEscalado(valor, outro.valor, moeda.getCasasDecimais()), arredondamento);
    }

    /**
     * Subtrai {@code outro} desta {@link QuantiaMonetaria} exigindo mesma moeda.
     *
     * @param outro subtraendo de mesma moeda
     * @return diferença monetária
     * @throws IllegalArgumentException se {@code outro} for nulo ou de moeda diferente
     */
    public QuantiaMonetaria subtrair(QuantiaMonetaria outro) {
        exigirMesmaMoeda(outro);
        return new QuantiaMonetaria(
                moeda,
                AritmeticaMonetaria.subtrairEscalado(valor, outro.valor, moeda.getCasasDecimais()),
                arredondamento);
    }

    @Override
    public QuantiaMonetaria abs() {
        return valor.signum() < 0 ? negar() : this;
    }

    @Override
    public QuantiaMonetaria negar() {
        return new QuantiaMonetaria(
                moeda, AritmeticaMonetaria.normalizarZero(valor.negate(), moeda.getCasasDecimais()), arredondamento);
    }

    @Override
    public QuantiaMonetaria comArredondamento(MetodoArredondamento novoArredondamento) {
        AritmeticaMonetaria.exigirArredondamento(novoArredondamento);
        return new QuantiaMonetaria(moeda, valor, novoArredondamento);
    }

    @Override
    public QuantiaMonetaria multiplicar(long fator) {
        return new QuantiaMonetaria(
                moeda, AritmeticaMonetaria.multiplicarPorLong(valor, fator, moeda.getCasasDecimais()), arredondamento);
    }

    @Override
    public QuantiaMonetaria multiplicar(BigDecimal fator) {
        return multiplicar(fator, arredondamento);
    }

    @Override
    public QuantiaMonetaria multiplicar(BigDecimal fator, MetodoArredondamento novoArredondamento) {
        return new QuantiaMonetaria(
                moeda,
                AritmeticaMonetaria.multiplicarPorDecimal(
                        valor, fator, moeda.getCasasDecimais(), novoArredondamento),
                novoArredondamento);
    }

    @Override
    public QuantiaMonetaria dividir(long divisor) {
        return dividir(divisor, arredondamento);
    }

    @Override
    public QuantiaMonetaria dividir(long divisor, MetodoArredondamento novoArredondamento) {
        return new QuantiaMonetaria(
                moeda,
                AritmeticaMonetaria.dividirPorLong(valor, divisor, moeda.getCasasDecimais(), novoArredondamento),
                novoArredondamento);
    }

    @Override
    public List<QuantiaMonetaria> ratear(int parcelas) {
        return ratear(parcelas, PoliticaResiduo.PRIMEIRAS_PARCELAS);
    }

    @Override
    public List<QuantiaMonetaria> ratear(int parcelas, PoliticaResiduo politica) {
        BigDecimal[] fatias =
                AritmeticaMonetaria.ratearEscalado(valor, moeda.getCasasDecimais(), parcelas, politica);
        return mapearFatias(fatias, f -> new QuantiaMonetaria(moeda, f, arredondamento));
    }

    @Override
    public List<QuantiaMonetaria> alocar(List<BigDecimal> pesos) {
        BigDecimal[] fatias =
                AritmeticaMonetaria.alocarHamiltonEscalado(valor, moeda.getCasasDecimais(), pesos);
        return mapearFatias(fatias, f -> new QuantiaMonetaria(moeda, f, arredondamento));
    }

    @Override
    QuantiaMonetaria criarMesmoSubtipo(BigDecimal valorEscalado, MetodoArredondamento novoArredondamento) {
        return new QuantiaMonetaria(moeda, valorEscalado, novoArredondamento);
    }

    @Override
    public int compareTo(QuantiaMonetaria outro) {
        return compararMesmaMoeda(outro);
    }
}
