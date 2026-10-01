package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The single home for "how many owners share a registration day" — the domain concept behind both the
 * per-day create-limit rule ({@link CheckOwnerDailyLimit}) and the bulk-signup warning
 * ({@link CheckBulkSignup}). Callers derive the effective business day once via {@link BusinessDay} and
 * pass it here, so the day definition and the count scan each live in exactly one place.
 */
final class DailyRegistrations {

    private DailyRegistrations() {
    }

    /**
     * Counts owners whose {@code registrationDate} equals {@code businessDay}. When {@code excludeId} is
     * non-null the owner with that id is not counted (so a persisted owner is not counted against
     * itself); pass {@code null} to count every owner on the day.
     */
    static int countOn(OwnerRepository ownerRepository, LocalDate businessDay, Integer excludeId) {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (excludeId != null && excludeId.equals(existing.getId())) {
                continue;
            }
            if (businessDay != null && businessDay.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        return count;
    }
}
