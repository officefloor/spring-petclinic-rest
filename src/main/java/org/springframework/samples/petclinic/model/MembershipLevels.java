package org.springframework.samples.petclinic.model;

/**
 * Derives an owner's membership points and the level mapped from them. Points:
 * +2 for a present email, +1 when namesakeCount is 0, +2 for a household of 3 or
 * more and +3 for tenure over 365 days. Level maps points 0-1 to 1, 2-3 to 2,
 * 4-5 to 3 and 6 or more to 4.
 */
public final class MembershipLevels {

    private MembershipLevels() {
    }

    /** Membership points earned by {@code owner}. */
    public static int pointsOf(Owner owner) {
        int points = 0;
        if (owner.getEmail() != null) {
            points += 2;
        }
        if (owner.getNamesakeCount() != null && owner.getNamesakeCount() == 0) {
            points += 1;
        }
        if (owner.getHouseholdMemberCount() != null && owner.getHouseholdMemberCount() >= 3) {
            points += 2;
        }
        java.time.LocalDate registered = owner.getRegistrationDate();
        if (registered != null
                && java.time.temporal.ChronoUnit.DAYS.between(registered, java.time.LocalDate.now()) > 365) {
            points += 3;
        }
        return points;
    }

    /** Membership level (1 to 4) mapped from {@code owner}'s points. */
    public static int levelOf(Owner owner) {
        return levelOfPoints(pointsOf(owner));
    }

    /** Maps a points total to its level band. */
    public static int levelOfPoints(int points) {
        if (points <= 1) {
            return 1;
        }
        if (points <= 3) {
            return 2;
        }
        if (points <= 5) {
            return 3;
        }
        return 4;
    }
}
