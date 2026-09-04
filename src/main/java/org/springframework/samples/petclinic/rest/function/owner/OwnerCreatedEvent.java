package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted on the dedicated {@code AUDIT} logger when an owner is
 * created. Serialised to JSON as {@code {seq, ownerId, customerCode, membershipLevel, event}}.
 *
 * <p>{@code seq} is a monotonically increasing sequence stamped across all creates, so events can be
 * totally ordered. {@code customerCode} carries the owner's <em>current primary identifier</em>: it is
 * the customerCode today and whatever replaces it later — see {@link AuditOwnerCreated#primaryIdentifier}
 * — so the event contract survives the identifier being unified into the memberId.
 *
 * <p>Being a record, every field is final and the event cannot be mutated after it is emitted.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, int membershipLevel,
        String event) {

    /** The marker naming this kind of event. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /** Builds an {@link #OWNER_CREATED} event; the {@code event} marker is fixed. */
    public OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, int membershipLevel) {
        this(seq, ownerId, customerCode, membershipLevel, OWNER_CREATED);
    }
}
