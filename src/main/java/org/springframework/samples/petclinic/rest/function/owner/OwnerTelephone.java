package org.springframework.samples.petclinic.rest.function.owner;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Normalizes a telephone number to E.164 form.
 *
 * <p>Spaces, dashes and brackets are stripped. A leading {@code '+'} and its country code are
 * kept as-is; otherwise country code {@code +61} is assumed and a single leading {@code '0'} is
 * dropped from the national digits. The result must have 8 to 15 digits after the {@code '+'} or
 * the number is rejected as an {@link InvalidTelephoneException}. So {@code "0412 345 678"}
 * becomes {@code "+61412345678"} and {@code "+64 21 123 456"} becomes {@code "+6421123456"}.
 *
 * <p>For recognized country codes the national-number length must match exactly, or the number is
 * rejected: {@code +61} (Australia) requires 9 national digits and {@code +1} (NANP) requires 10.
 * So {@code "+61 123"} is rejected, while {@code "+61 412 345 678"} and {@code "+1 608 555 1023"}
 * are accepted. Numbers whose country code is not recognized keep only the 8-to-15-digit rule.
 */
final class OwnerTelephone {

    /**
     * Recognized country codes mapped to the exact national-number length they require. Ordered
     * longest code first so the longest matching prefix wins (e.g. {@code "61"} before {@code "1"}).
     */
    private static final Map<String, Integer> NATIONAL_LENGTHS = new LinkedHashMap<>();

    static {
        NATIONAL_LENGTHS.put("61", 9);
        NATIONAL_LENGTHS.put("1", 10);
    }

    private OwnerTelephone() {
    }

    static String toE164(String input) throws InvalidTelephoneException {
        if (input == null) {
            throw new InvalidTelephoneException(null);
        }
        String cleaned = input.replaceAll("[\\s()\\[\\]-]", "");
        boolean hasPlus = cleaned.startsWith("+");
        String rest = hasPlus ? cleaned.substring(1) : cleaned;
        if (rest.isEmpty() || !rest.chars().allMatch(c -> c >= '0' && c <= '9')) {
            throw new InvalidTelephoneException(input);
        }
        String digits;
        if (hasPlus) {
            digits = rest;
        }
        else {
            if (rest.startsWith("0")) {
                rest = rest.substring(1);
            }
            digits = "61" + rest;
        }
        if (digits.length() < 8 || digits.length() > 15) {
            throw new InvalidTelephoneException(input);
        }
        for (Map.Entry<String, Integer> country : NATIONAL_LENGTHS.entrySet()) {
            String code = country.getKey();
            if (digits.startsWith(code)) {
                if (digits.length() - code.length() != country.getValue()) {
                    throw new InvalidTelephoneException(input);
                }
                break;
            }
        }
        return "+" + digits;
    }
}
