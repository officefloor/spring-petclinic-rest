package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted on {@code POST /api/owners} once an owner has been created,
 * serialized to JSON on the {@code AUDIT} logger alongside the human-readable audit line.
 *
 * <p>The {@code seq} is a monotonically increasing integer across all creates in the running process,
 * so events can be totally ordered. The {@code memberId} carries the owner's current
 * {@link org.springframework.samples.petclinic.model.OwnerPrimaryIdentifier primary identifier} - the
 * unified member id that replaced the former customer code and membership number, now in its
 * {@linkplain org.springframework.samples.petclinic.model.IdentityVersion version-2} form.
 *
 * <p>The {@code schemaVersion} pins the event to audit schema version 2, carrying the same
 * {@link org.springframework.samples.petclinic.model.IdentityVersion#VERSION version} as the response's
 * {@code apiVersion}, so a consumer can tell version-2 events (and version-2 identifiers) apart from the
 * version-1 ones.
 *
 * <p>Record components are declared in the event's canonical field order:
 * {@code {seq, ownerId, memberId, membershipLevel, schemaVersion, event}}.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, int membershipLevel,
        int schemaVersion, String event) {

    /** The fixed {@code event} discriminator for an owner-created event. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    public OwnerCreatedEvent(long seq, Integer ownerId, String memberId, int membershipLevel) {
        this(seq, ownerId, memberId, membershipLevel,
                org.springframework.samples.petclinic.model.IdentityVersion.VERSION, OWNER_CREATED);
    }
}
