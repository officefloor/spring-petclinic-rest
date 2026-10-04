package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Create-time daily-limit guard for {@code POST /owners}: rejects a request once 100 or more owners
 * have already been registered today (by {@code registrationDate}), with a 429 via
 * {@link DailyOwnerLimitException}.
 *
 * <p>Counts existing owners whose {@code registrationDate} equals today's date; new owners default
 * their registration date to today (see {@link BuildOwner}), so the count reflects owners created
 * today. Runs before {@link BuildOwner}.
 */
public class CheckDailyOwnerLimit {

    /** Maximum number of owners permitted to register in a single day. */
    private static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyOwnerLimitException {
        LocalDate today = LocalDate.now();
        long count = ownerRepository.findAll().stream()
            .map(Owner::getRegistrationDate)
            .filter(today::equals)
            .count();
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(count);
        }
    }
}
