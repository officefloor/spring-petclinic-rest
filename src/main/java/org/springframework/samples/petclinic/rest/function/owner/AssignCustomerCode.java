package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's unified {@code memberId} (stored in {@code customerCode}) as
 * {@code <REGION><FY><HASH8><CHK>}. REGION is the region code derived from the postcode (2xxx→NSW,
 * 3xxx→VIC, 4xxx→QLD, then by city, else UNKNOWN); FY is the 2-digit fiscal year of the registration
 * date; HASH8 is the first 8 upper-case hex characters of SHA-256 over the normalized (E.164)
 * telephone concatenated with the last name; CHK is a single Luhn check digit over the preceding
 * digits.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        String pc = owner.getPostcode();
        String city = owner.getCity();
        String region;
        if (pc != null && pc.matches("20\\d{2}")) {
            region = "NSW";
        }
        else if (pc != null && pc.matches("30\\d{2}")) {
            region = "VIC";
        }
        else if (pc != null && pc.matches("40\\d{2}")) {
            region = "QLD";
        }
        else if ("Sydney".equals(city)) {
            region = "NSW";
        }
        else if ("Melbourne".equals(city)) {
            region = "VIC";
        }
        else if ("Brisbane".equals(city)) {
            region = "QLD";
        }
        else {
            region = "UNKNOWN";
        }
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256")
                .digest((owner.getTelephone() + owner.getLastName()).getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
        StringBuilder hash8 = new StringBuilder(8);
        for (int i = 0; i < 4; i++) {
            hash8.append(String.format("%02X", digest[i]));
        }
        String core = region + String.format("%02d", FiscalYear.of(owner.getRegistrationDate()) % 100) + hash8;
        owner.setCustomerCode(core + CheckDigit.of(core));
    }
}
