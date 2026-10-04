package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

/**
 * Rejects an owner's email address when its domain is on the disposable-domain blocklist.
 * Syntactic validity is enforced separately by the field's {@code @Email} constraint, so a
 * {@code null} value is accepted here (an optional email) and any value without a domain part
 * is left for that constraint. The domain is compared case-insensitively.
 */
public class EmailDomainValidator implements ConstraintValidator<EmailDomainValidation, String> {

    private static final Set<String> DISPOSABLE_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) {
            return true; // optional; presence/syntax handled by other constraints
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return true; // no domain part; leave to the @Email constraint
        }
        String domain = email.substring(at + 1).toLowerCase();
        return !DISPOSABLE_DOMAINS.contains(domain);
    }
}
