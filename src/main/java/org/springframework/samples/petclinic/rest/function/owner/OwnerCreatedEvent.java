package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted once per successful owner create (see
 * {@link AuditOwnerCreated}). Serialized to a single JSON object on the {@code AUDIT}
 * logger, alongside the human-readable audit line.
 *
 * <p>{@code memberId} carries the owner's <em>current primary identifier</em> — the
 * unified member id resolved by {@link AuditOwnerCreated#primaryIdentifier} — and the
 * JSON field is named to match.
 *
 * <p>The {@code event} discriminator is intrinsic to this type: callers use the
 * four-argument constructor and the record fills {@code event} itself, so the event
 * can never be constructed with an inconsistent marker. Being a record, every field is
 * final and set once at construction — the event cannot be mutated after it is published.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId,
        Integer membershipLevel, String event) {

    /** The fixed event marker for an owner create. */
    private static final String OWNER_CREATED = "OWNER_CREATED";

    /** Builds an owner-create event, stamping the fixed {@code OWNER_CREATED} marker. */
    public OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel) {
        this(seq, ownerId, memberId, membershipLevel, OWNER_CREATED);
    }
}
