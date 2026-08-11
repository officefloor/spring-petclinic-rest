package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted when an owner is created.
 *
 * <p>Serialised to a compact JSON object
 * {@code {seq, ownerId, memberId, membershipLevel, event:'OWNER_CREATED'}} and written to the
 * {@code AUDIT} logger. {@code seq} is a monotonically increasing integer across creates and
 * {@code memberId} holds the owner's current primary identifier (see
 * {@link org.springframework.samples.petclinic.mapper.PrimaryIdentifier}).
 *
 * <p>The record is deeply immutable (all components are values), so once emitted the event cannot be
 * altered.
 */
public record OwnerCreatedEvent(int seq, Integer ownerId, String memberId, int membershipLevel,
        String event) {

    /** The marker every create event carries. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /** Compact JSON rendering with fields in the documented order. */
    public String toJson() {
        return "{\"seq\":" + this.seq
                + ",\"ownerId\":" + this.ownerId
                + ",\"memberId\":" + jsonString(this.memberId)
                + ",\"membershipLevel\":" + this.membershipLevel
                + ",\"event\":" + jsonString(this.event)
                + "}";
    }

    /** A JSON string literal for {@code value}, or {@code null} when absent. */
    private static String jsonString(String value) {
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
                    }
                    else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }
}
