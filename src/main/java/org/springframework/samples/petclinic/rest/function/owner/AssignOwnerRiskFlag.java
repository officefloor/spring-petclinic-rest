package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Sets the owner's transient {@code riskFlag} — true when the owner warrants a manual review
 * because any one of three independent risk signals holds, otherwise false:
 * <ul>
 *   <li>it is a possible duplicate — the flag set by {@link AssignOwnerPossibleDuplicate},
 *       which this step reads, so it must run after it;</li>
 *   <li>its email domain is <em>disposable-adjacent</em> — a near-variant of a known disposable
 *       domain that slips past the exact-match blocklist rejected outright by {@link OwnerEmail}
 *       (e.g. {@code mailinator.net} or {@code x.mailinator.com});</li>
 *   <li>its city is over its soft capacity — the city already holds {@value #SOFT_CAPACITY} or
 *       more owners (the hard limit enforced by {@link EnsureCityCapacity} being
 *       {@value EnsureCityCapacity#CAPACITY}).</li>
 * </ul>
 *
 * <p>Runs on both the create pipeline (after the flags it composes) and the read pipeline, so
 * the value is identical whether returned from the create response or a later GET, mirroring
 * {@link AssignOwnerCapacityWarning}. Counts all owners in the city including this one, so the
 * capacity signal matches whether computed after Save or on a later GET. Never persisted.
 */
public class AssignOwnerRiskFlag {

    /** Owners-in-city count (inclusive) at or above which the city is over its soft capacity. */
    static final int SOFT_CAPACITY = AssignOwnerCapacityWarning.WARN_LOW;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        boolean risky = Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || OwnerEmail.isDisposableAdjacent(owner.getEmail())
                || isOverSoftCapacity(owner, ownerRepository);
        owner.setRiskFlag(risky);
    }

    private static boolean isOverSoftCapacity(Owner owner, OwnerRepository ownerRepository) {
        String city = owner.getCity();
        long count = ownerRepository.findAll().stream()
                .filter(o -> city == null ? o.getCity() == null : city.equals(o.getCity()))
                .count();
        return count >= SOFT_CAPACITY;
    }
}
