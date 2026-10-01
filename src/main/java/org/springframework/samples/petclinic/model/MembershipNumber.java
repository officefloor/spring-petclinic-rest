package org.springframework.samples.petclinic.model;

/**
 * Single source of truth for an owner's {@code membershipNumber}: the derived value the owner response
 * carries and the create audit line records. The number is {@code <customerCode>-M<YY>}, where
 * {@code YY} is the two-digit year of the owner's registration date.
 *
 * <p>This is a model-level derivation of an {@link Owner} (like {@link MembershipLevel} and
 * {@link OwnerIdentity}): it is consumed both by the response mapper and by the create pipeline's
 * audit step, so it lives in the model layer rather than beside the pipeline functions.
 */
public final class MembershipNumber {

    private MembershipNumber() {
    }

    /** The {@code membershipNumber} of a persisted or in-flight owner, computed from its stored fields. */
    public static String of(Owner owner) {
        return owner.getCustomerCode() + "-M"
                + String.format("%02d", owner.getRegistrationDate().getYear() % 100);
    }
}
