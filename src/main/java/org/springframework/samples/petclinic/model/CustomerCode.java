package org.springframework.samples.petclinic.model;

/**
 * The single definition of an owner's customer code: {@code <REGION>-<HASH8>} where REGION is
 * the region derived from the owner's postcode and HASH8 is the first 8 upper-case hex
 * characters of SHA-256 over the owner's normalized telephone plus last name. Building the
 * code and reading its region back both live here so the format is defined in one place.
 */
public final class CustomerCode {

    /** Number of leading hex characters of the telephone-and-name digest kept in the code. */
    private static final int HASH_LENGTH = 8;

    private static final String SEPARATOR = "-";

    private CustomerCode() {
    }

    /**
     * The customer code for {@code owner}. The owner's telephone must already be normalized
     * (see the create pipeline), since it feeds the hash.
     *
     * @param owner the owner whose identity to derive
     * @return the {@code <REGION>-<HASH8>} customer code
     */
    public static String forOwner(Owner owner) {
        String region = Locality.regionForPostcode(owner.getPostcode());
        String hash = Sha256.upperHex(owner.getTelephone() + owner.getLastName(), HASH_LENGTH);
        return region + SEPARATOR + hash;
    }

    /**
     * The REGION segment of a customer code.
     *
     * @param customerCode the customer code (may be {@code null})
     * @return the region before the first separator, or {@code null} when the code is null
     */
    public static String region(String customerCode) {
        if (customerCode == null) {
            return null;
        }
        int separator = customerCode.indexOf(SEPARATOR);
        return separator < 0 ? customerCode : customerCode.substring(0, separator);
    }
}
