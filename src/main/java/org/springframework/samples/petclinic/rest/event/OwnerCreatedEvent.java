package org.springframework.samples.petclinic.rest.event;

/**
 * Immutable structured record of an owner creation, emitted to the {@code AUDIT} log alongside the
 * human-readable audit line. Each event carries a process-wide monotonically increasing
 * {@code seq}, the owner's generated {@code ownerId}, its current primary identifier and the
 * membership level, under the {@code OWNER_CREATED} event type.
 *
 * <p>The {@code primaryIdentifier} is the value that identifies the owner — the unified
 * {@code memberId} — and is serialized under the {@code memberId} key. Every event carries the audit
 * {@link #SCHEMA_VERSION schema version} so consumers can tell which shape they are reading.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String primaryIdentifier, Integer membershipLevel) {

    /** The event type discriminator carried in the serialized form. */
    public static final String EVENT = "OWNER_CREATED";

    /** The version of the audit event schema carried in the serialized form. */
    public static final int SCHEMA_VERSION = 2;

    /** Renders the event as a compact JSON object with fields in the documented order. */
    public String toJson() {
        return "{"
                + "\"schemaVersion\":" + SCHEMA_VERSION
                + ",\"seq\":" + seq
                + ",\"ownerId\":" + ownerId
                + ",\"memberId\":" + jsonString(primaryIdentifier)
                + ",\"membershipLevel\":" + membershipLevel
                + ",\"event\":" + jsonString(EVENT)
                + "}";
    }

    /** A JSON string literal for {@code value}, or the bare {@code null} literal when absent. */
    private static String jsonString(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder escaped = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '"' || c == '\\') {
                escaped.append('\\');
            }
            escaped.append(c);
        }
        return escaped.append('"').toString();
    }
}
