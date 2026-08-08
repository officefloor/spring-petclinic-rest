package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a bulk-signup surge on the new owner. Counts how many owners were already
 * created for the same business day (owners sharing this owner's adjusted
 * {@code registrationDate}), excluding the owner being created, and sets
 * {@code bulkSignupWarning} to true once that count exceeds {@value #WARNING_THRESHOLD}.
 *
 * <p>Runs after {@link DefaultOwnerRegistrationDate} (so the business-day registration
 * date is set) and before {@link SaveOwner} (so the owner being created is not yet
 * persisted and never counts itself). This is the same daily accumulation the
 * {@link RejectOwnerDailyLimit} rule counts, evaluated at a lower, non-rejecting
 * threshold. The value is a create-time snapshot returned as {@code bulkSignupWarning}.
 */
public class AssignOwnerBulkSignupWarning {

    /** Owners already created today must exceed this for the warning to be raised. */
    static final int WARNING_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        LocalDate businessDay = owner.getRegistrationDate();
        long count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getId() != null && owner.getId().equals(existing.getId())) {
                continue; // never count the owner itself
            }
            if (businessDay.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        owner.setBulkSignupWarning(count > WARNING_THRESHOLD);
    }
}
