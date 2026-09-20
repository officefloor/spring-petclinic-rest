package org.springframework.samples.petclinic.model;

/**
 * The owner's membership level: a number from 1 to 3 fixed at creation. It starts at
 * {@link #BASE}, gains a level when an email address is present and another when the owner
 * has no namesakes ({@code namesakeCount} is 0), and is capped at {@link #MAX}. Level 4 is
 * reserved for tenure.
 */
public final class MembershipLevel {

    /** The starting level, before any bonuses. */
    public static final int BASE = 1;

    /** The highest level attainable at creation; level 4 is reserved for tenure. */
    public static final int MAX = 3;

    private MembershipLevel() {
    }

    /**
     * The membership level for the given owner: {@link #BASE}, plus one when an email
     * address is present and one when the owner has no namesakes, capped at {@link #MAX}.
     */
    public static int of(Owner owner) {
        int level = BASE;
        if (owner.hasEmail()) {
            level++;
        }
        if (Integer.valueOf(0).equals(owner.getNamesakeCount())) {
            level++;
        }
        return Math.min(level, MAX);
    }
}
