package io.github.brasil4j.exemplos.java;

import io.github.brasil4j.identidade.AbstractDocument;
import io.github.brasil4j.identidade.CheckDigitDocument;
import io.github.brasil4j.identidade.Document;
import io.github.brasil4j.identidade.DocumentFormatter;
import io.github.brasil4j.identidade.DocumentParser;
import io.github.brasil4j.identidade.DocumentParsers;
import io.github.brasil4j.identidade.MaskedDocument;
import io.github.brasil4j.identidade.Rejection;
import io.github.brasil4j.identidade.RejectionReason;
import io.github.brasil4j.identidade.SecurityMaskedDocument;
import io.github.brasil4j.identidade.ValidationResult;
import io.github.brasil4j.identidade.cnpj.Cnpj;
import io.github.brasil4j.identidade.cnpj.CnpjValidationMode;
import io.github.brasil4j.identidade.composicao.DocumentCheck;
import io.github.brasil4j.identidade.composicao.DocumentCheckResult;
import io.github.brasil4j.identidade.cpf.Cpf;
import io.github.brasil4j.identidade.cpf.CpfValidationMode;
import io.github.brasil4j.identidade.nis.Nis;
import io.github.brasil4j.identidade.nis.NisRules;
import io.github.brasil4j.identidade.nis.NisValidationMode;
import io.github.brasil4j.mascara.DocumentMask;
import io.github.brasil4j.verificacao.Modulus11;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Exemplos dos contratos da Spec 010: interpretadores injetáveis com motivo de rejeição, documento
 * próprio do consumidor, máscara de segurança e validadores opcionais do NIS.
 *
 * <p>Todos os documentos são vetores sintéticos ou exemplos públicos de manuais técnicos.
 */
@DisplayName("Exemplos Java · Contratos, composição e polimorfismo")
class ContratosExemplosTest {

    /** Serviço de importação que depende só do contrato: a estratégia é escolhida por quem o configura. */
    static final class ImportadorDeCampo<D extends Document> {
        private final DocumentParser<D> parser;

        ImportadorDeCampo(DocumentParser<D> parser) {
            this.parser = parser;
        }

        String diagnostico(String campo) {
            ValidationResult<D> resultado = parser.parse(campo);
            // O motivo é seguro para log: a biblioteca nunca inclui o documento na rejeição.
            return resultado.fold(documento -> "OK",
                    rejeicao -> rejeicao.getReason() + rejeicao.getCode().map(codigo -> "/" + codigo).orElse(""));
        }
    }

    @Test
    @DisplayName("Interpretador injetável: a mesma classe com estratégias diferentes e motivo sem exceção")
    void exemploInterpretadorInjetavel() {
        // 1. O componente recebe um DocumentParser; trocar a regra é trocar a estratégia, sem if/else.
        ImportadorDeCampo<Cpf> cpfSoFormato = new ImportadorDeCampo<>(Cpf.parser(CpfValidationMode.FORMAT_ONLY));
        ImportadorDeCampo<Cpf> cpfComDv = new ImportadorDeCampo<>(Cpf.parser(CpfValidationMode.NUMERIC));
        assertEquals("OK", cpfSoFormato.diagnostico("280.012.389-39"));
        assertEquals("INVALID_CHECK_DIGITS", cpfComDv.diagnostico("280.012.389-39"));
        assertEquals("INVALID_FORMAT", cpfComDv.diagnostico("280.012.38939"));
        assertEquals("MISSING", cpfComDv.diagnostico(null));

        // 2. Regras do negócio decoram o interpretador e só rodam depois da aceitação nativa.
        DocumentParser<Cnpj> somenteMatriz = Cnpj.parser()
                .withPolicy(cnpj -> cnpj.getBranch().equals("0001"), "SOMENTE_MATRIZ");
        ImportadorDeCampo<Cnpj> fornecedores = new ImportadorDeCampo<>(somenteMatriz);
        assertEquals("OK", fornecedores.diagnostico("11.222.333/0001-81"));
        assertEquals("POLICY/SOMENTE_MATRIZ", fornecedores.diagnostico("12.ABC.345/01DE-35"));

        // 3. Em testes, um dublê substitui o interpretador por lambda.
        ImportadorDeCampo<Cpf> comDuble = new ImportadorDeCampo<>(
                campo -> ValidationResult.invalid(Rejection.of(RejectionReason.POLICY, "CADASTRO_BLOQUEADO")));
        assertEquals("POLICY/CADASTRO_BLOQUEADO", comDuble.diagnostico("qualquer"));

        // 4. A interpretação estrita continua lançando IllegalArgumentException com a mensagem de sempre.
        assertEquals(Cpf.parse("28001238938"), Cpf.parser(CpfValidationMode.NUMERIC).parseOrThrow("280.012.389-38"));
    }

