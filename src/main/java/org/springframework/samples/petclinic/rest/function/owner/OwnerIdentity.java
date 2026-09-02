package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Derives an owner's {@code identityKey}, the single value all duplicate detection is based on:
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}. Two owners are duplicates
 * only when their WHOLE keys are equal.
 *
 * <p>The household segment is empty for an owner not assigned to a shared household, so it never on its
 * own separates two owners: members of one household with different telephones already differ by the
 * telephone segment and are both allowed, while owners sharing a telephone and email collide.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    public static String key(String telephone, String email, String householdId) {
        String tel = telephone == null ? "" : telephone;
        String mail = email == null ? "" : email.toLowerCase();
        return tel + "|" + mail + "|" + (householdId == null ? "" : householdId);
    }
}
