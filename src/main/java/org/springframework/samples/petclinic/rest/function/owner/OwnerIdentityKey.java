package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single derived key used to detect duplicate owners, consolidating what were once
 * separate telephone, email and household checks into one value:
 * {@code normalizedTelephone + "|" + (email or empty) + "|" + householdId}.
 *
 * <p>The telephone is compared in canonical E.164 form (see {@link OwnerTelephone}); the email
 * is lower-cased and rendered empty when absent; the householdId is rendered empty when the
 * owner shares no household. Creation is rejected with 409 only when a new owner's WHOLE
 * identityKey equals an existing owner's — so, because the telephone is part of the key, two
 * members of the same household with different telephones have different keys and are both
 * allowed. See {@link EnsureUniqueOwnerIdentity}.
 */
public final class OwnerIdentityKey {

    private OwnerIdentityKey() {
    }

    /** The identityKey of a stored owner, from its telephone, email and householdId. */
    public static String of(Owner owner) {
        return of(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    /** The identityKey for the given telephone, email and householdId. */
    public static String of(String telephone, String email, String householdId) {
        String tel = OwnerTelephone.canonical(telephone);
        String mail = (email == null || email.isBlank()) ? "" : email.toLowerCase(Locale.ROOT);
        String household = (householdId == null) ? "" : householdId;
        return tel + "|" + mail + "|" + household;
    }
}
