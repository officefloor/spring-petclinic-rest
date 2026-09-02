package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code customerCode} formatted {@code <REGION>-<HASH8>}: REGION is the region
 * code derived from the postcode (see {@link Locality}), and HASH8 is the first 8 upper-case hex
 * characters of SHA-256 over the normalized telephone followed by the last name (e.g.
 * 'NSW-1A2B3C4D'). Every value derived downstream — the membership number and its check digit, the
 * create audit record, and the locality — reads this identity.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner) {
        owner.setCustomerCode(Locality.of(owner) + "-" + hash8(owner));
    }

    private static String hash8(Owner owner) {
        String key = owner.getTelephone() + owner.getLastName();
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                hash.append(String.format("%02X", digest[i]));
            }
            return hash.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
