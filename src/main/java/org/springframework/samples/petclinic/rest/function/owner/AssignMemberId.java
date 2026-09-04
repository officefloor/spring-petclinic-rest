package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's unified {@code memberId}, formatted {@code <REGION><FY><HASH8><CHK>} — the
 * region code derived from the postcode (NSW/VIC/QLD, else {@code UNKNOWN}), the two-digit fiscal year
 * the registrationDate falls in, the first 8 upper-case hex characters of SHA-256 over
 * {@code normalizedTelephone + lastName}, and a single Luhn check digit over the digits of
 * {@code <REGION><FY><HASH8>} (see {@link MemberId}, e.g. {@code NSW271A2B3C4D5}). The base id is fully
 * determined by the owner's region, fiscal year and hashed identity.
 *
 * <p>When that base id collides with an existing owner's {@code memberId}, it is de-duplicated by
 * appending {@code -<n>} with the smallest {@code n} of 2 or more that makes it unique (e.g.
 * {@code NSW271A2B3C4D5-2}). Runs before {@link SaveOwner}, so {@link OwnerRepository#findAll()}
 * returns only the owners that predate this create.
 */
public class AssignMemberId {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setMemberId(uniqueId(baseId(owner), assignedIds(ownerRepository)));
    }

    /**
     * The owner's base memberId before any collision suffix, formatted {@code <REGION><FY><HASH8><CHK>}.
     * Fully determined by the owner, so equal owners produce the same base.
     */
    private static String baseId(Owner owner) {
        String region = OwnerRegion.fromPostcode(owner.getPostcode());
        if (region == null) {
            region = "UNKNOWN";
        }
        String fiscalYearCode = OwnerMembership.fiscalYearCode(owner.getRegistrationDate());
        String hash8 = OwnerIdentity.customerHash(owner.getTelephone(), owner.getLastName());
        return MemberId.of(region, fiscalYearCode, hash8);
    }

    /**
     * {@code base} de-duplicated against {@code existing} by appending {@code -<n>} with the smallest
     * {@code n} of 2 or more that makes it unique (e.g. {@code NSW271A2B3C4D5-2}); {@code base} itself
     * when already free.
     */
    private static String uniqueId(String base, Set<String> existing) {
        String id = base;
        for (int n = 2; existing.contains(id); n++) {
            id = base + "-" + n;
        }
        return id;
    }

    /** The ids already assigned to existing owners — the set a new memberId must avoid colliding with. */
    private static Set<String> assignedIds(OwnerRepository ownerRepository) {
        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (other.getMemberId() != null) {
                existing.add(other.getMemberId());
            }
        }
        return existing;
    }
}
