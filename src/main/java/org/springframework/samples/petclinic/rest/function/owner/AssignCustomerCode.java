package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where REGION is
 * the region code derived from the postcode (see {@link OwnerRegion}) and HASH8 is the first 8
 * upper-case hex characters of the SHA-256 digest over the normalized telephone concatenated with
 * the last name (e.g. {@code NSW-1A2B3C4D}). There are no per-city sequence numbers.
 *
 * <p>Runs after {@link BuildOwner} (so the normalized telephone, last name and postcode are on the
 * owner) and before {@link SaveOwner}. The membership number, its Luhn check digit, the create
 * audit line and the derived locality all read this identity.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = OwnerRegion.fromPostcodeOrCity(owner.getPostcode(), owner.getCity());
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        owner.setCustomerCode(region + "-" + hash8(telephone + lastName));
    }

    /** First 8 upper-case hex characters of SHA-256 over the UTF-8 bytes of {@code value}. */
    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(16);
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
