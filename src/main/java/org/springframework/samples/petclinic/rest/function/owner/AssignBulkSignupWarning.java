package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code bulkSignupWarning}: {@code true} when more than 80 owners had already
 * been created on this request's registration day (by registrationDate) at the time this owner was
 * created, otherwise {@code false}.
 *
 * <p>Runs after {@link DefaultRegistrationDate} has resolved and business-day-adjusted the entity's
 * registrationDate and before {@link SaveOwner}, within the same write transaction; it counts the
 * owners persisted so far for that day (excluding this new, not-yet-saved one), sharing the daily
 * accumulation path with {@link CheckDailyLimit}.
 */
public class AssignBulkSignupWarning {

    private static final int WARNING_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        LocalDate day = owner.getRegistrationDate();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (day.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        owner.setBulkSignupWarning(count > WARNING_THRESHOLD);
    }
}
