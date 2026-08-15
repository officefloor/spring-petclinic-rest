package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code customerCode}, the region-and-hash identity {@code '<REGION>-<HASH8>'}
 * where REGION is the region code derived from the postcode (falling back to the city, then
 * {@code 'UNKNOWN'}) and HASH8 is the first 8 upper-case hex characters of SHA-256 over
 * {@code normalizedTelephone + lastName} (e.g. {@code 'NSW-1A2B3C4D'}). See {@link CustomerCode}.
 *
 * <p>There are no sequence numbers: the code is a pure function of the owner's already-normalized
 * telephone, last name, postcode and city. Runs after {@link BuildOwner} has mapped the request onto
 * the entity (telephone already normalized to E.164 by {@link ValidateOwnerFields}) and before
 * {@link SaveOwner}, within the same write transaction.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        owner.setCustomerCode(CustomerCode.of(
                owner.getTelephone(), owner.getLastName(), owner.getPostcode(), owner.getCity()));
    }
}
