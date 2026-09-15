package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Shared telephone normalization for the owner pipelines: convert a raw telephone to
 * E.164 form. Spaces, dashes and brackets are stripped; a number that already carries
 * a leading {@code '+'} keeps its country code, otherwise Australian country code
 * {@code '+61'} is assumed and a single leading {@code '0'} is dropped from the
 * national digits. The result is a {@code '+'} followed by 8 to 15 digits.
 *
 * <p>Used by {@link NormalizeOwnerTelephone} (which rejects a number that cannot form
 * valid E.164) and by {@link EnsureUniqueTelephone} (which normalizes each stored
 * telephone before comparing), so the two steps agree on what "normalized" means.
 */
final class TelephoneNormalizer {

    private TelephoneNormalizer() {
    }

    /**
     * @return the E.164 representation of {@code telephone}, or {@code null} when it
     *         cannot form a valid E.164 number.
     */
    static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = telephone.replaceAll("[\\s()-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        }
        else {
            if (cleaned.startsWith("0")) {
                cleaned = cleaned.substring(1);
            }
            digits = "61" + cleaned;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            return null;
        }
        return "+" + digits;
    }
}
