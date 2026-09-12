package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Hash8;
import org.springframework.samples.petclinic.model.Locality;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>}: REGION is
 * the region code derived from the owner's postcode (see {@link Locality#regionOf}) and
 * HASH8 is the owner's identity hash (see {@link Hash8}), e.g. {@code NSW-1A2B3C4D}. When
 * the computed code collides with an existing owner's
 * {@code customerCode}, {@code -<n>} is appended with the smallest {@code n} of 2 or more
 * that makes it unique. Runs after {@link BuildOwner} (so the telephone is already
 * normalized) and before {@link SaveOwner}.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Locality.regionOf(owner.getPostcode());
        String base = region + "-" + Hash8.of(owner);
        owner.setCustomerCode(deduplicate(base, existingCodes(ownerRepository)));
    }

    private static Set<String> existingCodes(OwnerRepository ownerRepository) {
        Set<String> codes = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            String code = existing.getCustomerCode();
            if (code != null) {
                codes.add(code);
            }
        }
        return codes;
    }

    /** Returns {@code base}, or {@code base-<n>} with the smallest {@code n >= 2} not in {@code taken}. */
    private static String deduplicate(String base, Set<String> taken) {
        if (!taken.contains(base)) {
            return base;
        }
        for (int n = 2; ; n++) {
            String candidate = base + "-" + n;
            if (!taken.contains(candidate)) {
                return candidate;
            }
        }
    }
}
