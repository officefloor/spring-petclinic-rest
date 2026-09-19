package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured audit event emitted when an owner is created, serialised to JSON on the
 * {@code AUDIT} logger by {@link EmitOwnerCreatedEvent}.
 *
 * <p>This is schema version {@value #SCHEMA_VERSION}: every event carries its {@code schemaVersion} so
 * consumers can tell version-2 events apart. The {@code memberId} field carries the owner's
 * {@link Owner#getPrimaryIdentifier() primary identifier} (now a version-2 identifier), and
 * {@code ownerSegment} its {@link Owner#getOwnerSegment() market segment} recomputed under the
 * version-2 identity. Sourcing both from those accessors keeps the event aligned with the owner.
 */
public record OwnerCreatedEvent(int schemaVersion, long seq, Integer ownerId, String memberId,
        Integer membershipLevel, String ownerSegment, String event) {

    /** The event-type marker carried by every owner-created event. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /** The schema version stamped on every owner-created event. */
    public static final int SCHEMA_VERSION = 2;

    /** Build the event for a freshly created, persisted {@code owner}, stamped with {@code seq}. */
    public static OwnerCreatedEvent of(long seq, Owner owner) {
        return new OwnerCreatedEvent(SCHEMA_VERSION, seq, owner.getId(), owner.getPrimaryIdentifier(),
                owner.getMembershipLevel(), owner.getOwnerSegment(), OWNER_CREATED);
    }
}
