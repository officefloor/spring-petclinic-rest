package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.function.common.Localities;

/**
 * The single definition of an owner's {@code customerCode} format — {@code <REGION>-<HASH8>}, a
 * region code and an 8-character hash joined by {@code '-'}. Co-locates construction ({@link #format})
 * and read-back ({@link #regionOf}) so both agree on the separator and layout and cannot drift apart,
 * the same idiom {@link IdentityKey} uses for the identity key.
 *
 * <p>Shared by {@link AssignOwnerCustomerCode} (which builds the code) and the owner mapper (which
 * reads the region back out as the owner's locality). Public because the mapper lives in another
 * package.
 */
public final class CustomerCode {

    /** The character separating the region from the hash. */
    private static final char SEPARATOR = '-';

    /** The customer code for the given region and hash segment. */
    public static String format(String region, String hash8) {
        return region + SEPARATOR + hash8;
    }

    /**
     * The region segment of a {@code <REGION>-<HASH8>} customer code — everything before the first
     * {@code '-'} — or {@link Localities#UNKNOWN} when the code is absent or blank.
     */
    public static String regionOf(String customerCode) {
        if (customerCode == null || customerCode.isBlank()) {
            return Localities.UNKNOWN;
        }
        int dash = customerCode.indexOf(SEPARATOR);
        return dash < 0 ? customerCode : customerCode.substring(0, dash);
    }

    private CustomerCode() {
    }
}
