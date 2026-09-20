package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.CityRegion;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns a new owner's member id before it is saved. The id is {@code <REGION><FY><HASH8><CHK>}
 * (see {@link MemberId}): the region derived from the owner's postcode, the two-digit fiscal year
 * of the registration date, a hash of the owner's normalized telephone and last name, and a Luhn
 * check digit. Runs after the telephone has been normalized and the registration date rolled onto
 * a business day, so {@link Owner#getTelephone()} and {@link Owner#getRegistrationDate()} already
 * hold the canonical forms the id is built over. Should that id collide with an existing owner's,
 * it is de-duplicated with a {@code -<n>} suffix.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = CityRegion.localityOf(owner.getPostcode(), owner.getCity());
        String memberId = MemberId.format(region, owner.getRegistrationDate(),
                owner.getTelephone(), owner.getLastName());
        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAllActive()) {
            if (existing.getMemberId() != null) {
                taken.add(existing.getMemberId());
            }
        }
        owner.setMemberId(MemberId.deduplicate(memberId, taken));
    }
}
