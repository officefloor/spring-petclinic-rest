package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Sets the owner's {@code bulkSignupWarning}: true when more than 80 owners have already been
 * registered on the current business day, signalling a possible bulk-signup event. Existing owners
 * are counted by registrationDate against the business-day-adjusted current date, mirroring the
 * daily-limit rule's accumulation. Runs before {@code save}, so the new owner is not yet counted.
 */
public class AssignBulkSignupWarning {

    private static final int BULK_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        LocalDate today = BusinessDays.adjust(LocalDate.now());
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (today.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        owner.setBulkSignupWarning(count > BULK_THRESHOLD);
    }
}
