package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

public class ApplyOwner {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request)
            throws InvalidEmailException, InvalidTelephoneException {
        owner.setAddress(request.getAddress());
        owner.setCity(request.getCity());
        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setTelephone(OwnerTelephone.toE164(request.getTelephone()));
        owner.setEmail(OwnerEmail.normalize(request.getEmail()));
        owner.setPostcode(request.getPostcode());
    }
}
