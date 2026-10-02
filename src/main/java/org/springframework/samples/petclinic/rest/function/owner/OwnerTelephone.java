package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Single source of truth for the owner telephone normalization rule, shared by the
 * owner-create pipeline. The uniqueness check only holds if both the value stored for a
 * new owner and the values compared against existing owners are normalized identically,
 * so that rule lives here rather than being duplicated across steps.
 */
final class OwnerTelephone {

    private OwnerTelephone() {
    }

    /**
     * Normalize a telephone to E.164 form, or {@code null} if the input is {@code null} or
     * cannot form a valid E.164 number.
     *
     * <p>Spaces, dashes and brackets are stripped. A leading {@code '+'} and country code are
     * kept when present; otherwise the country code {@code '+61'} is assumed and a single
     * leading {@code '0'} is dropped from the national digits. The result is valid only when
     * it has 8 to 15 digits after the {@code '+'}; anything else (including stray non-digit
     * characters) yields {@code null}. The function is idempotent: an already-E.164 value
     * normalizes to itself.
     */
    static String normalize(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = telephone.replaceAll("[\\s()-]", "");
        boolean hasPlus = cleaned.startsWith("+");
        String digits = hasPlus ? cleaned.substring(1) : cleaned;
        if (!digits.matches("[0-9]+")) {
            return null;
        }
        String e164Digits;
        if (hasPlus) {
            e164Digits = digits;
        }
        else {
            String national = digits.startsWith("0") ? digits.substring(1) : digits;
            e164Digits = "61" + national;
        }
        if (e164Digits.length() < 8 || e164Digits.length() > 15) {
            return null;
        }
        return "+" + e164Digits;
    }
}
