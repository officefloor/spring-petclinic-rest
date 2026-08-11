package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.mapper.Membership;
import org.springframework.samples.petclinic.model.Owner;

/**
 * Immutable structured record of a successful owner create, serialized to the {@code AUDIT} logger
 * as the JSON object {@code {seq, ownerId, primaryIdentifier, membershipLevel, event}} with
 * {@code event} fixed to {@code "OWNER_CREATED"}.
 *
 * <p>{@code primaryIdentifier} carries the owner's <em>current</em> primary identifier, whatever
 * that happens to be. Today that is the unified {@code memberId} (so the JSON key is
 * {@code "memberId"}), read in one place so any future identifier change flows through
 * {@link #from(long, Owner)} and {@link #IDENTIFIER_FIELD}. Every field is captured at
 * construction, so a published event never changes.
 */
public record OwnerCreatedEvent(long seq, int ownerId, String primaryIdentifier, int membershipLevel) {

    /** The one event type this record represents. */
    public static final String EVENT = "OWNER_CREATED";

    /** JSON key for {@link #primaryIdentifier()} — tracks the current primary identifier's name. */
    private static final String IDENTIFIER_FIELD = "memberId";

    /**
     * Captures the event for a just-saved {@code owner} at sequence {@code seq}, reading the owner's
     * current primary identifier and membership level from a single place so future identifier
     * changes flow through here.
     */
    public static OwnerCreatedEvent from(long seq, Owner owner) {
        return new OwnerCreatedEvent(seq, owner.getId(), owner.getMemberId(), Membership.levelOf(owner));
    }

    /** This event as a compact JSON object with the fields in specification order. */
    public String toJson() {
        return "{"
                + "\"seq\":" + this.seq
                + ",\"ownerId\":" + this.ownerId
                + ",\"" + IDENTIFIER_FIELD + "\":" + quote(this.primaryIdentifier)
                + ",\"membershipLevel\":" + this.membershipLevel
                + ",\"event\":" + quote(EVENT)
                + "}";
    }

    /** Renders {@code value} as a JSON string literal, or {@code null} when absent. */
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
