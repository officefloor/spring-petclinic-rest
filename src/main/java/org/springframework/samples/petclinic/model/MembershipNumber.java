package org.springframework.samples.petclinic.model;

/**
 * Single source of truth for an owner's {@code membershipNumber}: the derived value the owner response
 * carries and the create audit line records. The number is {@code <customerCode>-M<YY>}, where
 * {@code YY} is the two-digit fiscal year of the owner's (business-day-adjusted) registration date -
 * the fiscal year starts on 1 July, so the year segment comes from {@link FiscalYear}, not the calendar
 * year.
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
                + FiscalYear.twoDigitStartYear(owner.getRegistrationDate());
    }
}
