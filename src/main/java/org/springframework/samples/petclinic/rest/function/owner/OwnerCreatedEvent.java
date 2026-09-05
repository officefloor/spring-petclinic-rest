package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted once per successful owner create.
 *
 * <p>This is schema version 2: it carries a {@code schemaVersion} of {@code 2} and the owner segment
 * recomputed for the version-2 owner. {@code memberId} carries the owner's primary identifier, the
 * unified version-2 {@link org.springframework.samples.petclinic.model.Owner#getMemberId() memberId}.
 * See {@link AuditOwnerCreated#primaryIdentifier(org.springframework.samples.petclinic.model.Owner)},
 * the single point that decides which identifier is primary.
 *
 * <p>Being a record, the event is deeply immutable once constructed.
 */
public record OwnerCreatedEvent(int schemaVersion, long seq, Integer ownerId, String memberId,
        Integer membershipLevel, String ownerSegment, String event) {

    public static final String OWNER_CREATED = "OWNER_CREATED";

    /** The current audit event schema version. */
    public static final int SCHEMA_VERSION = 2;
}
