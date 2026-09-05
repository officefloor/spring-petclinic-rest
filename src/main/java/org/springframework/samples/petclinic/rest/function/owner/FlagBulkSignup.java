package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Sets {@code bulkSignupWarning} to {@code true} when more than {@link #BULK_SIGNUP_THRESHOLD}
 * owners have already been created for this owner's registration day, otherwise {@code false}. The
 * day is the owner's {@code registrationDate}, which {@link BuildOwner} has already rolled forward
 * off any weekend, so this matches the accumulation counted by {@link RejectOverDailyLimit}. Runs
 * before the owner is saved, so {@link OwnerRepository#findAll()} sees only the owners that existed
 * before this create.
 */
public class FlagBulkSignup {

    /** The warning is raised once strictly more than this many owners already exist for the day. */
    static final int BULK_SIGNUP_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (owner.getRegistrationDate() != null
                    && owner.getRegistrationDate().equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        owner.setBulkSignupWarning(count > BULK_SIGNUP_THRESHOLD);
    }
}
