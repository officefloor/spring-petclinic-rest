package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.DisposableDomains;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the {@code riskFlag} field produced by {@link OwnerMapper#toOwnerDto}: a single boolean that
 * is {@code true} when any of the owner's risk signals hold, otherwise {@code false}.
 *
 * <p>The signals are the ones already recorded on the owner plus a read-time email check:
 * <ul>
 * <li>the owner is a {@link Owner#getPossibleDuplicate() possible duplicate};</li>
 * <li>the owner's email domain is {@link DisposableDomains#isAdjacent(String) disposable-adjacent};</li>
 * <li>the owner's city is over soft capacity, recorded as its
 * {@link Owner#getCapacityWarning() capacityWarning}.</li>
 * </ul>
 */
public final class RiskFlag {

    private RiskFlag() {
    }

    /**
     * Derive the risk flag for an owner.
     *
     * @param owner the owner; its duplicate, email and capacity signals drive the result
     * @return {@code true} when any risk signal holds, otherwise {@code false}
     */
    public static boolean of(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || Boolean.TRUE.equals(owner.getCapacityWarning())
                || DisposableDomains.isAdjacentEmail(owner.getEmail());
    }
}
