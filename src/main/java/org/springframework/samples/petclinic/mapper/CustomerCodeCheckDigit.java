package org.springframework.samples.petclinic.mapper;

/**
 * Computes the Luhn check digit over the digits of an owner's {@code customerCode}.
 *
 * <p>The customer code contains non-digit separators (e.g. {@code 'SYD-SMI-0007'}); only the digit
 * characters take part in the Luhn calculation, processed right-to-left with every second digit
 * doubled and any result above nine reduced by nine. The check digit is the amount that must be
 * added to the running sum to reach the next multiple of ten.
 *
 * <p>Kept as a standalone helper rather than a method on {@link OwnerMapper}: a single-argument
 * method declared on a MapStruct mapper would be picked up as an implicit conversion and applied to
 * matching property mappings.
 */
public final class CustomerCodeCheckDigit {

    private CustomerCodeCheckDigit() {
    }

    /**
     * Returns the Luhn check digit (0-9) over the digit characters of {@code customerCode}, or
     * {@code 0} when the value is {@code null} or contains no digits.
     */
    public static int of(String customerCode) {
        if (customerCode == null) {
            return 0;
        }
        int sum = 0;
        boolean doubleDigit = true;
        for (int i = customerCode.length() - 1; i >= 0; i--) {
            char c = customerCode.charAt(i);
            if (c < '0' || c > '9') {
                continue;
            }
            int d = c - '0';
            if (doubleDigit) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            sum += d;
            doubleDigit = !doubleDigit;
        }
        return (10 - (sum % 10)) % 10;
    }
}
