package org.springframework.samples.petclinic.mapper;

/**
 * Derives an owner's {@code checkDigit}, the Luhn check digit computed over the
 * digits contained in the owner's {@code customerCode}. Kept out of
 * {@link OwnerMapper} so MapStruct does not mistake it for an implicit property
 * mapping method.
 */
public final class CheckDigitResolver {

    private CheckDigitResolver() {
    }

    /**
     * Returns the Luhn check digit (0-9) computed over the digits contained in the
     * given customer code. Non-digit characters are ignored.
     *
     * @param customerCode the owner's customer code, may be {@code null}
     * @return the Luhn check digit, between 0 and 9 inclusive
     */
    public static int deriveCheckDigit(String customerCode) {
        int sum = 0;
        boolean dbl = true;
        if (customerCode != null) {
            for (int i = customerCode.length() - 1; i >= 0; i--) {
                char c = customerCode.charAt(i);
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
