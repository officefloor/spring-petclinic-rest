package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.Regions;
import org.springframework.samples.petclinic.model.CustomerCode;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs after {@link BuildOwner} and before {@link SaveOwner} on {@code POST /api/owners}. Assigns the
 * new owner's {@code customerCode} formatted {@code <REGION>-<HASH8>}, where REGION is the region code
 * derived from the postcode (see {@link Regions#regionForPostcode(String)}, e.g. {@code NSW}, or
 * {@code UNKNOWN} when the postcode resolves to no known region) and HASH8 is the first 8 upper-case
 * hex characters of {@code SHA-256(normalizedTelephone + lastName)} (see
 * {@link CustomerCode#hash8(String, String)}, e.g. {@code NSW-1A2B3C4D}). The identity no longer uses
 * per-city sequence numbers, so this step depends only on the owner's own fields.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = Regions.regionForPostcode(owner.getPostcode());
        String hash8 = CustomerCode.hash8(owner.getTelephone(), owner.getLastName());
        owner.setCustomerCode(CustomerCode.format(region, hash8));
    }
}
