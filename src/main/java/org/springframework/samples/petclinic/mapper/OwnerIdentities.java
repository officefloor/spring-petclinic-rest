package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto;
import org.springframework.samples.petclinic.rest.function.owner.OwnerIdentity;

/**
 * Assembles the version-2 {@code identity} object of an owner's response, grouping the derived
 * identifiers — {@code memberId}, {@code householdId} and {@code identityKey} — that in version 1
 * were flat, top-level fields. Each identifier is already rederived with the fixed {@code V2}
 * version tag by its own producer ({@link org.springframework.samples.petclinic.rest.function.owner.AssignMemberId},
 * {@link org.springframework.samples.petclinic.rest.function.owner.AssignHousehold} and
 * {@link OwnerIdentity}); this helper only regroups them. Kept as a plain static helper (not a
 * mapper method) so MapStruct does not treat it as an implicit mapping method.
 */
public final class OwnerIdentities {

    private OwnerIdentities() {
    }

    /** The owner's grouped version-2 identity: its memberId, householdId and derived identityKey. */
    public static OwnerIdentityDto of(Owner owner) {
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(OwnerIdentity.of(owner));
        return identity;
    }
}
