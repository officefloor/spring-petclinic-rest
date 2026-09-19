package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Shared counting of owners already registered on a given business day, used by the
 * create-pipeline rules that accumulate per day: the hard daily limit
 * ({@link EnsureDailyLimit}) and the bulk-signup warning ({@link FlagBulkSignup}). Owners
 * are counted by their stored {@code registrationDate}, which is rolled forward onto a
 * business day on creation (see {@link RegistrationDates}), so callers pass an
 * already-effective day and both rules count the same owners.
 */
final class DailyRegistrations {

    private DailyRegistrations() {
    }

    /** Count stored owners whose registration date equals the given business day. */
    static int countOn(OwnerRepository ownerRepository, LocalDate businessDay) {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (businessDay.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        return count;
    }
}
