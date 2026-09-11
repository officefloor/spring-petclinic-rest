package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code bulkSignupWarning}: {@code true} when more than 80 owners
 * had already been created for this owner's registration business day at the moment this
 * owner was created, {@code false} otherwise. Runs after {@link BuildOwner} (so the
 * business-day-adjusted registration date is set) and before {@link SaveOwner} (so the
 * count reflects the owners that existed before this create and excludes the one being
 * created). The value is fixed at creation time and persisted with the owner.
 */
public class AssignBulkSignupWarning {

    /** More than this many owners already created today raises the warning. */
    private static final int BULK_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        LocalDate businessDay = owner.getRegistrationDate();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (businessDay.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        owner.setBulkSignupWarning(count > BULK_THRESHOLD);
    }
}
