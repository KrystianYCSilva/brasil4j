package io.github.brasil4j.exemplos.java;

import io.github.brasil4j.financas.Dolar;
import io.github.brasil4j.financas.Euro;
import io.github.brasil4j.financas.MetodoArredondamento;
import io.github.brasil4j.financas.PoliticaResiduo;
import io.github.brasil4j.financas.Real;
import io.github.brasil4j.financas.TaxaCambio;
import io.github.brasil4j.geografia.CodigoMunicipio;
import io.github.brasil4j.geografia.Uf;
import io.github.brasil4j.identidade.cnpj.Cnpj;
import io.github.brasil4j.identidade.cpf.Cpf;
import io.github.brasil4j.identidade.cpf.CpfValidationMode;
import io.github.brasil4j.identidade.nis.Nis;
import io.github.brasil4j.identidade.nis.NisFormatter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Cenários integrados ponta a ponta combinando {@code identidade}, {@code geografia} e {@code financas}
 * em fluxos típicos de sistemas empresariais brasileiros em Java 8.
 */
@DisplayName("Exemplos Java · Cenários Integrados (NF-e Interestadual e Folha/Reembolso)")
class CenariosIntegradosExemplosTest {

    @Test
    @DisplayName("Cenário 1: Emissão de venda interestadual (CNPJ alfanumérico + CPF + IBGE/UF + rateio de frete e duplicatas)")
    void cenarioVendaInterestadualComValidacaoGeograficaERateioContabil() {
        // 1. Identificação do emitente (SP) e do destinatário pessoa física (PR):
        Cnpj emitente = Cnpj.parse("12.ABC.345/01DE-35");
        CodigoMunicipio municipioOrigem = CodigoMunicipio.parse("3550308"); // São Paulo/SP

        Cpf destinatario = Cpf.parse("280.012.389-38", CpfValidationMode.NUMERIC);
        CodigoMunicipio municipioDestino = CodigoMunicipio.parse("4115200"); // Maringá/PR

        // 2. Consistência cadastral e detecção automática de operação interestadual:
        Uf ufOrigem = municipioOrigem.getUf().orElseThrow(AssertionError::new);
        Uf ufDestino = municipioDestino.getUf().orElseThrow(AssertionError::new);
        assertTrue(municipioOrigem.matchesUf(Uf.SP));
        assertTrue(municipioDestino.matchesUf(Uf.PR));
        assertNotEquals(ufOrigem, ufDestino); // SP -> PR caracteriza operação interestadual

        // 3. Cálculo dos itens, rateio exato do frete por Hamilton e geração de 3 duplicatas:
        Real itemA = Real.de("320.00");
        Real itemB = Real.de("180.00");
        Real itemBrinde = Real.ZERO;
        Real freteInterestadual = Real.de("50.00");

        List<Real> fretePorItem = freteInterestadual.alocar(
                Arrays.asList(itemA.getValor(), itemB.getValor(), itemBrinde.getValor()));
        assertEquals(Arrays.asList(Real.de("32.00"), Real.de("18.00"), Real.ZERO), fretePorItem);

        Real totalNota = itemA.somar(itemB).somar(freteInterestadual); // R$ 550,00
        List<Real> duplicatas = totalNota.ratear(3, PoliticaResiduo.PRIMEIRAS_PARCELAS);
        assertEquals(Arrays.asList(Real.de("183.34"), Real.de("183.33"), Real.de("183.33")), duplicatas);

        // 4. Registro de auditoria expõe CNPJ/municípios/total, mas oculta automaticamente o CPF do cliente:
        String resumoAuditoria = String.format(
                "emitente=%s origem=%s/%s destino=%s/%s cliente=%s total=%s",
                emitente,
                municipioOrigem,
                ufOrigem.getSigla(),
                municipioDestino,
                ufDestino.getSigla(),
                destinatario,
                totalNota);
        assertEquals(
                "emitente=12ABC34501DE35 origem=3550308/SP destino=4115200/PR cliente=***.***.***-** total=BRL 550.00",
                resumoAuditoria);
    }

    @Test
    @DisplayName("Cenário 2: Folha/RH de filial corporativa com NIS mascarado no holerite e reembolso internacional PTAX")
    void cenarioFolhaFilialComReembolsoDeViagemInternacional() {
        // 1. Vínculo entre matriz e filial conferido pela raiz de 8 caracteres do CNPJ:
        Cnpj matriz = Cnpj.parse("11.222.333/0001-81");
        Cnpj filial = Cnpj.parse("11.222.333/0002-62");
        assertEquals(matriz.getRoot(), filial.getRoot());

        // 2. Dados do colaborador com ocultação padrão em logs e formatação explícita apenas para o demonstrativo:
        Cpf cpfColaborador = Cpf.parse("000.123.456-01", CpfValidationMode.NUMERIC);
        Nis nisColaborador = Nis.parse("00000000280");

        String nisNoDemonstrativo = NisFormatter.format(nisColaborador, n -> {
            String d = n.getCanonicalValue();
            return d.substring(0, 3) + "." + d.substring(3, 8) + "." + d.substring(8, 10) + "-" + d.substring(10);
        });
        assertEquals("000.00000.28-0", nisNoDemonstrativo);
        assertEquals("Colaborador cpf=***.***.***-** nis=[NIS oculto]",
                "Colaborador cpf=" + cpfColaborador + " nis=" + nisColaborador);

        // 3. Liquidação em Real somando salário líquido + reembolso de hospedagem (USD) e transporte (EUR) via PTAX:
        Real salarioBase = Real.de("6800.00");
        Real descontoAdiantamento = Real.de("1200.00");

        TaxaCambio ptaxUsd = TaxaCambio.umDolarEmReais("5.4832");
        TaxaCambio ptaxEur = TaxaCambio.umEuroEmReais("6.1000");

        Real reembolsoHotel = Dolar.de("150.00").paraReal(ptaxUsd, MetodoArredondamento.FINANCEIRO); // R$ 822,48
        Real reembolsoTrem = Euro.de("45.00").paraReal(ptaxEur, MetodoArredondamento.FINANCEIRO);    // R$ 274,50

        Real totalDeposito = salarioBase
                .subtrair(descontoAdiantamento)
                .somar(reembolsoHotel)
                .somar(reembolsoTrem);

        assertEquals(Real.de("6696.98"), totalDeposito);
        assertEquals(669698L, totalDeposito.getCentavos()); // pronto para remessa bancária CNAB!
    }
}
