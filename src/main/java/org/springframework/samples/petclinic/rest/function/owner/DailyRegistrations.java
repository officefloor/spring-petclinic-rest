package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Counts how many owners are registered on a given day. The registration date is the
 * canonical "day an owner was created" in this domain (see {@link ResolveRegistrationDate}),
 * so this single primitive backs both the per-day create limit ({@link EnsureDailyLimit})
 * and the bulk-signup warning ({@link BulkSignup}).
 */
final class DailyRegistrations {

    private DailyRegistrations() {
    }

    /** The number of existing owners whose registration date equals {@code date}. */
    static long countOn(OwnerRepository ownerRepository, LocalDate date) {
        long count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (date.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        return count;
    }
}
