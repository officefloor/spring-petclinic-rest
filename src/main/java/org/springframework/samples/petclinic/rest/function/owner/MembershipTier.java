package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Membership tier for pet owners. An owner is {@code SILVER} when its {@code namesakeCount} is 0
 * (no existing owner shares its first and last name) and it has an email address; otherwise it is
 * {@code BRONZE}. Derived purely from the owner's own state, so it carries no stored data and is
 * seed-independent. Used by the owner mapper to expose {@code membershipTier} on responses.
 */
public final class MembershipTier {

    private MembershipTier() {
    }

    /**
     * The membership tier for the given namesake count and email: {@code SILVER} when the count is
     * exactly 0 and the email is present (non-null, non-blank), otherwise {@code BRONZE}.
     */
    public static String of(Integer namesakeCount, String email) {
        boolean unique = namesakeCount != null && namesakeCount == 0;
        boolean hasEmail = email != null && !email.isBlank();
        return unique && hasEmail ? "SILVER" : "BRONZE";
    }
}
