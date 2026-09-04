package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.mapper.LocalityLookup;
import org.springframework.samples.petclinic.model.Owner;

/**
 * The REGION segment of an owner's region-and-hash identity (see {@link AssignMemberId}). It is the
 * region derived from the owner's postcode with the city as a fallback (see
 * {@link LocalityLookup#regionOf(String, String)}). It is also the REGION prefix embedded in the
 * owner's {@code memberId}; deriving the read-time locality from this is how the locality moves onto
 * the region-and-hash identity.
 */
public final class OwnerRegion {

    private OwnerRegion() {
    }

    /** The REGION segment for {@code owner}. */
    public static String of(Owner owner) {
        return LocalityLookup.regionOf(owner.getCity(), owner.getPostcode());
    }
}
