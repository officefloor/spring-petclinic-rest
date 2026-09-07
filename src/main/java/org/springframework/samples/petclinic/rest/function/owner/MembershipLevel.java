package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Numeric membership level for pet owners, from 1 to 3. Every owner starts at level 1; the level
 * gains 1 when the owner has an email address (non-null, non-blank) and a further 1 when its
 * {@code namesakeCount} is 0 (no existing owner shares its first and last name), capped at 3. Level
 * 4 is reserved for tenure and is not awarded here. Derived purely from the owner's own stored state
 * ({@code namesakeCount} is recorded at create time), so it is seed-independent. Used by the owner
 * mapper to expose {@code membershipLevel} on responses.
 */
public final class MembershipLevel {

    private MembershipLevel() {
    }

    /**
     * The membership level for the given namesake count and email: 1 to start, +1 when the email is
     * present (non-null, non-blank), +1 when the namesake count is exactly 0, capped at 3.
     */
    public static int of(Integer namesakeCount, String email) {
        int level = 1;
        boolean hasEmail = email != null && !email.isBlank();
        if (hasEmail) {
            level++;
        }
        boolean unique = namesakeCount != null && namesakeCount == 0;
        if (unique) {
            level++;
        }
        return Math.min(level, 3);
    }
}
