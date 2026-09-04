package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.Locality;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code customerCode} formatted {@code <REGION>-<HASH8>}: the region
 * derived from the postcode, plus the first 8 upper-case hex characters of the SHA-256 of
 * the normalized telephone concatenated with the last name. It carries no sequence number,
 * so it is a pure function of the owner's own fields and needs no repository lookup.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = Locality.of(owner.getPostcode(), owner.getCity());
        String hash8 = hash8(owner.getTelephone() + owner.getLastName());
        owner.setCustomerCode(region + "-" + hash8);
    }

    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
