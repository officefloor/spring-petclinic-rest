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

    /**
     * Whether the address is <em>disposable-adjacent</em>: related to a blocked throwaway
     * domain without being exactly on the blocklist (an exact match is rejected outright by
     * {@link RejectDisposableEmail}, so it never reaches here). A domain is adjacent when it
     * is a subdomain of a blocked domain (e.g. {@code inbox.mailinator.com}) or shares a
     * blocked domain's base label under a different suffix (e.g. {@code mailinator.net}).
     * A soft signal only, used to raise the owner's risk flag.
     */
    static boolean isDisposableAdjacent(String email) {
        String domain = EmailNormalizer.domainOf(email);
        if (domain == null || BLOCKED.contains(domain)) {
            return false;
        }
        for (String blocked : BLOCKED) {
            if (domain.endsWith("." + blocked) || baseLabel(domain).equals(baseLabel(blocked))) {
                return true;
            }
        }
        return false;
    }

    /** The first (registrable) label of a domain, e.g. {@code mailinator} for both
     *  {@code mailinator.com} and {@code mailinator.net}. */
    private static String baseLabel(String domain) {
        int dot = domain.indexOf('.');
        return dot < 0 ? domain : domain.substring(0, dot);
    }
}
