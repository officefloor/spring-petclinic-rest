package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's unified {@code memberId} before {@link SaveOwner} runs, formatted
 * {@code <REGION><FY><HASH8><CHK>}: the region code derived from the postcode, the
 * 2-digit fiscal year of the registration date, the first eight upper-case hex
 * characters of SHA-256 over the telephone followed by the last name, and a single Luhn
 * check digit over the preceding digits. It depends only on the owner.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String base = IdentityRegion.code(owner)
                + String.format("%02d", FiscalYear.endYear(owner.getRegistrationDate()) % 100)
                + hash8(blank(owner.getTelephone()) + blank(owner.getLastName()));
        owner.setCustomerCode(base + CheckDigit.checkDigit(base));
    }

    private static String hash8(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(16);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String blank(String value) {
        return value == null ? "" : value;
    }
}
