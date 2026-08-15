package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted once per successful owner create, serialized to a single
 * JSON line on the dedicated {@code AUDIT} logger by {@link AuditOwnerCreated}.
 *
 * <p>Shape (schema version 2):
 * {@code {seq, ownerId, memberId, membershipLevel, schemaVersion:2, event:'OWNER_CREATED'}}.
 *
 * <ul>
 *   <li>{@code seq} — a process-wide, monotonically increasing sequence across all creates, so the
 *       relative order of events is recoverable from the log.</li>
 *   <li>{@code memberId} — the owner's <em>current primary identifier</em>, the version-2 unified
 *       {@code memberId} carried under the owner's {@code identity} (set via the single
 *       {@code AuditOwnerCreated#primaryIdentifier} accessor).</li>
 *   <li>{@code schemaVersion} — the event schema version, fixed at {@value #SCHEMA_VERSION}.</li>
 * </ul>
 *
 * <p>A record: its components are final, so a published event cannot be mutated after the fact.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel,
        int schemaVersion, String event) {

    /** The constant discriminator carried by every owner-create event. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /** The current audit event schema version. */
    public static final int SCHEMA_VERSION = 2;

    /**
     * Builds an {@code OWNER_CREATED} event at the current {@link #SCHEMA_VERSION}; the
     * {@code schemaVersion} and {@code event} discriminator are fixed.
     */
    public OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel) {
        this(seq, ownerId, memberId, membershipLevel, SCHEMA_VERSION, OWNER_CREATED);
    }
}
