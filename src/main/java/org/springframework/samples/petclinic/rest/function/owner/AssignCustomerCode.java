package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where REGION is the
 * canonical region derived from the owner's postcode (falling back to its city; see
 * {@link Locality}) and HASH8 is the first 8 upper-case hex characters of the SHA-256 digest of
 * {@code normalizedTelephone + lastName} (e.g. {@code NSW-1A2B3C4D}). The telephone is normalized to
 * E.164 form (see {@link OwnerIdentity#normalizedTelephone(String)}), the same normalization used by
 * the identity key. The code carries no per-city sequence, so it is derived purely from the owner's
 * own fields and is seed-independent.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = Locality.of(owner.getPostcode(), owner.getCity());
        String hash8 = hash8(OwnerIdentity.normalizedTelephone(owner.getTelephone()), owner.getLastName());
        owner.setCustomerCode(region + "-" + hash8);
    }

    /** First 8 upper-case hex characters of SHA-256 over {@code (normalizedTelephone + lastName)}. */
    private static String hash8(String normalizedTelephone, String lastName) {
        String input = normalizedTelephone + (lastName == null ? "" : lastName);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            // Two hex characters per byte, so four bytes yield the eight characters required.
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
