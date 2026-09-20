package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.CityRegion;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a new owner's customer code before it is saved. The code is {@code <REGION>-<HASH8>}
 * (see {@link CustomerCode}): the region derived from the owner's postcode plus a hash of the
 * owner's normalized telephone and last name. Runs after the telephone has been normalized, so
 * {@link Owner#getTelephone()} already holds the canonical form the hash is taken over. Should
 * that code collide with an existing owner's, it is de-duplicated with a {@code -<n>} suffix.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = CityRegion.localityOf(owner.getPostcode(), owner.getCity());
        String code = CustomerCode.format(region, owner.getTelephone(), owner.getLastName());
        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAllActive()) {
            if (existing.getCustomerCode() != null) {
                taken.add(existing.getCustomerCode());
            }
        }
        owner.setCustomerCode(CustomerCode.deduplicate(code, taken));
    }
}
