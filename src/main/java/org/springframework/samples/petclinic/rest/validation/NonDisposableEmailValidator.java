package org.springframework.samples.petclinic.rest.validation;

import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Rejects an email whose domain is a known disposable-mail provider. A blank or absent email,
 * or one with no domain part, is left to the field-level format constraint and accepted here.
 */
public class NonDisposableEmailValidator implements ConstraintValidator<NonDisposableEmail, String> {

    private static final Set<String> BLOCKED = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.isBlank()) {
            return true;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return true;
        }
        return !BLOCKED.contains(email.substring(at + 1).toLowerCase());
    }
}
