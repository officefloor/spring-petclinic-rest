package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.OwnerIdentities;

/**
 * Assigns the unified {@code memberId} to the freshly built {@link Owner} before it is saved.
 *
 * <p>The id is formatted {@code '<REGION><FY><HASH8><CHK>'} (no separators): REGION is the region
 * code derived from the owner's postcode (falling back to city — see
 * {@link org.springframework.samples.petclinic.util.Localities}), FY the two-digit fiscal year of
 * the owner's {@code registrationDate}, HASH8 the first 8 upper-case hex characters of SHA-256 over
 * {@code normalizedTelephone + lastName}, and CHK a single Luhn check digit over the decimal digits
 * of {@code '<REGION><FY><HASH8>'} (e.g. {@code 'NSW261A2B3C4D5'}). It is a pure function of the
 * owner's region, fiscal year and identity and does not depend on other owners.
 *
 * <p>The computed id can nonetheless collide with an existing owner's {@code memberId}. When it
 * does, {@code '-<n>'} is appended with the smallest {@code n} of 2 or more that makes the id unique
 * (e.g. {@code 'NSW261A2B3C4D5-2'}, then {@code '-3'}, …), and the de-duplicated id is stored.
 *
 * <p>Runs before {@link SaveOwner} so the new owner is not yet persisted and cannot collide with
 * itself.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String base = OwnerIdentities.memberId(owner);
        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(other.getId())) {
                continue; // never collide with self
            }
            if (other.getMemberId() != null) {
                existing.add(other.getMemberId());
            }
        }
        String memberId = base;
        for (int n = 2; existing.contains(memberId); n++) {
            memberId = base + "-" + n;
        }
        owner.setMemberId(memberId);
    }
}
