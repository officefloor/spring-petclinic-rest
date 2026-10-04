package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.samples.petclinic.util.DisposableDomains;

/**
 * Rejects an owner's email address when its domain is on the disposable-domain blocklist.
 * Syntactic validity is enforced separately by the field's {@code @Email} constraint, so a
 * {@code null} value is accepted here (an optional email) and any value without a domain part
 * is left for that constraint. The domain is compared case-insensitively.
 */
public class EmailDomainValidator implements ConstraintValidator<EmailDomainValidation, String> {

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        // A null (optional) email and a value with no domain part are both accepted here; their
        // presence/syntax is enforced by other constraints. isDisposableEmail treats both as not
        // disposable, so only a genuine blocklisted domain is rejected.
        return !DisposableDomains.isDisposableEmail(email);
    }
}
