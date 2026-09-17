package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted when an owner is created (schema version 2), serialized
 * to JSON as
 * {@code {schemaVersion, seq, ownerId, memberId, membershipLevel, ownerSegment, event}}.
 *
 * <p>{@code schemaVersion} tracks {@link OwnerIdentityVersion#VERSION}. {@code seq} is monotonically
 * increasing across creates (see {@link OwnerCreatedEventSequence}). {@code memberId} carries the
 * owner's current primary identifier (see
 * {@link org.springframework.samples.petclinic.model.OwnerPrimaryIdentifier}), the version-2
 * {@code <REGION><FY><HASH8><CHK>} identity. {@code ownerSegment} is the owner's marketing segment
 * (see {@link org.springframework.samples.petclinic.model.OwnerSegment}) recomputed for this event.
 */
public record OwnerCreatedEvent(int schemaVersion, long seq, Integer ownerId, String memberId,
        int membershipLevel, String ownerSegment, String event) {

    /** The discriminator value carried by every owner-created event. */
    public static final String EVENT = "OWNER_CREATED";

    /** The audit event schema version, tracking {@link OwnerIdentityVersion#VERSION}. */
    public static final int SCHEMA_VERSION = OwnerIdentityVersion.VERSION;

    public OwnerCreatedEvent(long seq, Integer ownerId, String memberId, int membershipLevel,
            String ownerSegment) {
        this(SCHEMA_VERSION, seq, ownerId, memberId, membershipLevel, ownerSegment, EVENT);
    }
}
