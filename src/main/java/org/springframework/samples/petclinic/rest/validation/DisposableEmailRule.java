package org.springframework.samples.petclinic.rest.validation;

import java.util.Locale;
import java.util.Set;

/**
 * Decides whether an email address belongs to a disposable-email provider.
 *
 * <p>Disposable providers hand out throwaway inboxes, so owners registered with one cannot be
 * reliably contacted. An email is rejected when the domain after its {@code '@'} is on a small,
 * fixed blocklist ({@code mailinator.com}, {@code tempmail.com}, {@code guerrillamail.com}). The
 * domain is compared case-insensitively; an absent email carries no domain and is accepted.
 */
public final class DisposableEmailRule {

    /** Lower-cased domains whose emails are rejected as disposable. */
    private static final Set<String> BLOCKED_DOMAINS = Set.of(
        "mailinator.com",
        "tempmail.com",
        "guerrillamail.com");

    private DisposableEmailRule() {
    }

    /**
     * Whether {@code email} uses a disposable-email domain.
     *
     * @param email the owner's email, possibly {@code null} or without an {@code '@'}
     * @return {@code true} when the domain following the last {@code '@'} is on the blocklist,
     *         {@code false} otherwise (including for a {@code null} email or one with no domain)
     */
    public static boolean isDisposable(String email) {
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        return BLOCKED_DOMAINS.contains(domain);
    }
}
