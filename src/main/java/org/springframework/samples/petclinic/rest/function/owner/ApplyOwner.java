package org.springframework.samples.petclinic.rest.function.owner;

import net.officefloor.plugin.variable.Val;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

public class ApplyOwner {

    public void service(@Val Owner owner, @Val OwnerFieldsDto request) {
        String line1 = request.getAddressLine1();
        if (line1 != null && !line1.isBlank()) {
            String line2 = request.getAddressLine2();
            boolean hasLine2 = line2 != null && !line2.isBlank();
            owner.setAddressLine1(line1);
            owner.setAddressLine2(hasLine2 ? line2 : null);
            owner.setAddress(hasLine2 ? line1 + " " + line2 : line1);
        }
        else {
            owner.setAddressLine1(null);
            owner.setAddressLine2(null);
            owner.setAddress(request.getAddress());
        }
        owner.setCity(request.getCity());
        owner.setFirstName(request.getFirstName());
        owner.setLastName(request.getLastName());
        owner.setTelephone(request.getTelephone());
        owner.setEmail(request.getEmail());
        owner.setPostcode(request.getPostcode());
    }
}
