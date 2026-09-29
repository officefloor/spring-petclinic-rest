package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

/**
 * The single definition of the disposable-email-domain blocklist. An owner email whose domain is one of
 * these throwaway providers is rejected. Matching is case-insensitive and applies only to a present,
 * syntactically valid address (a blank email has no domain to check).
 *
 * <p>Kept out of the function classes themselves because OfficeFloor requires each function class to
 * expose exactly one public method.
 */
final class DisposableEmailDomains {

    private static final Set<String> BLOCKED = Set.of(
            "mailinator.com",
            "tempmail.com",
            "guerrillamail.com");

    /**
     * Whether the given (non-blank) email address sits on a blocked domain. The domain is the part after
     * the last {@code @}, compared case-insensitively.
     */
    static boolean isDisposable(String email) {
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).trim().toLowerCase();
        return BLOCKED.contains(domain);
    }

    private DisposableEmailDomains() {
    }
}
