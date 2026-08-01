package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects creating an owner once {@value #DAILY_LIMIT} owners already carry today's
 * registration date, by throwing a {@link DailyOwnerLimitException}, handled as 400.
 */
public class RejectDailyOwnerLimit {

    static final int DAILY_LIMIT = 20;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DailyOwnerLimitException {
        LocalDate today = LocalDate.now();
        long todayCount = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (today.equals(existing.getRegistrationDate())) {
                todayCount++;
            }
        }
        if (todayCount >= DAILY_LIMIT) {
            throw new DailyOwnerLimitException(
                "The maximum number of owners for today has already been reached");
        }
    }
}
