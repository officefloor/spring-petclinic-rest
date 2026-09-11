package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.mapper.Localities;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code customerCode}, the {@code <REGION>-<HASH8>} identity every
 * other owner-code-derived value is built from.
 *
 * <p>{@code REGION} is the region code derived from the owner's postcode
 * ({@link Localities#forPostcode(String)}), or {@code UNKNOWN} when the postcode is
 * absent or in no known range. {@code HASH8} is the first 8 upper-case hex characters
 * of the SHA-256 digest of the normalized telephone ({@link Telephones#toE164(String)})
 * concatenated with the last name. There are no sequence numbers: the identity is a
 * pure function of the owner's own fields.
 *
 * <p>Centralising the derivation here keeps {@link AssignCustomerCode} (which assigns
 * the code) and the {@code locality} mapping (which reads the region back off it) in
 * exact agreement.
 */
public final class CustomerCodes {

    private CustomerCodes() {
    }

    /** The {@code <REGION>-<HASH8>} customerCode for the given owner. */
    public static String forOwner(Owner owner) {
        return region(owner.getPostcode()) + "-" + hash8(owner);
    }

    /**
     * The owner's locality: the {@code REGION} segment of its customerCode, so the
     * locality is derived from the region-and-hash identity. Falls back to the
     * postcode-derived region for owners without a customerCode.
     */
    public static String localityOf(Owner owner) {
        String code = owner.getCustomerCode();
        if (code != null) {
            int dash = code.indexOf('-');
            if (dash > 0) {
                return code.substring(0, dash);
            }
        }
        return region(owner.getPostcode());
    }

    /**
     * The owner's IANA timezone, derived from its {@link #localityOf(Owner) locality}
     * via the fixed region-to-timezone table, or {@code null} when the region is unknown.
     */
    public static String timezoneOf(Owner owner) {
        return Localities.timezoneForRegion(localityOf(owner));
    }

    /** The region code derived from a postcode, or {@code UNKNOWN} when unresolved. */
    public static String region(String postcode) {
        String region = Localities.forPostcode(postcode);
        return region != null ? region : "UNKNOWN";
    }

    /** First 8 upper-case hex chars of SHA-256 over (normalizedTelephone + lastName). */
    private static String hash8(Owner owner) {
        String telephone = Telephones.toE164(owner.getTelephone());
        String lastName = owner.getLastName();
        String basis = (telephone == null ? "" : telephone) + (lastName == null ? "" : lastName);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(basis.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
