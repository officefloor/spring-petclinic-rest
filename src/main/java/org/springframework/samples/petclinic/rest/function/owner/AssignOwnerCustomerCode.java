package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.CustomerCode;
import org.springframework.samples.petclinic.util.OwnerRegion;

/**
 * Assigns a newly built owner's {@code customerCode}, formatted {@code '<REGION>-<HASH8>'} where
 * REGION is the owner's {@link OwnerRegion region} (derived from its postcode) and HASH8 is the
 * first 8 upper-case hex characters of {@code SHA-256(normalizedTelephone + lastName)} (e.g.
 * 'NSW-1A2B3C4D'). There is no sequence number: the code is a deterministic function of the
 * owner's region and identity. The telephone has already been normalized to E.164 earlier in the
 * pipeline, so {@link Owner#getTelephone()} is the normalized telephone the hash covers. The code
 * is stored in place on the built entity so it is persisted and returned; the membership number,
 * its check digit and the locality are all derived from it. Runs after {@link BuildOwner}.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner) {
        String region = OwnerRegion.of(owner.getPostcode(), owner.getCity());
        owner.setCustomerCode(CustomerCode.of(region, owner.getTelephone(), owner.getLastName()));
    }
}
