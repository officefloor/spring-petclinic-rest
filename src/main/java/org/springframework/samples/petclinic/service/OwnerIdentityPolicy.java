package org.springframework.samples.petclinic.service;

import org.springframework.http.HttpStatus;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Business rule: every owner is identified by a single derived {@code identityKey}: the SHA-256
 * hex of {@code normalizedTelephone|lowerEmail|soundex(lastName)}. On creation an owner is
 * rejected only when its identityKey exactly matches an existing (non-deleted) owner's. Because the
 * telephone is part of the key, two owners with the same last name and postcode but DIFFERENT
 * telephones have different identityKeys and are both created — the second is flagged a soft match
 * by {@link OwnerPossibleDuplicatePolicy} rather than rejected. Kept as a small, self-contained unit
 * so the rule can be enforced from the create flow without adding complexity to the controller or
 * service.
 *
 * <p>Field canonicalisation is delegated to {@link OwnerFieldNormalizer} and the surname's phonetic
 * code to {@link Soundex}, keeping this class focused on the identity key itself.
 */
public final class OwnerIdentityPolicy {

    private OwnerIdentityPolicy() {
    }

    /**
     * Reject the owner when its identityKey matches any existing non-deleted owner's; otherwise flag
     * it as a possible duplicate when it soft-matches one (see {@link OwnerPossibleDuplicatePolicy}).
     *
     * @param clinicService source of the existing owners
     * @param owner         the owner being created
     * @throws DuplicateIdentityException if the identityKey is already in use
     */
    public static void rejectDuplicateIdentity(ClinicService clinicService, Owner owner) {
        String key = identityKey(owner);
        for (Owner existing : clinicService.findAllOwners()) {
            if (!existing.isDeleted() && !existing.getId().equals(owner.getId())
                && key.equals(identityKey(existing))) {
                throw new DuplicateIdentityException();
            }
        }
        OwnerPossibleDuplicatePolicy.assignPossibleDuplicate(clinicService, owner);
    }

    /** The owner's derived identity key: SHA-256 hex of {@code telephone|email|soundex(lastName)}. */
    public static String identityKey(Owner owner) {
        return Sha256Hex.lower("V2|" + OwnerFieldNormalizer.telephone(owner.getTelephone()) + "|"
            + OwnerFieldNormalizer.email(owner.getEmail()) + "|"
            + Soundex.of(owner.getLastName()), 32);
    }

    /** Thrown when another owner already has the same identityKey. */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class DuplicateIdentityException extends RuntimeException {
    }
}
