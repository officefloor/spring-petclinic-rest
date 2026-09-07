package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Step of {@code POST /api/owners} that records the owner's {@code bulkSignupWarning}: {@code true}
 * when more than 80 owners had already been created on the same adjusted business day (by
 * {@link Owner#getRegistrationDate()}) before this owner, otherwise {@code false}. Uses the same
 * per-day accumulation as {@link RequireDailyRegistrationLimit}. Runs before {@link SaveOwner} so
 * the owner being created is not itself counted.
 */
public class FlagBulkSignupWarning {

    /** Threshold above which a create is flagged as part of a bulk signup. */
    private static final int BULK_SIGNUP_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        LocalDate registrationDate = owner.getRegistrationDate();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (registrationDate.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        owner.setBulkSignupWarning(count > BULK_SIGNUP_THRESHOLD);
    }
}
