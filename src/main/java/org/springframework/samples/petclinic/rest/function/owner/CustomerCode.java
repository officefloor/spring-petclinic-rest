package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The owner's customer code — the single derived identity every other value hangs off. It is
 * {@code '<REGION>-<HASH8>'}: REGION is the canonical region derived from the owner's postcode
 * (falling back to the city table via {@link Locality}); HASH8 is the first 8 UPPER-case hex
 * characters of SHA-256 over the normalized (E.164) telephone concatenated with the last name.
 *
 * <p>There are no sequence numbers: the code is a pure function of region, telephone and last
 * name, so it needs no repository scan and is stable for a given owner. The membership number,
 * its Luhn check digit ({@link CheckDigit}), the create audit line and the reported locality
 * ({@link #region(String)}) are all built from this one value.
 */
public final class CustomerCode {

    private CustomerCode() {
    }

    /** The {@code '<REGION>-<HASH8>'} customer code for {@code owner}. */
    public static String of(Owner owner) {
        String region = Locality.of(owner.getCity(), owner.getPostcode());
        String hash8 = ShaHex.upperPrefix(owner.getTelephone() + owner.getLastName(), 8);
        return region + "-" + hash8;
    }

    /**
     * De-duplicate a computed customer code against those already in use. When {@code code}
     * collides with an existing owner's customer code, the smallest {@code n} of 2 or more is
     * appended as {@code '-<n>'} to make it unique; a code that does not collide is returned
     * unchanged (as is a {@code null} code).
     *
     * @param code          the computed customer code to make unique
     * @param existingCodes the customer codes already assigned to other owners
     * @return the de-duplicated customer code
     */
    public static String dedupe(String code, java.util.Set<String> existingCodes) {
        if (code == null || !existingCodes.contains(code)) {
            return code;
        }
        for (int n = 2; ; n++) {
            String candidate = code + "-" + n;
            if (!existingCodes.contains(candidate)) {
                return candidate;
            }
        }
    }

    /**
     * The REGION component of a customer code (everything before the final {@code '-'}), or
     * {@code null} when the code is absent. This is the owner's locality, now read straight off
     * the identity rather than re-derived.
     */
    public static String region(String customerCode) {
        if (customerCode == null) {
            return null;
        }
        int hyphen = customerCode.lastIndexOf('-');
        return hyphen < 0 ? customerCode : customerCode.substring(0, hyphen);
    }
}
