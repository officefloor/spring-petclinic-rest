package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.FiscalYear;
import org.springframework.samples.petclinic.util.MemberId;
import org.springframework.samples.petclinic.util.OwnerRegion;

/**
 * Assigns a newly built owner its unified {@code memberId}, formatted
 * {@code <REGION><FY><HASH8><CHK>} where REGION is the owner's region (see
 * {@link OwnerRegion}), FY the 2-digit fiscal year of its registration date, HASH8 the
 * first 8 upper-case hex characters of SHA-256 over the normalized telephone concatenated
 * with the last name, and CHK a single Luhn check digit over the digits of
 * {@code <REGION><FY><HASH8>} (e.g. {@code NSW261A2B3C4D5}; see {@link MemberId}). Runs
 * after {@link NormalizeOwnerTelephone} (so the telephone is canonical) and
 * {@link ApplyRegistrationDate} (so the fiscal year uses the adjusted business day) and
 * before {@link SaveOwner}, mutating the built owner in place so the id is stored and
 * returned with it. When the computed id collides with an existing owner's memberId it is
 * de-duplicated by appending {@code -<n>} (smallest n of 2 or more that is unique); the new
 * owner is not yet persisted here, so it never collides with itself.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = OwnerRegion.of(owner);
        String fiscalYear = FiscalYear.shortLabel(owner.getRegistrationDate());
        String memberId = MemberId.of(region, fiscalYear, owner.getTelephone(), owner.getLastName());
        owner.setMemberId(MemberId.deduplicate(memberId, takenIds(ownerRepository)::contains));
    }

    /** The member ids already in use by existing owners. */
    private static Set<String> takenIds(OwnerRepository ownerRepository) {
        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getMemberId() != null) {
                taken.add(existing.getMemberId());
            }
        }
        return taken;
    }
}
