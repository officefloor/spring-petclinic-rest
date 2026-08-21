package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted when an owner is created. Serialized to
 * JSON and logged to the dedicated {@code AUDIT} logger by {@link AuditOwnerCreated}.
 *
 * <p>{@code identifier} carries the owner's primary identifier — the unified
 * {@code memberId}. The field name in the emitted JSON tracks that identifier (see
 * {@link AuditOwnerCreated}).
 *
 * <p>Being a record, an instance cannot be mutated once constructed.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String identifier,
        Integer membershipLevel, String event) {

    /** The single event type this record represents. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    public OwnerCreatedEvent(long seq, Integer ownerId, String identifier, Integer membershipLevel) {
        this(seq, ownerId, identifier, membershipLevel, OWNER_CREATED);
    }
}
