package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted on the dedicated {@code AUDIT} logger when an owner is
 * created. Serialised to JSON as {@code {seq, ownerId, memberId, membershipLevel, event}}.
 *
 * <p>{@code seq} is a monotonically increasing sequence stamped across all creates, so events can be
 * totally ordered. {@code memberId} carries the owner's primary identifier, the unified member id —
 * see {@link AuditOwnerCreated#primaryIdentifier}.
 *
 * <p>Being a record, every field is final and the event cannot be mutated after it is emitted.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, int membershipLevel,
        String event) {

    /** The marker naming this kind of event. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /** Builds an {@link #OWNER_CREATED} event; the {@code event} marker is fixed. */
    public OwnerCreatedEvent(long seq, Integer ownerId, String memberId, int membershipLevel) {
        this(seq, ownerId, memberId, membershipLevel, OWNER_CREATED);
    }
}
