package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.CustomerCode;

/**
 * Assigns the {@code customerCode} to a newly built owner, formatted {@code <REGION>-<HASH8>}
 * where REGION is the region derived from the owner's postcode (with a city fallback) and HASH8 is
 * the first 8 upper-case hex characters of SHA-256 over the owner's normalized telephone and last
 * name (e.g. {@code NSW-1A2B3C4D}). The code is a pure function of the owner's own fields, so it no
 * longer depends on how many owners already exist and carries no sequence number.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        owner.setCustomerCode(CustomerCode.of(owner));
    }
}
