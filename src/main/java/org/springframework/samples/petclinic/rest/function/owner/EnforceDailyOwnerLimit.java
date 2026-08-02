package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitExceededException;

/**
 * Rejects creation of an <code>Owner</code> once the maximum number of owners for a
 * single day has already been reached, counting existing owners by their registration
 * date. The candidate owner registers today (see {@link BuildOwner}), so the cap is
 * measured against the owners already registered on that same date.
 */
public class EnforceDailyOwnerLimit {

    /** Maximum number of owners that may be created on any one registration date. */
    static final int MAX_OWNERS_PER_DAY = 20;

    public void service(@Val Owner owner, OwnerRepository ownerRepository)
            throws DailyOwnerLimitExceededException {
        LocalDate candidateDate = owner.getRegistrationDate();
        final LocalDate registrationDate = (candidateDate != null) ? candidateDate : LocalDate.now();
        long alreadyRegisteredToday = ownerRepository.findAll().stream()
                .filter(existing -> registrationDate.equals(existing.getRegistrationDate()))
                .count();
        if (alreadyRegisteredToday >= MAX_OWNERS_PER_DAY) {
            throw new DailyOwnerLimitExceededException(
                    "Cannot create more than " + MAX_OWNERS_PER_DAY
                            + " owners on " + registrationDate);
        }
    }
}
