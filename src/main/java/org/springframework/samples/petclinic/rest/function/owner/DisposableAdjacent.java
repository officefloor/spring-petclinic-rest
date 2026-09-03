package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

/**
 * Tests whether an email address is <em>disposable-adjacent</em>: its domain, or any label of it,
 * matches the registrable base of a known disposable-mail provider ({@code mailinator},
 * {@code tempmail}, {@code guerrillamail}). This catches subdomains and alternate TLDs that slip
 * past the exact-domain blocklist enforced by {@link EnsureOwnerEmailNotDisposable} at create time.
 */
public final class DisposableAdjacent {

    private static final Set<String> BASES = Set.of("mailinator", "tempmail", "guerrillamail");

    private DisposableAdjacent() {
    }

    public static boolean matches(String email) {
        int at = (email == null) ? -1 : email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        for (String label : email.substring(at + 1).toLowerCase(Locale.ROOT).split("\\.")) {
            if (BASES.contains(label)) {
                return true;
            }
        }
        return false;
    }
}
