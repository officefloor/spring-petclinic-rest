package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
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
 *
 * <p>The computed code can nonetheless collide with an existing owner's {@code customerCode}. When it
 * does, {@code '-<n>'} is appended with the smallest {@code n} of 2 or more that makes the code
 * unique (e.g. {@code 'NSW-1A2B3C4D-2'}, then {@code '-3'}, …), and the de-duplicated code is stored.
 *
 * <p>Runs before {@link SaveOwner} so the new owner is not yet persisted and cannot collide with
 * itself.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String base = OwnerIdentities.customerCode(owner);
        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(other.getId())) {
                continue; // never collide with self
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
