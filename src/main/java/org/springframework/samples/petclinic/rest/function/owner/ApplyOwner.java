package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

public class ApplyOwner {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request) {
        AddressNormalizer.Normalized address = AddressNormalizer.resolve(
                request.getAddressLine1(), request.getAddressLine2(), request.getAddress());
        owner.setAddressLine1(address.addressLine1());
        owner.setAddressLine2(address.addressLine2());
        owner.setAddress(address.address());
        owner.setCity(request.getCity());
        owner.setPostcode(request.getPostcode());
        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setTelephone(request.getTelephone());
        String email = request.getEmail();
        owner.setEmail(email == null || email.isBlank() ? null : EmailNormalizer.normalize(email));
    }
}
