package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerIdentityDto;

/**
 * Builds the nested {@code identity} object of the owner response, grouping the owner's version-2
 * {@code memberId}, {@code identityKey} and {@code householdId}. The {@code memberId} and
 * {@code householdId} are the stored, already version-2 values; the {@code identityKey} is derived at
 * read time via {@link IdentityKey}. Kept out of {@link OwnerMapper} so MapStruct does not mistake
 * the helper for an implicit mapping method, and invoked from it by expression.
 */
public final class OwnerIdentityMapper {

    private OwnerIdentityMapper() {
    }

    /** The grouped version-2 identity for {@code owner}. */
    public static OwnerIdentityDto of(Owner owner) {
        OwnerIdentityDto identity = new OwnerIdentityDto();
        identity.setMemberId(owner.getMemberId());
        identity.setHouseholdId(owner.getHouseholdId());
        identity.setIdentityKey(IdentityKey.of(owner));
        return identity;
    }
}
