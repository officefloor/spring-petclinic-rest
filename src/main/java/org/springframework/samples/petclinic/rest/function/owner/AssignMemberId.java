package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.MemberId;

/**
 * Assigns the unified {@code memberId} to a newly built owner, formatted
 * {@code <REGION><FY><HASH8><CHK>} where REGION is the region derived from the owner's postcode
 * (with a city fallback), FY is the two-digit fiscal year of the registrationDate, HASH8 is the
 * first 8 upper-case hex characters of SHA-256 over the owner's normalized telephone and last name,
 * and CHK is the Luhn check digit over the digits of {@code <REGION><FY><HASH8>}
 * (e.g. {@code NSW261A2B3C4D7}). The id is a pure function of the owner's own fields, so it does not
 * depend on how many owners already exist.
 *
 * <p>Should the computed id collide with an existing owner's {@code memberId}, it is de-duplicated
 * by appending {@code -<n>} with the smallest {@code n} of 2 or more that makes it unique. This runs
 * before the owner is saved, so it compares against the owners that already exist and excludes the
 * owner being created.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String base = MemberId.of(owner);

        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            String id = other.getMemberId();
            if (id != null) {
                existing.add(id);
            }
        }

        String memberId = base;
        for (int n = 2; existing.contains(memberId); n++) {
            memberId = base + "-" + n;
        }
        owner.setMemberId(memberId);
    }
}
