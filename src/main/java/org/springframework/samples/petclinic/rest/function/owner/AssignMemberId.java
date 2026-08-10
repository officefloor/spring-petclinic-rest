package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.util.Locality;
import org.springframework.samples.petclinic.util.MemberId;

/**
 * Assigns the owner's {@code memberId}, formatted {@code '<REGION><FY><HASH8><CHK>'} where REGION is
 * the region derived from the owner's postcode (falling back to the city table, see {@link Locality}),
 * FY is the two-digit fiscal year of the registration date, HASH8 is the first eight upper-case hex
 * characters of SHA-256 over the {@code normalizedTelephone + lastName} and CHK is a single Luhn check
 * digit over the digits of {@code <REGION><FY><HASH8>} (e.g. 'NSW271A2B3C4D5'). Runs after the owner is
 * built (so the telephone is already normalized to E.164 and the registration date populated) and before
 * it is saved, mutating the id in place via {@code @Val} for the save/respond steps to persist and return.
 *
 * <p>When the computed id collides with an existing owner's {@code memberId}, {@code '-<n>'} is appended
 * with the smallest {@code n} of 2 or more that makes it unique, and the de-duplicated id is assigned
 * (e.g. a second 'NSW271A2B3C4D5' becomes 'NSW271A2B3C4D5-2', a third 'NSW271A2B3C4D5-3').
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Locality.of(owner.getCity(), owner.getPostcode());
        String base = MemberId.of(region, owner.getRegistrationDate(), owner.getTelephone(),
                owner.getLastName());
        Set<String> existing = ownerRepository.findAll().stream()
                .map(Owner::getMemberId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        String memberId = base;
        for (int n = 2; existing.contains(memberId); n++) {
            memberId = base + "-" + n;
        }
        owner.setMemberId(memberId);
    }
}
