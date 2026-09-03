package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a create-owner request as a bulk sign-up. Runs before {@link SaveOwner}, so
 * {@link OwnerRepository#findAll()} returns only the owners that predate this create. It counts
 * those already registered on this owner's (adjusted) business day — the date {@link BuildOwner}
 * stored on the entity — and sets {@code bulkSignupWarning} true when more than
 * {@link #BULK_SIGNUP_THRESHOLD} such owners already exist; otherwise false. The count mirrors the
 * per-day accumulation used by {@link EnsureDailyOwnerLimit}.
 */
public class FlagBulkSignup {

    /** More than this many owners already created on the business day marks a bulk sign-up. */
    public static final int BULK_SIGNUP_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getRegistrationDate().equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        owner.setBulkSignupWarning(count > BULK_SIGNUP_THRESHOLD);
    }
}
