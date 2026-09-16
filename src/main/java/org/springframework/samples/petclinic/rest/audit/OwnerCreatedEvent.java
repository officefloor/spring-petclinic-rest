package org.springframework.samples.petclinic.rest.audit;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured audit event recorded when an owner is created, serialized to JSON on the
 * {@code AUDIT} logger so downstream systems have a machine-readable trail alongside the
 * human-readable audit line.
 *
 * <p>{@code memberId} is the owner's <em>current</em> primary identifier. {@link #of} is the
 * single place that decides which identifier the event carries, and every emitted event follows.
 *
 * @param seq             monotonically increasing sequence number across creates
 * @param ownerId         the created owner's id
 * @param memberId        the owner's current primary identifier
 * @param membershipLevel the owner's derived membership level (1-4)
 * @param event           the event marker, always {@link #OWNER_CREATED}
 */
public record OwnerCreatedEvent(long seq, int ownerId, String memberId, int membershipLevel, String event) {

    /** The event marker distinguishing an owner-created event in the audit stream. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /**
     * The owner-created event for {@code owner}, carrying its current primary identifier.
     *
     * @param seq             the sequence number for this event
     * @param owner           the just-saved owner (its id is already assigned)
     * @param membershipLevel the owner's derived membership level
     * @return the immutable event
     */
    public static OwnerCreatedEvent of(long seq, Owner owner, int membershipLevel) {
        return new OwnerCreatedEvent(seq, owner.getId(), owner.getMemberId(), membershipLevel, OWNER_CREATED);
    }
}
