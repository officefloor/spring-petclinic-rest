package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.Locality;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code '<REGION>-<HASH8>'} where REGION is the
 * region code derived from the owner's postcode (via {@link Locality}, postcode first then city) and
 * HASH8 is the first 8 upper-case hex characters of SHA-256 over the normalised telephone
 * concatenated with the last name. There is no per-city sequence number. Mutates the built owner in
 * place before it is saved.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = Locality.of(owner.getCity(), owner.getPostcode());
        String hash8 = OwnerIdentity.customerHash(owner.getTelephone(), owner.getLastName());
        owner.setCustomerCode(region + "-" + hash8);
    }
}
