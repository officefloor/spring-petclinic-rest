package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Shared telephone canonicalization for the create-owner pipeline: reduces a telephone to
 * its E.164 form so validation, uniqueness checks and storage all compare the same value.
 */
final class OwnerTelephones {

    private OwnerTelephones() {
    }

    /**
     * Canonicalizes {@code telephone} to E.164 (a leading {@code '+'} followed by 8 to 15
     * digits), or returns {@code null} when it cannot form a valid E.164 number.
     *
     * <p>Spaces, dashes and brackets are stripped. A leading {@code '+'} keeps the given
     * country code; otherwise country code {@code +61} is assumed and a single leading
     * {@code '0'} is dropped from the national digits. So {@code "0412 345 678"} becomes
     * {@code "+61412345678"} and {@code "+64 21 123 456"} becomes {@code "+6421123456"}.
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
        if (!digits.matches("\\d{8,15}")) {
            return null;
        }
        return "+" + digits;
    }
}
