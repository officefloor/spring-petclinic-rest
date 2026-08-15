package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted once per successful owner create, serialized to a single
 * JSON line on the dedicated {@code AUDIT} logger by {@link AuditOwnerCreated}.
 *
 * <p>Shape: {@code {seq, ownerId, memberId, membershipLevel, event:'OWNER_CREATED'}}.
 *
 * <ul>
 *   <li>{@code seq} — a process-wide, monotonically increasing sequence across all creates, so the
 *       relative order of events is recoverable from the log.</li>
 *   <li>{@code memberId} — the owner's <em>current primary identifier</em>, the unified
 *       {@code memberId} (set via the single {@code AuditOwnerCreated#primaryIdentifier} accessor).</li>
 * </ul>
 *
 * <p>A record: its components are final, so a published event cannot be mutated after the fact.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel,
        String event) {

    /** The constant discriminator carried by every owner-create event. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /** Builds an {@code OWNER_CREATED} event; the {@code event} discriminator is fixed. */
    public OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel) {
        this(seq, ownerId, memberId, membershipLevel, OWNER_CREATED);
    }
}
