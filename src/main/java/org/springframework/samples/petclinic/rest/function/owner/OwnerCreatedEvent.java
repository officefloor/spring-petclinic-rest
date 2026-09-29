package org.springframework.samples.petclinic.rest.function.owner;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Immutable structured audit event emitted on a successful owner create, alongside the plain audit
 * line. {@link AuditOwnerCreated} serializes it with the project's Jackson {@code ObjectMapper} — the
 * same serializer the {@code rest/dto} DTOs use — to a single-line JSON object with fields in the
 * order the spec lists them: {@code {"seq":..,"ownerId":..,"customerCode":..,"membershipLevel":..,"event":"OWNER_CREATED"}}.
 *
 * <p>The {@code customerCode} slot carries the owner's <strong>current primary identifier</strong>,
 * read from {@link org.springframework.samples.petclinic.model.Owner#primaryIdentifier()}. Today that
 * is the customer code; when it is unified into the {@code memberId}, the domain accessor returns the
 * memberId and this event follows with no change here.
 *
 * <p>{@code seq} is a monotonically increasing integer across creates (see {@link AuditOwnerCreated}).
 */
@JsonPropertyOrder({ "seq", "ownerId", "customerCode", "membershipLevel", "event" })
public record OwnerCreatedEvent(long seq, int ownerId, String customerCode, Integer membershipLevel) {

    /** The fixed event marker distinguishing this structured event from other AUDIT lines. */
    public static final String EVENT = "OWNER_CREATED";

    /** The constant event marker, exposed so it serializes as the {@code event} field. */
    @JsonProperty("event")
    public String event() {
        return EVENT;
    }
}
