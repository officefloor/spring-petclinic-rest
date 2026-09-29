package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The single definition of the E.164 form of a telephone number. Shared by
 * {@link NormalizeOwnerTelephone} (which stores the E.164 value) and {@link RequireUniqueTelephone}
 * (which compares against it), so both apply the same rule and cannot drift apart.
 *
 * <p>The rule: strip spaces, dashes and brackets. When a leading {@code '+'} is present, keep it and
 * the country code that follows; otherwise assume country code {@code '+61'} and drop a single leading
 * {@code '0'} from the national digits. The result must carry 8 to 15 digits after the {@code '+'};
 * anything else cannot form valid E.164 and yields {@code null}.
 *
 * <p>Kept out of the function classes themselves because OfficeFloor requires each function class to
 * expose exactly one public method.
 */
final class TelephoneNormalizer {

    /**
     * Returns the E.164 form of the given telephone, or {@code null} when it cannot form a valid one.
     */
    static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        // Strip spaces, dashes and brackets.
        String cleaned = telephone.replaceAll("[\\s()-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            // Explicit country code: keep the digits that follow the '+'.
            digits = cleaned.substring(1);
        }
        else {
            // No country code: assume +61 and drop a single leading '0' from the national digits.
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        // Everything after the '+' must be 8 to 15 digits.
        if (!digits.matches("\\d{8,15}")) {
            return null;
        }
        return "+" + digits;
    }

    private TelephoneNormalizer() {
    }
}
