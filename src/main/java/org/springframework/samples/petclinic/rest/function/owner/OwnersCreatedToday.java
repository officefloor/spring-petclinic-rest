package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * The single definition of "how many owners were created on today's business day": the count of
 * existing owners whose {@link Owner#getRegistrationDate()} equals the server date rolled forward
 * over weekends (see {@link BusinessDay}). Shared by the rules that bucket owners by that day — the
 * per-day create limit ({@link EnsureDailyOwnerLimit}) and the bulk-signup warning
 * ({@link BulkSignupWarning}).
 */
final class OwnersCreatedToday {

    private OwnersCreatedToday() {
    }

    /** The number of existing owners registered on today's (weekend-adjusted) business day. */
    static int count(OwnerRepository ownerRepository) {
        LocalDate today = BusinessDay.adjust(LocalDate.now());
        int createdToday = 0;
        for (Owner owner : ownerRepository.findAll()) {
            if (today.equals(owner.getRegistrationDate())) {
                createdToday++;
            }
        }
        return createdToday;
    }
}
