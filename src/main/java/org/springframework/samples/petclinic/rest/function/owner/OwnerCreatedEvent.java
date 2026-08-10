package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted once per successful owner create, rendered as the JSON
 * object {@code {seq, ownerId, customerCode, membershipLevel, event:'OWNER_CREATED'}}.
 *
 * <p>{@code seq} is a monotonically increasing integer across creates. {@code customerCode} carries
 * the owner's <em>current primary identifier</em> (see {@link
 * org.springframework.samples.petclinic.model.Owner#getPrimaryIdentifier()}); it is the customerCode
 * today and becomes the memberId once the two are unified, without any change here.
 *
 * <p>The record is immutable and self-serializing so the event cannot be mutated after it is built.
 */
public record OwnerCreatedEvent(long seq, int ownerId, String customerCode, int membershipLevel) {

    /** The fixed discriminator carried by every owner-create event. */
    public static final String EVENT = "OWNER_CREATED";

    /** Render this event as its canonical JSON object. */
    public String toJson() {
        return "{"
                + "\"seq\":" + this.seq + ","
                + "\"ownerId\":" + this.ownerId + ","
                + "\"customerCode\":" + quote(this.customerCode) + ","
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
