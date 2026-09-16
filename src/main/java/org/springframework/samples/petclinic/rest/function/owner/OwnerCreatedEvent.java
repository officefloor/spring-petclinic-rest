package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event recording that an owner was created. Serialised to JSON
 * and emitted to the {@code AUDIT} logger by {@link OwnerEventPublisher}.
 *
 * <p>{@code memberId} carries the owner's <em>primary identifier</em>
 * (see {@link org.springframework.samples.petclinic.model.Owner#getPrimaryIdentifier()}),
 * so it follows the identifier automatically if it is later changed. As of schema version
 * {@link #SCHEMA_VERSION 2} the event also carries the owner segment, recomputed from the
 * version-2 identity, and the {@code schemaVersion} marker itself.
 *
 * @param seq             monotonically increasing sequence, unique across creates
 * @param ownerId         the created owner's generated id
 * @param memberId        the owner's primary identifier at creation time
 * @param membershipLevel the owner's computed membership level at creation time
 * @param ownerSegment    the owner's segment, recomputed from the version-2 identity
 * @param schemaVersion   the audit event schema version, always {@link #SCHEMA_VERSION}
 * @param event           the event type, always {@link #EVENT_TYPE}
 */
public record OwnerCreatedEvent(long seq, int ownerId, String memberId, int membershipLevel,
        String ownerSegment, int schemaVersion, String event) {

    /** The {@code event} discriminator for an owner-created event. */
    public static final String EVENT_TYPE = "OWNER_CREATED";

    /** The current audit event schema version. */
    public static final int SCHEMA_VERSION = 2;
}
