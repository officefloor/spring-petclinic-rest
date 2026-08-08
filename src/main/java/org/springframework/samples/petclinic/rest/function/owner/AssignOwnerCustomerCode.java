package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns a newly created owner a {@code customerCode} formatted {@code '<REGION>-<HASH8>'}:
 * REGION is the region code derived from the owner's postcode (NSW 2000-2099, VIC 3000-3099,
 * QLD 4000-4099, otherwise {@code UNKNOWN}), and HASH8 is the first eight UPPER-case hex
 * characters of the SHA-256 digest over the normalized (E.164) telephone concatenated with the
 * owner's last name. The former per-city sequence number is gone, so the code no longer depends
 * on how many owners already exist. Mutates the {@link Owner} in place so it is persisted and
 * returned with the code, and so every value derived from the customerCode (membership number,
 * check digit, audit line and locality) reflects the new region-and-hash identity.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner) {
        String region = PostcodeRegions.regionForPostcode(owner.getPostcode());
        if (region == null) {
            region = "UNKNOWN";
        }
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String lastName = owner.getLastName() == null ? "" : owner.getLastName();
        owner.setCustomerCode(region + "-" + hash8(telephone + lastName));
    }

    /** First eight UPPER-case hex characters (four bytes) of SHA-256 over the UTF-8 bytes. */
    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
