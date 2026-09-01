package org.springframework.samples.petclinic.rest.validation;

import java.util.Locale;
import java.util.Set;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Rejects an owner email whose domain is on a blocklist of disposable-mail
 * providers. An absent email, or an email without a domain part, passes here -
 * presence and general format are enforced separately by the field constraints.
 */
public class DisposableEmailValidator implements ConstraintValidator<DisposableEmailValidation, String> {

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
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        return !BLOCKED_DOMAINS.contains(domain);
    }
}
