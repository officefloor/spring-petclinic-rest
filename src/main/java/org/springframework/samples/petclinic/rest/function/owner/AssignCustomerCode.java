package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.Localities;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where
 * REGION is the region code derived from the postcode (the shared derivation in
 * {@link Localities#region(String, String)}, postcode-range first, city fallback) and
 * HASH8 is the first eight upper-case hex characters of the SHA-256 digest over the
 * normalized telephone concatenated with the last name. The telephone is already in its
 * normalized E.164 form by this step (see {@link ValidateOwnerFields}). There are no
 * sequence numbers: the code is a pure function of the owner's identity, so no repository
 * scan is needed.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = Localities.region(owner.getCity(), owner.getPostcode());
        String hash8 = sha256Hex8(owner.getTelephone() + owner.getLastName());
        owner.setCustomerCode(region + "-" + hash8);
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
