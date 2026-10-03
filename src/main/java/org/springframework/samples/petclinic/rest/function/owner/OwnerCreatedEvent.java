package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event for a freshly created owner: a pure value type,
 * emitted as the schema-version-2 JSON object
 * {@code {schemaVersion:2, seq, ownerId, memberId, ownerSegment, membershipLevel,
 * event:"OWNER_CREATED"}} by {@link AuditOwnerCreated}.
 *
 * <p>The {@code memberId} component carries the owner's version-2
 * {@link org.springframework.samples.petclinic.model.Owner#getMemberId() memberId}, and
 * {@code ownerSegment} the segment recomputed from that version-2 identity.
 *
 * @param seq             monotonically increasing sequence number across all creates
 * @param ownerId         the created owner's id
 * @param memberId        the owner's unified member id
 * @param ownerSegment    the owner's segment, recomputed from the version-2 identity
 * @param membershipLevel the owner's derived membership level at creation time
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, String ownerSegment,
        Integer membershipLevel) {

    /** The fixed event type discriminator. */
    public static final String EVENT = "OWNER_CREATED";

    /** The audit event schema version. Version 2 groups the owner's version-2 identity
     *  and adds this discriminator and the recomputed {@link #ownerSegment()}. */
    public static final int SCHEMA_VERSION = 2;

    /** @return the event type, always {@value #EVENT}. */
    public String event() {
        return EVENT;
    }

    /** @return the audit schema version, always {@value #SCHEMA_VERSION}. */
    public int schemaVersion() {
        return SCHEMA_VERSION;
    }
}
