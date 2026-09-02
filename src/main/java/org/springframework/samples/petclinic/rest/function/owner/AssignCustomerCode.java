package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Assigns the owner's unified {@code memberId} (stored in {@code customerCode}) as
 * {@code <REGION><FY><HASH8><CHK>}. REGION is the version-2 region code: the plain {@link Region}
 * carrying the fixed {@code V2} tag, so no version-1 memberId is ever produced again. FY is the
 * 2-digit fiscal year of the registration
 * date; HASH8 is the first 8 upper-case hex characters of SHA-256 over the normalized (E.164)
 * telephone concatenated with the last name; CHK is a single Luhn check digit over the preceding
 * digits.
 */
public class AssignCustomerCode {

    public void service(@Val Owner owner) {
        // Version-2 region code inside the identifier: the plain region carrying the fixed 'V2'
        // tag, so the memberId never coincides with a version-1 value. The user-facing region
        // (locality/timezone/segment) stays plain and is derived separately via Region.
        String region = "V2" + Region.of(owner);
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
