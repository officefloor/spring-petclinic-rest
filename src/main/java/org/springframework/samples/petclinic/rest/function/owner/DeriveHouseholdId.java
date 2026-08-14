package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Runs in the create-owner pipeline before {@link EnsureUniqueIdentity} and {@link BuildOwner}.
 * Computes the deterministic {@code householdId} — the first 12 hex characters of SHA-256 over the
 * owner's normalized lastName and postcode (see {@link HouseholdId#compute}) — and publishes it as
 * an {@link Out} so {@link EnsureUniqueIdentity} can key duplicate detection on it and
 * {@link AssignHousehold} can stamp it onto the owner being created.
 *
 * <p>Because the identifier is derived from {@code (lastName, postcode)}, owners with the same
 * lastName and postcode share it automatically — no owner has to opt in. {@code sharesHousehold} no
 * longer forms the link; it only bypasses the household duplicate block in
 * {@link EnsureUniqueIdentity}. An owner with no postcode has no shared household, so {@code null}
 * is published and its identityKey carries an empty household component.
 */
public class DeriveHouseholdId {

    public void service(@Val OwnerFieldsDto request, Out<HouseholdId> householdIdOut) {
        String householdId = HouseholdId.compute(request.getLastName(), request.getPostcode());
        householdIdOut.set(householdId == null ? null : new HouseholdId(householdId));
    }
}
