package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Formats and reads an owner's customer code as {@code <REGION>-<HASH8>}, where
 * {@code REGION} is the region code derived from the owner's postcode (see
 * {@link org.springframework.samples.petclinic.model.CityRegion}) and {@code HASH8} is the
 * first 8 upper-case hex characters of {@code SHA-256(normalizedTelephone + lastName)}
 * (e.g. {@code NSW-1A2B3C4D}). The code is derived purely from the owner's own fields, so
 * the same owner always yields the same code without any sequence or coordination.
 */
public final class CustomerCode {

    /** Number of hex characters kept from the telephone-and-name hash. */
    private static final int HASH_LENGTH = 8;

    private CustomerCode() {
    }

    /**
     * The customer code for an owner in {@code region} whose normalized telephone and last
     * name are given.
     */
    public static String format(String region, String normalizedTelephone, String lastName) {
        String telephone = normalizedTelephone == null ? "" : normalizedTelephone;
        String last = lastName == null ? "" : lastName;
        return region + "-" + Hashes.upperHexPrefix(telephone + last, HASH_LENGTH);
    }

    /**
     * The {@code REGION} segment of a customer code — everything before the first {@code -}
     * separator, i.e. the region the code was minted in.
     */
    public static String region(String customerCode) {
        int separator = customerCode.indexOf('-');
        return separator < 0 ? customerCode : customerCode.substring(0, separator);
    }
}
