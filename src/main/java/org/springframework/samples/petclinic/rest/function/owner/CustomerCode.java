package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The owner's {@code customerCode} string format, kept in one place: how the code is assembled from
 * its parts, how its HASH8 segment is computed and how its REGION prefix is read back out. The code
 * is {@code <REGION>-<HASH8>} where REGION is the canonical region (see {@link Locality}) and HASH8 is
 * the first 8 upper-case hex characters of the SHA-256 digest of {@code normalizedTelephone + lastName}
 * (e.g. {@code NSW-1A2B3C4D}).
 *
 * <p>{@link AssignCustomerCode} builds the code through here and {@link Locality} reads its region
 * prefix through here, so neither has to know the code's internal layout. Concentrating the format in
 * a single home means a later change to it — for example unifying the code into a member id — is made
 * here rather than spread across its callers.
 */
public final class CustomerCode {

    private CustomerCode() {
    }

    /** The customer code assembled from its REGION and HASH8 segments, {@code <REGION>-<HASH8>}. */
    public static String of(String region, String hash8) {
        return region + "-" + hash8;
    }

    /**
     * The HASH8 segment: the first 8 upper-case hex characters of SHA-256 over
     * {@code (normalizedTelephone + lastName)}. The telephone is expected already normalized to
     * canonical E.164 form (see {@link OwnerIdentity#normalizedTelephone(String)}), the same
     * normalization the identity key uses, so the segment is derived purely from the owner's own
     * fields and is seed-independent.
     */
    public static String hash8(String normalizedTelephone, String lastName) {
        String input = normalizedTelephone + (lastName == null ? "" : lastName);
        return Sha256.hex(input).substring(0, 8).toUpperCase();
    }

    /**
     * The REGION prefix of a {@code <REGION>-<HASH8>} customer code — the text before the first
     * {@code -} — or {@code null} when the code is absent or carries no such prefix. Whether that
     * prefix names a recognised region is decided by its caller (see {@link Locality}).
     */
    public static String region(String customerCode) {
        if (customerCode == null) {
            return null;
        }
        int dash = customerCode.indexOf('-');
        if (dash <= 0) {
            return null;
        }
        return customerCode.substring(0, dash);
    }
}
