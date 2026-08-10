package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Records the {@code bulkSignupWarning} flag on the freshly built {@link Owner} before it is saved:
 * {@code true} when more than {@value #BULK_SIGNUP_THRESHOLD} owners have already been created for
 * the request's business day (counted by {@code registrationDate}), otherwise {@code false}.
 *
 * <p>Counts the same bucket as {@link CheckDailyOwnerLimit} — existing owners whose
 * {@code registrationDate} equals the effective, business-day-adjusted date resolved by
 * {@link ResolveRegistrationDate}. Runs before {@link SaveOwner} so the new owner is not yet counted,
 * matching the "already been created" wording: the flag reflects only owners that existed before
 * this create.
 */
public class FlagBulkSignup {

    /** More than this many owners already created today raises the bulk-signup warning. */
    public static final int BULK_SIGNUP_THRESHOLD = 80;

    public void service(@Val Owner owner, @Val LocalDate registrationDate,
            OwnerRepository ownerRepository) {
        long count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (registrationDate.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        owner.setBulkSignupWarning(count > BULK_SIGNUP_THRESHOLD);
    }
}
