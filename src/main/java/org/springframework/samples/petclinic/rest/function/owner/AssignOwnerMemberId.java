package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.function.common.FiscalYear;
import org.springframework.samples.petclinic.rest.function.common.Localities;
import org.springframework.samples.petclinic.rest.function.common.MemberId;

/**
 * Assigns the {@code memberId} to a newly built owner, formatted
 * {@code <REGION><FY><HASH8><CHK>} (no separators) where {@code REGION} is the region code
 * derived from the owner's postcode (see {@link Localities#locality(String, String)}),
 * {@code FY} is the two-digit fiscal year of the owner's registration date, {@code HASH8} is
 * the first 8 upper-case hex characters of the SHA-256 digest over the owner's normalized
 * telephone (E.164, see {@link OwnerTelephone}) concatenated with its last name, and
 * {@code CHK} is a single Luhn check digit over the digits of {@code <REGION><FY><HASH8>}
 * (e.g. {@code NSW271A2B3C4D5}).
 *
 * <p>This is a stable identity: the region-and-hash value carries no sequence number, so it
 * does not depend on how many owners already exist. When that value collides with an existing
 * owner's {@code memberId}, {@code -<n>} is appended with the smallest {@code n} of 2 or more
 * that makes it unique, so distinct owners always get distinct memberIds. Every value built
 * from the memberId — the fiscal-year label, the create audit line and the derived locality —
 * follows from this identity. Mutates the owner in place (the same object {@link SaveOwner}
 * persists).
 */
public class AssignOwnerMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Localities.locality(owner.getCity(), owner.getPostcode());
        String telephone = OwnerTelephone.canonical(owner.getTelephone());
        String fiscalYear = FiscalYear.twoDigit(owner.getRegistrationDate());
        String base = MemberId.base(region, fiscalYear, telephone, owner.getLastName());

        Set<String> taken = ownerRepository.findAll().stream()
                .filter(existing -> existing.getId() == null
                        || !existing.getId().equals(owner.getId()))
                .map(Owner::getMemberId)
                .filter(memberId -> memberId != null)
                .collect(Collectors.toCollection(HashSet::new));

        String memberId = base;
        for (int n = 2; taken.contains(memberId); n++) {
            memberId = base + "-" + n;
        }
        owner.setMemberId(memberId);
    }
}
