package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.TooManyOwnersTodayException;

/**
 * Rejects a create request once the maximum number of owners (100) have already been created for a
 * given business day, counted by registration date, so no more than 100 owners are registered per
 * business day. The day counted is this request's effective registration date rolled onto a
 * business day (see {@link BusinessDays}): the date supplied in the body, or the server date when
 * none was supplied, with a weekend rolled forward to Monday. Runs before {@link BuildOwner}; a day
 * already at its limit is a 429 via {@link TooManyOwnersTodayException}.
 */
public class EnsureDailyOwnerLimit {

    private static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws TooManyOwnersTodayException {
        LocalDate supplied = request.getRegistrationDate();
        LocalDate businessDay = BusinessDays.rollForward(supplied != null ? supplied : LocalDate.now());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (businessDay.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new TooManyOwnersTodayException();
        }
    }
}
