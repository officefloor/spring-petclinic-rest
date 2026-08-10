package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Sets the owner's transient {@code bulkSignupWarning} flag for the response. The flag is true when
 * more than 80 owners (including this one, when it has already been saved) share the same business-day
 * {@code registrationDate}, mirroring the accumulation used by {@link EnsureBelowDailyLimit}; otherwise
 * false. Mutated in place via {@code @Val} for the responding step to map onto the DTO.
 */
public class EvaluateBulkSignupWarning {

    /** Owners created for a single business day beyond this count trigger the warning. */
    private static final int BULK_SIGNUP_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        LocalDate registrationDate = owner.getRegistrationDate();
        int count = 0;
        if (registrationDate != null) {
            for (Owner existing : ownerRepository.findAll()) {
                if (registrationDate.equals(existing.getRegistrationDate())) {
                    count++;
                }
            }
        }
        owner.setBulkSignupWarning(count > BULK_SIGNUP_THRESHOLD);
    }
}
