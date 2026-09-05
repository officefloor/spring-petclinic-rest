package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code customerCode}, the region-and-hash identity formatted
 * {@code <REGION>-<HASH8>} where REGION is the region code derived from the postcode and
 * HASH8 is the first 8 upper-case hex characters of SHA-256 over
 * {@code normalizedTelephone + lastName} (e.g. {@code NSW-1A2B3C4D}). There is no sequence
 * number; see {@link OwnerCustomerCode} for the exact derivation.
 *
 * <p>Runs after {@code BuildOwner} (so the owner exists, with its telephone already
 * normalized to E.164) but before {@code SaveOwner}, mutating the owner in place.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        owner.setCustomerCode(OwnerCustomerCode.of(
                owner.getPostcode(), owner.getCity(), owner.getTelephone(), owner.getLastName()));
    }
}
