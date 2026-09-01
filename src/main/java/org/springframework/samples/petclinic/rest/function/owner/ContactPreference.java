package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Response value: 'EMAIL' when the owner has an email address, otherwise 'PHONE'.
 */
final class ContactPreference {

    private ContactPreference() {
    }

    static String of(Owner owner) {
        return owner.getEmail() != null ? "EMAIL" : "PHONE";
    }
}
