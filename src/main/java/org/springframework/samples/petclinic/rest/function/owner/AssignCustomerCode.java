package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the {@code customerCode} formatted {@code <REGION>-<HASH8>}, where REGION is the region
 * code derived from the owner's postcode (see {@link Locality}, which prefers the postcode's range,
 * falling back to the city table and finally {@code "UNKNOWN"}) and HASH8 is the first 8 upper-case
 * hex characters of SHA-256 over the normalized telephone concatenated with the last name (e.g.
 * {@code NSW-1A2B3C4D}). The telephone has already been normalized to E.164 form by
 * {@link NormalizeTelephone} earlier in the pipeline, so the stored value is used directly. There is
 * no per-city sequence any more, so this step no longer reads the repository.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = Locality.of(owner.getCity(), owner.getPostcode());
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        String hash8 = shaHex8(telephone + lastName);
        owner.setCustomerCode(region + "-" + hash8);
    }

    /** First 8 upper-case hex characters of SHA-256 over the UTF-8 bytes of {@code s}. */
    private static String shaHex8(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.substring(0, 8).toUpperCase(Locale.ROOT);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
