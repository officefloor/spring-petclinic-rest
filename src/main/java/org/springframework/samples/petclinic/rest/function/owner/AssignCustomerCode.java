package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where REGION is the
 * region derived from the owner's postcode (falling back to the city, then {@code "UNKNOWN"}) and
 * HASH8 is the first eight upper-case hex characters of {@code SHA-256(normalizedTelephone + lastName)}.
 * The identity depends only on the owner's own fields; there are no sequence numbers.
 *
 * <p>When the computed code collides with an existing owner's {@code customerCode}, {@code -<n>} is
 * appended with the smallest {@code n} of 2 or more that makes it unique.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String base = CustomerCodes.build(owner);
        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (other.getId() != null && other.getId().equals(owner.getId())) {
                continue;
            }
            if (other.getCustomerCode() != null) {
                existing.add(other.getCustomerCode());
            }
        }
        String code = base;
        for (int n = 2; existing.contains(code); n++) {
            code = base + "-" + n;
        }
        owner.setCustomerCode(code);
    }
}
