package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code memberId} as {@code <REGION><FY><HASH8><CHK>}: the region code derived
 * from the postcode (see {@link OwnerLocality#forPostcode}), the two-digit fiscal year of the
 * registration date (see {@link FiscalYear#twoDigit}), the first eight upper-case hex characters of
 * SHA-256 over the normalized telephone (set by {@link BuildOwner}) concatenated with the last name,
 * and a single Luhn check digit over the digits of {@code <REGION><FY><HASH8>} (see
 * {@link CustomerCodeCheckDigit}). The id is a pure function of the owner's identity, so it carries no
 * sequence number.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = OwnerLocality.forPostcode(owner.getPostcode(), owner.getCity());
        String fy = FiscalYear.twoDigit(owner.getRegistrationDate());
        String hash8 = hash8(owner.getTelephone() + owner.getLastName());
        String base = region + fy + hash8;
        owner.setCustomerCode(base + CustomerCodeCheckDigit.of(base));
    }

    /** First eight upper-case hex characters of SHA-256 over the UTF-8 bytes of {@code value}. */
    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
