package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code riskFlag}: {@code true} when the owner warrants review because any of the
 * three risk conditions hold, otherwise {@code false}.
 *
 * <ul>
 * <li>the owner is a possible duplicate ({@link Owner#getPossibleDuplicate()} — set by
 * {@link DetectPossibleDuplicate}),
 * <li>the email domain is disposable-adjacent
 * ({@link DisposableEmailDomains#isDisposableAdjacent(String)}),
 * <li>the city is over its soft capacity ({@link Owner#getCapacityWarning()} — the 40-49 warning band
 * set by {@link FlagCityCapacityWarning}).
 * </ul>
 *
 * <p>Every input is already persisted on the owner at create time, so this is computed at response time
 * by {@link org.springframework.samples.petclinic.mapper.OwnerMapper} — no extra column or pipeline step.
 */
public final class RiskFlag {

    private RiskFlag() {
    }

    public static boolean of(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
                || DisposableEmailDomains.isDisposableAdjacent(owner.getEmail())
                || Boolean.TRUE.equals(owner.getCapacityWarning());
    }
}
