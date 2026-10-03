package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where
 * REGION is the region code derived from the postcode (NSW/VIC/QLD, or {@code UNKNOWN}
 * when the postcode is absent or in no known range) and HASH8 is the first 8 upper-case
 * hex characters of the SHA-256 digest over the normalized telephone concatenated with
 * the last name (e.g. {@code NSW-A1B2C3D4}). Runs after the telephone has been normalized
 * to E.164, so the hash is stable for a given owner.
 *
 * <p>When the computed code collides with an existing owner's {@code customerCode}, it is
 * de-duplicated by appending {@code -<n>} with the smallest {@code n} of 2 or more that
 * makes it unique (e.g. {@code NSW-A1B2C3D4-2}), so distinct owners always receive
 * distinct customer codes.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Owner.identityRegion(owner.getPostcode());
        String hash8 = sha256UpperHex8(owner.getTelephone() + owner.getLastName());
        String base = region + "-" + hash8;

        Set<String> existing = new HashSet<>();
        for (Owner other : ownerRepository.findAll()) {
            if (other.getCustomerCode() != null) {
                existing.add(other.getCustomerCode());
            }
        }

        String code = base;
        for (int n = 2; existing.contains(code); n++) {
            code = base + "-" + n;
        }
        owner.setCustomerCode(code);
    }

    /**
     * The first 8 upper-case hex characters of the SHA-256 digest of the UTF-8 bytes of
     * {@code value}.
     */
    private static String sha256UpperHex8(String value) {
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required but unavailable", ex);
        }
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 4; i++) {
            sb.append(String.format("%02X", digest[i]));
        }
        return sb.toString();
    }
}
