package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.CustomerCode;
import org.springframework.samples.petclinic.util.OwnerRegion;

/**
 * Assigns a newly built owner its {@code customerCode}, formatted {@code <REGION>-<HASH8>}
 * where REGION is the owner's region (derived from its postcode, see {@link OwnerRegion})
 * and HASH8 the first 8 upper-case hex characters of SHA-256 over the normalized telephone
 * concatenated with the last name (e.g. {@code NSW-1A2B3C4D}). Runs after
 * {@link NormalizeOwnerTelephone} (so the telephone is canonical) and before
 * {@link SaveOwner}, mutating the built owner in place so the code is stored and returned
 * with the owner. When the computed code collides with an existing owner's customerCode it
 * is de-duplicated by appending {@code -<n>} (smallest n of 2 or more that is unique); the
 * new owner is not yet persisted here, so it never collides with itself.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = OwnerRegion.of(owner);
        String code = CustomerCode.of(region, owner.getTelephone(), owner.getLastName());
        owner.setCustomerCode(CustomerCode.deduplicate(code, takenCodes(ownerRepository)::contains));
    }

    /** The customer codes already in use by existing owners. */
    private static Set<String> takenCodes(OwnerRepository ownerRepository) {
        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getCustomerCode() != null) {
                taken.add(existing.getCustomerCode());
            }
        }
        return taken;
    }
}
