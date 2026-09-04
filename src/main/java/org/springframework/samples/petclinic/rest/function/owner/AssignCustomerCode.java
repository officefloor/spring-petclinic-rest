package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Locality;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's unified {@code memberId} as {@code <REGION><FY><HASH8><CHK>}: REGION is
 * the region code for the owner (postcode-derived, city as fallback), FY the 2-digit fiscal
 * year of registration, HASH8 the first eight upper-case hex characters of SHA-256 over
 * (normalized telephone + last name) and CHK a single Luhn check digit over the preceding
 * digits. No sequence number is used, so the identity is stable and city-count independent.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = Locality.of(owner.getCity(), owner.getPostcode());
        String fy = String.format("%02d", FiscalYear.of(owner.getRegistrationDate()) % 100);
        String base = region + fy + hash8(owner.getTelephone() + owner.getLastName());
        owner.setCustomerCode(base + CheckDigit.of(base));
    }

    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (int i = 0; hex.length() < 8; i++) {
                hex.append(String.format("%02X", digest[i]));
            }
            return hex.substring(0, 8);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
