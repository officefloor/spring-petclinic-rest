package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

public class BuildOwner {

    public void service(@Val OwnerFieldsDto request, OwnerMapper ownerMapper, Out<Owner> built) {
        Owner owner = ownerMapper.toOwner(request);
        java.time.LocalDate effective = owner.getRegistrationDate() == null
            ? java.time.LocalDate.now() : owner.getRegistrationDate();
        owner.setRegistrationDate(BusinessDay.roll(effective));
        built.set(owner);
    }
}
