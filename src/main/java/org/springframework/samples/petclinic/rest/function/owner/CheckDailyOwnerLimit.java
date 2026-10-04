package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Create-time daily-limit guard for {@code POST /owners}: rejects a request once 100 or more owners
 * have already been registered on the request's effective business day (by {@code registrationDate}),
 * with a 429 via {@link DailyOwnerLimitException}.
 *
 * <p>The effective business day is the supplied registration date, or the server date when omitted,
 * rolled forward over a weekend to Monday — exactly the adjusted date {@link BuildOwner} stores on a
 * new owner. Counts existing owners whose stored {@code registrationDate} equals that business day,
 * so the count reflects owners registered on the same business day. Runs before {@link BuildOwner}.
 */
public class CheckDailyOwnerLimit {

    /** Maximum number of owners permitted to register in a single day. */
    private static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyOwnerLimitException {
        // The limit is per business day of the EFFECTIVE registration date: use the supplied date
        // when present, else the server date, then roll a weekend forward to Monday — matching the
        // adjusted date stored on new owners (see BuildOwner), so the count lines up.
        LocalDate supplied = request.getRegistrationDate();
        LocalDate businessDay = BusinessDay.rollForward(supplied != null ? supplied : LocalDate.now());
        long count = BusinessDay.countOwnersOn(businessDay, ownerRepository);
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(count);
        }
    }
}
