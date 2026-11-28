package io.github.brasil4j.identidade;

import io.github.brasil4j.identidade.cnpj.Cnpj;
import io.github.brasil4j.identidade.cnpj.CnpjValidationMode;
import io.github.brasil4j.identidade.cpf.Cpf;
import io.github.brasil4j.identidade.cpf.CpfValidationMode;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Demonstra a inversão de dependência: o componente conhece só o contrato do interpretador. */
class ParserInjectionTest {
    /** Componente sintético de importação que recebe o interpretador por construtor. */
    static final class Importador<D extends Document> {
        private final DocumentParser<D> parser;

        Importador(DocumentParser<D> parser) {
            this.parser = parser;
        }

        String classificar(String campo) {
            return parser.parse(campo).fold(documento -> "ACEITO",
                    rejeicao -> rejeicao.getReason() + rejeicao.getCode().map(codigo -> ":" + codigo).orElse(""));
        }
    }

    @Test
    void mesmoComponenteComEstrategiasDiferentes() {
        assertEquals("ACEITO", new Importador<>(Cpf.parser(CpfValidationMode.FORMAT_ONLY)).classificar("28001238939"));
        assertEquals("INVALID_CHECK_DIGITS",
                new Importador<>(Cpf.parser(CpfValidationMode.NUMERIC)).classificar("28001238939"));
        assertEquals("UNSUPPORTED_BASE",
                new Importador<>(Cnpj.parser(CnpjValidationMode.NUMERIC)).classificar("12ABC34501DE35"));
        assertEquals("MISSING", new Importador<>(Cnpj.parser()).classificar(null));
    }

    @Test
    void dubleDeTesteSubstituiOInterpretador() {
        Rejection bloqueio = Rejection.of(RejectionReason.POLICY, "CADASTRO_BLOQUEADO");
        DocumentParser<Cpf> duble = campo -> ValidationResult.invalid(bloqueio);
        assertEquals("POLICY:CADASTRO_BLOQUEADO", new Importador<>(duble).classificar("01234567890"));
        assertEquals(Optional.of(bloqueio), duble.parse("qualquer").getRejection());
    }
}
