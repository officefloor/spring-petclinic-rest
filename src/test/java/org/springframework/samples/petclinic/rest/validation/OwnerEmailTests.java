package org.springframework.samples.petclinic.rest.validation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class OwnerEmailTests {

    @Test
    void acceptsAbsentEmail() {
        assertThatCode(() -> OwnerEmail.validate(null)).doesNotThrowAnyException();
    }

    @Test
    void acceptsNonDisposableDomains() {
        assertThatCode(() -> {
            OwnerEmail.validate("george.franklin@example.com");
            OwnerEmail.validate("someone@notmailinator.com");
            OwnerEmail.validate("someone@mailinator.com.au");
        }).doesNotThrowAnyException();
    }

    @Test
    void rejectsDisposableDomains() {
        assertThatThrownBy(() -> OwnerEmail.validate("x@mailinator.com")).isInstanceOf(InvalidEmailException.class);
        assertThatThrownBy(() -> OwnerEmail.validate("x@tempmail.com")).isInstanceOf(InvalidEmailException.class);
        assertThatThrownBy(() -> OwnerEmail.validate("x@guerrillamail.com")).isInstanceOf(InvalidEmailException.class);
    }

    @Test
    void rejectsDisposableDomainsCaseInsensitivelyAndSubdomains() {
        assertThatThrownBy(() -> OwnerEmail.validate("X@MailInator.COM")).isInstanceOf(InvalidEmailException.class);
        assertThatThrownBy(() -> OwnerEmail.validate("x@eu.mailinator.com")).isInstanceOf(InvalidEmailException.class);
    }
}
