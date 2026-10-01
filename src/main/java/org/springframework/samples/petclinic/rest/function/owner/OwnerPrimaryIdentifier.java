package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for an owner's <em>primary identifier</em> &mdash; the one value that
 * uniquely names the owner to the outside world.
 *
 * <p>That is the {@code memberId} — the unified {@code <REGION><FY><HASH8><CHK>} identifier that
 * replaced the former {@code customerCode} and {@code membershipNumber}. Every consumer (such as the
 * {@code OWNER_CREATED} audit event emitted by {@link AuditOwnerCreated}) reads the identifier through
 * this method, so a future change of identity scheme touches only here.
 */
public final class OwnerPrimaryIdentifier {

    private OwnerPrimaryIdentifier() {
    }

    /** The owner's current primary identifier. */
    public static String of(Owner owner) {
        return owner.getMemberId();
    }
}
