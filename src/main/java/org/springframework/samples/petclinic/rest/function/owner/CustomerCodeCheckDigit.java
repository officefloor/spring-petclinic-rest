package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Single source of truth for an owner's {@code checkDigit}: the Luhn check digit (0-9) computed
 * over the digits contained in the {@code customerCode}. Non-digit characters are ignored.
 */
public final class CustomerCodeCheckDigit {

    private CustomerCodeCheckDigit() {
    }

    public static int of(String customerCode) {
        int sum = 0;
        boolean doubling = true;
        for (int i = customerCode.length() - 1; i >= 0; i--) {
            char c = customerCode.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (doubling && (d *= 2) > 9) {
                d -= 9;
            }
            sum += d;
            doubling = !doubling;
        }
        return (10 - (sum % 10)) % 10;
    }
}
