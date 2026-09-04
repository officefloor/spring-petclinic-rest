package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted on the dedicated {@code AUDIT} logger when an owner is
 * created. Serialised to JSON as
 * {@code {seq, schemaVersion, ownerId, memberId, membershipLevel, event}}.
 *
 * <p>{@code seq} is a monotonically increasing sequence stamped across all creates, so events can be
 * totally ordered. {@code schemaVersion} is the audit schema version — fixed at {@link #SCHEMA_VERSION}
 * (2) so consumers can tell the version-2 shape apart. {@code memberId} carries the owner's primary
 * identifier, the unified member id (now version-2 derived) — see
 * {@link AuditOwnerCreated#primaryIdentifier}.
 *
 * <p>Being a record, every field is final and the event cannot be mutated after it is emitted.
 */
public record OwnerCreatedEvent(long seq, int schemaVersion, Integer ownerId, String memberId,
        int membershipLevel, String event) {

    /** The marker naming this kind of event. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /** The audit event schema version. Version 2 adds this {@code schemaVersion} field. */
    public static final int SCHEMA_VERSION = 2;

    /**
     * Builds an {@link #OWNER_CREATED} event at schema version {@link #SCHEMA_VERSION}; the
     * {@code schemaVersion} and {@code event} marker are fixed.
     */
    public OwnerCreatedEvent(long seq, Integer ownerId, String memberId, int membershipLevel) {
        this(seq, SCHEMA_VERSION, ownerId, memberId, membershipLevel, OWNER_CREATED);
    }
}
