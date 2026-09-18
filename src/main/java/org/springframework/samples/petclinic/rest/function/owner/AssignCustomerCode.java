package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.CityRegion;
import org.springframework.samples.petclinic.model.CustomerCode;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.RegionPostcodes;

/**
 * Assigns a newly built owner its {@code customerCode}, formatted {@code <REGION>-<HASH8>}: REGION is
 * the region code derived from the owner's postcode (falling back to its city, else
 * {@link CityRegion#UNKNOWN}) and HASH8 is the first 8 upper-case hex characters of SHA-256 over the
 * normalized telephone followed by the last name (e.g. {@code NSW-1A2B3C4D}). See
 * {@link CustomerCode}. Runs after the telephone has been normalized so the hash covers the stored
 * E.164 value.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = region(owner);
        owner.setCustomerCode(CustomerCode.of(region, owner.getTelephone(), owner.getLastName()));
    }

    /** The owner's region: derived from the postcode, else the city, else {@link CityRegion#UNKNOWN}. */
    private static String region(Owner owner) {
        String byPostcode = RegionPostcodes.regionOf(owner.getPostcode());
        return byPostcode != null ? byPostcode : CityRegion.of(owner.getCity());
    }
}
