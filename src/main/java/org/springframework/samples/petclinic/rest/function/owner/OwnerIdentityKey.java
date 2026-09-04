package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * The owner's {@code identityKey}: the full lower-case hex SHA-256 (64 characters) over
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}. It is the single basis of
 * the duplicate check — {@link EnsureUniqueIdentity} rejects a create whose key collides with an
 * existing non-deleted owner's key (409) — and is also surfaced on the owner response.
 *
 * <ul>
 * <li><b>normalizedTelephone</b> — the E.164 form (see {@link E164Telephone}); the stored telephone
 * is already E.164 and re-normalizing is idempotent. An unparseable/absent telephone contributes an
 * empty segment.</li>
 * <li><b>lowerEmail</b> — the trimmed, lower-cased address, or empty when the owner has none.</li>
 * <li><b>soundex(lastName)</b> — the {@link Soundex} code of the last name, so sound-alike surnames
 * share this segment.</li>
 * </ul>
 */
public final class OwnerIdentityKey {

    private OwnerIdentityKey() {
    }

    /** The identity key of an already-built/stored owner, for the response and the duplicate check. */
    public static String of(Owner owner) {
        return build(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    /** The identity key a create request would receive, for the duplicate check before the owner exists. */
    static String of(OwnerFieldsDto request) {
        return build(request.getTelephone(), request.getEmail(), request.getLastName());
    }

    /** The identity key from raw parts: SHA-256 hex over the normalized telephone, email and soundex. */
    static String build(String telephone, String email, String lastName) {
        String tel = E164Telephone.toE164OrNull(telephone);
        String source = (tel == null ? "" : tel) + "|"
                + (email == null ? "" : email.trim().toLowerCase()) + "|"
                + Soundex.of(lastName);
        return Sha256Hex.of(source);
    }
}
