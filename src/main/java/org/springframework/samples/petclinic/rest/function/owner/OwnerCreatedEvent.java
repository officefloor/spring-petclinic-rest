package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted once per successful owner create.
 *
 * <p>{@code memberId} carries the owner's primary identifier, the unified
 * {@link org.springframework.samples.petclinic.model.Owner#getMemberId() memberId}. See
 * {@link AuditOwnerCreated#primaryIdentifier(org.springframework.samples.petclinic.model.Owner)},
 * the single point that decides which identifier is primary.
 *
 * <p>Being a record, the event is deeply immutable once constructed.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId,
        Integer membershipLevel, String event) {

    public static final String OWNER_CREATED = "OWNER_CREATED";
}
