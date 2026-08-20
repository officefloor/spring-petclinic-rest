package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single derived identity of an owner used for duplicate detection.
 *
 * <p>An owner's {@code identityKey} is
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}, where the
 * telephone is its E.164 form, the email is its lower-cased form (empty when absent or
 * blank) and the householdId is the owner's shared-household identifier (empty when the
 * owner does not share a household). Two owners are duplicates only when their whole
 * identity keys are equal — so, for example, two members of the same household (same
 * householdId) with different telephones have different keys and are both allowed.
 */
public final class OwnerIdentityKey {

    private OwnerIdentityKey() {
    }

    /**
     * Builds an identity key from its already-normalized parts.
     *
     * @param telephoneE164 the E.164 telephone (never blank for a valid owner)
     * @param email the email, or {@code null}/blank when none
     * @param householdId the shared-household id, or {@code null}/empty when none
     * @return the {@code telephone|email|householdId} identity key
     */
    public static String of(String telephoneE164, String email, String householdId) {
        String tel = telephoneE164 == null ? "" : telephoneE164;
        String mail = (email == null || email.trim().isEmpty()) ? ""
                : email.trim().toLowerCase(Locale.ROOT);
        String household = (householdId == null || householdId.isEmpty()) ? "" : householdId;
        return tel + "|" + mail + "|" + household;
    }

    /**
     * Derives the identity key of a stored owner, normalizing its telephone to E.164 and its
     * email to lower case exactly as the create pipeline normalizes an incoming request, so a
     * new owner and an existing owner are compared on the same footing.
     */
    public static String forOwner(Owner owner) {
        return of(TelephoneNormalizer.toE164(owner.getTelephone()), owner.getEmail(),
                owner.getHouseholdId());
    }
}
