package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;

/**
 * Derives an owner's {@code contactPreference} — the preferred contact channel returned on the
 * owner DTO.
 *
 * <p>The preference is {@code EMAIL} when the owner has an email address present (non-blank),
 * otherwise {@code PHONE}.
 */
public final class ContactPreference {

    private ContactPreference() {
    }

    /** The owner's contact preference: {@code EMAIL} when an email is present, else {@code PHONE}. */
    public static OwnerDto.ContactPreferenceEnum of(Owner owner) {
        return (owner.getEmail() != null && !owner.getEmail().isBlank())
            ? OwnerDto.ContactPreferenceEnum.EMAIL
            : OwnerDto.ContactPreferenceEnum.PHONE;
    }
}
