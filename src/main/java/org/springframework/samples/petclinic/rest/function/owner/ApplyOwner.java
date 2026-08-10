package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

public class ApplyOwner {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request) {
        String line1 = AddressNormalizer.normalize(request.getAddressLine1());
        boolean structured = !line1.isEmpty();
        owner.setAddress(AddressNormalizer.compose(request.getAddressLine1(),
                request.getAddressLine2(), request.getAddress()));
        owner.setAddressLine1(structured ? line1 : null);
        owner.setAddressLine2(structured
                ? emptyToNull(AddressNormalizer.normalize(request.getAddressLine2()))
                : null);
        owner.setCity(request.getCity());
        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setTelephone(request.getTelephone());
        owner.setEmail(request.getEmail());
    }

    private static String emptyToNull(String value) {
        return value.isEmpty() ? null : value;
    }
}
