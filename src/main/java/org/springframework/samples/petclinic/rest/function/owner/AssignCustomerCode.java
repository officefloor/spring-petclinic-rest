package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.CityRegion;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns a new owner's customer code before it is saved. The code is {@code <REGION>-<HASH8>}
 * (see {@link CustomerCode}): the region derived from the owner's postcode plus a hash of the
 * owner's normalized telephone and last name. Runs after the telephone has been normalized, so
 * {@link Owner#getTelephone()} already holds the canonical form the hash is taken over.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = CityRegion.localityOf(owner.getPostcode(), owner.getCity());
        owner.setCustomerCode(CustomerCode.format(region, owner.getTelephone(), owner.getLastName()));
    }
}
