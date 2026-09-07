package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Membership tier for pet owners. An owner is {@code GOLD} when its household (owners sharing the
 * same {@code householdId}) has three or more members; otherwise it is {@code SILVER} when its
 * {@code namesakeCount} is 0 (no existing owner shares its first and last name) and it has an email
 * address, and {@code BRONZE} in every other case. Derived purely from the owner's own stored state
 * ({@code householdSize} and {@code namesakeCount} are recorded at create time), so it is
 * seed-independent. Used by the owner mapper to expose {@code membershipTier} on responses.
 */
public final class MembershipTier {

    private MembershipTier() {
    }

    /**
     * The membership tier for the given household size, namesake count and email: {@code GOLD} when
     * the household has three or more members; otherwise {@code SILVER} when the namesake count is
     * exactly 0 and the email is present (non-null, non-blank), and {@code BRONZE} otherwise.
     */
    public static String of(Integer householdSize, Integer namesakeCount, String email) {
        if (householdSize != null && householdSize >= 3) {
            return "GOLD";
        }
        boolean unique = namesakeCount != null && namesakeCount == 0;
        boolean hasEmail = email != null && !email.isBlank();
        return unique && hasEmail ? "SILVER" : "BRONZE";
    }
}
