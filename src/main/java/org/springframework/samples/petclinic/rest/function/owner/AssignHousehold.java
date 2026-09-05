package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's deterministic {@code householdId} (see {@link OwnerHouseholdId}): the
 * first 12 hex characters of {@code SHA-256(normalizedLastName + '|' + postcode)}. Because it
 * is derived purely from the (last name, postcode) pair, any two owners sharing a last name
 * (compared case-insensitively with runs of whitespace collapsed to a single space) and
 * postcode receive the <em>same</em> identifier automatically — they are, by definition, the
 * same household — and no owner ever has to look up or backfill another. The value never
 * changes as more members join.
 *
 * <p>Runs after {@link BuildOwner} (so the Owner exists) and within the create transaction,
 * unconditionally: {@code sharesHousehold} no longer creates the link (the link is implicit
 * in the identifier); it only bypasses the later household-duplicate block (see
 * {@link RejectDuplicateIdentity}).
 */
public class AssignHousehold {

    public void service(@Val Owner owner) {
        owner.setHouseholdId(OwnerHouseholdId.of(owner));
    }
}
