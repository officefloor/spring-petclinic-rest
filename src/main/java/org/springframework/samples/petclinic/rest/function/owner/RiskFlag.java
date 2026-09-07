package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code riskFlag}: {@code true} when the owner warrants review because any risk
 * signal holds, otherwise {@code false}. The signals are:
 *
 * <ul>
 *   <li>the owner is a <em>possible duplicate</em> — {@link Owner#getPossibleDuplicate()} is true
 *       (see {@link FlagPossibleDuplicate});</li>
 *   <li>the email domain is <em>disposable-adjacent</em> — it resembles a known disposable provider
 *       without being one of the exactly-rejected domains (see
 *       {@link OwnerEmail#isDisposableAdjacent(String)});</li>
 *   <li>the city is <em>over its soft capacity</em> — {@link Owner#getCapacityWarning()} is true,
 *       i.e. the city was in the approaching-capacity band when the owner was created (see
 *       {@link FlagCapacityWarning}).</li>
 * </ul>
 */
public final class RiskFlag {

    private RiskFlag() {
    }

    public static boolean of(Owner owner) {
        boolean possibleDuplicate = Boolean.TRUE.equals(owner.getPossibleDuplicate());
        boolean disposableAdjacent = OwnerEmail.isDisposableAdjacent(owner.getEmail());
        boolean overSoftCapacity = Boolean.TRUE.equals(owner.getCapacityWarning());
        return possibleDuplicate || disposableAdjacent || overSoftCapacity;
    }
}
