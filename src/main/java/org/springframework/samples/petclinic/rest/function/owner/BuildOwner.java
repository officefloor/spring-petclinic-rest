package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

public class BuildOwner {

    public void service(@Val OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built) {
        Owner owner = ownerMapper.toOwner(request);
        // Default the registration date to the server's current date when none was supplied.
        if (owner.getRegistrationDate() == null) {
            owner.setRegistrationDate(LocalDate.now());
        }
        // The effective registration date must fall on a business day: roll a weekend date
        // (supplied or defaulted) forward to the next Monday, so every value derived from it
        // (e.g. the membership number's year segment) uses the adjusted date.
        owner.setRegistrationDate(BusinessDays.rollForward(owner.getRegistrationDate()));
        built.set(owner);
    }
}
