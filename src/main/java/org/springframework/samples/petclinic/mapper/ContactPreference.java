package org.springframework.samples.petclinic.mapper;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Derives the {@code contactPreference} field produced by {@link OwnerMapper#toOwnerDto}: the
 * owner's preferred contact channel. It resolves to {@code EMAIL} when the owner has an email,
 * otherwise {@code PHONE}.
 */
public final class ContactPreference {

    private ContactPreference() {
    }

    /**
     * Derive the preferred contact channel for an owner.
     *
     * @param owner the owner; its email presence drives the result
     * @return {@code EMAIL} when the owner {@link Owner#hasEmail() has an email}, else {@code PHONE}
     */
    public static OwnerDto.ContactPreferenceEnum of(Owner owner) {
        return owner.hasEmail() ? OwnerDto.ContactPreferenceEnum.EMAIL : OwnerDto.ContactPreferenceEnum.PHONE;
    }
}
