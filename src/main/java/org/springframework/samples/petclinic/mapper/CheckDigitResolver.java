package org.springframework.samples.petclinic.mapper;

/**
 * Derives the Luhn check digit for an owner identifier, computed over the digits
 * contained in the identifier's value. It supplies the {@code CHK} segment of the
 * owner's {@code memberId} (see {@link IdentityKeyResolver#deriveMemberId}), computed
 * over the digits of the {@code <REGION><FY><HASH8>} prefix. Kept out of
 * {@link OwnerMapper} so MapStruct does not mistake it for an implicit property
 * mapping method.
 */
public final class CheckDigitResolver {

    private CheckDigitResolver() {
    }

    /**
     * Returns the Luhn check digit (0-9) computed over the digits contained in the
     * given identifier value. Non-digit characters are ignored, so the value may be
     * any identifier (or identifier segment) the check digit is appended to.
     *
     * @param value the identifier value to check, may be {@code null}
     * @return the Luhn check digit, between 0 and 9 inclusive
     */
    public static int deriveCheckDigit(String value) {
        int sum = 0;
        boolean dbl = true;
        if (value != null) {
            for (int i = value.length() - 1; i >= 0; i--) {
                char c = value.charAt(i);
                if (c < '0' || c > '9') {
                    continue;
                }
                int d = c - '0';
                if (dbl) {
                    d *= 2;
                    if (d > 9) {
                        d -= 9;
                    }
                }
                sum += d;
                dbl = !dbl;
            }
        }
        return (10 - (sum % 10)) % 10;
    }
}
