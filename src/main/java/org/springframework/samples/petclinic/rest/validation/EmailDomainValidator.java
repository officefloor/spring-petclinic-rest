package org.springframework.samples.petclinic.rest.validation;

import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates that an email address's domain is not on the disposable-domain blocklist
 * (mailinator.com, tempmail.com, guerrillamail.com). The domain is the part after the
 * last {@code '@'}, compared case-insensitively against the blocklist to match the
 * lower-cased canonical form {@code OwnerMapper} stores. A {@code null} value is left to
 * {@code @Email}, and a value without an {@code '@'} (not a valid address) is left to
 * {@code @Email} as well.
 */
public class EmailDomainValidator implements ConstraintValidator<EmailDomainValidation, String> {

    private static final Set<String> BLOCKED_DOMAINS = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) {
            return true;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return true;
        }
        String domain = email.substring(at + 1).trim().toLowerCase();
        return !BLOCKED_DOMAINS.contains(domain);
    }
}
