package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.Locality;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code '<REGION>-<HASH8>'}: REGION is the
 * canonical region derived from the owner's postcode (via {@link Locality}, falling back to the
 * city table, and {@code "UNKNOWN"} when neither resolves), and HASH8 is the first eight
 * upper-case hex characters of SHA-256 over the normalized telephone concatenated with the last
 * name (e.g. {@code 'NSW-1A2B3C4D'}). The identity is content-derived and carries no sequence
 * number, so it is stable and independent of creation order.
 *
 * <p>Runs after {@link NormalizeOwnerTelephone} (so the telephone is already E.164) and
 * {@link BuildOwner} (so the entity, hence its city, postcode and last name, exists), and before
 * {@link SaveOwner}; it mutates the built {@link Owner} in place. Every value built from the
 * customerCode — the membership number and its Luhn check digit, the create audit record, and the
 * derived locality — therefore reflects this region-and-hash identity.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = Locality.of(owner.getCity(), owner.getPostcode());
        String hash8 = hash8(owner.getTelephone() + owner.getLastName());
        owner.setCustomerCode(region + "-" + hash8);
    }

    /** First eight upper-case hex characters of SHA-256 over the UTF-8 bytes of {@code value}. */
    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 8).toUpperCase();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
