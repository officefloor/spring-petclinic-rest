package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a bulk signup on create: sets the owner's {@code bulkSignupWarning} to
 * {@code true} when more than 80 owners already share the new owner's
 * (business-day-adjusted) registration date, as counted by
 * {@link Owner#getRegistrationDate()}, otherwise {@code false}. Runs before
 * {@link SaveOwner}, so the owner being created is not counted — the same
 * accumulation path as {@link RejectOverDailyLimit}, at a lower threshold.
 */
public class FlagBulkSignup {

    private static final int BULK_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        long count = OwnerDailyCount.sharingRegistrationDate(ownerRepository, owner.getRegistrationDate());
        owner.setBulkSignupWarning(count > BULK_THRESHOLD);
    }
}
