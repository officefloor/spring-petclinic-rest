package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.OwnerIdentities;

/**
 * Assigns the {@code customerCode} to the freshly built {@link Owner} before it is saved.
 *
 * <p>The code is formatted {@code '<REGION>-<HASH8>'}: REGION is the region code derived from the
 * owner's postcode (falling back to city — see
 * {@link org.springframework.samples.petclinic.util.Localities}), and HASH8 is the first 8
 * upper-case hex characters of SHA-256 over {@code normalizedTelephone + lastName}
 * (e.g. {@code 'NSW-1A2B3C4D'}). The code no longer carries a per-city sequence number, so it is a
 * pure function of the owner's region and identity and does not depend on other owners.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        owner.setCustomerCode(OwnerIdentities.customerCode(owner));
    }
}
