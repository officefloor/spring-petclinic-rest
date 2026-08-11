package org.springframework.samples.petclinic.util;

import java.util.Locale;
import java.util.Set;

/**
 * The fixed set of disposable (throwaway) email domains and the two membership tests over it.
 *
 * <p>An <em>exact</em> match is refused outright at create/update time (see the owner email
 * validation), so a persisted owner never carries an exactly-disposable address. A
 * <em>disposable-adjacent</em> address is one whose domain is a sub-domain of a disposable domain
 * (e.g. {@code signup.mailinator.com}) — close enough to the throwaway providers to be worth
 * flagging, but not itself blocked. Adjacency therefore also treats an exact match as adjacent, so
 * the predicate is a safe superset of {@link #isBlocked(String)}.
 */
public final class DisposableEmailDomains {

    /** Disposable email domains that are refused outright when they appear as an owner's domain. */
    private static final Set<String> DOMAINS = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    private DisposableEmailDomains() {
    }

    /**
     * @param domain a bare email domain (the part after {@code '@'}), or {@code null}.
     * @return {@code true} when {@code domain} is exactly one of the disposable domains.
     */
    public static boolean isBlocked(String domain) {
        return domain != null && DOMAINS.contains(domain.trim().toLowerCase(Locale.ROOT));
    }

    /**
     * @param email a full email address, or {@code null}.
     * @return {@code true} when the address's domain is a disposable domain or a sub-domain of one;
     *         {@code false} when the email is absent or has no domain.
     */
    public static boolean isDisposableAdjacent(String email) {
        if (email == null) {
            return false;
        }
        int at = email.indexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).trim().toLowerCase(Locale.ROOT);
        for (String disposable : DOMAINS) {
            if (domain.equals(disposable) || domain.endsWith("." + disposable)) {
                return true;
            }
        }
        return false;
    }
}
