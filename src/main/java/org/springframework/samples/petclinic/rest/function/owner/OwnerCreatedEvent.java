package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted on a successful owner create, alongside the human-readable
 * audit line. Serialised to a compact JSON object
 * {@code {seq, ownerId, customerCode, membershipLevel, event:'OWNER_CREATED'}} and published to the
 * dedicated {@code AUDIT} logger by {@link AuditOwnerCreated}.
 *
 * <p>The {@code seq} is a monotonically increasing sequence across creates. The {@code identifier} is
 * the owner's <em>current primary identifier</em>: today the {@code customerCode}, and whatever
 * replaces it later — when the {@code customerCode} is unified into the {@code memberId}, this event
 * carries the {@code memberId} instead. It is serialised under the {@code customerCode} key.
 *
 * <p>The record is deliberately immutable: once constructed its fields never change, so an event that
 * has been emitted cannot be retroactively altered.
 */
public record OwnerCreatedEvent(long seq, int ownerId, String identifier, int membershipLevel) {

    /** The event marker. */
    public static final String EVENT = "OWNER_CREATED";

    /** This event as a compact JSON object string. */
    public String toJson() {
        return "{"
                + "\"seq\":" + this.seq
                + ",\"ownerId\":" + this.ownerId
                + ",\"customerCode\":" + quote(this.identifier)
                + ",\"membershipLevel\":" + this.membershipLevel
                + ",\"event\":\"" + EVENT + "\""
                + "}";
    }

    private static String quote(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
