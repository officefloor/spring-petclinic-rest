package org.springframework.samples.petclinic.util;

import java.util.Locale;

/**
 * Builds an owner's {@code customerCode}, formatted {@code <LAST3>-<NNNN>} where
 * {@code LAST3} is the upper-cased first three letters of the last name and
 * {@code NNNN} is a global 4-digit zero-padded sequence: one more than the
 * current number of owners (e.g. {@code SMI-0007}).
 */
public final class CustomerCode {

    private CustomerCode() {
    }

    public static String assign(String lastName, long ownerCount) {
        String last3 = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ENGLISH);
        return String.format("%s-%04d", last3, ownerCount + 1);
    }
}
