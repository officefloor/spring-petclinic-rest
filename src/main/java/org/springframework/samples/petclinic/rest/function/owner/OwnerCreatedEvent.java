package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event, emitted as JSON to the {@code AUDIT} logger alongside the
 * human-readable audit line whenever an owner is created.
 *
 * <p>Carries the owner id and the owner's primary identifier, the version-2 {@code memberId}, along
 * with the owner segment recomputed from the version-2 identity.
 *
 * <p>Schema version 2: {@code schemaVersion} is a fixed integer {@code 2} identifying this event
 * shape. {@code seq} is a monotonically increasing integer across creates. Being a record, an event
 * cannot be mutated after construction.
 */
public record OwnerCreatedEvent(int schemaVersion, long seq, Integer ownerId, String memberId,
        Integer membershipLevel, String ownerSegment, String event) {

    /** The single event type this record represents. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /** The current audit event schema version. */
    public static final int SCHEMA_VERSION = 2;
}
