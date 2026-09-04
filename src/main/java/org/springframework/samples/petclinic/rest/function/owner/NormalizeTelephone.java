package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingFieldsException;

/**
 * Strips every non-digit from the telephone and requires exactly 10 digits, storing the
 * normalized value back on the request so it is persisted and returned as {@code telephone}.
 * A non-conforming number is rejected with 400 via {@link MissingFieldsException}.
 */
public class NormalizeTelephone {

    public void service(@Val OwnerFieldsDto request) throws MissingFieldsException {
        String digits = request.getTelephone().replaceAll("\\D", "");
        if (digits.length() != 10) {
            throw new MissingFieldsException(List.of("telephone"));
        }
        request.setTelephone(digits);
    }
}
