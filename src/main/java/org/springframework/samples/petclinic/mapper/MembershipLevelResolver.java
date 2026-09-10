package org.springframework.samples.petclinic.mapper;

/**
 * Derives an owner's numeric 'membershipLevel', assigned on create. Kept out of
 * {@link OwnerMapper} so MapStruct does not mistake it for an implicit property
 * mapping method.
 */
public final class MembershipLevelResolver {

    /** The highest level attainable on create; level 4 is reserved for tenure. */
    private static final int MAX_LEVEL = 3;

    private MembershipLevelResolver() {
    }

    /**
     * Returns the membership level for an owner. Starts at 1, gains 1 when an
     * email is present (non-blank), gains 1 when the owner's name was unique on
     * create ({@code namesakeCount} is 0), and is capped at 3.
     *
     * @param email         the owner's email, may be {@code null}
     * @param namesakeCount the owner's namesake count, may be {@code null}
     * @return the membership level, between 1 and 3 inclusive
     */
    public static int deriveMembershipLevel(String email, Integer namesakeCount) {
        int level = 1;
        if (email != null && !email.isBlank()) {
            level++;
        }
        if (namesakeCount != null && namesakeCount == 0) {
            level++;
        }
        return Math.min(level, MAX_LEVEL);
    }
}
