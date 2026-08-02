package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.Objects;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.repository.OwnerRepository;
import org.springframework.samples.petclinic.rest.escalation.BadRequestException;

/**
 * Rejects creating an owner once {@value #DAILY_CAP} owners have already been
 * registered today (by registration date), with a 400.
 */
public class RejectOverDailyCap {

    static final int DAILY_CAP = 20;

    public void service(@Val Owner owner, OwnerRepository ownerRepository) throws BadRequestException {
        LocalDate today = LocalDate.now();
        long createdToday = ownerRepository.findAll().stream()
                .filter(existing -> !Objects.equals(existing.getId(), owner.getId()))
                .filter(existing -> today.equals(existing.getRegistrationDate()))
                .count();
        if (createdToday >= DAILY_CAP) {
            throw new BadRequestException("Daily owner creation limit reached");
        }
    }
}
