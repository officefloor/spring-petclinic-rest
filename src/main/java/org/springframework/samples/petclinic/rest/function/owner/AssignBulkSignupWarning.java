package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code bulkSignupWarning}: {@code true} when more than 80 owners had already
 * been created for this owner's registration day at the moment this owner was created, otherwise
 * {@code false}. The day is the built owner's {@link Owner#getRegistrationDate() registrationDate}
 * (already rolled forward off any weekend by {@link BuildOwner}), matching the accumulation used by
 * {@link RequireDailyOwnerLimit}.
 *
 * <p>Runs after {@link BuildOwner} (so the registration date is set) and before {@link SaveOwner},
 * so the count reflects only the owners that existed before this create.
 */
public class AssignBulkSignupWarning {

    /** A warning is raised once strictly more than this many owners exist for the day. */
    static final long BULK_SIGNUP_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        LocalDate businessDay = owner.getRegistrationDate();
        long createdThatDay = ownerRepository.findAll().stream()
                .filter(existing -> businessDay.equals(existing.getRegistrationDate()))
                .count();
        owner.setBulkSignupWarning(createdThatDay > BULK_SIGNUP_THRESHOLD);
    }
}
