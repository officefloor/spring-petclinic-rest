package org.springframework.samples.petclinic.model;

/**
 * The owner's current primary identifier.
 *
 * <p>Today this is the {@code memberId} (the unified {@code <REGION><FY><HASH8><CHK>} identity).
 * Centralizing the choice here means audit events and any other consumers follow the primary
 * identifier from a single place.
 */
public final class OwnerPrimaryIdentifier {

    private OwnerPrimaryIdentifier() {
    }

    /**
     * The owner's current primary identifier.
     *
     * @param owner the owner.
     * @return the primary identifier, or {@code null} when the owner or the identifier is absent.
     */
    public static String of(Owner owner) {
        return owner == null ? null : owner.getMemberId();
    }
}
