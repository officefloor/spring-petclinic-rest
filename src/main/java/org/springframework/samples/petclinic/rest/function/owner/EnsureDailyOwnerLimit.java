package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.DailyOwnerLimitException;
import org.springframework.samples.petclinic.repository.OwnerRepository;

/**
 * Rejects creating an owner when {@value DailyOwnerLimitException#DAILY_OWNER_LIMIT} or more owners
 * have already been created today, comparing each existing owner's {@link Owner#getRegistrationDate()}
 * against the current date. Runs before {@link BuildOwner}/{@link SaveOwner} so the limit is a 429
 * (Too Many Requests) rather than a persisted row.
 */
public class EnsureDailyOwnerLimit {

    public void service(OwnerRepository ownerRepository) throws DailyOwnerLimitException {
        LocalDate today = LocalDate.now();
        int count = 0;
        for (Owner existing : ownerRepository.findAll()) {
            if (today.equals(existing.getRegistrationDate())) {
                count++;
            }
        }
        if (count >= DailyOwnerLimitException.DAILY_OWNER_LIMIT) {
            throw new DailyOwnerLimitException(DailyOwnerLimitException.DAILY_OWNER_LIMIT);
        }
    }
}