    /**
     * Documento do próprio sistema, montado só com blocos públicos da biblioteca. A "matrícula funcional"
     * é sintética: oito dígitos de base, um DV por módulo 11 e máscara {@code 999.999-999}.
     */
    static final class MatriculaFuncional extends AbstractDocument
            implements MaskedDocument, SecurityMaskedDocument, CheckDigitDocument {
        private static final DocumentMask MASCARA = DocumentMask.of("999.999-999");
        private static final Modulus11 DV = Modulus11.builder().weightsFromRight(2, 9).digits(1).build();
        static final DocumentParser<MatriculaFuncional> PARSER = DocumentParsers.builder(MatriculaFuncional::new)
                .mask(MASCARA)
                .checkDigits(DV)
                .errorMessage("Matrícula inválida.")
                .build();

        private MatriculaFuncional(String canonico) {
            super(canonico); // igualdade, hashCode e toString oculto vêm da base
        }

        @Override
        public String applyMask() {
            return MASCARA.apply(getCanonicalValue());
        }

        @Override
        public String applySecurityMask(char caractere) {
            return MASCARA.applySecure(getCanonicalValue(), 3, 1, caractere);
        }

        @Override
        public String getBase() {
            return getCanonicalValue().substring(0, 8);
        }

        @Override
        public String getCheckDigits() {
            return getCanonicalValue().substring(8);
        }

        @Override
        public boolean hasValidCheckDigits() {
            return DV.verify(getCanonicalValue());
        }
    }

