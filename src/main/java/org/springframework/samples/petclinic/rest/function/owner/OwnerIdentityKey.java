package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * The single derived key that all owner duplicate detection is expressed through:
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}. Two owners are
 * duplicates only when their whole {@code identityKey} is equal. Because the normalized telephone is
 * part of the key, two members of the same household (same {@code householdId}) with different
 * telephones have different keys and are both allowed; only an exact full-key match is a duplicate.
 *
 * <ul>
 * <li><b>normalizedTelephone</b> — the E.164 form (see {@link E164Telephone}); the stored telephone
 * is already E.164 and re-normalizing is idempotent.</li>
 * <li><b>email</b> — the lower-cased address, or empty when the owner has none.</li>
 * <li><b>householdId</b> — the owner's assigned shared-household id (see {@link AssignHousehold}), or
 * empty when the owner is not part of a shared household.</li>
 * </ul>
 */
public final class OwnerIdentityKey {

    private OwnerIdentityKey() {
    }

    /** The identity key of an already-built/stored owner. */
    public static String of(Owner owner) {
        return build(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    /** The identity key from raw parts. Null telephone or email contribute an empty segment. */
    static String build(String telephone, String email, String householdId) {
        String tel = E164Telephone.toE164OrNull(telephone);
        return (tel == null ? "" : tel) + "|"
                + (email == null ? "" : email.trim().toLowerCase()) + "|"
                + (householdId == null ? "" : householdId);
    }

    /**
     * The household id a create request would be assigned, mirroring {@link AssignHousehold}: only a
     * request opting into a shared household ({@code sharesHousehold} true) that matches an existing
     * owner's normalized last name and address gets one; otherwise it has none (empty segment).
     */
    static String prospectiveHouseholdId(OwnerFieldsDto request, OwnerRepository ownerRepository) {
        if (!Boolean.TRUE.equals(request.getSharesHousehold())) {
            return null;
        }
        String lastName = normalizeName(request.getLastName());
        String address = AddressNormalizer.normalize(request.getAddress());
        for (Owner existing : ownerRepository.findAll()) {
            if (normalizeName(existing.getLastName()).equals(lastName)
                    && AddressNormalizer.normalize(existing.getAddress()).equals(address)) {
                return householdId(lastName, address);
            }
        }
        return null;
    }

    /** Last-name / address normalization used for household grouping: trim, collapse whitespace, lower-case. */
    static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /** Stable household id: upper-case hex of the first 8 bytes of SHA-256 of the normalized parts. */
    static String householdId(String normalizedLastName, String normalizedAddress) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((normalizedLastName + "|" + normalizedAddress).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(16);
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02x", digest[i]));
            }
            return sb.toString().toUpperCase();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
