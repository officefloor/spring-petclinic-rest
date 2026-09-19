package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.CustomerCode;
import org.springframework.samples.petclinic.util.Postcodes;

/**
 * Assigns the owner's customer code before it is saved. The code is the region-and-hash
 * identity {@code <REGION>-<HASH8>} (see {@link CustomerCode}): {@code REGION} is the region
 * derived from the owner's postcode and {@code HASH8} the first eight upper-case hex characters
 * of the SHA-256 digest over the normalized telephone and last name. Runs after
 * {@link NormalizeOwnerTelephone} has put the telephone in E.164 form and {@link BuildOwner}
 * has produced the entity, so the hash sees the normalized value; mutates the built owner in
 * place.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = Postcodes.regionCode(owner.getPostcode());
        owner.setCustomerCode(CustomerCode.of(region, owner.getTelephone(), owner.getLastName()));
    }
}
