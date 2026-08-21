package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.CustomerCode;

/**
 * Assigns the {@code customerCode} to a newly built owner, formatted {@code <REGION>-<HASH8>}
 * where REGION is the region derived from the owner's postcode (with a city fallback) and HASH8 is
 * the first 8 upper-case hex characters of SHA-256 over the owner's normalized telephone and last
 * name (e.g. {@code NSW-1A2B3C4D}). The code is a pure function of the owner's own fields, so it no
 * longer depends on how many owners already exist and carries no sequence number.
 *
 * <p>Should the computed code collide with an existing owner's {@code customerCode}, it is
 * de-duplicated by appending {@code -<n>} with the smallest {@code n} of 2 or more that makes it
 * unique. This runs before the owner is saved, so it compares against the owners that already
 * exist and excludes the owner being created.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String base = CustomerCode.of(owner);

        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            String code = other.getCustomerCode();
            if (code != null) {
                existing.add(code);
            }
        }

        String code = base;
        for (int n = 2; existing.contains(code); n++) {
            code = base + "-" + n;
        }
        owner.setCustomerCode(code);
    }
}
