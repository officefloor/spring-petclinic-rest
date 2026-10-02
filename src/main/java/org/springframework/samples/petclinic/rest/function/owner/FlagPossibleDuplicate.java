package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Ensures this owner's {@code possibleDuplicate} / {@code possibleDuplicateOf} response fields are set,
 * on both the create and read pipelines.
 *
 * <p>A possible duplicate was a soft near match: another owner sharing this owner's last name
 * (case-insensitively) and postcode. Now that the household is keyed on exactly (lastName, postcode)
 * (see {@link Household}), any such owner is in the <em>same household</em> and could only have been
 * created by declaring {@code sharesHousehold} to bypass the household duplicate block
 * ({@link CheckHouseholdUnique}) — a non-declared one is rejected with a 409. A declared household member
 * is not a suspected duplicate, so there is no longer any near match that should be flagged: the fields
 * are therefore always {@code false} / {@code null}. They are computed per request, not persisted, and
 * recomputed identically on each read.
 */
public class FlagPossibleDuplicate {

    public void service(@Val Owner owner) {
        owner.setPossibleDuplicate(false);
        owner.setPossibleDuplicateOf(null);
    }
}
