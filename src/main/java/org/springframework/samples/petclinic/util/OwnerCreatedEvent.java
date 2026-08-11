package org.springframework.samples.petclinic.util;

/**
 * Immutable structured audit event emitted when an owner is created, rendered as the JSON object
 * {@code {seq, ownerId, customerCode, membershipLevel, event:'OWNER_CREATED'}}.
 *
 * <p>{@code seq} is a monotonically increasing sequence assigned by the emitter (see
 * {@code RespondWithOwnerCreated}). The {@code customerCode} field carries the owner's current
 * <em>primary identifier</em> (see {@link OwnerIdentities#primaryIdentifier(org.springframework.samples.petclinic.model.Owner)}):
 * the customer code today, and whatever replaces it later (e.g. the {@code memberId}) — the value
 * follows the primary identifier without any change here.
 *
 * <p>The record is immutable: once constructed the event cannot be altered before or after it is
 * published.
 */
public record OwnerCreatedEvent(long seq, int ownerId, String customerCode, int membershipLevel) {

    /** The fixed {@code event} discriminator carried by every owner-created event. */
    public static final String EVENT = "OWNER_CREATED";

    /** Render this event as a compact JSON object. */
    public String toJson() {
        return new StringBuilder()
                .append('{')
                .append("\"seq\":").append(this.seq)
                .append(",\"ownerId\":").append(this.ownerId)
                .append(",\"customerCode\":").append(quote(this.customerCode))
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