    @Test
    @DisplayName("Documento do consumidor: mesmos blocos, mesmos contratos e mesma lista que CPF, CNPJ e NIS")
    void exemploDocumentoDoConsumidor() {
        MatriculaFuncional matricula = MatriculaFuncional.PARSER.parseOrThrow("123.456-789");
        assertEquals("123456789", matricula.getCanonicalValue());
        assertEquals("123.456-789", matricula.applyMask());
        assertEquals("***.456-78*", matricula.applySecurityMask());
        assertEquals("[documento oculto]", matricula.toString()); // seguro em logs por padrão
        assertEquals(matricula, MatriculaFuncional.PARSER.parseOrThrow("123456789"));
        assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_CHECK_DIGITS)),
                MatriculaFuncional.PARSER.parse("123.456-780").getRejection());

        // Uma única lista heterogênea, sem casts, e o motivo de cada decisão.
        Map<String, DocumentCheck<?>> campos = new LinkedHashMap<>();
        campos.put("matricula", DocumentCheck.of("123.456-789", MatriculaFuncional.PARSER));
        campos.put("cpf", DocumentCheck.cpf("280.012.389-39", CpfValidationMode.NUMERIC));
        campos.put("cnpj", DocumentCheck.cnpj("12ABC34501DE35", CnpjValidationMode.ALPHANUMERIC));
        campos.put("nis", DocumentCheck.nis("01234567898"));

        List<String> relatorio = new ArrayList<>();
        for (Map.Entry<String, DocumentCheck<?>> campo : campos.entrySet()) {
            DocumentCheckResult resultado = campo.getValue().evaluate();
            relatorio.add(campo.getKey() + "=" + resultado.getStatus()
                    + resultado.getRejection().map(r -> "(" + r.getReason() + ")").orElse(""));
        }
        assertEquals(Arrays.asList(
                "matricula=ACCEPTED",
                "cpf=REJECTED_BY_VALIDATION(INVALID_CHECK_DIGITS)",
                "cnpj=ACCEPTED",
                "nis=ACCEPTED"), relatorio);
    }

    @Test
    @DisplayName("Apresentação: máscara completa, máscara de segurança (* ou #) e representação oculta")
    void exemploMascaraDeSeguranca() {
        Cpf cpf = Cpf.parse("01234567890");
        Cnpj cnpj = Cnpj.parse("12ABC34501DE35");
        Nis nis = Nis.parse("01234567898");

        // Sem ocultação: máscara completa no CPF e no CNPJ e canônico no NIS (sem máscara pronta).
        assertEquals("012.345.678-90", cpf.applyMask());
        assertEquals("12.ABC.345/01DE-35", cnpj.applyMask());
        assertEquals("01234567898", nis.getCanonicalValue());

        // Máscara de segurança, por exemplo para telas de conferência e comprovantes.
        assertEquals("***.345.678-**", cpf.applySecurityMask());
        assertEquals("###.345.678-##", cpf.applySecurityMask('#'));
        assertEquals("**.ABC.345/01DE-**", cnpj.applySecurityMask());
        assertEquals("***3456789*", nis.applySecurityMask());

        // As mesmas apresentações como estratégias reutilizáveis (java.util.function.Function).
        assertEquals("***.345.678-**", DocumentFormatter.<Cpf>secure().apply(cpf));
        assertEquals("**********DE35", DocumentFormatter.<Cnpj>redacted(0, 4, '*').apply(cnpj));
        assertEquals("012.345.678-90", DocumentCheck.cpf("01234567890", CpfValidationMode.FORMAT_ONLY)
                .withFormatter(DocumentFormatter.masked()).evaluate().getFormattedValue().get());

        // O toString continua ocultando CPF e NIS em logs.
        assertEquals("***.***.***-**", cpf.toString());
        assertEquals("[NIS oculto]", nis.toString());
    }

    @Test
    @DisplayName("NIS: estrutural por padrão e validadores de DV opcionais, nomeados pela fonte")
    void exemploValidadoresOpcionaisDoNis() {
        // Padrão: só 11 dígitos ASCII, como nos leiautes; DV divergente continua aceito.
        assertTrue(Nis.parser().isValid("01234567898"));

        // Fórmula publicada no Caderno do Agente Financeiro v016 da Caixa (alcance: PIS/Pasep no FGTS).
        DocumentParser<Nis> caixaFgts = Nis.parser(NisValidationMode.CAIXA_FGTS_V016);
        assertTrue(caixaFgts.isValid("00000000019"));
        assertFalse(caixaFgts.isValid("00000000060")); // resto 1 invalida nessa fórmula

        // Convenção legada (Declarações/UEM): resto 1 vira DV 0 e onze algarismos iguais são recusados.
        DocumentParser<Nis> legado = Nis.parser(NisValidationMode.LEGACY_MOD11);
        assertTrue(legado.isValid("00000000060"));
        assertFalse(legado.isValid("00000000000"));

        // A guarda de repetidos (SIPREV RV_03) é opcional e encadeável a qualquer configuração.
        DocumentParser<Nis> caixaComGuarda = caixaFgts.withRule(NisRules.repeatedDigits());
        assertEquals(Optional.of(Rejection.of(RejectionReason.INVALID_CHECK_DIGITS, "REPEATED_DIGITS")),
                caixaComGuarda.parse("00000000000").getRejection());
        assertTrue(caixaComGuarda.isValid("11111111116"));
    }
}
