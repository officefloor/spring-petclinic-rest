package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted once per successful owner create, rendered as the JSON
 * object {@code {seq, schemaVersion, ownerId, memberId, membershipLevel, event:'OWNER_CREATED'}}.
 *
 * <p>{@code schemaVersion} is the event schema version, fixed at {@link #SCHEMA_VERSION 2} for the
 * version-2 identity release. {@code seq} is a monotonically increasing integer across creates.
 * {@code memberId} carries the owner's <em>primary identifier</em> (see {@link
 * org.springframework.samples.petclinic.model.Owner#getPrimaryIdentifier()}) — the unified,
 * version-2 identity formatted {@code <REGION><FY><HASH8><CHK>}.
 *
 * <p>The record is immutable and self-serializing so the event cannot be mutated after it is built.
 */
public record OwnerCreatedEvent(long seq, int schemaVersion, int ownerId, String memberId,
        int membershipLevel) {

    /** The fixed discriminator carried by every owner-create event. */
    public static final String EVENT = "OWNER_CREATED";

    /** The event schema version, bumped to 2 for the version-2 owner identity. */
    public static final int SCHEMA_VERSION = 2;

    /** Render this event as its canonical JSON object. */
    public String toJson() {
        return "{"
                + "\"seq\":" + this.seq + ","
                + "\"schemaVersion\":" + this.schemaVersion + ","
                + "\"ownerId\":" + this.ownerId + ","
                + "\"memberId\":" + quote(this.memberId) + ","
                + "\"membershipLevel\":" + this.membershipLevel + ","
                + "\"event\":\"" + EVENT + "\""
                + "}";
    }

    /** A JSON string literal for {@code value}, or {@code null} when absent. */
    private static String quote(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(value.length() + 2).append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.append('"').toString();
    }
}
