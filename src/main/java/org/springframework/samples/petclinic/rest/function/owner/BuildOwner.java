package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.util.BusinessDays;

public class BuildOwner {

    public void service(@Val OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built) {
        Owner owner = ownerMapper.toOwner(request);
        // When no registration date is supplied, default to the server's current date.
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        // The effective registration date must fall on a business day: a weekend rolls
        // forward to the next Monday. Everything derived from it (e.g. the membership
        // number's year segment) then uses the adjusted date.
        owner.setRegistrationDate(BusinessDays.toBusinessDay(owner.getRegistrationDate()));
        built.set(owner);
    }
}
