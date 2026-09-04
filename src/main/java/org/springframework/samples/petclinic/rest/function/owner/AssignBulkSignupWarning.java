package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records, on the owner being created, whether more than 80 owners had already been created for the
 * same business day. The count is a snapshot taken before this owner is saved, counted by
 * registration date exactly as {@link EnsureDailyOwnerLimit} counts the per-day cap: owners whose
 * registration date equals this owner's effective business day (see {@link ApplyRegistrationDate}).
 * Runs before {@link SaveOwner}; the resulting flag is echoed back on the owner as
 * {@code bulkSignupWarning}.
 */
public class AssignBulkSignupWarning {

    private static final int WARNING_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        LocalDate businessDay = owner.getRegistrationDate();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (businessDay.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        owner.setBulkSignupWarning(count > WARNING_THRESHOLD);
    }
}
