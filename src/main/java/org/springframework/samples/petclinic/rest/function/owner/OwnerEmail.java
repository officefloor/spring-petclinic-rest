package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidOwnerEmailException;

/**
 * Shared email handling for the owner pipelines. An owner may include an {@code email}: when present
 * it must be a syntactically valid address whose domain is not on the disposable-domain blocklist,
 * and is stored and returned lower-cased. When absent (null or blank) it is left unset. An invalid
 * or disposable address is rejected with {@link InvalidOwnerEmailException}.
 */
final class OwnerEmail {

    /** WHATWG "valid email address" syntax — a widely accepted syntactic check. */
    private static final Pattern EMAIL = Pattern.compile(
            "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+"
                    + "@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?"
                    + "(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");

    /** Known disposable email domains; an owner email in any of these is rejected. */
    private static final Set<String> DISPOSABLE_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    private OwnerEmail() {
    }

    /**
     * Validates and normalizes the email on the request in place: a present, non-blank email is
     * trimmed, checked for syntax and lower-cased; a null or blank email is left unchanged.
     */
    static void normalize(OwnerFieldsDto request) throws InvalidOwnerEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        String trimmed = email.trim();
        if (!EMAIL.matcher(trimmed).matches()) {
            throw new InvalidOwnerEmailException(email);
        }
        String normalized = trimmed.toLowerCase();
        String domain = normalized.substring(normalized.indexOf('@') + 1);
        if (DISPOSABLE_DOMAINS.contains(domain)) {
            throw new InvalidOwnerEmailException(email);
        }
        request.setEmail(normalized);
    }
}
