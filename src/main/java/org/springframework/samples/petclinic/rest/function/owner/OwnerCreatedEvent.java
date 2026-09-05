package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted once per successful owner create.
 *
 * <p>{@code customerCode} carries the owner's <em>current primary identifier</em>. Today that
 * is the {@link org.springframework.samples.petclinic.model.Owner#getCustomerCode() customerCode};
 * when the customerCode is later unified into the memberId, the primary identifier — and therefore
 * this field's value — becomes the memberId. See
 * {@link AuditOwnerCreated#primaryIdentifier(org.springframework.samples.petclinic.model.Owner)},
 * the single point that decides which identifier is primary.
 *
 * <p>Being a record, the event is deeply immutable once constructed.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode,
        Integer membershipLevel, String event) {

    public static final String OWNER_CREATED = "OWNER_CREATED";
}
