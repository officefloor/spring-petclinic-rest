package org.springframework.samples.petclinic.service;

import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Business rule: every owner is identified by a single derived {@code identityKey} of
 * {@code normalizedTelephone|email|householdId}. This consolidates the former separate
 * telephone, email and household duplicate checks into one key: on creation an owner is
 * rejected only when its WHOLE identityKey exactly matches an existing owner's. Because the
 * telephone is part of the key, two members of the same household (same householdId) with
 * different telephones have different identityKeys and are both allowed; only an exact
 * full-key match is a duplicate. Kept as a small, self-contained unit so the rule can be
 * enforced from the create flow without adding complexity to the controller or service.
 *
 * <p>Field canonicalisation is delegated to {@link OwnerFieldNormalizer} and the household-id
 * adoption performed before the check to {@link SharedHouseholdIdAssignment}, keeping this class
 * focused on the identity key itself.
 */
public final class OwnerIdentityPolicy {

    private OwnerIdentityPolicy() {
    }

    /**
     * Assign the owner's householdId (when it knowingly joins an existing household) and reject
     * the owner when its whole identityKey matches any other existing owner's.
     *
     * @param clinicService   source of the existing owners
     * @param owner           the owner being created
     * @param sharesHousehold whether the request opted in to a shared household
     * @throws DuplicateIdentityException if the identityKey is already in use
     */
    public static void rejectDuplicateIdentity(ClinicService clinicService, Owner owner, Boolean sharesHousehold) {
        SharedHouseholdIdAssignment.assign(clinicService, owner, sharesHousehold);
        String key = identityKey(owner);
        for (Owner existing : clinicService.findAllOwners()) {
            if (!existing.getId().equals(owner.getId()) && key.equals(identityKey(existing))) {
                throw new DuplicateIdentityException();
            }
        }
    }

    /** The owner's derived identity key: normalized telephone, email and household id, '|'-joined. */
    public static String identityKey(Owner owner) {
        return OwnerFieldNormalizer.telephone(owner.getTelephone()) + "|"
            + OwnerFieldNormalizer.email(owner.getEmail()) + "|"
            + OwnerFieldNormalizer.orEmpty(owner.getHouseholdId());
    }

    /** Thrown when another owner already has the same identityKey. */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DuplicateIdentityException extends RuntimeException {
    }
}
