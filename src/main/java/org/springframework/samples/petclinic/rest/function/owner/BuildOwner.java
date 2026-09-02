package org.springframework.samples.petclinic.rest.function.owner;

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
        java.time.LocalDate supplied = owner.getRegistrationDate();
        if (supplied != null && supplied.isAfter(java.time.LocalDate.now())) {
            throw new FutureRegistrationDateException(supplied);
        }
        java.time.LocalDate effective = supplied == null
                ? java.time.LocalDate.now() : supplied;
        owner.setRegistrationDate(BusinessDay.roll(effective));
        built.set(owner);
    }
}
