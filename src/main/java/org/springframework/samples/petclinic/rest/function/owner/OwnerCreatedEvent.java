package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Immutable structured audit event emitted once per successful owner create.
 *
 * <p>Carries a monotonically increasing {@code seq}, the saved owner's {@code ownerId}, the owner's
 * current primary identifier, its {@code membershipLevel} and its {@code ownerSegment} (recomputed
 * from the version-2 identity), with a fixed {@code event} discriminator of {@code OWNER_CREATED} and
 * a {@code schemaVersion} of {@value #SCHEMA_VERSION} identifying the event's schema.
 *
 * <p>The {@code memberId} field holds the owner's <em>current primary identifier</em>: the
 * {@link org.springframework.samples.petclinic.model.Owner#getMemberId() memberId} that unifies the
 * former customerCode and membershipNumber — the event always names whatever the current primary
 * identifier is.
 *
 * <p>A pure data record: it is serialized to JSON by the audit step via the shared {@code ObjectMapper},
 * so the event renders the same way as every other DTO in the system.
 */
public record OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel,
        String ownerSegment, int schemaVersion, String event) {

    /** The fixed discriminator for a successful owner create. */
    public static final String OWNER_CREATED = "OWNER_CREATED";

    /** The current audit event schema version. */
    public static final int SCHEMA_VERSION = 2;

    public OwnerCreatedEvent(long seq, Integer ownerId, String memberId, Integer membershipLevel,
            String ownerSegment) {
        this(seq, ownerId, memberId, membershipLevel, ownerSegment, SCHEMA_VERSION, OWNER_CREATED);
    }
}
