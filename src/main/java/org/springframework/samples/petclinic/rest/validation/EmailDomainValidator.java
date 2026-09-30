package org.springframework.samples.petclinic.rest.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class EmailDomainValidator implements ConstraintValidator<EmailDomainValidation, String> {

    private static final Set<String> BLOCKED_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * The base (second-level) labels of the blocked disposable domains, e.g. 'mailinator' for
     * 'mailinator.com'. Used to detect disposable-adjacent domains that are not on the outright
     * blocklist.
     */
    private static final Set<String> DISPOSABLE_BASES = BLOCKED_DOMAINS.stream()
        .map(EmailDomainValidator::baseLabel)
        .collect(Collectors.toUnmodifiableSet());

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        // absence and general syntax are handled elsewhere (@NotEmpty / format: email);
        // here we only reject disposable-email domains.
        String domain = domainOf(email);
        return domain == null || !BLOCKED_DOMAINS.contains(domain);
    }

    /**
     * Whether the given email address is disposable-adjacent: its domain shares the base
     * (second-level) label of a known disposable domain, so it is either a blocked disposable
     * domain, a subdomain of one (e.g. 'x.mailinator.com') or the same base name under a
     * different TLD (e.g. 'mailinator.net'). Returns false when the email or its domain is
     * absent, or the domain has no recognisable base label.
     */
    public static boolean isDisposableAdjacent(String email) {
        String domain = domainOf(email);
        if (domain == null) {
            return false;
        }
        String base = baseLabel(domain);
        return base != null && DISPOSABLE_BASES.contains(base);
    }

    /**
     * Extracts the lower-cased domain part of an email address (the text after the final '@'), or
     * null when the email is absent or carries no domain. Shared by the blocklist check and the
     * disposable-adjacent check so both parse the address the same way.
     */
    private static String domainOf(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return null;
        }
        return email.substring(at + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * Returns the base (second-level) label of a domain: the label immediately before the final
     * TLD label (e.g. 'mailinator' for both 'mailinator.com' and 'x.mailinator.com'). Null when
     * the domain has no dot (so no distinguishable TLD).
     */
    private static String baseLabel(String domain) {
        int lastDot = domain.lastIndexOf('.');
        if (lastDot <= 0) {
            return null;
        }
        String withoutTld = domain.substring(0, lastDot);
        int prevDot = withoutTld.lastIndexOf('.');
        return withoutTld.substring(prevDot + 1);
    }
}
