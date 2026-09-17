package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * An owner's preferred contact channel, derived from their details: {@link #EMAIL} when an email
 * address is present, otherwise {@link #PHONE}. Pure function of the owner's own fields, so it is
 * derived at response time rather than stored on the entity.
 */
public enum ContactPreference {

    /** Preferred when the owner has an email address. */
    EMAIL,

    /** The fallback when the owner has no email address. */
    PHONE;

    /** The contact preference for {@code owner}, or {@code null} when the owner is {@code null}. */
    public static ContactPreference of(Owner owner) {
        if (owner == null) {
            return null;
        }
        return owner.getEmail() != null && !owner.getEmail().isBlank() ? EMAIL : PHONE;
    }
}
