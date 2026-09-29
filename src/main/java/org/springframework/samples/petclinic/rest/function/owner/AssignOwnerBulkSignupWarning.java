package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Assigns the owner's {@code bulkSignupWarning} — {@code true} when more than
 * {@code THRESHOLD} (80) owners had already been created on this owner's
 * {@code registrationDate} at the time of this create, otherwise {@code false}.
 * Runs after {@link DefaultOwnerRegistrationDate} so the date is populated and before
 * {@link SaveOwner} so the count reflects existing owners only.
 */
public class AssignOwnerBulkSignupWarning {

    private static final int THRESHOLD = 80;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) {
        LocalDate registrationDate = owner.getRegistrationDate();
        long count = registrationDate == null ? 0 : ownerRepository.findAll().stream()
                .map(Owner::getRegistrationDate)
                .filter(registrationDate::equals)
                .count();
        owner.setBulkSignupWarning(count > THRESHOLD);
    }
}
