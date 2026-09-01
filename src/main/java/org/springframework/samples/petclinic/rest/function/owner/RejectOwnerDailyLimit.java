package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;

/**
 * Rejects 429 when 100 or more owners have already been registered today, by
 * {@code registrationDate}. The new owner is not yet saved, so a day already holding exactly
 * 100 owners rejects the 101st.
 */
public class RejectOwnerDailyLimit {

    private static final int LIMIT = 100;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws DailyOwnerLimitException {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (Owner other : ownerRepository.findAll()) {
            if (other != owner && today.equals(other.getRegistrationDate()) && ++count >= LIMIT) {
                throw new DailyOwnerLimitException(LIMIT);
            }
        }
    }
}
