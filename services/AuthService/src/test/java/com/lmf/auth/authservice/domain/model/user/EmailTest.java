package com.lmf.auth.authservice.domain.model.user;

import com.lmf.auth.authservice.domain.exception.InvalidEmailException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Test
    void normalizaParaMinusculasERemoveEspacos() {
        assertThat(new Email("  Alice@Example.COM ").value()).isEqualTo("alice@example.com");
    }

    @Test
    void rejeitaFormatoInvalido() {
        assertThatThrownBy(() -> new Email("sem-arroba")).isInstanceOf(InvalidEmailException.class);
        assertThatThrownBy(() -> new Email("a@b")).isInstanceOf(InvalidEmailException.class);
        assertThatThrownBy(() -> new Email("")).isInstanceOf(InvalidEmailException.class);
        assertThatThrownBy(() -> new Email(null)).isInstanceOf(InvalidEmailException.class);
    }
}
