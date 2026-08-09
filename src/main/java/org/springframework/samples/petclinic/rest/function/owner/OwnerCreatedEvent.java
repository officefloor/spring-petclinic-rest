package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event, emitted as JSON to the {@code AUDIT} logger alongside the
 * human-readable audit line whenever an owner is created.
 *
 * <p>Carries the owner id and the owner's primary identifier, the unified {@code memberId}.
 *
 * <p>{@code seq} is a monotonically increasing integer across creates. Being a record, an event
 * cannot be mutated after construction.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId,
        Integer membershipLevel, String event) {

    /** The single event type this record represents. */
    public static final String OWNER_CREATED = "OWNER_CREATED";
}
