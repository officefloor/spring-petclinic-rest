package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

public class ApplyOwner {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request) {
        AddressFields.normalizeInto(request);
        owner.setAddress(request.getAddress());
        owner.setAddressLine1(request.getAddressLine1());
        owner.setAddressLine2(request.getAddressLine2());
        owner.setCity(request.getCity());
        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setTelephone(request.getTelephone());
        String email = request.getEmail();
        owner.setEmail(email == null || email.isBlank() ? null : email.trim().toLowerCase());
        String postcode = request.getPostcode();
        owner.setPostcode(postcode == null || postcode.isBlank() ? null : postcode.trim());
    }
}
