package org.springframework.samples.petclinic.rest.function.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

public class BuildOwner {

    public void service(@Val OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built) {
        Owner owner = ownerMapper.toOwner(request);
        LocalDate date = owner.getRegistrationDate() != null ? owner.getRegistrationDate() : LocalDate.now();
        if (date.getDayOfWeek().getValue() > 5) { // Saturday or Sunday: roll forward to Monday
            date = date.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        }
        owner.setRegistrationDate(date);
        built.set(owner);
    }
}
