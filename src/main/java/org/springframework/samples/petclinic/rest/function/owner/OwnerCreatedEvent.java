package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.IdentityVersion;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerSegment;

/**
 * Immutable structured audit event recording the creation of an {@link Owner}.
 *
 * <p>Beyond the human-readable audit line, this event is emitted as JSON on the
 * dedicated {@code AUDIT} logger so downstream consumers can parse it reliably.
 *
 * <p>{@code seq} is a monotonically increasing sequence number across creates.
 * {@code memberId} carries the owner's primary identifier — the version-2 unified member id.
 * {@code ownerSegment} is the owner's segment recomputed from the version-2 identity (its AREA
 * still derives from the plain region, so the {@code V2} tag never leaks into it).
 * {@code schemaVersion} pins this event to schema version 2.
 * {@link #of(long, Owner)} is the single place that resolves them.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel,
        String ownerSegment, int schemaVersion, String event) {

    /** Marker value for the {@code event} field. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /**
     * Builds the event for a freshly created owner. This is the single site that
     * resolves the owner's primary identifier and recomputes the owner segment.
     */
    public static OwnerCreatedEvent of(long seq, Owner owner) {
        return new OwnerCreatedEvent(seq, owner.getId(), owner.getMemberId(), owner.getMembershipLevel(),
                OwnerSegment.of(owner), IdentityVersion.SCHEMA_VERSION, OWNER_CREATED);
    }
}
