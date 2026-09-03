package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where REGION is the
 * region code derived from the postcode (NSW/VIC/QLD, else {@code UNKNOWN}) and HASH8 is the first 8
 * upper-case hex characters of SHA-256 over {@code normalizedTelephone + lastName} (e.g.
 * {@code NSW-1A2B3C4D}). There are no sequence numbers: the code is fully determined by the owner's
 * region and hashed identity.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = OwnerRegion.fromPostcode(owner.getPostcode());
        if (region == null) {
            region = "UNKNOWN";
        }
        String hash8 = OwnerIdentity.customerHash(owner.getTelephone(), owner.getLastName());
        owner.setCustomerCode(region + "-" + hash8);
    }
}
