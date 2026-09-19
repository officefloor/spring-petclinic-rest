package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.CustomerCode;
import org.springframework.samples.petclinic.util.Postcodes;

/**
 * Assigns the owner's customer code before it is saved. The code is the region-and-hash
 * identity {@code <REGION>-<HASH8>} (see {@link CustomerCode}): {@code REGION} is the region
 * derived from the owner's postcode and {@code HASH8} the first eight upper-case hex characters
 * of the SHA-256 digest over the normalized telephone and last name. Should that code collide
 * with an existing owner's, it is {@link CustomerCode#deduplicate de-duplicated} with a
 * {@code -<n>} suffix so distinct owners always receive distinct codes. Runs after
 * {@link NormalizeOwnerTelephone} has put the telephone in E.164 form and {@link BuildOwner}
 * has produced the entity, so the hash sees the normalized value; mutates the built owner in
 * place.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Postcodes.regionCode(owner.getPostcode());
        String customerCode = CustomerCode.of(region, owner.getTelephone(), owner.getLastName());
        owner.setCustomerCode(CustomerCode.deduplicate(customerCode, existingCodes(ownerRepository)));
    }

    private static Set<String> existingCodes(OwnerRepository ownerRepository) {
        Set<String> codes = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getCustomerCode() != null) {
                codes.add(existing.getCustomerCode());
            }
        }
        return codes;
    }
}
