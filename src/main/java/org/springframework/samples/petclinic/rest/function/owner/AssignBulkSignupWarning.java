package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code bulkSignupWarning}: {@code true} when more than 80 owners have already
 * been created for this owner's business day, {@code false} otherwise. The business day is the
 * owner's adjusted {@link Owner#getRegistrationDate() registrationDate} (already rolled forward by
 * {@link BuildOwner}), so the warning counts owners per adjusted business day exactly as
 * {@link CheckOwnerDailyLimit} counts them for the daily create-limit.
 *
 * <p>Runs before {@link SaveOwner}, so the count reflects the owners already persisted (i.e. the
 * value before this create) and the new owner itself is not counted.
 */
public class AssignBulkSignupWarning {

    /** More than this many owners already created for the business day triggers the warning. */
    private static final int BULK_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        LocalDate businessDay = owner.getRegistrationDate();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (businessDay.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        owner.setBulkSignupWarning(count > BULK_THRESHOLD);
    }
}
