package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Populates the response-only {@link Owner#isBulkSignupWarning() bulkSignupWarning} flag:
 * true once more than 80 owners have already been created today, otherwise false. "Today"
 * is the current, weekend-adjusted business day (matching how {@link BuildOwner} stores and
 * {@link ValidateDailyOwnerLimit} buckets registration dates), and the count is of owners
 * whose {@code registrationDate} falls on that day. Mutates the loaded owner in place so the
 * responder's mapper carries the flag onto the DTO.
 */
public class FlagBulkSignup {

    private static final int BULK_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        LocalDate today = BusinessDay.effectiveRegistrationDate(null);
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (today.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        owner.setBulkSignupWarning(count > BULK_THRESHOLD);
    }
}
