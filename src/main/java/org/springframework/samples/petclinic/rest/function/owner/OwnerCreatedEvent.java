package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted on a successful owner create. Serialized to JSON and
 * published on the {@code AUDIT} logger alongside the human-readable audit line.
 *
 * <p>{@code identifier} carries the owner's <em>current primary identifier</em>. Today that is the
 * {@code customerCode}; when the customerCode is unified into the memberId the caller passes the
 * memberId here instead, without any change to this event's shape. The JSON field is named
 * {@code customerCode} to match the published contract.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String identifier, int membershipLevel) {

    private static final String EVENT = "OWNER_CREATED";

    /** Renders the event as the canonical JSON object
     *  {@code {seq, ownerId, customerCode, membershipLevel, event:'OWNER_CREATED'}}. */
    public String toJson() {
        return "{"
            + "\"seq\":" + seq
            + ",\"ownerId\":" + ownerId
            + ",\"customerCode\":" + quote(identifier)
            + ",\"membershipLevel\":" + membershipLevel
            + ",\"event\":\"" + EVENT + "\""
            + "}";
    }

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
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }
}
