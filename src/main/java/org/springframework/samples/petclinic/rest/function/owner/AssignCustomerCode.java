package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.CustomerCode;
import org.springframework.samples.petclinic.util.Locality;

/**
 * Assigns the owner's customerCode, formatted {@code <REGION>-<HASH8>} where REGION is the region
 * derived from the owner's postcode (falling back to the city; see {@link Locality}) and HASH8 is the
 * first eight upper-case hex characters of SHA-256 over the normalized telephone and last name (see
 * {@link CustomerCode}). The owner's telephone is already in canonical E.164 form here, having been
 * normalized upstream by {@link NormalizeTelephone}. The identity no longer carries a per-city
 * sequence, so it does not depend on other owners. Mutates the built {@link Owner} in place.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = Locality.of(owner.getCity(), owner.getPostcode());
        owner.setCustomerCode(CustomerCode.of(region, owner.getTelephone(), owner.getLastName()));
    }
}
