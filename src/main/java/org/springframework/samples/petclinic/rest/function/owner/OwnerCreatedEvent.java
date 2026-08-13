package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted on successful owner creation, alongside the human-readable
 * audit line. Serialized to a single-line JSON object on the {@code AUDIT} logger:
 * {@code {seq, ownerId, memberId, membershipLevel, event:"OWNER_CREATED", schemaVersion:2}}.
 *
 * <p>{@code schemaVersion} is 2: the memberId it carries is a version-2 identifier (see
 * {@link AssignOwnerMemberId}).
 *
 * <p>{@code memberId} carries the owner's <em>primary identifier</em> — the unified
 * {@link org.springframework.samples.petclinic.model.Owner#getMemberId() memberId}. See
 * {@link AuditOwnerCreated} where the identifier is resolved in one place.
 *
 * <p>A record so the emitted event is immutable: once constructed its fields cannot change.
 */
public record OwnerCreatedEvent(long seq, int ownerId, String memberId, Integer membershipLevel,
        String event, int schemaVersion) {

    /** The only event kind emitted here. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /** Audit schema version of this event; the version-2 identity release carries schema version 2. */
    public static final int SCHEMA_VERSION = 2;

    public OwnerCreatedEvent(long seq, int ownerId, String memberId, Integer membershipLevel) {
        this(seq, ownerId, memberId, membershipLevel, OWNER_CREATED, SCHEMA_VERSION);
    }

    /** Render as a compact, single-line JSON object. Field values here are simple identifiers and an
     *  integer level, so no characters need JSON string escaping. */
    public String toJson() {
        StringBuilder json = new StringBuilder(96);
        json.append("{\"seq\":").append(this.seq)
                .append(",\"ownerId\":").append(this.ownerId)
                .append(",\"memberId\":").append(quote(this.memberId))
                .append(",\"membershipLevel\":").append(this.membershipLevel)
                .append(",\"event\":").append(quote(this.event))
                .append(",\"schemaVersion\":").append(this.schemaVersion)
                .append('}');
        return json.toString();
    }

    private static String quote(String value) {
        return value == null ? "null" : "\"" + value + "\"";
    }
}
