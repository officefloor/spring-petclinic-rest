package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingFieldsException;

/**
 * Rewrites the telephone into E.164 form: keep a leading {@code '+'} and country code when
 * present, otherwise assume {@code '+61'} and drop a single leading {@code '0'} from the national
 * digits, stripping spaces, dashes and brackets. The result must carry 8 to 15 digits after the
 * {@code '+'}; the normalized value is stored back on the request so it is persisted and returned
 * as {@code telephone}. A non-conforming number is rejected with 400 via {@link MissingFieldsException}.
 */
public class NormalizeTelephone {

    public void service(@Val OwnerFieldsDto request) throws MissingFieldsException {
        String raw = request.getTelephone().trim();
        String digits = raw.replaceAll("\\D", "");
        if (!raw.startsWith("+")) {
            digits = "61" + (digits.startsWith("0") ? digits.substring(1) : digits);
        }
        if (digits.length() < 8 || digits.length() > 15) {
            throw new MissingFieldsException(List.of("telephone"));
        }
        request.setTelephone("+" + digits);
    }
}
