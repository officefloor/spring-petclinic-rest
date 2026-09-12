package org.springframework.samples.petclinic.model;

import java.util.Set;

/**
 * Derives an owner's {@code riskFlag}: a single summary boolean that is {@code true} when the
 * owner trips any of the individual soft-risk signals, {@code false} otherwise.
 *
 * <p>The flag is {@code true} when <em>any</em> of these hold:
 * <ul>
 *   <li>the owner is a possible duplicate ({@code possibleDuplicate} is true);</li>
 *   <li>the owner's email domain is <em>disposable-adjacent</em> (see
 *       {@link #isDisposableAdjacent(String)});</li>
 *   <li>the owner's city is over its soft capacity ({@code capacityWarning} is true — the
 *       city already held 40 or more owners, approaching the hard capacity limit).</li>
 * </ul>
 *
 * <p>Derived from the owner's own persisted fields, so a fetched owner always reports the same
 * value the signals imply.
 */
public final class RiskFlag {

    /**
     * The registrable base labels of the known disposable email domains (the second-level
     * label of {@code mailinator.com}, {@code tempmail.com}, {@code guerrillamail.com}). An
     * exact disposable domain is already rejected at create time; a domain that merely shares
     * one of these base labels — a different TLD or a subdomain — is "disposable-adjacent".
     */
    private static final Set<String> DISPOSABLE_BASES =
            Set.of("mailinator", "tempmail", "guerrillamail");

    private RiskFlag() {
    }

    /** True when any soft-risk signal is present on the owner. */
    public static boolean of(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || Boolean.TRUE.equals(owner.getCapacityWarning())
                || isDisposableAdjacent(owner.getEmail());
    }

    /**
     * True when {@code email}'s domain is disposable-adjacent: its second-level label (the
     * label immediately left of the final dot) matches a known disposable service — so a
     * different TLD (e.g. {@code mailinator.org}) or a subdomain (e.g. {@code mx.tempmail.io})
     * of one counts, without the domain being on the exact disposable blocklist. A null,
     * blank or domain-less value is never adjacent.
     */
    private static boolean isDisposableAdjacent(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).trim().toLowerCase();
        String[] labels = domain.split("\\.");
        if (labels.length < 2) {
            return false;
        }
        String secondLevel = labels[labels.length - 2];
        return DISPOSABLE_BASES.contains(secondLevel);
    }
}
