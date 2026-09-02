package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's customerCode as {@code <REGION>-<HASH8>}: REGION is the region code
 * derived from the postcode (see {@link CityLocality}) and HASH8 is the first eight
 * upper-case hex characters of SHA-256 over the normalized telephone followed by the last
 * name. No sequence number is used, so the code is stable for the same identity.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = CityLocality.of(owner.getCity(), owner.getPostcode());
        String hash8 = hash8(owner.getTelephone() + owner.getLastName());
        owner.setCustomerCode(region + "-" + hash8);
    }

    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest).substring(0, 8).toUpperCase();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
