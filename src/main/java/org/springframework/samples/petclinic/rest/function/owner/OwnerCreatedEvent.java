package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event for a freshly created owner: a pure value type,
 * emitted as the JSON object
 * {@code {seq, ownerId, memberId, membershipLevel, event:"OWNER_CREATED"}} by
 * {@link AuditOwnerCreated}.
 *
 * <p>The {@code memberId} component carries the owner's
 * {@link org.springframework.samples.petclinic.model.Owner#getMemberId() memberId}.
 *
 * @param seq             monotonically increasing sequence number across all creates
 * @param ownerId         the created owner's id
 * @param memberId        the owner's unified member id
 * @param membershipLevel the owner's derived membership level at creation time
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel) {

    /** The fixed event type discriminator. */
    public static final String EVENT = "OWNER_CREATED";

    /** @return the event type, always {@value #EVENT}. */
    public String event() {
        return EVENT;
    }
}
