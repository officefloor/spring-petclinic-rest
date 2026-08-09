package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event, emitted as JSON to the {@code AUDIT} logger alongside the
 * human-readable audit line whenever an owner is created.
 *
 * <p>Carries the owner id and the owner's <em>current primary identifier</em>. Today that primary
 * identifier is the {@code customerCode}; when the {@code customerCode} is later unified into the
 * {@code memberId}, the producer sources the identifier from that field instead and the event
 * automatically carries the {@code memberId}.
 *
 * <p>{@code seq} is a monotonically increasing integer across creates. Being a record, an event
 * cannot be mutated after construction.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode,
        Integer membershipLevel, String event) {

    /** The single event type this record represents. */
    public static final String OWNER_CREATED = "OWNER_CREATED";
}
