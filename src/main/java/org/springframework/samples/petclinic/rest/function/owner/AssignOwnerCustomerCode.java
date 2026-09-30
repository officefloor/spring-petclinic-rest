package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where REGION is the
 * region code derived from the owner's postcode (see {@link Owner#regionOfPostcode(String)}, or
 * {@code UNKNOWN} when the postcode maps to no known region) and HASH8 is the first 8 upper-case hex
 * characters of the SHA-256 of the normalized telephone concatenated with the lastName (e.g.
 * {@code NSW-3F9A1C7E}). The customer code no longer carries any per-city sequence number. Runs after
 * {@link BuildOwner} builds the entity (so the telephone is already in canonical E.164 form) and
 * before {@link SaveOwner} persists it, mutating the built owner in place so the saved and returned
 * record carries the code. Every value derived from the customer code — the membership number, its
 * check digit and the {@link Owner#getLocality() locality} — follows from this region-and-hash
 * identity.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner) {
        String region = Owner.regionOfPostcode(owner.getPostcode());
        if (region == null) {
            region = "UNKNOWN";
        }
        String hash8 = sha256Hex8(owner.getTelephone() + owner.getLastName());
        owner.setCustomerCode(region + "-" + hash8);
    }

    /**
     * The first 8 upper-case hex characters of the SHA-256 digest of {@code value}, taken over its
     * UTF-8 bytes — i.e. the first 4 digest bytes rendered as hex.
     */
    private static String sha256Hex8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format(Locale.ROOT, "%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
