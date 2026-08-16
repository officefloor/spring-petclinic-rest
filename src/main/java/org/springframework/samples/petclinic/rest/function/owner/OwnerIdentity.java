package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The single derived identity of an owner used for duplicate detection.
 *
 * <p>The {@code identityKey} consolidates the former separate telephone, email and household
 * duplicate checks into ONE value: {@code normalizedTelephone + '|' + (email or empty) + '|' +
 * householdId}. Two owners are duplicates only when their WHOLE key is equal, so — because the
 * telephone is part of the key — two members of the same household (same {@code householdId})
 * with different telephones have different keys and are both allowed.
 *
 * <ul>
 *   <li>telephone — normalized to E.164 with {@link NormalizeOwnerTelephone#toE164(String)} so
 *       equivalent numbers in different formats collide; the raw value is used only as a
 *       fallback when it cannot form a valid E.164 number.</li>
 *   <li>email — lower-cased so addresses differing only in case collide; a null or blank email
 *       contributes an empty segment.</li>
 *   <li>householdId — the shared household identifier, or an empty segment when the owner is not
 *       part of a household.</li>
 * </ul>
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /** Builds the {@code telephone|email|householdId} identity key. */
    public static String identityKey(String telephone, String email, String householdId) {
        String tel = NormalizeOwnerTelephone.toE164(telephone);
        if (tel == null) {
            tel = telephone == null ? "" : telephone;
        }
        String mail = (email == null || email.isBlank()) ? "" : email.toLowerCase();
        String hh = householdId == null ? "" : householdId;
        return tel + "|" + mail + "|" + hh;
    }
}
