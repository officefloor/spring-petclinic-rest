package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted on a successful owner create. Serialized to JSON and
 * published on the {@code AUDIT} logger alongside the human-readable audit line.
 *
 * <p>{@code identifier} carries the owner's <em>primary identifier</em>, the unified {@code memberId}.
 * The JSON field is named {@code memberId} to match the published contract.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String identifier, int membershipLevel) {

    private static final String EVENT = "OWNER_CREATED";

    /** Renders the event as the canonical JSON object
     *  {@code {seq, ownerId, memberId, membershipLevel, event:'OWNER_CREATED'}}. */
    public String toJson() {
        return "{"
            + "\"seq\":" + seq
            + ",\"ownerId\":" + ownerId
            + ",\"memberId\":" + quote(identifier)
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
