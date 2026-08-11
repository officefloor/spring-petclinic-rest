package org.springframework.samples.petclinic.mapper;

import java.util.Set;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code riskFlag} &mdash; a single boolean that is true when the owner warrants
 * review because any one of three independent risk signals holds, false when none does:
 *
 * <ul>
 * <li>the owner is a possible duplicate ({@code possibleDuplicate} set true at creation);</li>
 * <li>its city is over its soft capacity ({@code capacityWarning} set true at creation, i.e. the
 * city already held 40 or more owners, approaching the hard limit of 50);</li>
 * <li>its email domain is <em>disposable-adjacent</em> &mdash; the domain shares a known disposable
 * provider's second-level label ({@code mailinator}, {@code tempmail} or {@code guerrillamail}) but
 * is not itself on the hard blocklist (a create with an exactly-blocked domain is rejected 400 by
 * {@code RequireEmailDomain} and never persisted, so a stored disposable-adjacent email is one under
 * a different, non-blocked top-level domain such as {@code mailinator.org}).</li>
 * </ul>
 *
 * <p>Purely derived from the owner's already-stored fields, so it is recomputed identically on every
 * read. Kept as a standalone class (not a method on {@link OwnerMapper}) so MapStruct does not
 * mistake it for an implicit mapping method, mirroring {@link OwnerSegment} and {@link Locality}.
 */
public final class RiskFlag {

    /**
     * Second-level labels of the known disposable email providers. An email whose domain uses one of
     * these labels is disposable-adjacent; the exact blocked domains ({@code <label>.com}) are
     * rejected before a create, so any such email that is stored uses a different top-level domain.
     */
    private static final Set<String> DISPOSABLE_BASES = Set.of("mailinator", "tempmail", "guerrillamail");

    private RiskFlag() {
    }

    /** True when any single risk signal holds for {@code owner}; false when none does. */
    public static boolean of(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
            || Boolean.TRUE.equals(owner.getCapacityWarning())
            || isDisposableAdjacent(owner.getEmail());
    }

    /** True when {@code email}'s domain shares a known disposable provider's second-level label. */
    private static boolean isDisposableAdjacent(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase();
        String[] labels = domain.split("\\.");
        if (labels.length < 2) {
            return false; // no distinct second-level label to classify
        }
        return DISPOSABLE_BASES.contains(labels[labels.length - 2]);
    }
}
