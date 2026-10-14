package org.springframework.samples.petclinic.rest.validation;

/**
 * Normalizes and validates owner telephone numbers.
 */
public final class OwnerTelephone {

    public static final int REQUIRED_DIGITS = 10;

    private OwnerTelephone() {
    }

    /**
     * Strips every non-digit character and requires exactly {@value #REQUIRED_DIGITS} digits to remain.
     *
     * @param telephone the telephone number as supplied in the request
     * @return the normalized {@value #REQUIRED_DIGITS}-digit telephone number
     * @throws InvalidTelephoneException if the number does not contain exactly {@value #REQUIRED_DIGITS} digits
     */
    public static String normalize(String telephone) {
        String digits = telephone.replaceAll("\\D", "");
        if (digits.length() != REQUIRED_DIGITS) {
            throw new InvalidTelephoneException(telephone);
        }
        return digits;
    }

    /**
     * @param telephone the telephone number as supplied in the request
     * @throws InvalidTelephoneException if the number contains anything other than digits
     */
    public static void requireDigitsOnly(String telephone) {
        if (telephone != null && !telephone.chars().allMatch(Character::isDigit)) {
            throw new InvalidTelephoneException(telephone);
        }
    }
}
