package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Locality;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>}: REGION is
 * the region code derived from the owner's postcode (see {@link Locality#regionOf}) and
 * HASH8 is the first 8 upper-case hexadecimal characters of the SHA-256 digest over the
 * concatenation of the owner's normalized (E.164) telephone and lastName (e.g.
 * {@code NSW-1A2B3C4D}). There are no sequence numbers — the same telephone and last name
 * in the same region always yield the same code. Runs after {@link BuildOwner} (so the
 * telephone is already normalized) and before {@link SaveOwner}.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = Locality.regionOf(owner.getPostcode());
        String hash8 = hash8(nullToEmpty(owner.getTelephone()) + nullToEmpty(owner.getLastName()));
        owner.setCustomerCode(region + "-" + hash8);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    /** The first 8 upper-case hex characters of SHA-256 over the UTF-8 bytes of {@code value}. */
    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
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
