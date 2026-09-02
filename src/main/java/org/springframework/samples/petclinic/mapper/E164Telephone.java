package org.springframework.samples.petclinic.mapper;

/**
 * Converts a raw telephone into E.164 form. Kept as a small, self-contained unit so the
 * rule can be reused without adding complexity to the mapper or the owner model.
 */
final class E164Telephone {

    private E164Telephone() {
    }

    /**
     * Normalize {@code telephone} to E.164: keep an explicit leading '+' and its country
     * code when present, otherwise assume '+61' and drop a single leading '0' from the
     * national digits. Spaces, dashes and brackets are stripped. The result is returned
     * only when it holds 8 to 15 digits after the '+', so an unconvertible number stays
     * as its cleaned form and is rejected by validation.
     */
    static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = telephone.replaceAll("[\\s()\\-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            return cleaned;
        }
        return "+" + digits;
    }
}
