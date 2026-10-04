package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code bulkSignupWarning}: {@code true} when more than 80 owners have already
 * been created on this owner's registration business day, {@code false} otherwise.
 *
 * <p>Runs within the create transaction after {@link BuildOwner} (so the adjusted business-day
 * {@code registrationDate} is set) and before {@link SaveOwner} (so {@code findAll} excludes the
 * owner being created). Counts existing owners whose stored {@code registrationDate} equals this
 * owner's business day — the same accumulation the daily-limit rule ({@link CheckDailyOwnerLimit})
 * uses, so the warning turns on at 81 while the hard cap still rejects at 100.
 */
public class AssignBulkSignupWarning {

    /** Owners already created today above which the bulk-signup warning is raised. */
    private static final int BULK_SIGNUP_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        LocalDate businessDay = owner.getRegistrationDate();
        long count = BusinessDay.countOwnersOn(businessDay, ownerRepository);
        owner.setBulkSignupWarning(count > BULK_SIGNUP_THRESHOLD);
    }
}
