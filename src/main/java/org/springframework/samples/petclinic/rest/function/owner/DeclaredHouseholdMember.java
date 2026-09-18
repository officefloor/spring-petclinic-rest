package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Pipeline marker published by {@link EnsureUniqueHousehold} when a create-owner request opted into
 * sharing a household that already has a member. Its presence tells {@link DetectPossibleDuplicate}
 * that the owner is a declared household member and so must not be flagged as a possible duplicate —
 * a deliberately declared member is not a suspected one. Absent (the variable stays {@code null})
 * for every other owner.
 */
enum DeclaredHouseholdMember {
    INSTANCE
}
