package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Shared normalization for the optional owner email. An email is optional: when absent (null or
 * blank) it is left as {@code null}. When present it must be a syntactically valid address; the
 * canonical stored/returned form is trimmed and lower-cased.
 */
final class OwnerEmail {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /** Disposable email providers rejected up front: their addresses are throwaway. */
    private static final Set<String> DISPOSABLE_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    private OwnerEmail() {
    }

    /**
     * @return the normalized (trimmed, lower-cased) email, or {@code null} when none was supplied.
     * @throws InvalidEmailException when a non-blank value is not a valid address.
     */
    static String normalize(String email) throws InvalidEmailException {
        if (email == null || email.isBlank()) {
            return null;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(normalized).matches()) {
            throw new InvalidEmailException("Email must be a syntactically valid address");
        }
        String domain = normalized.substring(normalized.indexOf('@') + 1);
        if (DISPOSABLE_DOMAINS.contains(domain)) {
            throw new InvalidEmailException("Email domain is not accepted");
        }
        return normalized;
    }

    /**
     * Whether an email's domain is <em>disposable-adjacent</em>: it resembles a known disposable
     * provider without being one of the exactly-rejected {@link #DISPOSABLE_DOMAINS} (those never
     * reach storage — {@link #normalize} rejects them). A domain is adjacent when it is a subdomain
     * of a disposable domain (e.g. {@code inbox.mailinator.com}) or carries the same provider brand
     * — the disposable domain's second-level label — under a different name (e.g. {@code
     * mailinator.net} or {@code tempmail.io}). A null, blank or otherwise unrelated domain is not
     * adjacent.
     */
    static boolean isDisposableAdjacent(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        int at = normalized.indexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = normalized.substring(at + 1);
        if (domain.isBlank() || DISPOSABLE_DOMAINS.contains(domain)) {
            return false; // absent, or an exactly-rejected domain that never reaches storage
        }
        List<String> labels = Arrays.asList(domain.split("\\.", -1));
        for (String disposable : DISPOSABLE_DOMAINS) {
            if (domain.endsWith("." + disposable)) {
                return true; // a subdomain of a disposable domain
            }
            String brand = disposable.substring(0, disposable.indexOf('.'));
            if (labels.contains(brand)) {
                return true; // same provider brand under a different name
            }
        }
        return false;
    }
}
