package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Computes the {@code checkDigit}: a single Luhn check digit (0-9) over the digits
 * contained in the {@code customerCode} (e.g. {@code SMI-0007} → the Luhn digit of 0007).
 */
public final class CheckDigit {

    private CheckDigit() {
    }

    public static int of(String customerCode) {
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
