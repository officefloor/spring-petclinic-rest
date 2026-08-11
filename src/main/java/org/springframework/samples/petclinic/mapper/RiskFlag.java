package org.springframework.samples.petclinic.mapper;

import java.util.Locale;
import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's read-time {@code riskFlag}: {@code true} when the owner warrants a closer
 * look for any of three independent reasons, otherwise {@code false}. The reasons are:
 *
 * <ol>
 *   <li>the owner is a possible (soft) duplicate - {@link Owner#getPossibleDuplicate()} is true;</li>
 *   <li>the owner's email domain is <em>disposable-adjacent</em> (see {@link #disposableAdjacent});</li>
 *   <li>the owner's city is over its soft capacity - {@link Owner#getCapacityWarning()} is true,
 *       i.e. the city has reached the soft warning threshold below the hard per-city cap.</li>
 * </ol>
 *
 * <p>Kept out of {@link OwnerMapper} so MapStruct does not mistake the helper for an implicit
 * mapping method and apply it to every boolean property.
 */
public final class RiskFlag {

    /**
     * Base (second-level) names of the disposable providers on the hard email blocklist enforced at
     * creation. A domain is disposable-adjacent when one of its labels is one of these - i.e. it is
     * in the neighbourhood of a disposable provider without being an exact, hard-blocked entry.
     */
    private static final Set<String> DISPOSABLE_BASES = Set.of(
        "mailinator", "tempmail", "guerrillamail");

    private RiskFlag() {
    }

    /** True when any of the three risk signals holds; otherwise false. */
    public static boolean of(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
            || Boolean.TRUE.equals(owner.getCapacityWarning())
            || disposableAdjacent(owner.getEmail());
    }

    /**
     * True when the email's domain is <em>disposable-adjacent</em>: it shares a label with a known
     * disposable provider, catching a different TLD ({@code mailinator.net}) or a subdomain
     * ({@code inbox.mailinator.com}). The exact blocklisted domains are rejected at creation and so
     * never reach here; this is the softer, still-suspicious neighbourhood around them.
     */
    private static boolean disposableAdjacent(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        for (String label : domain.split("\\.")) {
            if (DISPOSABLE_BASES.contains(label)) {
                return true;
            }
        }
        return false;
    }
}
