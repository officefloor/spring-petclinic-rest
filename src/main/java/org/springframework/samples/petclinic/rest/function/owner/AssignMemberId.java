package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.CityRegion;
import org.springframework.samples.petclinic.model.FiscalYear;
import org.springframework.samples.petclinic.model.MemberId;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.RegionPostcodes;

/**
 * Assigns a newly built owner its {@code memberId}, formatted {@code <REGION><FY><HASH8><CHK>}: REGION
 * is the region code derived from the owner's postcode (falling back to its city, else
 * {@link CityRegion#UNKNOWN}), FY is the two-digit fiscal year of the business-day-adjusted
 * registration date, HASH8 is the first 8 upper-case hex characters of SHA-256 over the normalized
 * telephone followed by the last name, and CHK is the Luhn check digit over the preceding digits (e.g.
 * {@code NSW261A2B3C4D5}). See {@link MemberId}. Runs after the telephone has been normalized and the
 * registration date assigned, so both the hash and the fiscal year cover the stored values.
 */
public class AssignMemberId {

    public void service(@Val Owner owner) {
        String region = region(owner);
        int fiscalYear = FiscalYear.twoDigit(owner.getRegistrationDate());
        owner.setMemberId(MemberId.of(region, fiscalYear, owner.getTelephone(), owner.getLastName()));
    }

    /** The owner's region: derived from the postcode, else the city, else {@link CityRegion#UNKNOWN}. */
    private static String region(Owner owner) {
        String byPostcode = RegionPostcodes.regionOf(owner.getPostcode());
        return byPostcode != null ? byPostcode : CityRegion.of(owner.getCity());
    }
}
