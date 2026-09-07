package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

public class ApplyOwner {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request) {
        applyAddress(owner, request);
        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setTelephone(request.getTelephone());
        String email = request.getEmail();
        owner.setEmail(email == null || email.isBlank() ? null
                : email.trim().toLowerCase(java.util.Locale.ROOT));
    }

    /** Copy the request's address fields onto the owner. */
    private static void applyAddress(Owner owner, OwnerFieldsDto request) {
        owner.setAddress(request.getAddress());
        owner.setCity(request.getCity());
        owner.setPostcode(request.getPostcode());
    }
}
