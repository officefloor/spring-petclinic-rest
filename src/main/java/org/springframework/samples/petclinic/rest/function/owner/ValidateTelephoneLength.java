package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;

/**
 * Checks the national-number length of the already-normalised E.164 telephone against its country
 * code: '+61' requires 9 national digits, '+1' requires 10. Rejects with 400 when the length is
 * wrong for the country; leaves other country codes untouched.
 */
public class ValidateTelephoneLength {

    public void service(@Val OwnerFieldsDto request) throws MissingOwnerFieldsException {
        String tel = request.getTelephone();
        if (tel.startsWith("+61") && tel.length() != 3 + 9
                || tel.startsWith("+1") && tel.length() != 2 + 10) {
            throw new MissingOwnerFieldsException(List.of("telephone"));
        }
    }
}
