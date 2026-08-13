package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.Localities;

/**
 * Assigns a new owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where REGION is the
 * region derived from the postcode alone ({@link Localities#ofPostcode}: NSW 2000-2099, VIC
 * 3000-3099, QLD 4000-4099, else {@code UNKNOWN}) and HASH8 is the first 8 UPPER-case hex characters
 * of the SHA-256 digest over {@code normalizedTelephone + lastName} (e.g. {@code NSW-3F9A0C71}).
 * There are no per-city sequence numbers: the identity is stable for a given telephone, surname and
 * region. Runs after {@link BuildOwner} maps the request (so the telephone is already the normalized
 * E.164 value) and before {@link SaveOwner} persists it; every downstream region-and-hash value (the
 * membership number and its check digit, the locality and the create audit record) flows from this
 * code.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner) {
        String region = Localities.ofPostcode(owner.getPostcode());
        String hash8 = hash8(owner.getTelephone() + owner.getLastName());
        owner.setCustomerCode(region + "-" + hash8);
    }

    /** First 8 UPPER-case hex characters of SHA-256 over the UTF-8 bytes of {@code input}. */
    private static String hash8(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(8);
            for (byte b : digest) {
                hex.append(String.format("%02X", b));
                if (hex.length() >= 8) {
                    break;
                }
            }
            return hex.substring(0, 8);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
