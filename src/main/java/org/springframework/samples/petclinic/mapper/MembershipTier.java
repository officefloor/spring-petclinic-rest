package org.springframework.samples.petclinic.mapper;

/**
 * Derives the owner's membership tier: {@code SILVER} when the owner had no namesakes
 * at creation (namesakeCount is 0) and has an email on file, otherwise {@code BRONZE}.
 * The value is a pure function of those two fields, so it needs no stored state.
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
