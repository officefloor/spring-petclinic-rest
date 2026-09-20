package org.springframework.samples.petclinic.model;

/**
 * How an owner prefers to be contacted: {@link #EMAIL} when an email address is present,
 * otherwise {@link #PHONE}. Derived at read time from the owner's fields.
 */
public enum ContactPreference {

    /** The owner has an email address, so email is preferred. */
    EMAIL,

    /** The owner has no email address, so phone is preferred. */
    PHONE;

    /**
     * The contact preference for the given owner: {@link #EMAIL} when an email address is
     * present, otherwise {@link #PHONE}.
     */
    public static ContactPreference of(Owner owner) {
        return owner.hasEmail() ? EMAIL : PHONE;
    }
}
