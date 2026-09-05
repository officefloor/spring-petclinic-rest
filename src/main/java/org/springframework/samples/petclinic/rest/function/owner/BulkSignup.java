package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Bulk sign-up detection: today's business day counts as a bulk sign-up once more than 80
 * owners have already been registered on it. "Today" is the server date rolled forward to a
 * business day (see {@link BusinessDay}) — the same effective date newly created owners land
 * on (see {@link BuildOwner}) and that {@link RejectDailyLimit} caps. The count is of existing
 * owners whose {@code registrationDate} equals that business day.
 */
public final class BulkSignup {

    private static final int WARNING_THRESHOLD = 80;

    private BulkSignup() {
    }

    /**
     * Returns true when more than 80 owners have already been created for today's business day.
     */
    public static boolean warning(OwnerRepository ownerRepository) {
        LocalDate businessDay = BusinessDay.roll(LocalDate.now());
        long count = ownerRepository.findAll().stream()
                .filter(o -> businessDay.equals(o.getRegistrationDate()))
                .count();
        return count > WARNING_THRESHOLD;
    }
}
