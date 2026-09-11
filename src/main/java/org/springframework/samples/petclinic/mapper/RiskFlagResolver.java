package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.validation.DisposableEmailPolicy;

/**
 * Derives an owner's {@code riskFlag}: a single roll-up risk signal that is {@code true} when <em>any</em> of the
 * owner's individual risk conditions holds, and {@code false} otherwise. The flag is raised when
 *
 * <ul>
 *   <li>the owner is a possible (soft) duplicate ({@code possibleDuplicate} is true);</li>
 *   <li>the owner's email domain is disposable-adjacent (see
 *       {@link DisposableEmailPolicy#isDisposableAdjacent}); or</li>
 *   <li>the owner's city is over its soft capacity — the capacity warning it carried on create
 *       ({@code capacityWarning} is true).</li>
 * </ul>
 *
 * <p>Every input is derived purely from the owner's own stored state (the two snapshot flags taken at create and the
 * stored email), so the flag a create returns and the flag a later read returns always agree. Kept a static utility
 * like the other mapper resolvers, since it derives purely from its argument.
 */
public final class RiskFlagResolver {

    private RiskFlagResolver() {
    }

    /**
     * Returns the owner's roll-up risk flag: {@code true} when the owner is a possible duplicate, its email domain is
     * disposable-adjacent, or its city was over its soft capacity on create; {@code false} otherwise.
     *
     * @param owner the owner to evaluate
     * @return {@code true} when any risk condition holds
     */
    public static boolean deriveRiskFlag(Owner owner) {
        return Boolean.TRUE.equals(owner.getPossibleDuplicate())
            || Boolean.TRUE.equals(owner.getCapacityWarning())
            || DisposableEmailPolicy.isDisposableAdjacent(owner.getEmail());
    }
}
