package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.FutureRegistrationDateException;

public class BuildOwner {

    public void service(@Val OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built)
            throws FutureRegistrationDateException {
        Owner owner = ownerMapper.toOwner(request);
        LocalDate effective = owner.getRegistrationDate();
        if (effective == null) {
            effective = LocalDate.now();
        }
        // A supplied registration date cannot be in the future; reject a date later than the
        // server's current date with a 400 before it is rolled onto a business day.
        else if (effective.isAfter(LocalDate.now())) {
            throw new FutureRegistrationDateException(effective);
        }
        // The effective registration date must fall on a business day, whether supplied in the
        // request or defaulted to the server date; a weekend rolls forward to the next Monday.
        owner.setRegistrationDate(BusinessDays.rollForward(effective));
        built.set(owner);
    }
}
