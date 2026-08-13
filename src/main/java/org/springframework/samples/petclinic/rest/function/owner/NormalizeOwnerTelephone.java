package org.springframework.samples.petclinic.rest.function.owner;

import java.util.LinkedHashMap;
import java.util.Map;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Runs after {@link RequireOwnerFields} on {@code POST /api/owners}: normalizes the telephone into
 * E.164 form, replacing the field in place (so {@link BuildOwner} maps the normalized value).
 * <p>
 * Spaces, dashes and brackets are stripped. When the number carries a leading {@code '+'} the
 * country code is kept as given; otherwise country code {@code '+61'} is assumed and a single
 * leading {@code '0'} is dropped from the national digits. The result must have 8 to 15 digits
 * after the {@code '+'}.
 * <p>
 * The national-number length is then validated against the country code: {@code '+61'} requires
 * exactly 9 national digits and {@code '+1'} requires exactly 10. A number that cannot form a valid
 * E.164 string, or whose national number is the wrong length for its country code, is rejected with
 * 400 via {@link InvalidTelephoneException}.
 */
public class NormalizeOwnerTelephone {

    /**
     * Country code (digits after the {@code '+'}) to the exact national-number length it requires.
     * Longest prefixes first so {@code '61'} is matched before {@code '1'}.
     */
    private static final Map<String, Integer> NATIONAL_LENGTHS = new LinkedHashMap<>();

    static {
        NATIONAL_LENGTHS.put("61", 9);
        NATIONAL_LENGTHS.put("1", 10);
    }

    public void service(@Val OwnerFieldsDto request) throws InvalidTelephoneException {
        String raw = request.getTelephone();
        String stripped = raw == null ? "" : raw.replaceAll("[\\s\\-()]", "");

        String digits;
        if (stripped.startsWith("+")) {
            digits = stripped.substring(1);
        }
        else {
            String national = stripped.startsWith("0") ? stripped.substring(1) : stripped;
            digits = "61" + national;
        }

        if (!digits.matches("\\d{8,15}")) {
            throw new InvalidTelephoneException(raw);
        }

        for (Map.Entry<String, Integer> country : NATIONAL_LENGTHS.entrySet()) {
            String code = country.getKey();
            if (digits.startsWith(code)) {
                if (digits.length() - code.length() != country.getValue()) {
                    throw new InvalidTelephoneException(raw);
                }
                break;
            }
        }

        request.setTelephone("+" + digits);
    }
}
