package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Derives the owner's {@code riskFlag} at response time: {@code true} when any of three soft risk
 * signals holds, otherwise {@code false}.
 *
 * <ul>
 *   <li>the owner is a {@link Owner#getPossibleDuplicate() possible (soft) duplicate} — set at
 *   creation by {@link DetectOwnerPossibleDuplicate} and persisted;</li>
 *   <li>the email domain is disposable-adjacent — shares a second-level label with a known
 *   disposable provider without being an exact blocklist entry (see
 *   {@link ValidateOwnerEmailDomain#isDisposableAdjacent(String)});</li>
 *   <li>the owner's city is over its {@link #SOFT_CAPACITY soft capacity}, counted with
 *   {@link CheckOwnerCityCapacity#countInCity(OwnerRepository, String)} (the same population the
 *   hard 409 gate and the capacity warning use).</li>
 * </ul>
 *
 * <p>Wired into both the create pipeline and the single-owner GET pipeline, mirroring
 * {@link DeriveOwnerCapacityWarning}, so {@code riskFlag} is populated (never null) on reads as well
 * as on the create response. The flag is transient and set on the in-flight owner so the responder
 * carries it; it is not persisted.
 */
public class DeriveOwnerRiskFlag {

    /**
     * The city crowding threshold for the risk signal. It is the same knob as the capacity-warning
     * lower bound — this is {@link DeriveOwnerCapacityWarning#WARN_THRESHOLD}, so the two move
     * together — but the comparisons differ by intent: the warning marks a city <em>approaching</em>
     * the hard {@link CheckOwnerCityCapacity#CITY_CAPACITY} (band {@code [40, 50)}, using {@code >=}),
     * whereas the risk flag fires once a city is <em>over</em> that threshold ({@code > 40}) and stays
     * set all the way to (and at) the hard cap.
     */
    static final int SOFT_CAPACITY = DeriveOwnerCapacityWarning.WARN_THRESHOLD;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        boolean possibleDuplicate = Boolean.TRUE.equals(owner.getPossibleDuplicate());
        boolean disposableAdjacent = ValidateOwnerEmailDomain.isDisposableAdjacent(owner.getEmail());
        long inCity = CheckOwnerCityCapacity.countInCity(ownerRepository, owner.getCity());
        boolean overSoftCapacity = inCity > SOFT_CAPACITY;
        owner.setRiskFlag(possibleDuplicate || disposableAdjacent || overSoftCapacity);
    }
}
