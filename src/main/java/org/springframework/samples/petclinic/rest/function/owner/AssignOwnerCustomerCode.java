package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners}: assigns the new owner's {@code customerCode}. The code is
 * {@code <REGION>-<HASH8>} — the region derived from the owner's postcode (see
 * {@link Locality}) and a stable hash of the owner's normalized telephone and last name; it
 * carries no sequence number. It runs after the telephone has been normalized (so the hash
 * covers the E.164 value) and is persisted with the row and returned on later reads.
 *
 * <p>Should the formatted code collide with an existing owner's {@code customerCode}, it is
 * de-duplicated by appending {@code -<n>} (see {@link CustomerCode#deduplicate}).
 *
 * @see CustomerCode for the {@code <REGION>-<HASH8>} format.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Locality.region(owner.getCity(), owner.getPostcode());
        String code = CustomerCode.format(region, owner.getTelephone(), owner.getLastName());
        Set<String> inUse = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getCustomerCode() != null) {
                inUse.add(existing.getCustomerCode());
            }
        }
        owner.setCustomerCode(CustomerCode.deduplicate(code, inUse::contains));
    }
}
