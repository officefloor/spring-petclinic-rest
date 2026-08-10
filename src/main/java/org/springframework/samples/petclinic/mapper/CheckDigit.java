package org.springframework.samples.petclinic.mapper;

/**
 * Derives an owner's {@code checkDigit}: a single Luhn check digit (0-9) computed over the
 * digits contained in the owner's {@code customerCode}, ignoring any non-digit characters.
 *
 * <p>Kept as a standalone helper (referenced from {@link OwnerMapper}'s {@code checkDigit}
 * expression) rather than a mapper {@code default} method: a {@code String}-to-{@code int}
 * method on the mapper interface would be picked up by MapStruct as an automatic conversion.
 */
final class CheckDigit {

    private CheckDigit() {
    }

    /**
     * The Luhn check digit over the digits in {@code customerCode}. Every digit is summed
     * right-to-left, doubling alternate digits (starting with the rightmost) and subtracting 9
     * from any doubled value over 9; the check digit makes that sum a multiple of 10.
     */
    static int of(String customerCode) {
        int sum = 0;
        boolean dbl = true;
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
        return (10 - (sum % 10)) % 10;
    }
}
