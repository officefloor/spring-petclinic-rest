package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Runs just before the responder on both {@code GET /api/owners/{ownerId}} and {@code POST /api/owners}.
 * Computes the {@code bulkSignupWarning} flag carried on the owner response: it is {@code true} once more
 * than {@value #BULK_SIGNUP_THRESHOLD} other owners share this owner's effective (business-day)
 * registration date, otherwise {@code false}.
 *
 * <p>The effective day is derived through {@link BusinessDay} and the per-day count through
 * {@link DailyRegistrations#countOn}, exactly as the per-day create-limit rule
 * ({@link CheckOwnerDailyLimit}) does, so the two rules cannot drift. The owner itself is excluded from
 * the count, so on a create it reflects the owners already created that day and on a read the other
 * owners sharing the day.
 */
public class CheckBulkSignup {

    /** More than this many other owners already registered for the day raises the warning. */
    static final int BULK_SIGNUP_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository, Out<Boolean> bulkSignupWarning) {
        LocalDate businessDay = BusinessDay.effective(owner.getRegistrationDate());
        int count = DailyRegistrations.countOn(ownerRepository, businessDay, owner.getId());
        bulkSignupWarning.set(count > BULK_SIGNUP_THRESHOLD);
    }
}
