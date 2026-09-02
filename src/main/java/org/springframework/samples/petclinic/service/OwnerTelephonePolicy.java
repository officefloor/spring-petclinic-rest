package org.springframework.samples.petclinic.service;

import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Business rule: an owner's telephone must be unique across all owners once normalized
 * (non-digit characters removed). Kept as a small, self-contained unit so the rule can be
 * enforced from the create flow without adding complexity to the controller or service.
 *
 * @author Vitaliy Fedoriv
 */
public final class OwnerTelephonePolicy {

    private OwnerTelephonePolicy() {
    }

    /**
     * Reject the given owner if its normalized telephone matches any other existing owner.
     *
     * @param clinicService source of the existing owners
     * @param owner         the owner being created
     * @throws DuplicateTelephoneException if the normalized telephone is already in use
     */
    public static void rejectDuplicateTelephone(ClinicService clinicService, Owner owner) {
        String normalized = normalize(owner.getTelephone());
        if (normalized.isEmpty()) {
            return;
        }
        for (Owner existing : clinicService.findAllOwners()) {
            if (!existing.getId().equals(owner.getId()) && normalized.equals(normalize(existing.getTelephone()))) {
                throw new DuplicateTelephoneException();
            }
        }
    }

    private static String normalize(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }

    /** Thrown when an owner's normalized telephone is already used by another owner. */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DuplicateTelephoneException extends RuntimeException {
    }
}
