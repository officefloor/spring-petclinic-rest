package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;
import java.util.List;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.MissingFieldsException;

public class BuildOwner {

    public void service(@Val OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built)
            throws MissingFieldsException {
        Owner owner = ownerMapper.toOwner(request);
        LocalDate supplied = owner.getRegistrationDate();
        if (supplied != null && supplied.isAfter(LocalDate.now())) { // future date: reject as 400
            throw new MissingFieldsException(List.of("registrationDate"));
        }
        LocalDate date = supplied != null ? supplied : LocalDate.now();
        owner.setRegistrationDate(BusinessDay.roll(date)); // skip weekends and public holidays
        built.set(owner);
    }
}
