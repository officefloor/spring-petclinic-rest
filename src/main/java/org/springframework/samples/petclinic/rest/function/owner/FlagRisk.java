package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Computes this owner's overall {@code riskFlag} response field, on both the create and read pipelines.
 *
 * <p>The flag is {@code true} when <em>any</em> of three soft risk signals hold, otherwise {@code false}:
 * <ul>
 * <li>the owner is a {@link Owner#getPossibleDuplicate() possible duplicate} (a soft near-match of another
 * owner, as decided earlier by {@link FlagPossibleDuplicate});</li>
 * <li>the email domain is {@link OwnerEmail#isDisposableAdjacent disposable-adjacent};</li>
 * <li>the city is over its soft capacity — it holds at least {@link FlagCityCapacityWarning#WARNING_THRESHOLD}
 * owners (the per-city soft limit, below the hard {@link CheckCityCapacity#CAPACITY} cap).</li>
 * </ul>
 *
 * <p>A roll-up of signals that are themselves computed per request; it is therefore also computed per
 * request, mutated onto the loaded owner in place, and not persisted. Runs after the individual flag steps
 * so {@code possibleDuplicate} is already set.
 */
public class FlagRisk {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        boolean possibleDuplicate = Boolean.TRUE.equals(owner.getPossibleDuplicate());
        boolean overSoftCapacity = OwnerCity.countInCity(owner.getCity(), ownerRepository)
                >= FlagCityCapacityWarning.WARNING_THRESHOLD;

        owner.setRiskFlag(possibleDuplicate
                || OwnerEmail.isDisposableAdjacent(owner.getEmail())
                || overSoftCapacity);
    }
}
