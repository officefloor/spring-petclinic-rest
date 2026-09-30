package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.CityRegions;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerTelephones;

/**
 * Assigns the owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where REGION is
 * the region derived from the postcode by the shared {@link CityRegions} table (or {@code UNKNOWN}
 * when the postcode is absent or in no known range) and HASH8 is the first 8 upper-case hex
 * characters of SHA-256 over {@code normalizedTelephone + lastName} (e.g. {@code NSW-3A7F1C2D}).
 * The telephone is normalized to E.164 via {@link OwnerTelephones} so the hash is stable regardless
 * of the incoming format. The identity is fully determined by the owner's own fields — there are no
 * sequence numbers, so this step no longer depends on the other owners already stored.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = CityRegions.identityRegion(owner.getPostcode());
        String normalizedTelephone = OwnerTelephones.toE164OrRaw(owner.getTelephone());
        String hash8 = hash8(normalizedTelephone + owner.getLastName());
        owner.setCustomerCode(region + "-" + hash8);
    }

    /** First 8 upper-case hex characters of SHA-256 over the UTF-8 bytes of {@code value}. */
    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
                if (sb.length() >= 8) {
                    break;
                }
            }
            return sb.substring(0, 8);
        }
        catch (NoSuchAlgorithmException ex) {
            // SHA-256 is a required algorithm on every JVM, so this cannot happen in practice.
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
