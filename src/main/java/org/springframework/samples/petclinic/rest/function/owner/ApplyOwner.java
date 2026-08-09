package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerEmailInvalidException;

public class ApplyOwner {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request) throws OwnerEmailInvalidException {
        owner.setAddress(request.getAddress());
        owner.setCity(request.getCity());
        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setTelephone(request.getTelephone());
        owner.setEmail(OwnerEmail.normalize(request.getEmail()));
    }
}
