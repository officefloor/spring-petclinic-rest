package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Single source of truth for the "E.164 telephone" concept shared by the create pipeline:
 * {@link ValidateOwner} converts the submitted value to E.164 before storing it, and
 * {@link CheckOwnerTelephoneUnique} converts stored owner values the same way when comparing for
 * duplicates. Keeping the rule here ensures the value that gets stored and the value compared for
 * uniqueness can never silently diverge.
 */
public final class OwnerTelephone {

    private OwnerTelephone() {
    }

    /**
     * Convert a raw telephone to E.164 form, or return {@code null} if it cannot form a valid E.164
     * number. Spaces, dashes and brackets are stripped. A leading {@code '+'} keeps its country code;
     * otherwise country code {@code '+61'} is assumed and a single leading {@code '0'} is dropped from
     * the national digits. The result must have 8 to 15 digits after the {@code '+'}.
     */
    public static String toE164(String value) {
        if (value == null) {
            return null;
        }
        // Strip spaces, dashes and brackets (round and square).
        String cleaned = value.replaceAll("[\\s()\\[\\]-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        return digits.matches("[0-9]{8,15}") ? "+" + digits : null;
    }
}
