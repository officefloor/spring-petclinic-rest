package org.springframework.samples.petclinic.util;

/**
 * Immutable structured audit event emitted when an owner is created, rendered under schema version 2
 * as the JSON object
 * {@code {schemaVersion:2, seq, ownerId, memberId, membershipLevel, event:'OWNER_CREATED'}}.
 * The {@code memberId} it carries is the owner's version-2 primary identifier.
 *
 * <p>{@code seq} is a monotonically increasing sequence assigned by the emitter (see
 * {@code RespondWithOwnerCreated}). The {@code memberId} field carries the owner's current
 * <em>primary identifier</em> (see {@link OwnerIdentities#primaryIdentifier(org.springframework.samples.petclinic.model.Owner)}):
 * the unified member id.
 *
 * <p>The record is immutable: once constructed the event cannot be altered before or after it is
 * published.
 */
public record OwnerCreatedEvent(long seq, int ownerId, String memberId, int membershipLevel) {

    /** The fixed {@code event} discriminator carried by every owner-created event. */
    public static final String EVENT = "OWNER_CREATED";

    /** The audit schema version this event is emitted under. */
    public static final int SCHEMA_VERSION = 2;

    /** Render this event as a compact JSON object. */
    public String toJson() {
        return new StringBuilder()
                .append('{')
                .append("\"schemaVersion\":").append(SCHEMA_VERSION)
                .append(",\"seq\":").append(this.seq)
                .append(",\"ownerId\":").append(this.ownerId)
                .append(",\"memberId\":").append(quote(this.memberId))
                .append(",\"membershipLevel\":").append(this.membershipLevel)
                .append(",\"event\":").append(quote(EVENT))
                .append('}')
                .toString();
    }

    private static String quote(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(value.length() + 2).append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.append('"').toString();
    }
}
