package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.Locality;
import org.springframework.samples.petclinic.model.Owner;

/**
 * On create, assigns the owner's {@code customerCode} as '&lt;REGION&gt;-&lt;HASH8&gt;', where REGION is
 * the region code derived from the postcode (NSW/VIC/QLD, else UNKNOWN) and HASH8 is the first eight
 * upper-case hex characters of SHA-256 over the normalized telephone concatenated with the last name
 * (e.g. 'NSW-1A2B3C4D'). Runs after the telephone has been normalized so the hash is stable, and there
 * are no sequence numbers.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner) {
        String region = Locality.of(null, owner.getPostcode());
        String hash8 = sha256Hex(orEmpty(owner.getTelephone()) + orEmpty(owner.getLastName())).substring(0, 8);
        owner.setCustomerCode(region + "-" + hash8.toUpperCase());
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
