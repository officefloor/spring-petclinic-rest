package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's contact preference: "EMAIL" when an email address is
 * present, otherwise "PHONE".
 */
final class ContactPreferences {

    private ContactPreferences() {
    }

    /** "EMAIL" when {@code owner} has an email, otherwise "PHONE". */
    static String preferenceOf(Owner owner) {
        return owner.getEmail() != null ? "EMAIL" : "PHONE";
    }
}
