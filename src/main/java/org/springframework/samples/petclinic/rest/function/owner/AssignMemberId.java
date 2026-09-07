package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code memberId}, formatted {@code <REGION><FY><HASH8><CHK>} where REGION is
 * the canonical region derived from the owner's postcode (falling back to its city; see
 * {@link Locality}), FY is the two-digit fiscal year of the owner's business-day-adjusted
 * {@code registrationDate} (see {@link FiscalYear}), HASH8 is the first 8 upper-case hex characters
 * of the SHA-256 digest of {@code normalizedTelephone + lastName} and CHK is a single Luhn check
 * digit over the digits of {@code <REGION><FY><HASH8>} (e.g. {@code NSW271A2B3C4D5}). The telephone
 * is normalized to E.164 form (see {@link OwnerIdentity#normalizedTelephone(String)}), the same
 * normalization used by the identity key. The id carries no per-city sequence, so it is derived
 * purely from the owner's own fields and is seed-independent.
 *
 * <p>When the computed id collides with an existing owner's {@code memberId}, {@code -<n>} is
 * appended with the smallest {@code n} of 2 or more that makes it unique, so distinct owners always
 * receive distinct member ids.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Locality.of(owner.getPostcode(), owner.getCity());
        String hash8 = MemberId.hash8(OwnerIdentity.normalizedTelephone(owner.getTelephone()),
                owner.getLastName());
        String base = MemberId.of(region, FiscalYear.of(owner.getRegistrationDate()), hash8);
        owner.setMemberId(deduplicate(base, ownerRepository));
    }

    /**
     * The base id if no existing owner already uses it, otherwise {@code base-<n>} with the smallest
     * {@code n} of 2 or more that is not already taken.
     */
    private static String deduplicate(String base, OwnerRepository ownerRepository) {
        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            String memberId = other.getMemberId();
            if (memberId != null) {
                existing.add(memberId);
            }
        }
        if (!existing.contains(base)) {
            return base;
        }
        for (int n = 2; ; n++) {
            String candidate = base + "-" + n;
            if (!existing.contains(candidate)) {
                return candidate;
            }
        }
    }
}
