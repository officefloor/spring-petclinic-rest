package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted once per successful owner create.
 *
 * <p>Carries a monotonically increasing {@code seq}, the saved owner's {@code ownerId}, the owner's
 * current primary identifier and its {@code membershipLevel}, with a fixed {@code event} discriminator
 * of {@code OWNER_CREATED}.
 *
 * <p>The {@code customerCode} field holds the owner's <em>current primary identifier</em>. Today that
 * is the {@link org.springframework.samples.petclinic.model.Owner#getCustomerCode() customerCode}; when
 * a later checkpoint unifies the customerCode into a memberId, the same field carries the memberId
 * instead — the event always names whatever the current primary identifier is.
 *
 * <p>A pure data record: it is serialized to JSON by the audit step via the shared {@code ObjectMapper},
 * so the event renders the same way as every other DTO in the system.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, Integer membershipLevel,
        String event) {

    /** The fixed discriminator for a successful owner create. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    public OwnerCreatedEvent(long seq, Integer ownerId, String customerCode, Integer membershipLevel) {
        this(seq, ownerId, customerCode, membershipLevel, OWNER_CREATED);
    }
}
