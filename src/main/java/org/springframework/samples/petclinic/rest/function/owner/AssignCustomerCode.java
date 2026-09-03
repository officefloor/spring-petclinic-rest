package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code customerCode} before {@link SaveOwner} runs, formatted
 * {@code <REGION>-<HASH8>}: the region code derived from the postcode, then the first
 * eight upper-case hex characters of SHA-256 over the normalized telephone followed by
 * the last name. No sequence numbers are used, so the code depends only on the owner.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        owner.setCustomerCode(Locality.region(owner) + "-" + hash8(blank(owner.getTelephone()) + blank(owner.getLastName())));
    }

    private static String hash8(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(16);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String blank(String value) {
        return value == null ? "" : value;
    }
}
