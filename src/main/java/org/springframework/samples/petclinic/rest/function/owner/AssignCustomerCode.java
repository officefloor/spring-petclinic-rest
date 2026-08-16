package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where REGION is the
 * region code derived from the postcode (see {@link CityRegion#localityOf(String, String)}) and
 * HASH8 is the first 8 upper-case hex characters of SHA-256 over {@code normalizedTelephone + lastName}
 * (see {@link OwnerIdentity#customerCodeHash}). For example {@code NSW-1A2B3C4D}.
 *
 * <p>The code carries no sequence number, so it depends only on this owner's own fields and is stable
 * regardless of how many owners exist. Every value derived from the customer code — the membership
 * number and its check digit (see {@link AssignMembershipNumber}, {@link CustomerCodeCheckDigit}), the
 * create audit line (see {@link AuditOwnerCreated}) and the response {@code locality} — follows this
 * region-and-hash identity.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = CityRegion.localityOf(owner.getCity(), owner.getPostcode());
        String hash8 = OwnerIdentity.customerCodeHash(owner.getTelephone(), owner.getLastName());
        owner.setCustomerCode(region + "-" + hash8);
    }
}
