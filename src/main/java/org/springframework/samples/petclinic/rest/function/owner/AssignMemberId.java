package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.IdentityVersion;
import org.springframework.samples.petclinic.util.Locality;
import org.springframework.samples.petclinic.util.MemberId;

/**
 * Assigns the owner's memberId, formatted {@code <REGION><FY><HASH8><CHK>} (see {@link MemberId}):
 * REGION is the {@link IdentityVersion#regionCode(String) version-2 region code} — the region derived
 * from the owner's postcode (falling back to the city; see {@link Locality}) with the version tag
 * mixed in — FY the two-digit fiscal year of the resolved registration date, HASH8 the first eight
 * upper-case hex characters of SHA-256 over the normalized telephone and last name, and CHK the Luhn
 * check digit over the {@code <REGION><FY><HASH8>} body. The owner's telephone is already in canonical
 * E.164 form here, having been normalized upstream by {@link NormalizeTelephone}, and its registration
 * date has been resolved by {@link ResolveRegistrationDate}. The identity carries no per-owner
 * sequence, so it does not depend on other owners. Mutates the built {@link Owner} in place.
 */
public class AssignMemberId {

    public void service(@Val Owner owner) {
        String region = IdentityVersion.regionCode(Locality.of(owner.getCity(), owner.getPostcode()));
        owner.setMemberId(
                MemberId.of(region, owner.getRegistrationDate(), owner.getTelephone(), owner.getLastName()));
    }
}
