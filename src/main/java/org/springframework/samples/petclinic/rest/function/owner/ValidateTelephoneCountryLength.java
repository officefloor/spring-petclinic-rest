package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingOwnerFieldsException;

/**
 * Checks the E.164 telephone's national-number length against its country code: '+61' requires 9
 * national digits and '+1' requires 10. Runs after {@link NormalizeOwnerTelephone}, so the value is
 * already in E.164 form; a wrong national length for the country rejects with 400.
 */
public class ValidateTelephoneCountryLength {

    public void service(@Val OwnerFieldsDto request) throws MissingOwnerFieldsException {
        String telephone = request.getTelephone();
        check(telephone, "+61", 9);
        check(telephone, "+1", 10);
    }

    private static void check(String telephone, String countryCode, int nationalDigits)
            throws MissingOwnerFieldsException {
        if (telephone.startsWith(countryCode) && telephone.length() - countryCode.length() != nationalDigits) {
            throw new MissingOwnerFieldsException(List.of("telephone"));
        }
    }
}
