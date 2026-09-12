package org.springframework.samples.petclinic.rest.function.owner;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Locality;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Sha256;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>}: REGION is
 * the region code derived from the owner's postcode (see {@link Locality#regionOf}) and
 * HASH8 is the first 8 upper-case hexadecimal characters of the SHA-256 digest over the
 * concatenation of the owner's normalized (E.164) telephone and lastName (e.g.
 * {@code NSW-1A2B3C4D}). When the computed code collides with an existing owner's
 * {@code customerCode}, {@code -<n>} is appended with the smallest {@code n} of 2 or more
 * that makes it unique. Runs after {@link BuildOwner} (so the telephone is already
 * normalized) and before {@link SaveOwner}.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Locality.regionOf(owner.getPostcode());
        String hash8 = hash8(nullToEmpty(owner.getTelephone()) + nullToEmpty(owner.getLastName()));
        String base = region + "-" + hash8;
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

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    /** The first 8 upper-case hex characters of SHA-256 over the UTF-8 bytes of {@code value}. */
    private static String hash8(String value) {
        return Sha256.hex(value).substring(0, 8).toUpperCase();
    }
}
