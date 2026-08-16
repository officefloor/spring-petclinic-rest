package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Derives the {@code riskFlag} response signal: {@code true} when the owner warrants a closer look
 * because any one of the soft risk conditions holds, otherwise {@code false}.
 *
 * <p>The conditions reuse the same helpers as the individual response flags, so {@code riskFlag} is
 * exactly the OR of signals surfaced elsewhere:
 * <ul>
 * <li>a possible duplicate — another owner soft-matches (see {@link PossibleDuplicate});</li>
 * <li>a disposable-adjacent email domain (see {@link DisposableEmail#isAdjacent(String)});</li>
 * <li>a city over its soft capacity — approaching the per-city limit (see
 * {@link CityCapacity#warningFor(Owner, OwnerRepository)}).</li>
 * </ul>
 */
public final class RiskFlag {

    private RiskFlag() {
    }

    public static boolean of(Owner owner, OwnerRepository ownerRepository) {
        return PossibleDuplicate.matchFor(owner, ownerRepository) != null
                || DisposableEmail.isAdjacent(owner.getEmail())
                || CityCapacity.warningFor(owner, ownerRepository);
    }
}
