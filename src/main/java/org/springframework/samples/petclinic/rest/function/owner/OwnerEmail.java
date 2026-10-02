package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Single source of truth for owner email-domain classification, used by the create-time validation and the
 * per-response risk roll-up. Owns the "domain = everything after the last '@', lower-cased" rule
 * ({@link #domain}) and the disposable-provider blocklist, so the exact-match rejection and the softer
 * adjacency signal stay in step. Sibling to {@link OwnerCity}, {@link OwnerAddress} and {@link OwnerTelephone}.
 *
 * <p>The blocklist ({@link #DISPOSABLE_DOMAINS}) is the set of throwaway-inbox providers whose exact
 * domains {@link ValidateNewOwner} rejects with a 400. {@link #isDisposableAdjacent} is the related soft
 * signal: a domain whose second-level label matches one of those providers — catching both subdomains of a
 * blocked provider ({@code inbox.mailinator.com}) and look-alikes under another TLD ({@code mailinator.net}).
 * The exact blocklisted domains are rejected before an owner is persisted, so only adjacent ones reach the
 * risk roll-up.
 */
final class OwnerEmail {

    private OwnerEmail() {
    }

    /** Disposable-email providers whose exact domains are rejected (a 400) at create time. */
    static final Set<String> DISPOSABLE_DOMAINS = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * The second-level labels of {@link #DISPOSABLE_DOMAINS} (e.g. {@code mailinator}, {@code tempmail},
     * {@code guerrillamail}), derived so the adjacency rule tracks the blocklist as its single source.
     */
    private static final Set<String> DISPOSABLE_BASES = DISPOSABLE_DOMAINS.stream()
            .map(OwnerEmail::secondLevelLabel)
            .collect(Collectors.toUnmodifiableSet());

    /**
     * The domain of {@code email}: everything after the last {@code '@'}, lower-cased, or {@code null} when
     * the email is {@code null} or blank.
     */
    static String domain(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.substring(email.lastIndexOf('@') + 1).toLowerCase(Locale.ROOT);
    }

    /** Whether {@code email}'s domain is on the exact disposable {@link #DISPOSABLE_DOMAINS blocklist}. */
    static boolean isDisposable(String email) {
        String domain = domain(email);
        return domain != null && DISPOSABLE_DOMAINS.contains(domain);
    }

    /**
     * Whether {@code email}'s domain is disposable-adjacent: its second-level label matches that of a known
     * disposable provider (see {@link #DISPOSABLE_BASES}). A null or blank email is not adjacent.
     */
    static boolean isDisposableAdjacent(String email) {
        String domain = domain(email);
        return domain != null && DISPOSABLE_BASES.contains(secondLevelLabel(domain));
    }

    /**
     * The label immediately to the left of a domain's final dot (its second-level label): {@code mailinator}
     * for both {@code mailinator.com} and {@code inbox.mailinator.com}. A domain with no dot is returned as
     * is.
     */
    private static String secondLevelLabel(String domain) {
        String[] labels = domain.split("\\.");
        return labels.length >= 2 ? labels[labels.length - 2] : domain;
    }
}
