package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted once per successful owner create, serialized to a single
 * JSON line on the dedicated {@code AUDIT} logger by {@link AuditOwnerCreated}.
 *
 * <p>Shape: {@code {seq, ownerId, customerCode, membershipLevel, event:'OWNER_CREATED'}}.
 *
 * <ul>
 *   <li>{@code seq} — a process-wide, monotonically increasing sequence across all creates, so the
 *       relative order of events is recoverable from the log.</li>
 *   <li>{@code customerCode} — the owner's <em>current primary identifier</em>. Today that is the
 *       {@code customerCode}; when the customerCode is later unified into the memberId, the single
 *       {@code AuditOwnerCreated#primaryIdentifier} accessor changes and this field then carries the
 *       memberId instead.</li>
 * </ul>
 *
 * <p>A record: its components are final, so a published event cannot be mutated after the fact.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, Integer membershipLevel,
        String event) {

    /** The constant discriminator carried by every owner-create event. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /** Builds an {@code OWNER_CREATED} event; the {@code event} discriminator is fixed. */
    public OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, Integer membershipLevel) {
        this(seq, ownerId, customerCode, membershipLevel, OWNER_CREATED);
    }
}
