package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event for a freshly created owner: a pure value type,
 * emitted as the JSON object
 * {@code {seq, ownerId, customerCode, membershipLevel, event:"OWNER_CREATED"}} by
 * {@link AuditOwnerCreated}.
 *
 * <p>The {@code customerCode} component carries the owner's <em>current primary
 * identifier</em>. Today that is the owner's customerCode; when the customerCode is
 * later unified into the memberId the same component carries the memberId instead,
 * because the value is sourced from
 * {@link org.springframework.samples.petclinic.model.Owner#getPrimaryIdentifier()}.
 * The JSON key stays {@code customerCode} per the event spec.
 *
 * @param seq             monotonically increasing sequence number across all creates
 * @param ownerId         the created owner's id
 * @param customerCode    the owner's current primary identifier
 * @param membershipLevel the owner's derived membership level at creation time
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, Integer membershipLevel) {

    /** The fixed event type discriminator. */
    public static final String EVENT = "OWNER_CREATED";

    /** @return the event type, always {@value #EVENT}. */
    public String event() {
        return EVENT;
    }
}
