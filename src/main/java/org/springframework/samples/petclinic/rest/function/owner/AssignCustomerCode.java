package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.CheckDigit;
import org.springframework.samples.petclinic.mapper.FiscalYear;
import org.springframework.samples.petclinic.mapper.Locality;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's {@code memberId} formatted {@code <REGION><FY><HASH8><CHK>}: the region
 * derived from the postcode, the two-digit fiscal year of the registration date, the first 8
 * upper-case hex characters of the SHA-256 of the normalized telephone concatenated with the
 * last name, and a single Luhn check digit over the digits of the preceding segments. It is a
 * pure function of the owner's own fields, so it needs no repository lookup.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String region = Locality.of(owner.getPostcode(), owner.getCity());
        String fiscalYear = String.format("%02d", FiscalYear.of(owner.getRegistrationDate()) % 100);
        String hash8 = hash8(owner.getTelephone() + owner.getLastName());
        String base = region + fiscalYear + hash8;
        owner.setCustomerCode(base + CheckDigit.of(base));
    }

    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
