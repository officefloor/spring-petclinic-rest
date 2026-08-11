package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Formats an owner's stored E.164 {@code telephone} for humans: the country code, a space, then the
 * national digits grouped in threes from the left (e.g. {@code '+61412345678'} becomes
 * {@code '+61 412 345 678'}). The raw {@code telephone} is left untouched; this only drives the
 * derived {@code telephoneDisplay} read at request time.
 *
 * <p>The country code is recognised from the same prefixes the app normalizes to: {@code '+61'}
 * (Australia) and {@code '+1'} (NANP). A value that is absent or not in E.164 form is returned as
 * given. Kept out of {@link OwnerMapper} so MapStruct does not mistake the helper for an implicit
 * mapping method and apply it to every string property.
 */
public final class TelephoneDisplay {

    /** Recognised country codes, longest first so '+61' wins over '+1'-style prefixes. */
    private static final String[] COUNTRY_CODES = {"61", "1"};

    private TelephoneDisplay() {
    }

    /** The human-formatted telephone for {@code owner}, or the raw value when it is not E.164. */
    public static String of(Owner owner) {
        return of(owner.getTelephone());
    }

    /** The human-formatted form of the stored E.164 {@code telephone}. */
    public static String of(String telephone) {
        if (telephone == null || !telephone.startsWith("+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        for (String code : COUNTRY_CODES) {
            if (digits.startsWith(code)) {
                return "+" + code + " " + groupInThrees(digits.substring(code.length()));
            }
        }
        return "+" + groupInThrees(digits);
    }

    /** Groups {@code digits} in blocks of three from the left, space-separated. */
    private static String groupInThrees(String digits) {
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(digits.charAt(i));
        }
        return grouped.toString();
    }
}
