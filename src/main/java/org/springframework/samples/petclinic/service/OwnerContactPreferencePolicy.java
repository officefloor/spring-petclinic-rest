package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: an owner's {@code contactPreference} is {@code EMAIL} when an email is present,
 * otherwise {@code PHONE}. Kept as a small, self-contained unit so the rule can be applied from the
 * read flow without adding complexity to the mapper, controller, or service.
 */
public final class OwnerContactPreferencePolicy {

    private OwnerContactPreferencePolicy() {
    }

    /**
     * Derive the {@code contactPreference} for the given owner.
     *
     * @param owner the owner whose contact preference to derive
     * @return {@code "EMAIL"} when the owner has an email, otherwise {@code "PHONE"}
     */
    public static String contactPreference(Owner owner) {
        String email = owner.getEmail();
        return email != null && !email.isBlank() ? "EMAIL" : "PHONE";
    }
}
