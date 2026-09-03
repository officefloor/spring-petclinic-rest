package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code customerCode} as {@code <REGION>-<HASH8>}: the region code
 * derived from the postcode (see {@link Locality}), a hyphen, then the first eight
 * upper-case hex characters of the SHA-256 of the normalised telephone concatenated with
 * the last name. No sequence numbers are used, so the code is a stable function of the
 * owner's identity. Everything else built from the code — the membership number and its
 * check digit, the create audit line and the locality — follows from this same identity.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner) {
        owner.setCustomerCode(Locality.of(owner) + "-" + hash8(owner.getTelephone() + owner.getLastName()));
    }

    private static String hash8(String seed) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(seed.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
