package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags the owner with {@code bulkSignupWarning} when the day's signups are running high.
 *
 * <p>Runs in the {@code POST /api/owners} pipeline after {@link CountOwnerNamesakes} and before
 * {@link SaveOwner}, so the new owner is not yet persisted and therefore not counted. When more than
 * 80 owners already carry this owner's (business-day adjusted) registration date, the warning is set
 * to {@code true}; otherwise {@code false}. The same per-day accumulation the daily-limit rule uses,
 * at a lower, non-rejecting threshold. The value is frozen at creation time and returned unchanged
 * on later reads.
 */
public class FlagOwnerBulkSignup {

    /** Above this many owners already created for the day, the next owner carries the warning. */
    private static final int BULK_WARNING_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        LocalDate businessDay = BusinessDays.effectiveRegistrationDate(owner.getRegistrationDate());
        long createdToday = BusinessDays.createdOn(ownerRepository, businessDay);
        owner.setBulkSignupWarning(createdToday > BULK_WARNING_THRESHOLD);
    }
}
