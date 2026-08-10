package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.OwnerEmailDisposableException;
import org.springframework.samples.petclinic.rest.escalation.OwnerEmailInvalidException;
import org.springframework.samples.petclinic.rest.escalation.OwnerPostcodeInvalidException;

public class ApplyOwner {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request)
            throws OwnerEmailInvalidException, OwnerEmailDisposableException, OwnerPostcodeInvalidException {
        // Postcode is optional; when present it must be 4 digits and valid for the city's region.
        OwnerPostcode.validate(request.getPostcode(), request.getCity());
        owner.setAddress(request.getAddress());
        owner.setCity(request.getCity());
        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setTelephone(request.getTelephone());
        owner.setEmail(OwnerEmail.normalize(request.getEmail()));
        owner.setPostcode(request.getPostcode());
    }
}
