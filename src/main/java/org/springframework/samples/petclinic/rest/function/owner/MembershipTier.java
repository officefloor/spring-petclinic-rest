package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Derives the {@code membershipTier}: {@code SILVER} for an owner with no namesakes
 * (namesakeCount 0) who has an email address, otherwise {@code BRONZE}.
 */
public final class MembershipTier {

    private MembershipTier() {
    }

    public static String of(Integer namesakeCount, String email) {
        boolean unique = namesakeCount != null && namesakeCount == 0;
        boolean hasEmail = email != null && !email.isBlank();
        return unique && hasEmail ? "SILVER" : "BRONZE";
    }
}
