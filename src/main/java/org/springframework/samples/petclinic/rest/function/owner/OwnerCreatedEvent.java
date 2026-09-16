package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event recording that an owner was created. Serialised to JSON
 * and emitted to the {@code AUDIT} logger by {@link OwnerEventPublisher}.
 *
 * <p>{@code memberId} carries the owner's <em>primary identifier</em>
 * (see {@link org.springframework.samples.petclinic.model.Owner#getPrimaryIdentifier()}),
 * so it follows the identifier automatically if it is later changed.
 *
 * @param seq             monotonically increasing sequence, unique across creates
 * @param ownerId         the created owner's generated id
 * @param memberId        the owner's primary identifier at creation time
 * @param membershipLevel the owner's computed membership level at creation time
 * @param event           the event type, always {@link #EVENT_TYPE}
 */
public record OwnerCreatedEvent(long seq, int ownerId, String memberId, int membershipLevel,
        String event) {

    /** The {@code event} discriminator for an owner-created event. */
    public static final String EVENT_TYPE = "OWNER_CREATED";
}
