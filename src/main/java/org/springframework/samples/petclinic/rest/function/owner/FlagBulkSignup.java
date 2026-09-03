package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags the response with {@code bulkSignupWarning} when more than 80 owners have already
 * been registered today (by registrationDate), before {@link SaveOwner} runs so the count
 * excludes this owner.
 */
public class FlagBulkSignup {

    static final int BULK_THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        LocalDate today = owner.getRegistrationDate();
        long count = ownerRepository.findAll().stream()
                .filter(other -> today.equals(other.getRegistrationDate()))
                .count();
        owner.setBulkSignupWarning(count > BULK_THRESHOLD);
    }
}
