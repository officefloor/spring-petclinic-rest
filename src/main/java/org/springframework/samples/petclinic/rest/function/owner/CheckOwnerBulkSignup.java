package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a create-owner request as part of a bulk signup surge: sets {@code bulkSignupWarning} true
 * once more than {@value #BULK_SIGNUP_THRESHOLD} owners have already been created on the new
 * owner's registrationDate, otherwise false. That date has already been rolled onto a business day
 * by {@link BuildOwner}, so the count is per adjusted business day. Runs after {@link BuildOwner}
 * and before the owner is saved, so the count reflects only pre-existing owners. Stamps the value
 * on the owner via {@link Owner#setBulkSignupWarning(boolean)}; it is returned as
 * {@code bulkSignupWarning}.
 */
public class CheckOwnerBulkSignup {

    /** More than this many owners already created on the same day triggers the warning. */
    private static final int BULK_SIGNUP_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        // Count against the new owner's (already business-day-adjusted) registration date, matching
        // the daily-limit rule, so the warning applies per adjusted business day.
        LocalDate registrationDate = owner.getRegistrationDate();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (existing.getId() != null && existing.getId().equals(owner.getId())) {
                continue; // the owner being created is not counted against the threshold
            }
            if (registrationDate.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        owner.setBulkSignupWarning(count > BULK_SIGNUP_THRESHOLD);
    }
}
