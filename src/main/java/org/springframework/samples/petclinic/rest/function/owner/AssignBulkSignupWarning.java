package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Sets {@code bulkSignupWarning} true when more than 80 <em>other</em> owners already share this
 * owner's registration date — i.e. more than 80 owners had already been created that day — otherwise
 * false. Mirrors the accumulation path of {@link CheckOwnerDailyLimit}: it counts owners by their
 * effective, business-day-adjusted {@code registrationDate}, but excludes the owner itself so the
 * threshold reflects the signups that preceded this one. Runs on both the create and read pipelines
 * so the flag is derived consistently from current data.
 */
public class AssignBulkSignupWarning {

    private static final int BULK_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        LocalDate date = owner.getRegistrationDate();
        long othersSameDay = ownerRepository.findAll().stream()
                .filter(existing -> !existing.getId().equals(owner.getId()))
                .filter(existing -> date != null && date.equals(existing.getRegistrationDate()))
                .count();
        owner.setBulkSignupWarning(othersSameDay > BULK_THRESHOLD);
    }
}
