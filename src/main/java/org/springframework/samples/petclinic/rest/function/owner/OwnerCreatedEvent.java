package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured audit event recording the creation of an {@link Owner}.
 *
 * <p>Beyond the human-readable audit line, this event is emitted as JSON on the
 * dedicated {@code AUDIT} logger so downstream consumers can parse it reliably.
 *
 * <p>{@code seq} is a monotonically increasing sequence number across creates.
 * {@code customerCode} carries the owner's <em>current primary identifier</em>.
 * Today that is the customer code; when the customer code is later unified into
 * the member id, {@link #of(long, Owner)} is the single place that changes so the
 * event then carries the member id instead.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, Integer membershipLevel,
        String event) {

    /** Marker value for the {@code event} field. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /**
     * Builds the event for a freshly created owner. This is the single site that
     * resolves the owner's primary identifier, so a future switch to the member id
     * only touches this method.
     */
    public static OwnerCreatedEvent of(long seq, Owner owner) {
        return new OwnerCreatedEvent(seq, owner.getId(), owner.getCustomerCode(), owner.getMembershipLevel(),
                OWNER_CREATED);
    }
}
