package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured audit event emitted when an owner is created.
 *
 * <p>Serialised to JSON as {@code {schemaVersion, seq, ownerId, memberId, membershipLevel, ownerSegment,
 * event}} — the record component order is the JSON field order. This is schema version 2: the leading
 * {@code schemaVersion} of {@value #SCHEMA_VERSION} carries the audit contract version, and the
 * {@code ownerSegment} is recomputed from the version-2 identity. {@code seq} is a monotonically
 * increasing integer across creates.
 *
 * <p>The {@code memberId} component carries the owner's primary (version-2) identifier, sourced from
 * {@link #from(int, Owner)} via {@link Owner#getMemberId()}.
 */
public record OwnerCreatedEvent(int schemaVersion, int seq, int ownerId, String memberId,
        int membershipLevel, String ownerSegment, String event) {

    /** The {@code event} discriminator carried by every owner-created event. */
    public static final String EVENT_TYPE = "OWNER_CREATED";

    /** The audit schema version carried by every owner-created event. */
    public static final int SCHEMA_VERSION = 2;

    /**
     * Builds the event for a freshly created owner, stamping it with the schema version, the given sequence
     * number, the owner's current primary identifier and its owner segment recomputed from the version-2
     * identity.
     */
    public static OwnerCreatedEvent from(int seq, Owner owner) {
        return new OwnerCreatedEvent(SCHEMA_VERSION, seq, owner.getId(), owner.getMemberId(),
                owner.getMembershipLevel(), owner.getOwnerSegment(), EVENT_TYPE);
    }
}
