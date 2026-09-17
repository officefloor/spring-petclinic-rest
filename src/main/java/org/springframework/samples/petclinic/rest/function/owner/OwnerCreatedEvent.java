package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted when an owner is created, serialized to JSON as
 * {@code {seq, ownerId, memberId, membershipLevel, event}}.
 *
 * <p>{@code seq} is monotonically increasing across creates (see {@link OwnerCreatedEventSequence}).
 * {@code memberId} carries the owner's current primary identifier (see
 * {@link org.springframework.samples.petclinic.model.OwnerPrimaryIdentifier}), the unified
 * {@code <REGION><FY><HASH8><CHK>} identity.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, int membershipLevel,
        String event) {

    /** The discriminator value carried by every owner-created event. */
    public static final String EVENT = "OWNER_CREATED";

    public OwnerCreatedEvent(long seq, Integer ownerId, String memberId, int membershipLevel) {
        this(seq, ownerId, memberId, membershipLevel, EVENT);
    }
}
