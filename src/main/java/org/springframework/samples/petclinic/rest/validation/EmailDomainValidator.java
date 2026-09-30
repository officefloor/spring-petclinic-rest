package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Locale;
import java.util.Set;

public class EmailDomainValidator implements ConstraintValidator<EmailDomainValidation, String> {

    private static final Set<String> BLOCKED_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        // absence and general syntax are handled elsewhere (@NotEmpty / format: email);
        // here we only reject disposable-email domains.
        if (email == null || email.isBlank()) {
            return true;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return true;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        return !BLOCKED_DOMAINS.contains(domain);
    }
}
