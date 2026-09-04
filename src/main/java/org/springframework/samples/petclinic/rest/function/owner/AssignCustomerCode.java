package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Locality;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code customerCode} as {@code <REGION>-<HASH8>}: REGION is the
 * region code for the owner (postcode-derived, city as fallback) and HASH8 is the first
 * eight upper-case hex characters of SHA-256 over (normalized telephone + last name). No
 * sequence number is used, so the identity is stable and city-count independent.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = Locality.of(owner.getCity(), owner.getPostcode());
        owner.setCustomerCode(region + "-" + hash8(owner.getTelephone() + owner.getLastName()));
    }

    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; hex.length() < 8; i++) {
                hex.append(String.format("%02X", digest[i]));
            }
            return hex.substring(0, 8);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
