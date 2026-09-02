package org.springframework.samples.petclinic.service;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Business rule: an owner that is not a hard duplicate but shares an existing owner's last name
 * and postcode with a different telephone is still created and flagged as a possible duplicate.
 * On creation the owner records {@code possibleDuplicateOf} (the matching owner's id), from which
 * {@code possibleDuplicate} is derived; when nothing matches it stays absent. Kept as a small,
 * self-contained unit so the rule can be applied from the create flow without adding complexity
 * to the controller or service.
 */
public final class OwnerPossibleDuplicatePolicy {

    private OwnerPossibleDuplicatePolicy() {
    }

    /**
     * Record {@code possibleDuplicateOf} for a newly created owner when another owner shares its
     * last name and postcode but has a different telephone.
     *
     * @param clinicService source of the existing owners
     * @param owner         the owner being created
     */
    public static void assignPossibleDuplicate(ClinicService clinicService, Owner owner) {
        String postcode = OwnerFieldNormalizer.orEmpty(owner.getPostcode());
        if (postcode.isEmpty()) {
            return;
        }
        String lastName = OwnerFieldNormalizer.name(owner.getLastName());
        String telephone = OwnerFieldNormalizer.telephone(owner.getTelephone());
        for (Owner existing : clinicService.findAllOwners()) {
            if (!existing.getId().equals(owner.getId())
                && lastName.equals(OwnerFieldNormalizer.name(existing.getLastName()))
                && postcode.equals(OwnerFieldNormalizer.orEmpty(existing.getPostcode()))
                && !telephone.equals(OwnerFieldNormalizer.telephone(existing.getTelephone()))) {
                owner.setPossibleDuplicateOf(existing.getId());
                return;
            }
        }
    }
}
