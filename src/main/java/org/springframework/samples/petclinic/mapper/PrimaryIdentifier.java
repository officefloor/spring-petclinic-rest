package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The owner's <em>current</em> primary identifier &mdash; the single value that downstream
 * consumers (such as the {@code OWNER_CREATED} audit event) quote to name an owner.
 *
 * <p>Today that identifier is the {@code customerCode}. When the customer code is later unified into
 * a {@code memberId}, only this one method changes to return the {@code memberId}, and every
 * consumer routed through it (the create audit event, in particular) carries the new identifier
 * automatically &mdash; no consumer hard-codes {@link Owner#getCustomerCode()} directly.
 *
 * <p>Kept as a standalone class (like {@link MembershipLevel}) so MapStruct does not mistake it for
 * an implicit mapping method, and so there is a single source of truth for "the owner's identifier".
 */
public final class PrimaryIdentifier {

    private PrimaryIdentifier() {
    }

    /** The owner's current primary identifier: the {@code customerCode}. */
    public static String of(Owner owner) {
        return owner.getCustomerCode();
    }
}
