package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * The single definition of an owner's customer code: {@code <CITY3>-<LAST3>-<NNNN>}, where
 * CITY3 is the upper-cased first three letters of the city and LAST3 the upper-cased first
 * three letters of the last name (fewer when either is shorter), and NNNN is a 4-digit
 * zero-padded sequence number.
 *
 * <p>Formatting only; the sequence value is supplied by the caller
 * ({@link AssignOwnerCustomerCode} derives it from the owners already in the city).
 */
final class CustomerCode {

    private static final int PREFIX_LENGTH = 3;

    private CustomerCode() {
    }

    /**
     * Format {@code city}, {@code lastName} and {@code sequence} into a customer code, e.g.
     * {@code format("Springfield", "Smithers", 7)} yields {@code "SPR-SMI-0007"}.
     */
    static String format(String city, String lastName, int sequence) {
        return String.format("%s-%s-%04d", prefix(city), prefix(lastName), sequence);
    }

    /** The upper-cased first three letters of {@code value} (fewer when it is shorter). */
    private static String prefix(String value) {
        return value.substring(0, Math.min(PREFIX_LENGTH, value.length())).toUpperCase(Locale.ROOT);
    }
}
