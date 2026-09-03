package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Decides the {@code bulkSignupWarning} flag: true once more than {@value #THRESHOLD}
 * other owners were already created on {@code owner}'s (business-day adjusted)
 * registration date, sharing the same accumulation path as the daily create limit.
 */
public final class BulkSignup {

    private static final int THRESHOLD = 80;

    private BulkSignup() {
    }

    public static boolean isWarned(Owner owner, OwnerRepository ownerRepository) {
        long sameDay = ownerRepository.findAll().stream()
                .filter(other -> other.getId() != null && !other.getId().equals(owner.getId()))
                .filter(other -> owner.getRegistrationDate().equals(other.getRegistrationDate()))
                .count();
        return sameDay > THRESHOLD;
    }
}
