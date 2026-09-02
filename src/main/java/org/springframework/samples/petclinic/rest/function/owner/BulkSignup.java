package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Decides whether an owner response should carry a bulk-signup warning: true once more than
 * {@value #THRESHOLD} owners have already been registered today (by {@code registrationDate},
 * rolled onto a business day just as {@link BuildOwner} stores it), otherwise false.
 */
public final class BulkSignup {

    private static final int THRESHOLD = 80;

    private BulkSignup() {
    }

    public static boolean warned(OwnerRepository ownerRepository) {
        LocalDate today = BusinessDay.roll(LocalDate.now());
        int count = 0;
        for (Owner owner : ownerRepository.findAll()) {
            if (today.equals(owner.getRegistrationDate()) && ++count > THRESHOLD) {
                return true;
            }
        }
        return false;
    }
}
