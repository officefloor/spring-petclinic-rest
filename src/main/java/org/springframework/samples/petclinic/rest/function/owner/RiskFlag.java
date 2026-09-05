package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The owner {@code riskFlag}: true when any single risk signal holds, otherwise false. The signals
 * are
 * <ul>
 *   <li>the owner is a possible (soft) duplicate ({@link Owner#getPossibleDuplicate()}, see
 *       {@link AssignPossibleDuplicate});</li>
 *   <li>the email domain is disposable-adjacent (a subdomain of a known disposable domain, see
 *       {@link RejectDisposableEmailDomain#adjacent(String)});</li>
 *   <li>the owner's city is over its soft capacity (see
 *       {@link CityCapacity#overSoftCapacity(Owner, OwnerRepository)}).</li>
 * </ul>
 */
public final class RiskFlag {

    private RiskFlag() {
    }

    /** Computes the {@code riskFlag} for {@code owner}. */
    static boolean of(Owner owner, OwnerRepository ownerRepository) {
        if (owner == null) {
            return false;
        }
        boolean possibleDuplicate = Boolean.TRUE.equals(owner.getPossibleDuplicate());
        boolean disposableAdjacent = RejectDisposableEmailDomain.adjacent(owner.getEmail());
        boolean overSoftCapacity = CityCapacity.overSoftCapacity(owner, ownerRepository);
        return possibleDuplicate || disposableAdjacent || overSoftCapacity;
    }
}
