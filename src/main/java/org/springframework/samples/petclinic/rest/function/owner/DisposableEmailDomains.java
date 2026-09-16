package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

/**
 * The disposable-email policy: the fixed blocklist of throwaway mail domains and the
 * decision of whether an address belongs to one. Kept as its own unit so the blocklist
 * has a single home and {@link RejectDisposableEmail} stays free of policy data.
 */
final class DisposableEmailDomains {

    private static final Set<String> BLOCKED = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    private DisposableEmailDomains() {
    }

    static boolean isDisposable(String email) {
        String domain = EmailNormalizer.domainOf(email);
        return domain != null && BLOCKED.contains(domain);
    }
}
