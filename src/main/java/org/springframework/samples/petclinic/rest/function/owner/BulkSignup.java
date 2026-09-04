package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Signals a bulk signup: true once more than 80 owners have already been created on the
 * current business day (by registrationDate), otherwise false.
 */
public final class BulkSignup {

    private static final int THRESHOLD = 80;

    private BulkSignup() {
    }

    public static boolean warns(OwnerRepository ownerRepository) {
        LocalDate today = BusinessDay.onOrAfter(LocalDate.now());
        long count = ownerRepository.findAll().stream()
                .filter(o -> today.equals(o.getRegistrationDate()))
                .count();
        return count > THRESHOLD;
    }
}
