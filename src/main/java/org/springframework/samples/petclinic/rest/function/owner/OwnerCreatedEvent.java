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
 * <p>The {@code event} discriminator and the {@code schemaVersion} are intrinsic to this
 * type: callers use the four-argument constructor and the record fills {@code event} and
 * {@code schemaVersion} itself, so the event can never be constructed with an inconsistent
 * marker or an out-of-date schema version. The event is at schema version 2, so
 * {@code schemaVersion} is always {@code 2}. Being a record, every field is final and set
 * once at construction — the event cannot be mutated after it is published.
 */
public record OwnerCreatedEvent(long seq, int schemaVersion, Integer ownerId, String memberId,
        Integer membershipLevel, String event) {

    /** The fixed event marker for an owner create. */
    private static final String OWNER_CREATED = "OWNER_CREATED";

    /** The current audit-event schema version. */
    private static final int SCHEMA_VERSION = 2;

    /** Builds an owner-create event, stamping the fixed {@code OWNER_CREATED} marker and the
     *  current {@code schemaVersion} of 2. */
    public OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel) {
        this(seq, SCHEMA_VERSION, ownerId, memberId, membershipLevel, OWNER_CREATED);
    }
}
