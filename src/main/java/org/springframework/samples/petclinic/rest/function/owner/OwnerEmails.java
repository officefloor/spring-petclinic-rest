package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Shared email handling for the owner pipeline. Not an OfficeFloor function class (it is never
 * referenced from a step), so it may expose helper methods.
 */
final class OwnerEmails {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /** Disposable / throwaway email domains that a create-owner request may not use. */
    private static final Set<String> DISPOSABLE_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    private OwnerEmails() {
    }

    /**
     * Rejects an email whose domain is on the disposable-domain blocklist. An absent value (null or
     * blank) passes through untouched; a present value is expected to already be
     * {@link #normalize(String) normalized} (lower-cased), so the domain comparison is exact.
     */
    static void rejectDisposableDomain(String email) throws DisposableEmailDomainException {
        if (email == null || email.isBlank()) {
            return;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return;
        }
        String domain = email.substring(at + 1);
        if (DISPOSABLE_DOMAINS.contains(domain)) {
            throw new DisposableEmailDomainException(email);
        }
    }

    /**
     * Returns the email lower-cased, or the original value unchanged when absent (null or blank).
     * Throws when a present value is not a syntactically valid address.
     */
    static String normalize(String email) throws InvalidEmailException {
        if (email == null || email.isBlank()) {
            return email;
        }
        if (!EMAIL.matcher(email).matches()) {
            throw new InvalidEmailException(email);
        }
        return email.toLowerCase();
    }
}
