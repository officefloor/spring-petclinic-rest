package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * The identity key summarising who an owner is for duplicate detection: the full lower-case
 * SHA-256 hex of {@code normalizedTelephone + "|" + lowerEmail + "|" + soundex(lastName)}, each
 * part canonicalized the same way it is stored (E.164 telephone, lower-cased email, Soundex of
 * the last name). Two owners collide only when all three parts match, so a differing telephone
 * yields a different key. Duplicate rejection (409) is keyed on this value (see
 * {@link EnsureUniqueIdentity}) and it is also exposed on the response.
 */
public final class IdentityKeys {

    private IdentityKeys() {
    }

    /** The identity key of an existing owner, derived from its stored fields. */
    public static String of(Owner owner) {
        return key(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    /** The identity key a create request resolves to, from its (normalized) fields. */
    public static String of(OwnerFieldsDto request) {
        return key(request.getTelephone(), request.getEmail(), request.getLastName());
    }

    private static String key(String telephone, String email, String lastName) {
        String normalizedTelephone = telephone == null ? ""
                : Telephones.toE164(telephone).orElse(telephone);
        String lowerEmail = email == null || email.isBlank() ? "" : Emails.normalize(email);
        String soundex = Soundex.encode(lastName);
        return Hashes.lowerHex(normalizedTelephone + "|" + lowerEmail + "|" + soundex);
    }
}
