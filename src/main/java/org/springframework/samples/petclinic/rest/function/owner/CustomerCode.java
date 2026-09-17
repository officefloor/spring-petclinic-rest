package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * The single definition of an owner's customer code: {@code <LAST3>-<NNNN>}, where LAST3 is
 * the upper-cased first three letters of the last name (fewer when the name is shorter) and
 * NNNN is a 4-digit zero-padded sequence number.
 *
 * <p>Formatting only; the sequence value is supplied by the caller
 * ({@link AssignOwnerCustomerCode} derives it from the current number of owners).
 */
final class CustomerCode {

    private static final int PREFIX_LENGTH = 3;

    private CustomerCode() {
    }

    /**
     * Format {@code lastName} and {@code sequence} into a customer code, e.g.
     * {@code format("Smithers", 7)} yields {@code "SMI-0007"}.
     */
    static String format(String lastName, int sequence) {
        String prefix = lastName.substring(0, Math.min(PREFIX_LENGTH, lastName.length()))
                .toUpperCase(Locale.ROOT);
        return String.format("%s-%04d", prefix, sequence);
    }
}
