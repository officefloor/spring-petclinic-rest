package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's unified {@code memberId} as {@code <REGION><FY><HASH8><CHK>}: the
 * region code derived from the postcode (see {@link Locality}), the two-digit fiscal year,
 * the first eight upper-case hex characters of the SHA-256 of the normalised telephone
 * concatenated with the last name (the same HASH8 as the region-and-hash identity), then a
 * single Luhn check digit ({@link CheckDigit}) over the digits of {@code <REGION><FY><HASH8>}.
 * No sequence numbers are used, so the id is a stable function of the owner's identity.
 * The value is stored in the owner's customer-code column and de-duplicated downstream.
 */
public class AssignOwnerCustomerCode {

    public void service(@Val Owner owner) {
        String base = Locality.of(owner)
                + String.format("%02d", FiscalYear.of(owner.getRegistrationDate()) % 100)
                + hash8(owner.getTelephone() + owner.getLastName());
        owner.setCustomerCode(base + CheckDigit.of(base));
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
