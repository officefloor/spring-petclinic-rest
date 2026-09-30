package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Counts the owners already registered on the resolved registration date (see
 * {@link ResolveRegistrationDate}) and publishes the total so the per-day rules share a
 * single accumulation. Runs before the owner is saved, so the count excludes the owner
 * being created; both {@link EnsureDailyRegistrationLimit} and
 * {@link AssignBulkSignupWarning} read the published value rather than re-scanning the
 * table.
 */
public class CountDailyRegistrations {

    public void service(@Val LocalDate registrationDate, OwnerRepository ownerRepository,
            Out<Integer> dailyRegistrationCount) {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (registrationDate.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        dailyRegistrationCount.set(count);
    }
}
