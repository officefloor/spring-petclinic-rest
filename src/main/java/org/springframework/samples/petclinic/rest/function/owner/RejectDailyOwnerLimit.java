package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitExceededException;

/**
 * Rejects creating an owner once the maximum number of owners for a single day has already been
 * reached, responding 400 via {@link DailyOwnerLimitExceededException}. Owners are grouped by
 * registration date; when {@value #MAX_OWNERS_PER_DAY} owners already share today's registration
 * date, no further owner may be created today. Runs after {@code build} (so the registration date is
 * assigned) and before {@code save}.
 */
public class RejectDailyOwnerLimit {

    static final int MAX_OWNERS_PER_DAY = 20;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DailyOwnerLimitExceededException {
        LocalDate today = LocalDate.now();
        long createdToday = ownerRepository.findAll().stream()
                .filter(existing -> today.equals(existing.getRegistrationDate()))
                .count();
        if (createdToday >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitExceededException(
                    "The maximum number of owners for today has already been reached");
        }
    }
}
