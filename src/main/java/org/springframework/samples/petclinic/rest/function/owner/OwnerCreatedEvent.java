package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted once per successful owner create (see
 * {@link AuditOwnerCreated}). Serialized to a single JSON object on the {@code AUDIT}
 * logger, alongside the human-readable audit line.
 *
 * <p>{@code customerCode} carries the owner's <em>current primary identifier</em>.
 * Today that is the {@code customerCode}, and the JSON field is named to match (see
 * {@link AuditOwnerCreated#primaryIdentifier}). When the primary identifier is later
 * unified into the {@code memberId}, flipping {@code primaryIdentifier} changes the
 * value carried — rename this component to {@code memberId} at the same time so the
 * field name and the value it holds stay coherent.
 *
 * <p>The {@code event} discriminator is intrinsic to this type: callers use the
 * four-argument constructor and the record fills {@code event} itself, so the event
 * can never be constructed with an inconsistent marker. Being a record, every field is
 * final and set once at construction — the event cannot be mutated after it is published.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode,
        Integer membershipLevel, String event) {

    /** The fixed event marker for an owner create. */
    private static final String OWNER_CREATED = "OWNER_CREATED";

    /** Builds an owner-create event, stamping the fixed {@code OWNER_CREATED} marker. */
    public OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, Integer membershipLevel) {
        this(seq, ownerId, customerCode, membershipLevel, OWNER_CREATED);
    }
}
