package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Runs in the create-owner pipeline before {@link SaveOwner}. Assigns the owner's
 * {@code customerCode}, formatted {@code '<REGION>-<HASH8>'} where REGION is the region code shared
 * with {@link Owner#getLocality()} (derived from the postcode, falling back to the city, else
 * {@code 'UNKNOWN'}) and HASH8 is the first eight upper-case hex characters of a SHA-256 digest over
 * the normalized telephone concatenated with the last name (e.g. 'NSW-1A2B3C4D'). The sequence
 * numbers of the former city-and-last-name format are gone. Mutates the built owner in place so
 * later steps (membership number, check digit, audit) derive from and store the assigned code.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = owner.getLocality();
        String hash8 = shaHex8(owner.getTelephone() + owner.getLastName());
        owner.setCustomerCode(region + "-" + hash8);
    }

    /** First eight UPPER-case hex characters of SHA-256 over the UTF-8 bytes of {@code s}. */
    private static String shaHex8(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
                if (sb.length() >= 8) {
                    break;
                }
            }
            return sb.substring(0, 8);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
