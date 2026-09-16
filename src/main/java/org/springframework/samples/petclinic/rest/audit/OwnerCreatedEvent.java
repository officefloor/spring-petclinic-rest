package org.springframework.samples.petclinic.rest.audit;

import org.springframework.samples.petclinic.model.IdentityVersion;
import org.springframework.samples.petclinic.model.MemberId;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerSegment;

/**
 * Immutable structured audit event recorded when an owner is created, serialized to JSON on the
 * {@code AUDIT} logger so downstream systems have a machine-readable trail alongside the
 * human-readable audit line.
 *
 * <p>Schema version 2: the event now carries a {@code schemaVersion} and an {@code ownerSegment}
 * recomputed from the owner's version-2 identity — its segment derived from the region embedded in
 * the version-2 {@code memberId} (see {@link MemberId#region}), not the plain response locality.
 *
 * <p>{@code memberId} is the owner's <em>current</em> primary identifier. {@link #of} is the
 * single place that decides which identifier the event carries, and every emitted event follows.
 *
 * @param seq             monotonically increasing sequence number across creates
 * @param ownerId         the created owner's id
 * @param memberId        the owner's current primary identifier
 * @param membershipLevel the owner's derived membership level (1-4)
 * @param ownerSegment    the owner's segment, recomputed from the version-2 identity
 * @param schemaVersion   the audit event schema version, always {@link IdentityVersion#NUMBER}
 * @param event           the event marker, always {@link #OWNER_CREATED}
 */
public record OwnerCreatedEvent(long seq, int ownerId, String memberId, int membershipLevel,
        String ownerSegment, int schemaVersion, String event) {

    /** The event marker distinguishing an owner-created event in the audit stream. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /**
     * The owner-created event for {@code owner}, carrying its current primary identifier and a
     * segment recomputed from its version-2 identity.
     *
     * @param seq             the sequence number for this event
     * @param owner           the just-saved owner (its id is already assigned)
     * @param membershipLevel the owner's derived membership level
     * @return the immutable event
     */
    public static OwnerCreatedEvent of(long seq, Owner owner, int membershipLevel) {
        String ownerSegment = OwnerSegment.of(membershipLevel, MemberId.region(owner.getMemberId()));
        return new OwnerCreatedEvent(seq, owner.getId(), owner.getMemberId(), membershipLevel,
                ownerSegment, IdentityVersion.NUMBER, OWNER_CREATED);
    }
}
