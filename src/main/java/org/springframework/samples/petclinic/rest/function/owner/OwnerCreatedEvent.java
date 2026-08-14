package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted once per created owner.
 *
 * <p>Schema version 2. Rendered to AUDIT as the compact JSON object
 * {@code {seq, ownerId, memberId, membershipLevel, ownerSegment, schemaVersion:2, event:"OWNER_CREATED"}}.
 * {@code seq} is a monotonically increasing integer across creates (see {@link AuditOwnerCreated}).
 *
 * <p>The {@code identifier} carries the owner's <em>current primary identifier</em>, the (version-2)
 * memberId, rendered under the primary-identifier key so the wire shape follows the identity. The
 * {@code ownerSegment} is recomputed from that version-2 identity.
 */
public record OwnerCreatedEvent(long seq, int ownerId, String identifier, int membershipLevel,
        String ownerSegment) {

    /** The event type marker. */
    public static final String EVENT = "OWNER_CREATED";

    /** The audit event schema version. */
    public static final int SCHEMA_VERSION = 2;

    /** The JSON key for the primary identifier — the {@code memberId}. */
    private static final String IDENTIFIER_KEY = "memberId";

    /**
     * Compact, deterministic JSON:
     * {@code {"seq":..,"ownerId":..,"memberId":..,"membershipLevel":..,"ownerSegment":..,"schemaVersion":2,"event":"OWNER_CREATED"}}.
     */
    public String toJson() {
        return "{\"seq\":" + this.seq
                + ",\"ownerId\":" + this.ownerId
                + ",\"" + IDENTIFIER_KEY + "\":" + json(this.identifier)
                + ",\"membershipLevel\":" + this.membershipLevel
                + ",\"ownerSegment\":" + json(this.ownerSegment)
                + ",\"schemaVersion\":" + SCHEMA_VERSION
                + ",\"event\":\"" + EVENT + "\"}";
    }

    /** JSON-encode a nullable string value (bare {@code null}, otherwise a quoted, escaped string). */
    private static String json(String value) {
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
