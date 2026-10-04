package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for an owner's {@code customerCode}: the region-and-hash identity every
 * other derived owner value flows from. It owns both sides of the {@code <REGION>-<HASH8>} format
 * so the write-time side ({@link AssignCustomerCode}, which constructs it) and the read-time side
 * ({@code OwnerMapper.locality}, which recovers the region from it) cannot drift — mirroring how
 * {@link OwnerIdentity}, {@link OwnerTelephone} and {@link OwnerLocality} centralise their rules.
 *
 * <p>The format is {@code <REGION>-<HASH8>} where:
 * <ul>
 *   <li>{@code REGION} is the owner's region ({@link #regionFor}): the postcode's region, falling
 *       back to the city-to-region table, and {@link #UNKNOWN_REGION} when neither resolves;</li>
 *   <li>{@code HASH8} is the first 8 upper-case hex characters of SHA-256 over the owner's
 *       canonical telephone ({@link OwnerTelephone#toE164OrRaw(String)}) concatenated with its
 *       last name (e.g. {@code NSW-1A2B3C4D}).</li>
 * </ul>
 *
 * <p>Because {@code REGION} is a fixed set of letters ({@code NSW}/{@code VIC}/{@code QLD}/
 * {@code UNKNOWN}) and {@code HASH8} is hex, neither contains a {@code '-'}, so the first
 * {@code '-'} cleanly separates the two halves for {@link #regionOf}.
 */
public final class OwnerCustomerCode {

    /** Region sentinel used when neither the postcode nor the city resolves to a known region. */
    public static final String UNKNOWN_REGION = "UNKNOWN";

    private static final char SEPARATOR = '-';

    private OwnerCustomerCode() {
    }

    /** The region-and-hash {@code customerCode} for the owner's current fields. */
    public static String of(Owner owner) {
        return regionFor(owner) + SEPARATOR + hash8(OwnerTelephone.toE164OrRaw(owner.getTelephone())
                + owner.getLastName());
    }

    /**
     * The owner's region for identity and display: the postcode/city region (see
     * {@link OwnerLocality#regionFor}), or {@link #UNKNOWN_REGION} when neither resolves.
     */
    public static String regionFor(Owner owner) {
        String region = OwnerLocality.regionFor(owner.getPostcode(), owner.getCity());
        return region != null ? region : UNKNOWN_REGION;
    }

    /**
     * A de-duplicated variant of {@code base}, formed by appending {@code -<n>} (e.g.
     * {@code NSW-1A2B3C4D} with {@code n == 2} becomes {@code NSW-1A2B3C4D-2}). Used when the
     * derived {@code customerCode} collides with an existing owner's: the caller picks the smallest
     * {@code n} of 2 or more that is free. Keeping the suffix spelling here, next to {@link #of} and
     * {@link #regionOf}, keeps this class the single authority for the {@code customerCode} format —
     * the {@code REGION} prefix is unchanged, so {@link #regionOf} still reads the region from the
     * first {@code '-'}.
     */
    public static String withSuffix(String base, int n) {
        return base + SEPARATOR + n;
    }

    /**
     * The {@code REGION} half of a {@code customerCode} (the text before the first {@code '-'}), or
     * {@code null} when {@code customerCode} is null or carries no region prefix.
     */
    public static String regionOf(String customerCode) {
        if (customerCode == null) {
            return null;
        }
        int sep = customerCode.indexOf(SEPARATOR);
        return sep > 0 ? customerCode.substring(0, sep) : null;
    }

    /** The first 8 upper-case hex characters (4 bytes) of SHA-256 over {@code value}. */
    private static String hash8(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
