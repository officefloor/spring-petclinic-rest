package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where REGION is the
 * region code derived from the postcode (NSW/VIC/QLD, else {@code UNKNOWN}) and HASH8 is the first 8
 * upper-case hex characters of SHA-256 over {@code normalizedTelephone + lastName} (e.g.
 * {@code NSW-1A2B3C4D}). The base code is fully determined by the owner's region and hashed identity.
 *
 * <p>When that base code collides with an existing owner's {@code customerCode}, it is de-duplicated
 * by appending {@code -<n>} with the smallest {@code n} of 2 or more that makes it unique (e.g.
 * {@code NSW-1A2B3C4D-2}). Runs before {@link SaveOwner}, so {@link OwnerRepository#findAll()} returns
 * only the owners that predate this create.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        owner.setCustomerCode(uniqueCode(baseCode(owner), assignedCodes(ownerRepository)));
    }

    /**
     * The owner's base code before any collision suffix, formatted {@code <REGION>-<HASH8>}: REGION is
     * the region derived from the postcode ({@code UNKNOWN} when none), HASH8 the owner's hashed
     * identity. Fully determined by the owner, so equal owners produce the same base.
     */
    private static String baseCode(Owner owner) {
        String region = OwnerRegion.fromPostcode(owner.getPostcode());
        if (region == null) {
            region = "UNKNOWN";
        }
        String hash8 = OwnerIdentity.customerHash(owner.getTelephone(), owner.getLastName());
        return region + "-" + hash8;
    }

    /**
     * {@code base} de-duplicated against {@code existing} by appending {@code -<n>} with the smallest
     * {@code n} of 2 or more that makes it unique (e.g. {@code NSW-1A2B3C4D-2}); {@code base} itself
     * when already free.
     */
    private static String uniqueCode(String base, Set<String> existing) {
        String code = base;
        for (int n = 2; existing.contains(code); n++) {
            code = base + "-" + n;
        }
        return code;
    }

    /** The codes already assigned to existing owners — the set a new code must avoid colliding with. */
    private static Set<String> assignedCodes(OwnerRepository ownerRepository) {
        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (other.getCustomerCode() != null) {
                existing.add(other.getCustomerCode());
            }
        }
        return existing;
    }
}
