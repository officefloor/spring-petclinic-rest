package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Resolves whether the owner response should carry the composite risk flag and publishes it for the
 * responder. The flag is raised when the owner needs a closer look for any of three reasons:
 *
 * <ul>
 * <li>it is a possible (soft) duplicate of an existing owner (see {@link DetectPossibleDuplicate});</li>
 * <li>its city is over its soft capacity, i.e. the per-city {@link ResolveCapacityWarning capacity
 * warning} is raised;</li>
 * <li>its email domain is {@link DisposableDomains#isAdjacent(String) disposable-adjacent}.</li>
 * </ul>
 *
 * <p>Runs before the responder so both the create and read responses carry the flag. The two
 * warning-style inputs it reuses are already resolved upstream, so this step only composes them with
 * the stored duplicate flag and the derived email signal.
 */
public class ResolveRiskFlag {

    public void service(@Val Owner owner, @CapacityWarning @Val Boolean capacityWarning,
            @RiskFlag Out<Boolean> riskFlag) {
        boolean disposableAdjacent = DisposableDomains.isAdjacent(DisposableDomains.domainOf(owner.getEmail()));
        riskFlag.set(owner.getPossibleDuplicate() || Boolean.TRUE.equals(capacityWarning) || disposableAdjacent);
    }
}
