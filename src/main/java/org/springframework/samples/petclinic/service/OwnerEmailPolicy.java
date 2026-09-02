package org.springframework.samples.petclinic.service;

import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Business rule: an owner's email must be unique across all owners when compared case-insensitively
 * (i.e. by its lower-cased form). Kept as a small, self-contained unit so the rule can be enforced
 * from the create flow without adding complexity to the controller or service.
 */
public final class OwnerEmailPolicy {

    private OwnerEmailPolicy() {
    }

    /**
     * Reject the given owner if its lower-cased email matches any other existing owner.
     *
     * @param clinicService source of the existing owners
     * @param owner         the owner being created
     * @throws DuplicateEmailException if the lower-cased email is already in use
     */
    public static void rejectDuplicateEmail(ClinicService clinicService, Owner owner) {
        String email = normalize(owner.getEmail());
        if (email.isEmpty()) {
            return;
        }
        for (Owner existing : clinicService.findAllOwners()) {
            if (!existing.getId().equals(owner.getId()) && email.equals(normalize(existing.getEmail()))) {
                throw new DuplicateEmailException();
            }
        }
    }

    private static String normalize(String email) {
        return email == null ? "" : email.toLowerCase();
    }

    /** Thrown when an owner's lower-cased email is already used by another owner. */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DuplicateEmailException extends RuntimeException {
    }
}
