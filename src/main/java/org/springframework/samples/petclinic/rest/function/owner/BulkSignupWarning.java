package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.Objects;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Flags a bulk signup: {@code true} when more than 80 other owners already share this owner's
 * {@code registrationDate} (i.e. were created on the same day), otherwise {@code false}. The owner
 * itself is excluded by id, matching the daily accumulation the create endpoint reports.
 */
public final class BulkSignupWarning {

    private static final int THRESHOLD = 80;

    private BulkSignupWarning() {
    }

    public static boolean of(Owner owner, OwnerRepository repository) {
        LocalDate day = owner.getRegistrationDate();
        int count = 0;
        for (Owner other : repository.findAll()) {
            if (!Objects.equals(other.getId(), owner.getId()) && day.equals(other.getRegistrationDate())
                    && ++count > THRESHOLD) {
                return true;
            }
        }
        return false;
    }
}
