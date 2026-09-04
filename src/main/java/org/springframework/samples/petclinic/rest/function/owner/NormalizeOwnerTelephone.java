package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;

/**
 * Rewrites the create body's telephone into E.164 form. Keeps a leading '+' and its
 * country code when present; otherwise assumes '+61' and drops a single leading '0'
 * from the national digits. Spaces, dashes and brackets are stripped and 8 to 15
 * digits are required after the '+', so anything else is a 400. The national-number
 * length is also checked against the country code ('+61' => 9 national digits, '+1' =>
 * 10); a wrong length for the country is a 400. Runs before
 * {@link BuildOwner} validates, so the stored value is the E.164 string.
 */
public class NormalizeOwnerTelephone {

    public void service(@Val OwnerFieldsDto request) throws MissingOwnerFieldsException {
        String raw = request.getTelephone().trim();
        String digits = raw.replaceAll("\\D", "");
        if (!raw.startsWith("+")) {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            digits = "61" + digits;
        }
        if (digits.length() < 8 || digits.length() > 15) {
            throw new MissingOwnerFieldsException(List.of("telephone"));
        }
        // Per-country national-number length: '+61' needs 9 national digits, '+1' needs 10.
        if (digits.startsWith("61") && digits.length() - 2 != 9
                || digits.startsWith("1") && digits.length() - 1 != 10) {
            throw new MissingOwnerFieldsException(List.of("telephone"));
        }
        request.setTelephone("+" + digits);
    }
}
