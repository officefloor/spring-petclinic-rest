package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured event recorded on successful owner creation, emitted as JSON on
 * the dedicated {@code AUDIT} logger by {@link EmitOwnerCreatedEvent}.
 *
 * <p>Beyond the human-readable audit line, this is the machine-readable record of the
 * create. It carries the owner's <em>current primary identifier</em> — today the
 * {@code customerCode}. When the customerCode is later unified into a {@code memberId},
 * {@link #primaryIdentifier(Owner)} is the single point that switches, and the event then
 * carries the memberId instead.
 *
 * @param seq             monotonically increasing sequence number across creates
 * @param ownerId         the generated owner id
 * @param customerCode    the owner's current primary identifier (see above)
 * @param membershipLevel the owner's assigned membership level
 * @param event           the event marker, always {@link #OWNER_CREATED}
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode,
        Integer membershipLevel, String event) {

    /** Event marker for a successful owner create. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /**
     * The owner's current primary identifier. Today the {@code customerCode}; when the
     * customerCode is unified into the {@code memberId} this method returns the memberId,
     * and the emitted event carries that instead — the one place that changes.
     */
    public static String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }

    /** Builds the event for the given create, stamping the next sequence number. */
    public static OwnerCreatedEvent of(long seq, Owner owner) {
        return new OwnerCreatedEvent(seq, owner.getId(), primaryIdentifier(owner),
                owner.getMembershipLevel(), OWNER_CREATED);
    }
}
