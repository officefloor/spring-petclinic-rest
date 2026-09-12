package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.MembershipLevel;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured audit event emitted (as JSON) when an owner is created.
 *
 * <p>Besides the human-readable audit line, {@link AuditOwnerCreated} emits one of
 * these per successful create as
 * {@code {seq, ownerId, customerCode, membershipLevel, event:'OWNER_CREATED'}}.
 *
 * <p>The event carries the owner's <em>current primary identifier</em>. Today that is
 * the {@code customerCode}; the value is sourced in a single place
 * ({@link #primaryIdentifier(Owner)}), so when the customerCode is later unified into
 * the memberId, changing that one method makes the event carry the memberId instead —
 * the rest of the pipeline is untouched.
 *
 * <p>A record: once constructed the event cannot be mutated.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, int membershipLevel) {

    /** The {@code event} discriminator carried by every owner-created event. */
    public static final String EVENT = "OWNER_CREATED";

    /**
     * Builds the event for a freshly created owner, reading the current primary
     * identifier and derived membership level from it.
     */
    public static OwnerCreatedEvent of(long seq, Owner owner) {
        return new OwnerCreatedEvent(seq, owner.getId(), primaryIdentifier(owner),
                MembershipLevel.of(owner));
    }

    /**
     * The owner's current primary identifier. Today the {@code customerCode}; when the
     * customerCode is unified into the memberId, return {@code owner.getMemberId()} here
     * and the emitted event carries the memberId instead.
     */
    private static String primaryIdentifier(Owner owner) {
        return owner.getCustomerCode();
    }

    /**
     * Renders the event as a compact JSON object with a stable key order:
     * {@code {"seq":..,"ownerId":..,"customerCode":..,"membershipLevel":..,"event":"OWNER_CREATED"}}.
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder(96);
        sb.append('{');
        sb.append("\"seq\":").append(seq);
        sb.append(",\"ownerId\":").append(ownerId);
        sb.append(",\"customerCode\":").append(jsonString(customerCode));
        sb.append(",\"membershipLevel\":").append(membershipLevel);
        sb.append(",\"event\":").append(jsonString(EVENT));
        sb.append('}');
        return sb.toString();
    }

    /** JSON-encodes a string value, or the literal {@code null} when absent. */
    private static String jsonString(String value) {
        if (value == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder(value.length() + 2);
        sb.append('"');
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
        sb.append('"');
        return sb.toString();
    }
}
