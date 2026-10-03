package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitExceededException;

/**
 * Rejects creating an owner when 100 or more owners have already been created today, as
 * counted by {@link Owner#getRegistrationDate()}. Runs before {@link SaveOwner}, so the
 * owner being created is not counted. At (or over) the limit escalates as a 429 via
 * {@link DailyOwnerLimitExceededException}.
 */
public class RejectOverDailyLimit {

    private static final int DAILY_LIMIT = 100;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DailyOwnerLimitExceededException {
        LocalDate today = LocalDate.now();
        long count = ownerRepository.findAll().stream()
                .filter(existing -> today.equals(existing.getRegistrationDate()))
                .count();
        if (count >= DAILY_LIMIT) {
            throw new DailyOwnerLimitExceededException(
                    DAILY_LIMIT + " or more owners have already been created today");
        }
    }
}
