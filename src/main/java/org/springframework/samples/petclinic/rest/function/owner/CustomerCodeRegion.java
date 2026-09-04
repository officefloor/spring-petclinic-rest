package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.mapper.LocalityLookup;
import org.springframework.samples.petclinic.model.Owner;

/**
 * The REGION segment of an owner's region-and-hash identity (see {@link AssignCustomerCode}). It is
 * the prefix of an already-assigned {@code customerCode} (the part before the {@code '-'} separating
 * it from the hash), otherwise the region derived from the owner's postcode with the city as a
 * fallback (see {@link LocalityLookup#regionOf(String, String)}). Deriving the read-time locality from
 * this is how the locality moves onto the region-and-hash identity.
 */
public final class CustomerCodeRegion {

    private CustomerCodeRegion() {
    }

    /** The REGION segment for {@code owner}. */
    public static String of(Owner owner) {
        String code = owner.getCustomerCode();
        if (code != null) {
            int dash = code.indexOf('-');
            if (dash > 0) {
                return code.substring(0, dash);
            }
        }
        return LocalityLookup.regionOf(owner.getCity(), owner.getPostcode());
    }
}
