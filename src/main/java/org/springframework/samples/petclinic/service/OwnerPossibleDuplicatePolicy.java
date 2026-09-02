package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: an owner that is not a hard duplicate but whose {@code soundex(lastName)} and
 * postcode match an existing (non-deleted) owner's — while its identityKey differs — is still
 * created and flagged as a possible duplicate. On creation the owner records
 * {@code possibleDuplicateOf} (the matching owner's id), from which {@code possibleDuplicate} is
 * derived; when nothing matches it stays absent. Kept as a small, self-contained unit so the rule
 * can be applied from the create flow without adding complexity to the controller or service.
 */
public final class OwnerPossibleDuplicatePolicy {

    private OwnerPossibleDuplicatePolicy() {
    }

    /**
     * Record {@code possibleDuplicateOf} for a newly created owner when a non-deleted owner shares
     * its {@code soundex(lastName)} and postcode but has a different identityKey.
     *
     * @param clinicService source of the existing owners
     * @param owner         the owner being created
     */
    public static void assignPossibleDuplicate(ClinicService clinicService, Owner owner) {
        String postcode = OwnerFieldNormalizer.orEmpty(owner.getPostcode());
        if (postcode.isEmpty()) {
            return;
        }
        String soundex = Soundex.of(owner.getLastName());
        String key = OwnerIdentityPolicy.identityKey(owner);
        for (Owner existing : clinicService.findAllOwners()) {
            if (!existing.isDeleted() && !existing.getId().equals(owner.getId())
                && soundex.equals(Soundex.of(existing.getLastName()))
                && postcode.equals(OwnerFieldNormalizer.orEmpty(existing.getPostcode()))
                && !key.equals(OwnerIdentityPolicy.identityKey(existing))) {
                owner.setPossibleDuplicateOf(existing.getId());
                return;
            }
        }
    }
}
