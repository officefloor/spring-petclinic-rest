package org.springframework.samples.petclinic.mapper;

/**
 * Derives an owner's {@code telephoneDisplay}: the stored E.164 {@code telephone} formatted for
 * humans as the country code, a space, then the national digits grouped in threes. So
 * {@code '+61412345678'} becomes {@code '+61 412 345 678'}. The stored {@code telephone} is left as
 * raw E.164.
 *
 * <p>The country code is recognised from the E.164 prefix — {@code '+61'} and {@code '+1'} are the
 * codes this application accepts (see the owner telephone normalization); any other value falls back
 * to a single-digit country code. A value that is not a {@code '+'} followed by digits is returned
 * unchanged.
 *
 * <p>Kept as a standalone helper (referenced from {@link OwnerMapper}'s {@code telephoneDisplay}
 * expression) rather than a mapper {@code default} method: a {@code String}-to-{@code String} method
 * on the mapper interface would be picked up by MapStruct as an automatic conversion for every
 * String property.
 */
final class TelephoneDisplay {

    private TelephoneDisplay() {
    }

    /**
     * Format the stored E.164 {@code telephone} for display, or return it unchanged when it is null
     * or not a {@code '+'} followed by digits.
     */
    static String of(String telephone) {
        if (telephone == null || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        if (!digits.matches("[0-9]+")) {
            return telephone;
        }
        String countryCode;
        if (digits.startsWith("61")) {
            countryCode = "61";
        }
        else if (digits.startsWith("1")) {
            countryCode = "1";
        }
        else {
            countryCode = digits.substring(0, 1);
        }
        String national = digits.substring(countryCode.length());
        StringBuilder display = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i += 3) {
            display.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return display.toString();
    }
}
