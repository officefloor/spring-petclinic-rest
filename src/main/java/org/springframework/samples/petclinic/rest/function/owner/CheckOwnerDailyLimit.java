package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects a create-owner request once {@link #DAILY_LIMIT} or more owners have already been
 * registered on this request's adjusted business day, comparing each stored owner's
 * {@code registrationDate} against the effective registration date (supplied or defaulted to
 * today, rolled forward off any weekend). Runs before {@link SaveOwner} so the request itself is
 * not counted. On reaching the limit raises {@link DailyOwnerLimitException} (429).
 */
public class CheckOwnerDailyLimit {

    /** Maximum number of owners permitted to be created in a single day; the request is
     *  rejected at this count. */
    public static final int DAILY_LIMIT = 100;

    public void service(@Val OwnerFieldsDto request, OwnerRepository ownerRepository)
            throws DailyOwnerLimitException {
        LocalDate businessDay = BusinessDay.effective(request);
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (businessDay.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(count);
        }
    }
}
