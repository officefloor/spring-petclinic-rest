package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.rest.dto.OwnerDto.ContactPreferenceEnum;

/**
 * Derives an owner's preferred contact channel ('contactPreference') from its
 * stored email address.
 *
 * <p>Kept as a standalone helper (referenced from {@link OwnerMapper}'s
 * {@code contactPreference} expression) rather than a mapper {@code default}
 * method to avoid MapStruct picking it up as an automatic conversion.
 */
final class ContactPreference {

    private ContactPreference() {
    }

    /**
     * {@link ContactPreferenceEnum#EMAIL} when {@code email} is present (non-blank),
     * otherwise {@link ContactPreferenceEnum#PHONE}.
     */
    static ContactPreferenceEnum of(String email) {
        return email != null && !email.isBlank()
                ? ContactPreferenceEnum.EMAIL
                : ContactPreferenceEnum.PHONE;
    }
}
