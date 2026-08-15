package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

public class ApplyOwner {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request) {
        owner.setAddress(request.getAddress());
        owner.setCity(request.getCity());
        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setTelephone(request.getTelephone());
        // Email is optional; the ValidateOwner step's @Valid has already rejected an invalid
        // address with 400, so store any present value lower-cased (null clears it).
        String email = request.getEmail();
        owner.setEmail(email == null ? null : email.toLowerCase(Locale.ROOT));
    }
}
