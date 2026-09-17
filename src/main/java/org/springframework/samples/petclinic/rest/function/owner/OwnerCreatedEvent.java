package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted when an owner is created, serialized to JSON as
 * {@code {seq, ownerId, customerCode, membershipLevel, event}}.
 *
 * <p>{@code seq} is monotonically increasing across creates (see {@link OwnerCreatedEventSequence}).
 * {@code customerCode} carries the owner's current primary identifier (see
 * {@link org.springframework.samples.petclinic.model.OwnerPrimaryIdentifier}), so the event follows
 * that identifier when the customer code is later unified into the memberId.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, int membershipLevel,
        String event) {

    /** The discriminator value carried by every owner-created event. */
    public static final String EVENT = "OWNER_CREATED";

    public OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, int membershipLevel) {
        this(seq, ownerId, customerCode, membershipLevel, EVENT);
    }
}
