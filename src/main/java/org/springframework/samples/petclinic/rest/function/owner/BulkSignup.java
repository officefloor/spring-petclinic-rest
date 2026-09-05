package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a bulk signup: {@code true} once more than {@value #THRESHOLD} owners have already
 * been created today, otherwise {@code false}. Owners are counted by registrationDate on the
 * current business day, matching how {@link BuildOwner} dates newly created owners and how
 * {@link RequireDailyCapacity} enforces the daily cap.
 */
public final class BulkSignup {

    static final int THRESHOLD = 80;

    private BulkSignup() {
    }

    public static boolean warningToday(OwnerRepository ownerRepository) {
        LocalDate today = BusinessDay.roll(LocalDate.now());
        long count = ownerRepository.findAll().stream()
            .filter(existing -> today.equals(existing.getRegistrationDate()))
            .count();
        return count > THRESHOLD;
    }
}
