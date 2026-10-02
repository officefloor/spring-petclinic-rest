package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Populates the response-only {@link Owner#isRiskFlag() riskFlag}: true when any of these hold —
 * the owner is a {@link Owner#isPossibleDuplicate() possible duplicate}, the email domain is
 * {@link OwnerEmail#isDisposableAdjacent disposable-adjacent}, or the owner's city is at or over
 * its soft capacity — otherwise false.
 *
 * <p>Runs after {@link FlagCapacityWarning} and {@link FlagPossibleDuplicate}, so the first two
 * inputs are already decided: the "possible duplicate" input reuses the loaded owner's
 * {@code possibleDuplicate} flag, and the "soft capacity" input reuses its {@code capacityWarning}
 * flag (the soft-capacity band of {@link CityPopulation#WARNING_THRESHOLD} or more owners, below
 * the hard limit). Only the email input is computed here, delegating the adjacency rule to the
 * shared {@link OwnerEmail} authority. Mutates the loaded owner in place so the responder's mapper
 * carries the flag onto the DTO.
 */
public class FlagRiskFlag {

    public void service(@Val Owner owner) {
        boolean risk = owner.isPossibleDuplicate()
                || owner.isCapacityWarning()
                || OwnerEmail.isDisposableAdjacent(owner.getEmail());
        owner.setRiskFlag(risk);
    }
}
