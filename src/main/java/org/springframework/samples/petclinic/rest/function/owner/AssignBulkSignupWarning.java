package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code bulkSignupWarning}: {@code true} when more than 80 owners had already
 * been created today, otherwise {@code false}. "Today" follows the same accumulation path as the
 * per-day create limit — owners whose adjusted business-day {@code registrationDate} equals today's
 * business day. Runs before the new owner is saved, so the count reflects only the owners that
 * existed before this create.
 */
public class AssignBulkSignupWarning {

    private static final long BULK_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        LocalDate businessDay = BusinessDays.rollForward(LocalDate.now());
        long createdToday = ownerRepository.findAll().stream()
                .filter(existing -> businessDay.equals(existing.getRegistrationDate()))
                .count();
        owner.setBulkSignupWarning(createdToday > BULK_THRESHOLD);
    }
}
