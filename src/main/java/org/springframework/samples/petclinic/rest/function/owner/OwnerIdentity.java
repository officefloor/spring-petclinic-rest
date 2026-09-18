package org.springframework.samples.petclinic.rest.function.owner;

/**
 * The derived duplicate-detection identity of an owner: a single key that consolidates the
 * telephone, email and household the create pipeline rejects duplicates by. The key is
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + (householdId or empty)}.
 *
 * <p>Two owners are duplicates only when their <em>whole</em> keys are equal, so members of
 * one household (same household id) with different telephones have different keys and are both
 * allowed — only an exact full-key match is a duplicate.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /**
     * The identity key for the given canonical components. The telephone is compared in its
     * {@link OwnerTelephone#toE164(String) E.164} form and the email in its
     * {@link OwnerEmail#normalize(String) lower-cased} form; an absent email or household id
     * contributes the empty string.
     */
    public static String key(String telephone, String email, String householdId) {
        return normalizedTelephone(telephone) + "|" + normalizedEmail(email) + "|" + orEmpty(householdId);
    }

    private static String normalizedTelephone(String telephone) {
        return OwnerTelephone.toE164(telephone).orElse(orEmpty(telephone));
    }

    private static String normalizedEmail(String email) {
        return (email == null || email.isBlank()) ? "" : OwnerEmail.normalize(email);
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
