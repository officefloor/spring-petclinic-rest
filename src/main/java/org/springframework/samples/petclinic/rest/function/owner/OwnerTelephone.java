package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Optional;

/**
 * Telephone normalization shared by the owner pipelines: converts a supplied telephone to
 * canonical E.164 form (a leading {@code +}, country code, then national digits), so an
 * owner's telephone is stored, returned and compared in one representation.
 */
public final class OwnerTelephone {

    /** Country code assumed when the input carries no explicit {@code +} prefix. */
    private static final String DEFAULT_COUNTRY_CODE = "61";

    /** E.164 allows between 8 and 15 digits after the {@code +}. */
    private static final int MIN_DIGITS = 8;
    private static final int MAX_DIGITS = 15;

    private OwnerTelephone() {
    }

    /**
     * The E.164 form of {@code telephone}, or {@link Optional#empty()} when it cannot form a
     * valid E.164 number. Spaces, dashes and brackets are stripped. A leading {@code +} keeps
     * the given country code; otherwise country code {@code +61} is assumed and a single
     * leading {@code 0} is dropped from the national digits. The result must carry 8 to 15
     * digits after the {@code +}.
     */
    public static Optional<String> toE164(String telephone) {
        if (telephone == null) {
            return Optional.empty();
        }
        String cleaned = telephone.replaceAll("[\\s()\\-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = DEFAULT_COUNTRY_CODE + national;
        }
        if (!digits.matches("[0-9]{" + MIN_DIGITS + "," + MAX_DIGITS + "}")) {
            return Optional.empty();
        }
        return Optional.of("+" + digits);
    }
}
