package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.CityRegion;
import org.springframework.samples.petclinic.mapper.CustomerCode;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Step of {@code POST /api/owners} that assigns the owner's {@code customerCode}.
 *
 * <p>The code is formatted {@code '<REGION>-<HASH8>'}, where REGION is the canonical region derived
 * from the postcode (postcode-preferred, city-table fallback; see {@link CityRegion}) and HASH8 is the
 * first 8 upper-case hex characters of SHA-256 over {@code normalizedTelephone + lastName}
 * (e.g. {@code 'NSW-1A2B3C4D'}). The telephone has already been normalized to E.164 form by
 * {@link NormalizeOwnerTelephone}, so it feeds the hash verbatim. There are no sequence numbers.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = CityRegion.locality(owner.getPostcode(), owner.getCity());
        owner.setCustomerCode(CustomerCode.of(region, owner.getTelephone(), owner.getLastName()));
    }
}
