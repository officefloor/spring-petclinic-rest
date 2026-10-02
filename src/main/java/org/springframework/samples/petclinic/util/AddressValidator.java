package org.springframework.samples.petclinic.util;

/**
 * Validates that an owner request supplies a usable postal address in one of the accepted forms.
 *
 * <p>An owner must supply an address in EITHER form: a non-blank structured {@code addressLine1}
 * or the flat {@code address}. Both forms are composed into a single effective address before this
 * check runs, so a {@code null} or blank composed address means neither was supplied.
 */
public final class AddressValidator {

    private AddressValidator() {
    }

    /**
     * Validate the composed effective address, throwing when it is absent.
     *
     * @param composedAddress the composed effective address (normalized {@code addressLine1} with
     *                        the normalized {@code addressLine2} appended, else the normalized flat
     *                        {@code address}), or {@code null} when neither form was supplied
     * @throws InvalidAddressException if the composed address is {@code null} or blank.
     */
    public static void validate(String composedAddress) {
        if (composedAddress == null || composedAddress.isBlank()) {
            throw new InvalidAddressException();
        }
    }
}
