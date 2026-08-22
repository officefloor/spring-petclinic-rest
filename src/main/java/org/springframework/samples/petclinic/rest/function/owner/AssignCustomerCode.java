package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.Localities;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where
 * REGION is the region code derived from the postcode (the shared derivation in
 * {@link Localities#region(String, String)}, postcode-range first, city fallback) and
 * HASH8 is the first eight upper-case hex characters of the SHA-256 digest over the
 * normalized telephone concatenated with the last name. The telephone is already in its
 * normalized E.164 form by this step (see {@link ValidateOwnerFields}).
 *
 * <p>The base code is a pure function of the owner's identity, but two distinct owners can
 * still hash to the same {@code <REGION>-<HASH8>}. When the computed code collides with an
 * existing owner's {@code customerCode}, {@code -<n>} is appended with the smallest
 * {@code n} of 2 or more that makes it unique. The new owner is not yet saved, so it is
 * not among the existing owners scanned here.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        String region = Localities.region(owner.getCity(), owner.getPostcode());
        String hash8 = sha256Hex8(owner.getTelephone() + owner.getLastName());
        String base = region + "-" + hash8;

        Set<String> taken = new HashSet<>();
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getCustomerCode() != null) {
                taken.add(existing.getCustomerCode());
            }
        }

        String code = base;
        for (int n = 2; taken.contains(code); n++) {
            code = base + "-" + n;
        }
        owner.setCustomerCode(code);
    }

    /** The first eight upper-case hex characters of the SHA-256 digest of {@code s}. */
    private static String sha256Hex8(String s) {
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; sb.length() < 8; i++) {
            sb.append(String.format("%02X", digest[i]));
        }
        return sb.substring(0, 8);
    }
}
