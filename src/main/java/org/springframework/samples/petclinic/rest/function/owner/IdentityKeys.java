package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The derived key summarising an owner's identity for display: {@code normalizedTelephone +
 * "|" + (email or empty) + "|" + householdId}, canonicalizing each part the same way it is
 * stored (E.164 telephone, lower-cased email, deterministic household id). Duplicate rejection
 * itself is keyed on the household alone (see {@link EnsureUniqueHousehold}); this key exposes
 * the full derivation on the response.
 */
public final class IdentityKeys {

    private IdentityKeys() {
    }

    /**
     * The identity key of an existing owner, derived from its stored fields.
     */
    public static String of(Owner owner) {
        return key(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    private static String key(String telephone, String email, String householdId) {
        String normalizedTelephone = telephone == null ? ""
                : Telephones.toE164(telephone).orElse(telephone);
        String normalizedEmail = email == null || email.isBlank() ? "" : Emails.normalize(email);
        String household = householdId == null ? "" : householdId;
        return normalizedTelephone + "|" + normalizedEmail + "|" + household;
    }
}
